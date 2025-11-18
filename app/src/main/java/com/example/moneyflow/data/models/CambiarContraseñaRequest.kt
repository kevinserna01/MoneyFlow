package com.example.moneyflow.data.models

import com.google.gson.annotations.SerializedName

data class CambiarContraseñaRequest(
    @SerializedName("nuevaContraseña")
    val nuevaContraseña: String,
    
    @SerializedName("confirmarContraseña")
    val confirmarContraseña: String
)
