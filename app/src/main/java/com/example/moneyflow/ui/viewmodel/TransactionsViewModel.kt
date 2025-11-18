package com.example.moneyflow.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneyflow.data.local.TokenManager
import com.example.moneyflow.data.models.TransactionResponse
import com.example.moneyflow.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TransactionsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TransactionRepository()
    private val tokenManager = TokenManager(application)
    
    private val _transactionsState = MutableStateFlow<TransactionsState>(TransactionsState.Idle)
    val transactionsState: StateFlow<TransactionsState> = _transactionsState.asStateFlow()
    
    fun loadTransactions(tipo: String? = null, categoriaId: String? = null) {
        viewModelScope.launch {
            val userId = tokenManager.getUserIdSync()
            if (userId == null) {
                _transactionsState.value = TransactionsState.Error("No se encontró el ID de usuario")
                return@launch
            }
            
            _transactionsState.value = TransactionsState.Loading
            
            repository.getTransacciones(userId, tipo, categoriaId)
                .onSuccess { transactions ->
                    _transactionsState.value = TransactionsState.Success(transactions)
                }
                .onFailure { error ->
                    _transactionsState.value = TransactionsState.Error(error.message ?: "Error al cargar transacciones")
                }
        }
    }
    
    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            repository.deleteTransaccion(id)
                .onSuccess {
                    // Recargar transacciones después de eliminar
                    loadTransactions()
                }
                .onFailure { error ->
                    _transactionsState.value = TransactionsState.Error(error.message ?: "Error al eliminar transacción")
                }
        }
    }
}

sealed class TransactionsState {
    object Idle : TransactionsState()
    object Loading : TransactionsState()
    data class Success(val transactions: List<TransactionResponse>) : TransactionsState()
    data class Error(val message: String) : TransactionsState()
}
