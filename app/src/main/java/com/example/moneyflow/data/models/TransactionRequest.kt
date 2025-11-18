package com.example.moneyflow.data.models

import com.google.gson.annotations.SerializedName

data class TransactionRequest(
    @SerializedName("tipo")
    val tipo: String, // "ingreso" o "gasto"
    
    @SerializedName("monto")
    val monto: Long,
    
    @SerializedName("categoriaId")
    val categoriaId: String,
    
    @SerializedName("usuarioId")
    val usuarioId: String,
    
    @SerializedName("fecha")
    val fecha: String, // ISO 8601 format
    
    @SerializedName("descripcion")
    val descripcion: String? = null
)
