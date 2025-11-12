package com.example.moneyflow.data.models

import com.google.gson.annotations.SerializedName

data class UpdateUsuarioRequest(
    @SerializedName("nombre")
    val nombre: String? = null,
    
    @SerializedName("telefono")
    val telefono: String? = null,
    
    @SerializedName("correo")
    val correo: String? = null,
    
    @SerializedName("contraseña")
    val contraseña: String? = null
)
