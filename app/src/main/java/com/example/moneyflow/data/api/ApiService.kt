package com.example.moneyflow.data.api

import com.example.moneyflow.data.models.ApiResponse
import com.example.moneyflow.data.models.CambiarContraseñaRequest
import com.example.moneyflow.data.models.CategoryRequest
import com.example.moneyflow.data.models.CategoryResponse
import com.example.moneyflow.data.models.ContrasenaActualResponse
import com.example.moneyflow.data.models.CreateUsuarioRequest
import com.example.moneyflow.data.models.LoginRequest
import com.example.moneyflow.data.models.LoginResponse
import com.example.moneyflow.data.models.TransactionRequest
import com.example.moneyflow.data.models.TransactionResponse
import com.example.moneyflow.data.models.UpdateTransactionRequest
import com.example.moneyflow.data.models.UpdateUsuarioRequest
import com.example.moneyflow.data.models.Usuario
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    
    @POST("usuarios/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>
    
    @POST("usuarios")
    suspend fun createUsuario(@Body request: CreateUsuarioRequest): ApiResponse<Usuario>
    
    @GET("usuarios")
    suspend fun getAllUsuarios(): ApiResponse<List<Usuario>>
    
    @GET("usuarios/{id}")
    suspend fun getUsuarioById(@Path("id") id: String): ApiResponse<Usuario>
    
    @GET("usuarios/{id}/contrasena-actual")
    suspend fun getContrasenaActual(@Path("id") id: String): ApiResponse<ContrasenaActualResponse>
    
    @PUT("usuarios/{id}")
    suspend fun updateUsuario(
        @Path("id") id: String,
        @Body request: UpdateUsuarioRequest
    ): ApiResponse<Usuario>
    
    @PUT("usuarios/{id}/cambiar-contrasena")
    suspend fun cambiarContraseña(
        @Path("id") id: String,
        @Body request: CambiarContraseñaRequest
    ): ApiResponse<Usuario>
    
    @DELETE("usuarios/{id}")
    suspend fun deleteUsuario(@Path("id") id: String): ApiResponse<Unit>
    
    // Categorías endpoints
    @GET("categorias")
    suspend fun getCategorias(
        @Query("usuarioId") usuarioId: String,
        @Query("tipo") tipo: String? = null,
        @Query("includeStats") includeStats: Boolean? = null
    ): ApiResponse<List<CategoryResponse>>
    
    @GET("categorias/{id}")
    suspend fun getCategoriaById(
        @Path("id") id: String,
        @Query("includeStats") includeStats: Boolean? = null,
        @Query("usuarioId") usuarioId: String? = null
    ): ApiResponse<CategoryResponse>
    
    @POST("categorias")
    suspend fun createCategoria(@Body request: CategoryRequest): ApiResponse<CategoryResponse>
    
    @PUT("categorias/{id}")
    suspend fun updateCategoria(
        @Path("id") id: String,
        @Body request: CategoryRequest
    ): ApiResponse<CategoryResponse>
    
    @DELETE("categorias/{id}")
    suspend fun deleteCategoria(@Path("id") id: String): ApiResponse<Unit>
    
    // Transacciones endpoints
    @GET("transacciones")
    suspend fun getTransacciones(
        @Query("usuarioId") usuarioId: String,
        @Query("tipo") tipo: String? = null,
        @Query("categoriaId") categoriaId: String? = null
    ): ApiResponse<List<TransactionResponse>>
    
    @GET("transacciones/{id}")
    suspend fun getTransaccionById(@Path("id") id: String): ApiResponse<TransactionResponse>
    
    @POST("transacciones")
    suspend fun createTransaccion(@Body request: TransactionRequest): ApiResponse<TransactionResponse>
    
    @PUT("transacciones/{id}")
    suspend fun updateTransaccion(
        @Path("id") id: String,
        @Body request: UpdateTransactionRequest
    ): ApiResponse<TransactionResponse>
    
    @DELETE("transacciones/{id}")
    suspend fun deleteTransaccion(@Path("id") id: String): ApiResponse<Unit>
    
    // Resumen general
    @GET("transacciones/resumen/{usuarioId}")
    suspend fun getResumenGeneral(@Path("usuarioId") usuarioId: String): ApiResponse<com.example.moneyflow.data.models.ResumenGeneral>
    
    // Estadísticas mensuales
    @GET("transacciones/estadisticas/{usuarioId}")
    suspend fun getEstadisticas(
        @Path("usuarioId") usuarioId: String,
        @Query("año") año: Int? = null,
        @Query("mes") mes: Int? = null
    ): ApiResponse<com.example.moneyflow.data.models.EstadisticasMensuales>
}
