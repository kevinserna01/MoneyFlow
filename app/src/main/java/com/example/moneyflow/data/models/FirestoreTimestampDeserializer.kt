package com.example.moneyflow.data.models

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class FirestoreTimestampDeserializer : JsonDeserializer<String> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): String {
        if (json == null || json.isJsonNull) {
            return ""
        }
        
        // Si es un objeto (formato Firestore Timestamp)
        if (json.isJsonObject) {
            val jsonObject = json.asJsonObject
            val seconds = jsonObject.get("_seconds")?.asLong ?: return ""
            val nanoseconds = jsonObject.get("_nanoseconds")?.asLong ?: 0L
            
            // Convertir a milisegundos
            val milliseconds = seconds * 1000 + (nanoseconds / 1_000_000)
            val date = Date(milliseconds)
            
            // Formatear como ISO 8601 string
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            return sdf.format(date)
        }
        
        // Si ya es un string, devolverlo tal cual
        if (json.isJsonPrimitive && json.asJsonPrimitive.isString) {
            return json.asString
        }
        
        return ""
    }
}
