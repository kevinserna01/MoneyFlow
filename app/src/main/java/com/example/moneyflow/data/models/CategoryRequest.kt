package com.example.moneyflow.data.models

import com.google.gson.annotations.SerializedName

data class CategoryRequest(
    @SerializedName("nombre")
    val nombre: String,
    
    @SerializedName("icono")
    val icono: String,
    
    @SerializedName("tipo")
    val tipo: String, // "ingresos" o "gastos"
    
    @SerializedName("presupuestoMensual")
    val presupuestoMensual: Long? = null,
    
    @SerializedName("moneda")
    val moneda: String = "COP",
    
    @SerializedName("descripcion")
    val descripcion: String? = null,
    
    @SerializedName("usuarioId")
    val usuarioId: String
)
