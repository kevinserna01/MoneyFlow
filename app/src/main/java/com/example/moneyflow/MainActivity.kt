package com.example.moneyflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.moneyflow.data.api.ApiClient
import com.example.moneyflow.data.local.TokenManager
import com.example.moneyflow.navigation.appNavigation
import com.example.moneyflow.theme.MoneyFlowTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar token desde DataStore
        lifecycleScope.launch {
            val tokenManager = TokenManager(this@MainActivity)
            val token = tokenManager.getTokenSync()
            token?.let { ApiClient.setToken(it) }
        }
        
        setContent {
            MoneyFlowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "login"
                    ) {
                        appNavigation(navController = navController)
                    }
                }
            }
        }
    }
}