package com.example.moneyflow.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneyflow.data.local.TokenManager
import com.example.moneyflow.data.models.EstadisticasMensuales
import com.example.moneyflow.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TransactionRepository()
    private val tokenManager = TokenManager(application)
    
    private val _estadisticasState = MutableStateFlow<StatisticsState>(StatisticsState.Idle)
    val estadisticasState: StateFlow<StatisticsState> = _estadisticasState.asStateFlow()
    
    fun loadEstadisticas(año: Int? = null, mes: Int? = null) {
        viewModelScope.launch {
            val userId = tokenManager.getUserIdSync()
            if (userId == null) {
                _estadisticasState.value = StatisticsState.Error("Usuario no autenticado")
                return@launch
            }
            
            _estadisticasState.value = StatisticsState.Loading
            
            repository.getEstadisticas(userId, año, mes)
                .onSuccess { estadisticas ->
                    _estadisticasState.value = StatisticsState.Success(estadisticas)
                }
                .onFailure { error ->
                    _estadisticasState.value = StatisticsState.Error(error.message ?: "Error al cargar estadísticas")
                }
        }
    }
}

sealed class StatisticsState {
    object Idle : StatisticsState()
    object Loading : StatisticsState()
    data class Success(val estadisticas: EstadisticasMensuales) : StatisticsState()
    data class Error(val message: String) : StatisticsState()
}
