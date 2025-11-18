package com.example.moneyflow.screens

import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.moneyflow.components.AppPrimaryButton
import com.example.moneyflow.data.models.CategoryResponse
import com.example.moneyflow.model.TransactionType
import com.example.moneyflow.theme.ExpenseColor
import com.example.moneyflow.theme.IncomeColor
import com.example.moneyflow.theme.InputShape
import com.example.moneyflow.ui.viewmodel.AddTransactionViewModel
import com.example.moneyflow.ui.viewmodel.CategoriesViewModel
import com.example.moneyflow.ui.viewmodel.CategoriesState
import com.example.moneyflow.ui.viewmodel.SaveTransactionState
import com.example.moneyflow.utils.CurrencyFormatter
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(navController: NavController) {
    val context = LocalContext.current
    val transactionViewModel: AddTransactionViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as Application
        )
    )
    val categoriesViewModel: CategoriesViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as Application
        )
    )
    
    val saveState by transactionViewModel.saveState.collectAsState()
    val categoriesState by categoriesViewModel.categoriesState.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    var transactionType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<CategoryResponse?>(null) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var description by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    // Función para mostrar el DatePicker
    val showDatePickerDialog = remember(selectedDate) {
        {
            val calendar = Calendar.getInstance()
            calendar.set(selectedDate.year, selectedDate.monthValue - 1, selectedDate.dayOfMonth)
            
            android.app.DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }
    
    // Cargar categorías al iniciar
    LaunchedEffect(Unit) {
        categoriesViewModel.loadCategories()
    }
    
    // Filtrar categorías según tipo
    val availableCategories = when (val state = categoriesState) {
        is CategoriesState.Success -> {
            val tipoFilter = if (transactionType == TransactionType.INCOME) "ingresos" else "gastos"
            state.categories.filter { it.tipo.lowercase() == tipoFilter }
        }
        else -> emptyList()
    }
    
    // Actualizar categoría seleccionada cuando cambia el tipo
    LaunchedEffect(transactionType) {
        selectedCategory = null
    }
    
    val isSaving = saveState is SaveTransactionState.Loading
    
    // Manejar estados del ViewModel
    LaunchedEffect(saveState) {
        when (val state = saveState) {
            is SaveTransactionState.Success -> {
                showSuccessDialog = true
            }
            is SaveTransactionState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                transactionViewModel.resetState()
            }
            else -> {}
        }
    }
    
    // Navegar automáticamente después de mostrar el diálogo de éxito
    LaunchedEffect(showSuccessDialog) {
        if (showSuccessDialog) {
            kotlinx.coroutines.delay(2000) // Esperar 2 segundos
            showSuccessDialog = false
            transactionViewModel.resetState()
            // Establecer flag de refresh en el entry del Dashboard
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("refreshTransactions", true)
            // Refrescar categorías siempre porque:
            // - Ingresos: actualizan el presupuesto de la categoría de ingreso
            // - Gastos: se restan DIRECTAMENTE (no proporcionalmente) de las categorías de ingresos
            //   empezando por la categoría con mayor presupuesto. Si llega a $0, se genera alerta (agotado: true)
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("refreshCategories", true)
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Nueva Transacción",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Atrás"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { /* No permitir cerrar manualmente */ },
                title = { Text("¡Transacción creada!") },
                text = {
                    Text(
                        text = "Tu transacción se guardó correctamente. Regresando al dashboard...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showSuccessDialog = false
                                    transactionViewModel.resetState()
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("refreshTransactions", true)
                                    // Refrescar categorías siempre porque:
                                    // - Ingresos: actualizan el presupuesto de la categoría de ingreso
                                    // - Gastos: se restan DIRECTAMENTE (no proporcionalmente) de las categorías de ingresos
                                    //   empezando por la categoría con mayor presupuesto. Si llega a $0, se genera alerta (agotado: true)
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("refreshCategories", true)
                                    navController.popBackStack()
                                }
                            ) {
                                Text("Ir ahora")
                            }
                        }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Type Selector
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = androidx.compose.material3.CardDefaults.cardElevation(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Income Button
                    FilterChip(
                        selected = transactionType == TransactionType.INCOME,
                        onClick = { transactionType = TransactionType.INCOME },
                        label = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text("Ingreso")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IncomeColor,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )

                    // Expense Button
                    FilterChip(
                        selected = transactionType == TransactionType.EXPENSE,
                        onClick = { transactionType = TransactionType.EXPENSE },
                        label = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text("Gasto")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ExpenseColor,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )
                }
            }

            // Amount Input
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "Monto",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    placeholder = {
                        Text(
                            "0.00",
                            style = MaterialTheme.typography.displayMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                    },
                    textStyle = MaterialTheme.typography.displayMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    shape = InputShape,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }

            // Category Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "Categoría",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                when (val state = categoriesState) {
                    is CategoriesState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is CategoriesState.Error -> {
                        Text(
                            text = "Error al cargar categorías: ${state.message}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    is CategoriesState.Success -> {
                        if (availableCategories.isEmpty()) {
                            Text(
                                text = "No hay categorías disponibles. Crea una categoría primero.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.height(240.dp)
                            ) {
                                items(availableCategories) { category ->
                                    val isSelected = selectedCategory?.id == category.id
                                    Surface(
                                        onClick = { selectedCategory = category },
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant,
                                        border = if (isSelected)
                                            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                        else null,
                                        modifier = Modifier.aspectRatio(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = category.icono,
                                                style = MaterialTheme.typography.headlineMedium
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = category.nombre,
                                                style = MaterialTheme.typography.labelSmall,
                                                maxLines = 2
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }

            // Date Field
            OutlinedTextField(
                value = selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                onValueChange = {},
                label = { Text("Fecha") },
                enabled = false,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clickable(enabled = true) { showDatePickerDialog() },
                shape = InputShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            // Description Field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción (opcional)") },
                placeholder = { Text("Añade una nota...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(horizontal = 24.dp),
                shape = InputShape,
                maxLines = 4,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            // Buttons Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Cancelar")
                }

                Box(modifier = Modifier.weight(1f)) {
                    AppPrimaryButton(
                        text = if (isSaving) "Guardando..." else "Guardar",
                        onClick = {
                            if (isSaving) return@AppPrimaryButton
                            
                            val amountValue = parseAmountInput(amount)
                            if (amountValue == null || amountValue <= 0) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Ingresa un monto válido")
                                }
                                return@AppPrimaryButton
                            }
                            
                            if (selectedCategory == null) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Selecciona una categoría")
                                }
                                return@AppPrimaryButton
                            }
                            
                            val tipoString = if (transactionType == TransactionType.INCOME) "ingreso" else "gasto"
                            transactionViewModel.createTransaction(
                                tipo = tipoString,
                                monto = amountValue,
                                categoriaId = selectedCategory!!.id,
                                fecha = selectedDate,
                                descripcion = description.takeIf { it.isNotBlank() }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving
                    )
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

private fun parseAmountInput(input: String): Long? {
    if (input.isBlank()) return null
    val normalized = input.replace(",", "").replace(".", "").trim()
    return normalized.toLongOrNull()
}
