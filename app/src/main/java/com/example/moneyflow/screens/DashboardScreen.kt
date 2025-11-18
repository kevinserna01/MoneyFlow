package com.example.moneyflow.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneyflow.components.TransactionCard
import com.example.moneyflow.data.models.TransactionResponse
import com.example.moneyflow.model.Transaction
import com.example.moneyflow.model.TransactionType
import com.example.moneyflow.theme.ExpenseColor
import com.example.moneyflow.theme.IncomeColor
import com.example.moneyflow.ui.viewmodel.DashboardViewModel
import com.example.moneyflow.ui.viewmodel.TransactionsViewModel
import com.example.moneyflow.ui.viewmodel.TransactionsState
import com.example.moneyflow.utils.CurrencyFormatter

@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current
    val dashboardViewModel: DashboardViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as Application
        )
    )
    val transactionsViewModel: TransactionsViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as Application
        )
    )
    
    val userFirstName by dashboardViewModel.userFirstName.collectAsState()
    val userFullName by dashboardViewModel.userFullName.collectAsState()
    val transactionsState by transactionsViewModel.transactionsState.collectAsState()
    val resumenState by dashboardViewModel.resumenState.collectAsState()
    
    val greetingName = when {
        userFullName.isNotBlank() -> userFullName
        userFirstName.isNotBlank() -> userFirstName
        else -> "Usuario"
    }
    var selectedItem by remember { mutableIntStateOf(0) }
    
    // Cargar resumen y transacciones al iniciar
    LaunchedEffect(Unit) {
        dashboardViewModel.loadResumen()
        transactionsViewModel.loadTransactions()
    }
    
    // Recargar cuando se vuelve de AddTransactionScreen
    val refreshFlow = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("refreshTransactions", false)
    val shouldRefresh by refreshFlow?.collectAsState() ?: remember { mutableStateOf(false) }

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            dashboardViewModel.loadResumen()
            transactionsViewModel.loadTransactions()
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("refreshTransactions", false)
        }
    }
    
    // Usar resumen de la API si está disponible, de lo contrario calcular desde transacciones
    val resumen = when (val state = resumenState) {
        is com.example.moneyflow.ui.viewmodel.ResumenState.Success -> state.resumen
        else -> null
    }
    
    val ingresos = resumen?.totalIngresos?.toDouble() ?: run {
        val transactions = when (val state = transactionsState) {
            is TransactionsState.Success -> state.transactions
            else -> emptyList()
        }
        transactions.filter { it.tipo.lowercase() == "ingreso" }.sumOf { it.monto.toDouble() }
    }
    
    val gastos = resumen?.totalGastos?.toDouble() ?: run {
        val transactions = when (val state = transactionsState) {
            is TransactionsState.Success -> state.transactions
            else -> emptyList()
        }
        transactions.filter { it.tipo.lowercase() == "gasto" }.sumOf { it.monto.toDouble() }
    }
    
    val balanceTotal = resumen?.balance?.toDouble() ?: (ingresos - gastos)
    
    // Obtener últimas 3 transacciones
    val transactions = when (val state = transactionsState) {
        is TransactionsState.Success -> state.transactions
        else -> emptyList()
    }
    
    val recentTransactions = transactions
        .sortedByDescending { it.fecha }
        .take(3)
        .map { it.toTransaction() }

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Inicio"
                        )
                    },
                    label = { Text("Inicio") },
                    selected = selectedItem == 0,
                    onClick = { selectedItem = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Análisis"
                        )
                    },
                    label = { Text("Análisis") },
                    selected = selectedItem == 1,
                    onClick = {
                        selectedItem = 1
                        navController.navigate("statistics")
                    }
                )

                // FAB en el centro
                Box(
                    modifier = Modifier
                        .padding(top = 16.dp)
                ) {
                    FloatingActionButton(
                        onClick = { navController.navigate("add_transaction") },
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primary,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 12.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Agregar",
                            tint = Color.White
                        )
                    }
                }

                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Wallet,
                            contentDescription = "Cartera"
                        )
                    },
                    label = { Text("Cartera") },
                    selected = selectedItem == 2,
                    onClick = {
                        selectedItem = 2
                        navController.navigate("categories")
                    }
                )

                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Perfil"
                        )
                    },
                    label = { Text("Perfil") },
                    selected = selectedItem == 3,
                    onClick = {
                        selectedItem = 3
                        navController.navigate("profile")
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFF97316),
                                Color(0xFFEA580C)
                            )
                        ),
                        shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                    )
                    .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 128.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hola de nuevo,",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFFEDD5)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                          Text(
                              text = greetingName,
                              style = MaterialTheme.typography.titleLarge,
                              color = Color.White
                          )
                    }

                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.2f)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "👋", style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }
            }

            // Balance Card (Elevated)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .offset(y = (-96).dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 16.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Balance Total
                    Text(
                        text = "Balance Total",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                          Text(
                              text = CurrencyFormatter.formatCOP(balanceTotal),
                              style = MaterialTheme.typography.displaySmall.copy(
                                  fontSize = 36.sp
                              ),
                              color = Color.White
                          )

                        // Badge de porcentaje
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = IncomeColor.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = IncomeColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "+12.5%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = IncomeColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Income & Expenses Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Income Card
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.05f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.size(32.dp),
                                        shape = CircleShape,
                                        color = IncomeColor.copy(alpha = 0.2f)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.TrendingDown,
                                                contentDescription = null,
                                                tint = IncomeColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Ingresos",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                  Text(
                                      text = CurrencyFormatter.formatCOP(ingresos),
                                      style = MaterialTheme.typography.titleLarge,
                                      color = Color.White
                                  )
                            }
                        }

                        // Expense Card
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.05f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.size(32.dp),
                                        shape = CircleShape,
                                        color = ExpenseColor.copy(alpha = 0.2f)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.TrendingUp,
                                                contentDescription = null,
                                                tint = ExpenseColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Gastos",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                  Text(
                                      text = CurrencyFormatter.formatCOP(gastos),
                                      style = MaterialTheme.typography.titleLarge,
                                      color = Color.White
                                  )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Transactions Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transacciones Recientes",
                        style = MaterialTheme.typography.titleLarge
                    )
                    TextButton(onClick = { navController.navigate("transactions") }) {
                        Text("Ver todas")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                  if (recentTransactions.isEmpty()) {
                      Text(
                          text = "No hay transacciones recientes",
                          style = MaterialTheme.typography.bodyMedium,
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                  } else {
                      LazyColumn(
                          verticalArrangement = Arrangement.spacedBy(12.dp)
                      ) {
                          items(recentTransactions) { transaction ->
                              TransactionCard(
                                  transaction = transaction,
                                  onClick = {
                                      navController.navigate("transaction_detail/${transaction.id}")
                                  }
                              )
                          }
                      }
                  }
            }
        }
    }
}

