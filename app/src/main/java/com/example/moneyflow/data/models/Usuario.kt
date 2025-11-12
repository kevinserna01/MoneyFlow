package com.example.moneyflow.data.models

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName

data class Usuario(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("nombre")
    val nombre: String,
    
    @SerializedName("telefono")
    val telefono: String,
    
    @SerializedName("correo")
    val correo: String,
    
    @SerializedName("createdAt")
    @JsonAdapter(FirestoreTimestampDeserializer::class)
    val createdAt: String,
    
    @SerializedName("updatedAt")
    @JsonAdapter(FirestoreTimestampDeserializer::class)
    val updatedAt: String
)
