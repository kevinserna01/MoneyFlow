package com.example.moneyflow.screens

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.moneyflow.components.TransactionCard
import com.example.moneyflow.data.models.TransactionResponse
import com.example.moneyflow.model.Category
import com.example.moneyflow.model.Transaction
import com.example.moneyflow.model.TransactionType
import com.example.moneyflow.theme.CategoryEducation
import com.example.moneyflow.theme.CategoryEntertainment
import com.example.moneyflow.theme.CategoryFood
import com.example.moneyflow.theme.CategoryHealth
import com.example.moneyflow.theme.CategoryShopping
import com.example.moneyflow.theme.CategoryTransport
import com.example.moneyflow.theme.ExpenseBackground
import com.example.moneyflow.theme.IncomeBackground
import com.example.moneyflow.theme.IncomeColor
import com.example.moneyflow.ui.viewmodel.TransactionsState
import com.example.moneyflow.ui.viewmodel.TransactionsViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: TransactionsViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as Application
        )
    )
    val transactionsState by viewModel.transactionsState.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    
    // Cargar transacciones al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadTransactions()
    }
    
    // Recargar cuando se vuelve de AddTransactionScreen
    val refreshFlow = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("refreshTransactions", false)
    val shouldRefresh by refreshFlow?.collectAsState() ?: remember { mutableStateOf(false) }

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            viewModel.loadTransactions()
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("refreshTransactions", false)
        }
    }
    
    // Cargar según el tab seleccionado
    LaunchedEffect(selectedTab) {
        val tipoFilter = when (selectedTab) {
            1 -> "ingreso"
            2 -> "gasto"
            else -> null
        }
        viewModel.loadTransactions(tipo = tipoFilter)
    }

    val allTransactions = when (val state = transactionsState) {
        is TransactionsState.Success -> state.transactions.map { it.toTransaction() }
        else -> emptyList()
    }

    val filteredTransactions = allTransactions

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transacciones") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Show filter sheet */ }) {
                        Icon(Icons.Default.FilterList, "Filtros")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Todas") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Ingresos") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Gastos") }
                )
            }

            // Transactions List
            when (val state = transactionsState) {
                is TransactionsState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is TransactionsState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Error al cargar transacciones",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                is TransactionsState.Success -> {
                    if (filteredTransactions.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay transacciones. Agrega una nueva.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredTransactions) { transaction ->
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
                is TransactionsState.Idle -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
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
        val instant = Instant.parse(this.fecha)
        val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
        val formatter = DateTimeFormatter.ofPattern("d MMM", java.util.Locale("es", "CO"))
        localDate.format(formatter)
    } catch (e: Exception) {
        "Fecha inválida"
    }
    
    // Crear Category desde los datos de la respuesta
    val categoryColor = when (categoriaNombre.lowercase()) {
        "comida" -> CategoryFood
        "transporte" -> CategoryTransport
        "entretenimiento" -> CategoryEntertainment
        "salud" -> CategoryHealth
        "educación" -> CategoryEducation
        "compras" -> CategoryShopping
        else -> if (tipo == TransactionType.INCOME) IncomeColor else CategoryFood
    }
    
    val categoryBackground = if (categoriaTipo.lowercase() == "ingresos") {
        IncomeBackground
    } else {
        ExpenseBackground
    }
    
    val category = Category(
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