// Función de extensión para convertir TransactionResponse a Transaction
private fun TransactionResponse.toTransaction(): Transaction {
    val tipo = if (this.tipo.lowercase() == "ingreso") TransactionType.INCOME else TransactionType.EXPENSE
    
    // Parsear fecha ISO 8601 a formato corto
    val fechaFormateada = try {
        val instant = java.time.Instant.parse(this.fecha)
        val localDate = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
        val formatter = java.time.format.DateTimeFormatter.ofPattern("d MMM", java.util.Locale("es", "CO"))
        localDate.format(formatter)
    } catch (e: Exception) {
        "Fecha inválida"
    }
    
    // Crear Category desde los datos de la respuesta
    val categoryColor = when (categoriaNombre.lowercase()) {
        "comida" -> com.example.moneyflow.theme.CategoryFood
        "transporte" -> com.example.moneyflow.theme.CategoryTransport
        "entretenimiento" -> com.example.moneyflow.theme.CategoryEntertainment
        "salud" -> com.example.moneyflow.theme.CategoryHealth
        "educación" -> com.example.moneyflow.theme.CategoryEducation
        "compras" -> com.example.moneyflow.theme.CategoryShopping
        else -> if (tipo == TransactionType.INCOME) IncomeColor else com.example.moneyflow.theme.CategoryFood
    }
    
    val categoryBackground = if (categoriaTipo.lowercase() == "ingresos") {
        com.example.moneyflow.theme.IncomeBackground
    } else {
        com.example.moneyflow.theme.ExpenseBackground
    }
    
    val category = com.example.moneyflow.model.Category(
        id = categoriaId,
        name = categoriaNombre,
        icon = categoriaIcono,
        color = categoryColor,
        backgroundColor = categoryBackground
    )
    
    return Transaction(
        id = this.id,
        name = this.descripcion ?: categoriaNombre,
        category = category,
        amount = this.monto.toDouble(),
        date = fechaFormateada,
        description = this.descripcion,
        type = tipo,
        paymentMethod = null
    )
}
