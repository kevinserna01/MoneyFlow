package com.example.moneyflow.screens

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.moneyflow.data.models.TransactionResponse
import com.example.moneyflow.model.TransactionType
import com.example.moneyflow.theme.ExpenseColor
import com.example.moneyflow.theme.IncomeColor
import com.example.moneyflow.ui.viewmodel.TransactionDetailState
import com.example.moneyflow.ui.viewmodel.TransactionDetailViewModel
import com.example.moneyflow.utils.CurrencyFormatter
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(navController: NavController, transactionId: String?) {
    val context = LocalContext.current
    val viewModel: TransactionDetailViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as Application
        )
    )
    val transactionState by viewModel.transactionState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // Cargar transacción al iniciar
    LaunchedEffect(transactionId) {
        if (transactionId != null) {
            viewModel.loadTransaction(transactionId)
        }
    }
    
    // Manejar eliminación exitosa
    LaunchedEffect(updateState) {
        when (updateState) {
            is com.example.moneyflow.ui.viewmodel.UpdateTransactionState.Deleted -> {
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("refreshTransactions", true)
                // Refrescar categorías siempre porque:
                // - Ingresos: resta el monto del presupuesto de la categoría de ingreso
                // - Gastos: suma de vuelta el monto al presupuesto de las categorías de ingresos
                //   (resta directa, empezando por la categoría con mayor presupuesto)
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("refreshCategories", true)
                navController.popBackStack()
            }
            else -> {}
        }
    }
    
    val transaction = when (val state = transactionState) {
        is TransactionDetailState.Success -> state.transaction
        else -> null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Transacción") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        when (val state = transactionState) {
            is TransactionDetailState.Idle -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is TransactionDetailState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is TransactionDetailState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Error al cargar transacción",
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
            is TransactionDetailState.Success -> {
                val transaction = state.transaction
                
                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Eliminar transacción") },
                        text = {
                            Text(
                                text = "¿Seguro que deseas eliminar esta transacción? Esta acción no se puede deshacer.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.deleteTransaction(transaction.id)
                                    showDeleteDialog = false
                                }
                            ) {
                                Text("Eliminar", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(paddingValues)
                ) {
            // Main Detail Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .offset(y = (-64).dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icon
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        color = if (transaction.tipo.lowercase() == "ingreso") 
                            Color(0xFFD1FAE5) 
                        else 
                            Color(0xFFFFEDD5)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = transaction.categoriaIcono, style = MaterialTheme.typography.headlineLarge)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = transaction.descripcion ?: transaction.categoriaNombre,
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val formattedAmount = CurrencyFormatter.formatCOP(transaction.monto.toDouble())
                    val isIncome = transaction.tipo.lowercase() == "ingreso"
                    Text(
                        text = if (isIncome) "+$formattedAmount" else "-$formattedAmount",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = 36.sp
                        ),
                        color = if (isIncome) IncomeColor else ExpenseColor
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    HorizontalDivider()

                    Spacer(modifier = Modifier.height(24.dp))

                    // Detail Items
                    DetailItem(
                        icon = Icons.Default.Category,
                        iconColor = if (isIncome) IncomeColor else ExpenseColor,
                        iconBackground = if (isIncome) Color(0xFFD1FAE5) else Color(0xFFFFEDD5),
                        label = "Categoría",
                        value = transaction.categoriaNombre
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DetailItem(
                        icon = Icons.Default.CalendarToday,
                        iconColor = Color(0xFF3B82F6),
                        iconBackground = Color(0xFFDBEAFE),
                        label = "Fecha",
                        value = formatTransactionDate(transaction.fecha)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DetailItem(
                        icon = Icons.Default.Description,
                        iconColor = Color(0xFFA855F7),
                        iconBackground = Color(0xFFF3E8FF),
                        label = "Descripción",
                        value = transaction.descripcion ?: "Sin descripción",
                        multiline = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DetailItem(
                        icon = Icons.Default.CheckCircle,
                        iconColor = Color(0xFF22C55E),
                        iconBackground = Color(0xFFDCFCE7),
                        label = "Estado",
                        value = "Completado"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Payment Method Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Método de Pago",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFF97316),
                                            Color(0xFFEA580C)
                                        )
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "💳", style = MaterialTheme.typography.headlineSmall)
                        }

                        Column {
                            Text(
                                text = "Efectivo",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Pago realizado",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Transaction ID Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ID de Transacción",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = transaction.id,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    TextButton(onClick = { /* Copy to clipboard */ }) {
                        Text("Copiar")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { 
                        navController.navigate("edit_transaction/${transaction.id}")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Editar")
                }

                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar")
                }
            }
                }
            }
        }
    }
}

private fun formatTransactionDate(fechaISO: String): String {
    return try {
        val instant = Instant.parse(fechaISO)
        val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
        val formatter = DateTimeFormatter.ofPattern("d 'de' MMMM, yyyy", java.util.Locale("es", "CO"))
        localDate.format(formatter)
    } catch (e: Exception) {
        fechaISO
    }
}

@Composable
fun DetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    iconBackground: Color,
    label: String,
    value: String,
    multiline: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = if (multiline) Alignment.Top else Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(12.dp),
            color = iconBackground
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
