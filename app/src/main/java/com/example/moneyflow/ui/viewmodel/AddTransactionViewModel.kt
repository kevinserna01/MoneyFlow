package com.example.moneyflow.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneyflow.data.local.TokenManager
import com.example.moneyflow.data.models.TransactionRequest
import com.example.moneyflow.data.models.TransactionResponse
import com.example.moneyflow.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AddTransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TransactionRepository()
    private val tokenManager = TokenManager(application)
    
    private val _saveState = MutableStateFlow<SaveTransactionState>(SaveTransactionState.Idle)
    val saveState: StateFlow<SaveTransactionState> = _saveState.asStateFlow()
    
    fun createTransaction(
        tipo: String, // "ingreso" o "gasto"
        monto: Long,
        categoriaId: String,
        fecha: LocalDate,
        descripcion: String? = null
    ) {
        viewModelScope.launch {
            val userId = tokenManager.getUserIdSync()
            if (userId == null) {
                _saveState.value = SaveTransactionState.Error("No se encontró el ID de usuario")
                return@launch
            }
            
            _saveState.value = SaveTransactionState.Loading
            
            // Convertir LocalDate a ISO 8601 string
            val fechaString = fecha.atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toString()
            
            val request = TransactionRequest(
                tipo = tipo,
                monto = monto,
                categoriaId = categoriaId,
                usuarioId = userId,
                fecha = fechaString,
                descripcion = descripcion
            )
            
            repository.createTransaccion(request)
                .onSuccess { transaction ->
                    _saveState.value = SaveTransactionState.Success(transaction)
                }
                .onFailure { error ->
                    _saveState.value = SaveTransactionState.Error(error.message ?: "Error al crear transacción")
                }
        }
    }
    
    fun resetState() {
        _saveState.value = SaveTransactionState.Idle
    }
}

sealed class SaveTransactionState {
    object Idle : SaveTransactionState()
    object Loading : SaveTransactionState()
    data class Success(val transaction: TransactionResponse) : SaveTransactionState()
    data class Error(val message: String) : SaveTransactionState()
}
