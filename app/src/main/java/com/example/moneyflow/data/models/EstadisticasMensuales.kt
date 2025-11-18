package com.example.moneyflow.data.models

import com.google.gson.annotations.SerializedName

data class EstadisticasMensuales(
    @SerializedName("mesActual")
    val mesActual: MesData,
    
    @SerializedName("mesAnterior")
    val mesAnterior: MesData,
    
    @SerializedName("comparacion")
    val comparacion: ComparacionData
)

data class MesData(
    @SerializedName("año")
    val año: Int,
    
    @SerializedName("mes")
    val mes: Int,
    
    @SerializedName("ingresos")
    val ingresos: Long,
    
    @SerializedName("gastos")
    val gastos: Long,
    
    @SerializedName("balance")
    val balance: Long
)

data class ComparacionData(
    @SerializedName("ingresos")
    val ingresos: CambioData,
    
    @SerializedName("gastos")
    val gastos: CambioData
)

data class CambioData(
    @SerializedName("cambio")
    val cambio: Long,
    
    @SerializedName("porcentajeCambio")
    val porcentajeCambio: Double,
    
    @SerializedName("esPositivo")
    val esPositivo: Boolean
)
