package com.example.moneyflow.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneyflow.data.local.TokenManager
import com.example.moneyflow.data.models.ResumenGeneral
import com.example.moneyflow.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    private val repository = TransactionRepository()

    val userFullName: StateFlow<String> = tokenManager.userFullName
        .map { it.orEmpty() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ""
        )

    val userEmail: StateFlow<String> = tokenManager.userEmail
        .map { it.orEmpty() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ""
        )
    
    val userId: StateFlow<String?> = tokenManager.userId
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )
    
    private val _resumenState = MutableStateFlow<ResumenState>(ResumenState.Idle)
    val resumenState: StateFlow<ResumenState> = _resumenState.asStateFlow()
    
    fun loadResumen() {
        viewModelScope.launch {
            val userId = tokenManager.getUserIdSync()
            if (userId == null) {
                _resumenState.value = ResumenState.Error("No se encontró el ID de usuario")
                return@launch
            }
            
            _resumenState.value = ResumenState.Loading
            
            repository.getResumenGeneral(userId)
                .onSuccess { resumen ->
                    _resumenState.value = ResumenState.Success(resumen)
                }
                .onFailure { error ->
                    _resumenState.value = ResumenState.Error(error.message ?: "Error al cargar resumen")
                }
        }
    }
}
