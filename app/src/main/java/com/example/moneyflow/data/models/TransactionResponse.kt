package com.example.moneyflow.data.models

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName

data class TransactionResponse(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("tipo")
    val tipo: String, // "ingreso" o "gasto"
    
    @SerializedName("monto")
    val monto: Long,
    
    @SerializedName("categoriaId")
    val categoriaId: String,
    
    @SerializedName("categoriaNombre")
    val categoriaNombre: String,
    
    @SerializedName("categoriaIcono")
    val categoriaIcono: String,
    
    @SerializedName("categoriaTipo")
    val categoriaTipo: String,
    
    @SerializedName("usuarioId")
    val usuarioId: String,
    
    @SerializedName("fecha")
    @JsonAdapter(FirestoreTimestampDeserializer::class)
    val fecha: String, // ISO 8601 format
    
    @SerializedName("descripcion")
    val descripcion: String? = null,
    
    @SerializedName("createdAt")
    @JsonAdapter(FirestoreTimestampDeserializer::class)
    val createdAt: String? = null,
    
    @SerializedName("updatedAt")
    @JsonAdapter(FirestoreTimestampDeserializer::class)
    val updatedAt: String? = null
)
