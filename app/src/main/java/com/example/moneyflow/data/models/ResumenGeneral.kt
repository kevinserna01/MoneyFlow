package com.example.moneyflow.data.models

import com.google.gson.annotations.SerializedName

data class ResumenGeneral(
    @SerializedName("totalIngresos")
    val totalIngresos: Long,
    
    @SerializedName("totalGastos")
    val totalGastos: Long,
    
    @SerializedName("balance")
    val balance: Long,
    
    @SerializedName("porcentajeUsado")
    val porcentajeUsado: Double,
    
    @SerializedName("restante")
    val restante: Long
)
