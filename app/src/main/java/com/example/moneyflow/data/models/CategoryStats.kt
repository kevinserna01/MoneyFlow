package com.example.moneyflow.data.models

import com.google.gson.annotations.SerializedName

data class CategoryStats(
    @SerializedName("gastado")
    val gastado: Long,
    
    @SerializedName("presupuesto")
    val presupuesto: Long,
    
    @SerializedName("presupuestoOriginal")
    val presupuestoOriginal: Long? = null,
    
    @SerializedName("presupuestoActual")
    val presupuestoActual: Long? = null,
    
    @SerializedName("porcentajeUsado")
    val porcentajeUsado: Double,
    
    @SerializedName("restante")
    val restante: Long,
    
    @SerializedName("sobrepasado")
    val sobrepasado: Boolean = false,
    
    @SerializedName("exceso")
    val exceso: Long = 0,
    
    @SerializedName("agotado")
    val agotado: Boolean = false
)
