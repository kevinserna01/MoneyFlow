package com.example.moneyflow.data.repository

import com.example.moneyflow.data.api.ApiClient
import com.example.moneyflow.data.models.ApiResponse
import com.example.moneyflow.data.models.CategoryRequest
import com.example.moneyflow.data.models.CategoryResponse

class CategoriaRepository {
    private val apiService = ApiClient.apiService
    
    suspend fun getCategorias(
        usuarioId: String, 
        tipo: String? = null,
        includeStats: Boolean = false
    ): Result<List<CategoryResponse>> {
        return try {
            val response = apiService.getCategorias(usuarioId, tipo, if (includeStats) true else null)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Error al obtener categorías"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getCategoriaById(
        id: String,
        includeStats: Boolean = false,
        usuarioId: String? = null
    ): Result<CategoryResponse> {
        return try {
            val response = apiService.getCategoriaById(
                id, 
                if (includeStats) true else null,
                usuarioId
            )
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Error al obtener categoría"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createCategoria(request: CategoryRequest): Result<CategoryResponse> {
        return try {
            val response = apiService.createCategoria(request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Error al crear categoría"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateCategoria(id: String, request: CategoryRequest): Result<CategoryResponse> {
        return try {
            val response = apiService.updateCategoria(id, request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Error al actualizar categoría"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteCategoria(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteCategoria(id)
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Error al eliminar categoría"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
