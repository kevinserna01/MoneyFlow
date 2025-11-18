package com.example.moneyflow.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneyflow.data.local.TokenManager
import com.example.moneyflow.data.models.CategoryResponse
import com.example.moneyflow.data.repository.CategoriaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoriesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CategoriaRepository()
    private val tokenManager = TokenManager(application)
    
    private val _categoriesState = MutableStateFlow<CategoriesState>(CategoriesState.Idle)
    val categoriesState: StateFlow<CategoriesState> = _categoriesState.asStateFlow()
    
    fun loadCategories(tipo: String? = null, includeStats: Boolean = false) {
        viewModelScope.launch {
            val userId = tokenManager.getUserIdSync()
            if (userId == null) {
                _categoriesState.value = CategoriesState.Error("No se encontró el ID de usuario")
                return@launch
            }
            
            _categoriesState.value = CategoriesState.Loading
            
            // Si tipo es null y queremos stats, necesitamos cargar ingresos y gastos por separado
            // Para gastos: solicitamos estadísticas (gastado, sobrepasado, etc.)
            // Para ingresos: también solicitamos estadísticas para obtener alertas (agotado)
            if (tipo == null && includeStats) {
                val ingresosResult = repository.getCategorias(userId, "ingresos", true)
                val gastosResult = repository.getCategorias(userId, "gastos", true)
                
                when {
                    ingresosResult.isSuccess && gastosResult.isSuccess -> {
                        val allCategories = ingresosResult.getOrNull()!! + gastosResult.getOrNull()!!
                        _categoriesState.value = CategoriesState.Success(allCategories)
                    }
                    ingresosResult.isSuccess -> {
                        _categoriesState.value = CategoriesState.Success(ingresosResult.getOrNull()!!)
                    }
                    gastosResult.isSuccess -> {
                        _categoriesState.value = CategoriesState.Success(gastosResult.getOrNull()!!)
                    }
                    else -> {
                        val error = ingresosResult.exceptionOrNull() ?: gastosResult.exceptionOrNull()
                        _categoriesState.value = CategoriesState.Error(error?.message ?: "Error al cargar categorías")
                    }
                }
            } else {
                // Solicitar estadísticas según el tipo:
                // - Gastos: para obtener gastado, sobrepasado, etc.
                // - Ingresos: para obtener alertas (agotado) cuando el presupuesto llega a $0
                val shouldIncludeStats = includeStats && (tipo == "gastos" || tipo == "ingresos")
                
                repository.getCategorias(userId, tipo, shouldIncludeStats)
                    .onSuccess { categories ->
                        _categoriesState.value = CategoriesState.Success(categories)
                    }
                    .onFailure { error ->
                        _categoriesState.value = CategoriesState.Error(error.message ?: "Error al cargar categorías")
                    }
            }
        }
    }
    
    fun deleteCategory(id: String) {
        viewModelScope.launch {
            repository.deleteCategoria(id)
                .onSuccess {
                    // Recargar categorías después de eliminar con estadísticas
                    loadCategories(includeStats = true)
                }
                .onFailure { error ->
                    _categoriesState.value = CategoriesState.Error(error.message ?: "Error al eliminar categoría")
                }
        }
    }
}

sealed class CategoriesState {
    object Idle : CategoriesState()
    object Loading : CategoriesState()
    data class Success(val categories: List<CategoryResponse>) : CategoriesState()
    data class Error(val message: String) : CategoriesState()
}
