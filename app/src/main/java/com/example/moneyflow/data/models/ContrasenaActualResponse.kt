package com.example.moneyflow.data.models

import com.google.gson.annotations.SerializedName

data class ContrasenaActualResponse(
    @SerializedName("tieneContraseña")
    val tieneContraseña: Boolean
)
