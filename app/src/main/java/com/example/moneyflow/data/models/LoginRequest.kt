package com.example.moneyflow.data.models

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("correo")
    val correo: String,
    
    @SerializedName("contraseña")
    val contraseña: String
)
