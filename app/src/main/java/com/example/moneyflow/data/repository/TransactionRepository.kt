package com.example.moneyflow.data.repository

import com.example.moneyflow.data.api.ApiClient
import com.example.moneyflow.data.models.ApiResponse
import com.example.moneyflow.data.models.EstadisticasMensuales
import com.example.moneyflow.data.models.ResumenGeneral
import com.example.moneyflow.data.models.TransactionRequest
import com.example.moneyflow.data.models.TransactionResponse
import com.example.moneyflow.data.models.UpdateTransactionRequest

class TransactionRepository {
    private val apiService = ApiClient.apiService
    
    suspend fun getTransacciones(
        usuarioId: String,
        tipo: String? = null,
        categoriaId: String? = null
    ): Result<List<TransactionResponse>> {
        return try {
            val response = apiService.getTransacciones(usuarioId, tipo, categoriaId)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Error al obtener transacciones"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getTransaccionById(id: String): Result<TransactionResponse> {
        return try {
            val response = apiService.getTransaccionById(id)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Error al obtener transacción"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createTransaccion(request: TransactionRequest): Result<TransactionResponse> {
        return try {
            val response = apiService.createTransaccion(request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Error al crear transacción"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateTransaccion(
        id: String,
        request: UpdateTransactionRequest
    ): Result<TransactionResponse> {
        return try {
            val response = apiService.updateTransaccion(id, request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Error al actualizar transacción"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteTransaccion(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteTransaccion(id)
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Error al eliminar transacción"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getResumenGeneral(usuarioId: String): Result<ResumenGeneral> {
        return try {
            val response = apiService.getResumenGeneral(usuarioId)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Error al obtener resumen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getEstadisticas(
        usuarioId: String,
        año: Int? = null,
        mes: Int? = null
    ): Result<EstadisticasMensuales> {
        return try {
            val response = apiService.getEstadisticas(usuarioId, año, mes)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Error al obtener estadísticas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
