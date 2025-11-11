package com.example.moneyflow.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.moneyflow.screens.AddTransactionScreen
import com.example.moneyflow.screens.CategoriesScreen
import com.example.moneyflow.screens.DashboardScreen
import com.example.moneyflow.screens.LoginScreen
import com.example.moneyflow.screens.ProfileScreen
import com.example.moneyflow.screens.RegisterScreen
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

    composable("transactions") {
        TransactionsScreen(navController = navController)
    }

    composable("categories") {
        CategoriesScreen(navController = navController)
    }

    composable("statistics") {
        StatisticsScreen(navController = navController)
    }

    composable("profile") {
        ProfileScreen(navController = navController)
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
