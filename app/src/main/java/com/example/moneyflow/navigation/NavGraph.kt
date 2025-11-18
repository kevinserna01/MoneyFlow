package com.example.moneyflow.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.moneyflow.screens.AddCategoryScreen
import com.example.moneyflow.screens.AddTransactionScreen
import com.example.moneyflow.screens.CategoriesScreen
import com.example.moneyflow.screens.DashboardScreen
import com.example.moneyflow.screens.EditTransactionScreen
import com.example.moneyflow.screens.LoginScreen
import com.example.moneyflow.screens.ProfileScreen
import com.example.moneyflow.screens.RegisterScreen
import com.example.moneyflow.screens.SettingsScreen
import com.example.moneyflow.screens.StatisticsScreen
import com.example.moneyflow.screens.TransactionDetailScreen
import com.example.moneyflow.screens.TransactionsScreen

fun NavGraphBuilder.appNavigation(navController: NavController) {
    // Authentication Flow
    composable("login") {
        LoginScreen(navController = navController)
    }

    composable("register") {
        RegisterScreen(navController = navController)
    }

    // Main App Flow
    composable("dashboard") {
        DashboardScreen(navController = navController)
    }

    composable("add_transaction") {
        AddTransactionScreen(navController = navController)
    }

    composable(
        route = "edit_transaction/{transactionId}",
        arguments = listOf(
            navArgument("transactionId") { type = androidx.navigation.NavType.StringType }
        )
    ) { backStackEntry ->
        val transactionId = backStackEntry.arguments?.getString("transactionId")
        EditTransactionScreen(navController = navController, transactionId = transactionId)
    }

    composable("transactions") {
        TransactionsScreen(navController = navController)
    }

    composable("categories") {
        CategoriesScreen(navController = navController)
    }

    composable(
        route = "add_category?categoryId={categoryId}",
        arguments = listOf(
            navArgument("categoryId") {
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val categoryId = backStackEntry.arguments?.getString("categoryId")
        AddCategoryScreen(navController = navController, categoryId = categoryId)
    }

    composable("statistics") {
        StatisticsScreen(navController = navController)
    }

    composable("profile") {
        ProfileScreen(navController = navController)
    }

    composable("settings") {
        SettingsScreen(navController = navController)
    }

    composable(
        route = "transaction_detail/{transactionId}",
        arguments = listOf(
            navArgument("transactionId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val transactionId = backStackEntry.arguments?.getString("transactionId")
        TransactionDetailScreen(navController = navController, transactionId = transactionId)
    }
}
