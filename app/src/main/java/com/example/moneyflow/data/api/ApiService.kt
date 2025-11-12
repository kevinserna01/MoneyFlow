package com.example.moneyflow.data.api

import com.example.moneyflow.data.models.ApiResponse
import com.example.moneyflow.data.models.CreateUsuarioRequest
import com.example.moneyflow.data.models.LoginRequest
import com.example.moneyflow.data.models.LoginResponse
import com.example.moneyflow.data.models.UpdateUsuarioRequest
import com.example.moneyflow.data.models.Usuario
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    
    @POST("usuarios/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>
    
    @POST("usuarios")
    suspend fun createUsuario(@Body request: CreateUsuarioRequest): ApiResponse<Usuario>
    
    @GET("usuarios")
    suspend fun getAllUsuarios(): ApiResponse<List<Usuario>>
    
    @GET("usuarios/{id}")
    suspend fun getUsuarioById(@Path("id") id: String): ApiResponse<Usuario>
    
    @PUT("usuarios/{id}")
    suspend fun updateUsuario(
        @Path("id") id: String,
        @Body request: UpdateUsuarioRequest
    ): ApiResponse<Usuario>
    
    @DELETE("usuarios/{id}")
    suspend fun deleteUsuario(@Path("id") id: String): ApiResponse<Unit>
}
