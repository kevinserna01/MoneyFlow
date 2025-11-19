package com.example.moneyflow.data.api

object ApiConfig {
    // Cambiar a false para usar el servidor de producción
    // Cambiar a true para usar el servidor local
    private const val USE_LOCAL_SERVER = false
    
    // URLs de los servidores
    private const val LOCAL_URL = "http://10.0.2.2:4000/api/"
    private const val PRODUCTION_URL = "https://moneyflow-backend-taupe.vercel.app/api/"
    
    // URL base seleccionada según la configuración
    val BASE_URL = if (USE_LOCAL_SERVER) LOCAL_URL else PRODUCTION_URL
}
