package com.example.moneyflow.data.models

import com.google.gson.annotations.SerializedName

data class UpdateTransactionRequest(
    @SerializedName("monto")
    val monto: Long? = null,
    
    @SerializedName("descripcion")
    val descripcion: String? = null,
    
    @SerializedName("categoriaId")
    val categoriaId: String? = null,
    
    @SerializedName("fecha")
    val fecha: String? = null
)
