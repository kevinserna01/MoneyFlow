package com.example.moneyflow.data.models

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName

data class CategoryResponse(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("nombre")
    val nombre: String,
    
    @SerializedName("icono")
    val icono: String,
    
    @SerializedName("tipo")
    val tipo: String, // "ingresos" o "gastos"
    
    @SerializedName("presupuestoMensual")
    val presupuestoMensual: Long,
    
    @SerializedName("moneda")
    val moneda: String? = "COP",
    
    @SerializedName("descripcion")
    val descripcion: String? = null,
    
    @SerializedName("usuarioId")
    val usuarioId: String? = null,
    
    @SerializedName("createdAt")
    @JsonAdapter(FirestoreTimestampDeserializer::class)
    val createdAt: String? = null,
    
    @SerializedName("updatedAt")
    @JsonAdapter(FirestoreTimestampDeserializer::class)
    val updatedAt: String? = null,
    
    @SerializedName("estadisticas")
    val estadisticas: CategoryStats? = null
)
