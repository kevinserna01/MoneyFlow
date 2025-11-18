package com.example.moneyflow.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneyflow.data.local.TokenManager
import com.example.moneyflow.data.models.TransactionResponse
import com.example.moneyflow.data.models.UpdateTransactionRequest
import com.example.moneyflow.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TransactionDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TransactionRepository()
    private val tokenManager = TokenManager(application)
    
    private val _transactionState = MutableStateFlow<TransactionDetailState>(TransactionDetailState.Idle)
    val transactionState: StateFlow<TransactionDetailState> = _transactionState.asStateFlow()
    
    private val _updateState = MutableStateFlow<UpdateTransactionState>(UpdateTransactionState.Idle)
    val updateState: StateFlow<UpdateTransactionState> = _updateState.asStateFlow()
    
    fun loadTransaction(id: String) {
        viewModelScope.launch {
            _transactionState.value = TransactionDetailState.Loading
            repository.getTransaccionById(id)
                .onSuccess { transaction ->
                    _transactionState.value = TransactionDetailState.Success(transaction)
                }
                .onFailure { error ->
                    _transactionState.value = TransactionDetailState.Error(error.message ?: "Error al cargar transacción")
                }
        }
    }
    
    fun updateTransaction(
        id: String,
        monto: Long? = null,
        descripcion: String? = null,
        categoriaId: String? = null,
        fecha: String? = null
    ) {
        viewModelScope.launch {
            _updateState.value = UpdateTransactionState.Loading
            
            val request = UpdateTransactionRequest(
                monto = monto,
                descripcion = descripcion,
                categoriaId = categoriaId,
                fecha = fecha
            )
            
            repository.updateTransaccion(id, request)
                .onSuccess { transaction ->
                    _updateState.value = UpdateTransactionState.Success(transaction)
                    // Actualizar también el estado de la transacción
                    _transactionState.value = TransactionDetailState.Success(transaction)
                }
                .onFailure { error ->
                    _updateState.value = UpdateTransactionState.Error(error.message ?: "Error al actualizar transacción")
                }
        }
    }
    
    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            repository.deleteTransaccion(id)
                .onSuccess {
                    _updateState.value = UpdateTransactionState.Deleted
                }
                .onFailure { error ->
                    _updateState.value = UpdateTransactionState.Error(error.message ?: "Error al eliminar transacción")
                }
        }
    }
    
    fun resetUpdateState() {
        _updateState.value = UpdateTransactionState.Idle
    }
}

sealed class TransactionDetailState {
    object Idle : TransactionDetailState()
    object Loading : TransactionDetailState()
    data class Success(val transaction: TransactionResponse) : TransactionDetailState()
    data class Error(val message: String) : TransactionDetailState()
}

sealed class UpdateTransactionState {
    object Idle : UpdateTransactionState()
    object Loading : UpdateTransactionState()
    data class Success(val transaction: TransactionResponse) : UpdateTransactionState()
    object Deleted : UpdateTransactionState()
    data class Error(val message: String) : UpdateTransactionState()
}
