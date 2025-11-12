package com.example.moneyflow.data.repository

import com.example.moneyflow.data.api.ApiClient
import com.example.moneyflow.data.models.CreateUsuarioRequest
import com.example.moneyflow.data.models.LoginRequest
import com.example.moneyflow.data.models.UpdateUsuarioRequest
import com.example.moneyflow.data.models.Usuario

class UsuarioRepository {
    private val api = ApiClient.apiService
    
    suspend fun login(correo: String, contraseña: String): Result<com.example.moneyflow.data.models.LoginResponse> {
        return try {
            val response = api.login(LoginRequest(correo, contraseña))
            if (response.success && response.data != null) {
                // Guardar token en el cliente
                ApiClient.setToken(response.data.token)
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error ?: "Error desconocido"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createUsuario(
        nombre: String,
        telefono: String,
        correo: String,
        contraseña: String
    ): Result<Usuario> {
        return try {
            val response = api.createUsuario(
                CreateUsuarioRequest(nombre, telefono, correo, contraseña)
            )
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error ?: "Error desconocido"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAllUsuarios(): Result<List<Usuario>> {
        return try {
            val response = api.getAllUsuarios()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error ?: "Error desconocido"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUsuarioById(id: String): Result<Usuario> {
        return try {
            val response = api.getUsuarioById(id)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error ?: "Error desconocido"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateUsuario(
        id: String,
        nombre: String? = null,
        telefono: String? = null,
        correo: String? = null,
        contraseña: String? = null
    ): Result<Usuario> {
        return try {
            val response = api.updateUsuario(
                id,
                UpdateUsuarioRequest(nombre, telefono, correo, contraseña)
            )
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error ?: "Error desconocido"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteUsuario(id: String): Result<Unit> {
        return try {
            val response = api.deleteUsuario(id)
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.error ?: "Error desconocido"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
