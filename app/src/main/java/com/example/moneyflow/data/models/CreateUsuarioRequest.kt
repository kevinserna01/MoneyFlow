package com.example.moneyflow.data.models

import com.google.gson.annotations.SerializedName

data class CreateUsuarioRequest(
    @SerializedName("nombre")
    val nombre: String,
    
    @SerializedName("telefono")
    val telefono: String,
    
    @SerializedName("correo")
    val correo: String,
    
    @SerializedName("contraseña")
    val contraseña: String
)
