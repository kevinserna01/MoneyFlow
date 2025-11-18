package com.example.moneyflow.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneyflow.data.local.TokenManager
import com.example.moneyflow.data.models.CategoryRequest
import com.example.moneyflow.data.models.CategoryResponse
import com.example.moneyflow.data.repository.CategoriaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddCategoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CategoriaRepository()
    private val tokenManager = TokenManager(application)
    
    private val _saveState = MutableStateFlow<SaveCategoryState>(SaveCategoryState.Idle)
    val saveState: StateFlow<SaveCategoryState> = _saveState.asStateFlow()

    private val _categoryDetailState = MutableStateFlow<CategoryDetailState>(CategoryDetailState.Idle)
    val categoryDetailState: StateFlow<CategoryDetailState> = _categoryDetailState.asStateFlow()
    
    fun createCategory(
        nombre: String,
        icono: String,
        tipo: String,
        presupuestoMensual: Long? = null,
        descripcion: String? = null
    ) {
        viewModelScope.launch {
            val userId = tokenManager.getUserIdSync()
            if (userId == null) {
                _saveState.value = SaveCategoryState.Error("No se encontró el ID de usuario")
                return@launch
            }
            
            _saveState.value = SaveCategoryState.Loading
            
            // Para ingresos, si no hay presupuesto, establecer en 0
            // Para gastos, el presupuesto debe ser proporcionado (validado en la UI)
            val presupuestoFinal = if (tipo == "ingresos" && presupuestoMensual == null) {
                0L
            } else {
                presupuestoMensual
            }
            
            val request = CategoryRequest(
                nombre = nombre,
                icono = icono,
                tipo = tipo,
                presupuestoMensual = presupuestoFinal,
                moneda = "COP",
                descripcion = descripcion,
                usuarioId = userId
            )
            
            repository.createCategoria(request)
                .onSuccess { category ->
                    _saveState.value = SaveCategoryState.Success(category)
                }
                .onFailure { error ->
                    _saveState.value = SaveCategoryState.Error(error.message ?: "Error al crear categoría")
                }
        }
    }

    fun updateCategory(
        categoryId: String,
        nombre: String,
        icono: String,
        tipo: String,
        presupuestoMensual: Long? = null,
        descripcion: String? = null
    ) {
        viewModelScope.launch {
            val userId = tokenManager.getUserIdSync()
            if (userId == null) {
                _saveState.value = SaveCategoryState.Error("No se encontró el ID de usuario")
                return@launch
            }

            _saveState.value = SaveCategoryState.Loading

            // Para ingresos, si no hay presupuesto, establecer en 0
            // Para gastos, el presupuesto debe ser proporcionado (validado en la UI)
            val presupuestoFinal = if (tipo == "ingresos" && presupuestoMensual == null) {
                0L
            } else {
                presupuestoMensual
            }

            val request = CategoryRequest(
                nombre = nombre,
                icono = icono,
                tipo = tipo,
                presupuestoMensual = presupuestoFinal,
                moneda = "COP",
                descripcion = descripcion,
                usuarioId = userId
            )

            repository.updateCategoria(categoryId, request)
                .onSuccess { category ->
                    _saveState.value = SaveCategoryState.Success(category)
                }
                .onFailure { error ->
                    _saveState.value = SaveCategoryState.Error(error.message ?: "Error al actualizar categoría")
                }
        }
    }

    fun loadCategoryDetail(categoryId: String, includeStats: Boolean = false) {
        viewModelScope.launch {
            _categoryDetailState.value = CategoryDetailState.Loading
            
            val userId = if (includeStats) tokenManager.getUserIdSync() else null
            val shouldIncludeStats = includeStats && userId != null
            
            repository.getCategoriaById(categoryId, shouldIncludeStats, userId)
                .onSuccess { category ->
                    _categoryDetailState.value = CategoryDetailState.Success(category)
                }
                .onFailure { error ->
                    _categoryDetailState.value = CategoryDetailState.Error(error.message ?: "Error al cargar categoría")
                }
        }
    }
    
    fun resetState() {
        _saveState.value = SaveCategoryState.Idle
    }

    fun resetDetailState() {
        _categoryDetailState.value = CategoryDetailState.Idle
    }
}

sealed class SaveCategoryState {
    object Idle : SaveCategoryState()
    object Loading : SaveCategoryState()
    data class Success(val category: com.example.moneyflow.data.models.CategoryResponse) : SaveCategoryState()
    data class Error(val message: String) : SaveCategoryState()
}

sealed class CategoryDetailState {
    object Idle : CategoryDetailState()
    object Loading : CategoryDetailState()
    data class Success(val category: CategoryResponse) : CategoryDetailState()
    data class Error(val message: String) : CategoryDetailState()
}
