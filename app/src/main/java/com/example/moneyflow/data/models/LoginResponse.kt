package com.example.moneyflow.data.models

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("usuario")
    val usuario: Usuario,
    
    @SerializedName("token")
    val token: String
)
