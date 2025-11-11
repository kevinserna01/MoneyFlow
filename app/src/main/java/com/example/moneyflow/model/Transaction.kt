package com.example.moneyflow.model

import androidx.compose.ui.graphics.Color
import com.example.moneyflow.theme.CategoryEducation
import com.example.moneyflow.theme.CategoryEntertainment
import com.example.moneyflow.theme.CategoryFood
import com.example.moneyflow.theme.CategoryHealth
import com.example.moneyflow.theme.CategoryShopping
import com.example.moneyflow.theme.CategoryTransport
import com.example.moneyflow.theme.ExpenseBackground
import com.example.moneyflow.theme.IncomeBackground

enum class TransactionType {
    INCOME,
    EXPENSE
}

data class Transaction(
    val id: String,
    val name: String,
    val category: Category,
    val amount: Double,
    val date: String,
    val description: String? = null,
    val type: TransactionType,
    val paymentMethod: String? = null
)

data class Category(
    val id: String,
    val name: String,
    val icon: String,
    val color: Color,
    val backgroundColor: Color
)

object Categories {
    val food = Category(
        id = "food",
        name = "Comida",
        icon = "🍔",
        color = CategoryFood,
        backgroundColor = ExpenseBackground
    )
    
    val transport = Category(
        id = "transport",
        name = "Transporte",
        icon = "🚗",
        color = CategoryTransport,
        backgroundColor = ExpenseBackground
    )
    
    val entertainment = Category(
        id = "entertainment",
        name = "Entretenimiento",
        icon = "🎬",
        color = CategoryEntertainment,
        backgroundColor = ExpenseBackground
    )
    
    val health = Category(
        id = "health",
        name = "Salud",
        icon = "🏥",
        color = CategoryHealth,
        backgroundColor = ExpenseBackground
    )
    
    val education = Category(
        id = "education",
        name = "Educación",
        icon = "📚",
        color = CategoryEducation,
        backgroundColor = ExpenseBackground
    )
    
    val shopping = Category(
        id = "shopping",
        name = "Compras",
        icon = "🛒",
        color = CategoryShopping,
        backgroundColor = ExpenseBackground
    )
    
    val salary = Category(
        id = "salary",
        name = "Salario",
        icon = "💰",
        color = com.example.moneyflow.theme.IncomeColor,
        backgroundColor = IncomeBackground
    )
    
    val allCategories = listOf(
        food, transport, entertainment, health, education, shopping, salary
    )
    
    val expenseCategories = listOf(
        food, transport, entertainment, health, education, shopping
    )
}
