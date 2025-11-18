package com.example.moneyflow.utils

import java.lang.ThreadLocal
import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val locale = Locale("es", "CO")
    private val formatterThreadLocal = ThreadLocal.withInitial {
        NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 0
            isGroupingUsed = true
        }
    }

    fun formatCOP(amount: Double): String {
        return "$${formatterThreadLocal.get().format(amount)}"
    }
}
