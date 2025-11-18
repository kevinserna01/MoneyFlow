package com.example.moneyflow.screens

import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.moneyflow.components.AppPrimaryButton
import com.example.moneyflow.ui.viewmodel.AddCategoryViewModel
import com.example.moneyflow.ui.viewmodel.CategoryDetailState
import com.example.moneyflow.ui.viewmodel.SaveCategoryState
import com.example.moneyflow.utils.CurrencyFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryScreen(navController: NavController, categoryId: String? = null) {
    val context = LocalContext.current
    val viewModel: AddCategoryViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as Application
        )
    )
    val saveState by viewModel.saveState.collectAsState()
    val categoryDetailState by viewModel.categoryDetailState.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var categoryName by remember { mutableStateOf("") }
    var budgetInput by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("🍔") }
    var selectedType by remember { mutableStateOf(CategoryTypeOption.EXPENSE) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val emojiOptions = listOf("🍔", "🚗", "🎬", "🏡", "🎁", "🧾", "🎓", "💼", "🛍️", "🎧")
    val expensePresets = listOf(
        CategoryPreset("Comida", "🍽️", CategoryTypeOption.EXPENSE),
        CategoryPreset("Transporte", "🚗", CategoryTypeOption.EXPENSE),
        CategoryPreset("Entretenimiento", "🎬", CategoryTypeOption.EXPENSE),
        CategoryPreset("Salud", "💊", CategoryTypeOption.EXPENSE),
        CategoryPreset("Educación", "🎓", CategoryTypeOption.EXPENSE),
        CategoryPreset("Compras", "🛍️", CategoryTypeOption.EXPENSE)
    )
    val incomePresets = listOf(
        CategoryPreset("Salario", "💼", CategoryTypeOption.INCOME),
        CategoryPreset("Bonos", "🎁", CategoryTypeOption.INCOME),
        CategoryPreset("Freelance", "🧾", CategoryTypeOption.INCOME),
        CategoryPreset("Inversiones", "📈", CategoryTypeOption.INCOME)
    )
    val parsedBudget = parseBudgetInput(budgetInput)
    val formattedBudgetPreview = parsedBudget?.let { CurrencyFormatter.formatCOP(it) } ?: CurrencyFormatter.formatCOP(0.0)
    
    val isSaving = saveState is SaveCategoryState.Loading
    val isEditMode = categoryId != null

    var hasInitializedForm by remember(categoryId) { mutableStateOf(false) }

    LaunchedEffect(categoryId) {
        if (categoryId != null) {
            // Solicitar estadísticas solo para categorías de gastos
            viewModel.loadCategoryDetail(categoryId, includeStats = true)
        } else {
            viewModel.resetDetailState()
            hasInitializedForm = true
        }
    }

    LaunchedEffect(categoryDetailState) {
        if (isEditMode) {
            when (val state = categoryDetailState) {
                is CategoryDetailState.Success -> {
                    if (!hasInitializedForm) {
                        val category = state.category
                        categoryName = category.nombre
                        selectedEmoji = category.icono
                        selectedType = if (category.tipo.equals("gastos", ignoreCase = true)) {
                            CategoryTypeOption.EXPENSE
                        } else {
                            CategoryTypeOption.INCOME
                        }
                        budgetInput = category.presupuestoMensual.toString()
                        description = category.descripcion.orEmpty()
                        hasInitializedForm = true
                    }
                }
                is CategoryDetailState.Error -> {
                    snackbarHostState.showSnackbar(state.message)
                    viewModel.resetDetailState()
                    navController.popBackStack()
                }
                else -> {}
            }
        }
    }

    // Manejar estados del ViewModel
    LaunchedEffect(saveState) {
        when (val state = saveState) {
            is SaveCategoryState.Success -> {
                showSuccessDialog = true
            }
            is SaveCategoryState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Categoría") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
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
                onDismissRequest = {},
                title = { Text(if (isEditMode) "¡Categoría actualizada!" else "¡Categoría creada!") },
                text = {
                    Text(
                        text = if (isEditMode)
                            "Tu categoría se actualizó correctamente. Regresaremos a la lista para que puedas verla."
                        else
                            "Tu categoría se guardó correctamente. Regresaremos a la lista para que puedas verla.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showSuccessDialog = false
                            viewModel.resetState()
                            viewModel.resetDetailState()
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("refreshCategories", true)
                            navController.popBackStack()
                        }
                    ) {
                        Text("Ir a categorías")
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = categoryName,
                        onValueChange = { categoryName = it },
                        label = { Text("Nombre de la categoría") },
                        placeholder = { Text("Ej. Entretenimiento") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Selecciona un ícono",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            emojiOptions.forEach { emoji ->
                                Surface(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clickable { selectedEmoji = emoji },
                                    shape = CircleShape,
                                    color = if (selectedEmoji == emoji)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    border = if (selectedEmoji == emoji)
                                        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                    else
                                        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Text(text = emoji, style = MaterialTheme.typography.titleLarge)
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = selectedEmoji,
                            onValueChange = { input ->
                                selectedEmoji = input
                                    .takeIf { it.isNotBlank() }
                                    ?.trim()
                                    ?.take(2)
                                    ?: ""
                            },
                            label = { Text("Emoji personalizado") },
                            placeholder = { Text("Ej. 😊") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Tipo de categoría",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CategoryTypeOption.values().forEach { option ->
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clickable { selectedType = option },
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (selectedType == option)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    border = if (selectedType == option)
                                        BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                    else
                                        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Text(
                                            text = option.label,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (selectedType == option)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Plantillas rápidas",
                            style = MaterialTheme.typography.labelLarge
                        )
                        PresetPickerRow(
                            presets = if (selectedType == CategoryTypeOption.EXPENSE) expensePresets else incomePresets,
                            selectedName = categoryName,
                            onPresetSelected = { preset ->
                                selectedType = preset.type
                                categoryName = preset.name
                                selectedEmoji = preset.emoji
                            }
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = budgetInput,
                            onValueChange = { budgetInput = it },
                            label = { 
                                Text(
                                    if (selectedType == CategoryTypeOption.EXPENSE) 
                                        "Presupuesto mensual *" 
                                    else 
                                        "Presupuesto mensual (opcional)"
                                ) 
                            },
                            placeholder = { Text("Ej. 1200000") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Number
                            ),
                            trailingIcon = {
                                Text(
                                    text = "COP",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors()
                        )

                        Text(
                            text = if (selectedType == CategoryTypeOption.EXPENSE) 
                                "El presupuesto es requerido para categorías de gastos"
                            else 
                                "Para categorías de ingresos, el presupuesto es opcional (por defecto: 0)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "Vista previa: $formattedBudgetPreview",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción (opcional)") },
                        placeholder = { Text("Notas para identificar esta categoría") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        singleLine = false,
                        maxLines = 4
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Resumen",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tipo seleccionado",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = selectedType.label,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Ícono",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = selectedEmoji,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Presupuesto",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formattedBudgetPreview,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                HorizontalDivider()
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                AppPrimaryButton(
                    text = when {
                        isSaving && isEditMode -> "Actualizando..."
                        isSaving -> "Guardando..."
                        isEditMode -> "Actualizar categoría"
                        else -> "Guardar categoría"
                    },
                    onClick = {
                        if (isSaving) return@AppPrimaryButton
                        
                        // Validaciones básicas
                        if (categoryName.isBlank() || selectedEmoji.isBlank()) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Completa el nombre y el emoji")
                            }
                            return@AppPrimaryButton
                        }
                        
                        val budgetValue = parsedBudget
                        val tipoString = if (selectedType == CategoryTypeOption.EXPENSE) "gastos" else "ingresos"
                        
                        // Validar presupuesto según el tipo
                        if (selectedType == CategoryTypeOption.EXPENSE) {
                            // Para gastos, el presupuesto es requerido y debe ser > 0
                            if (budgetValue == null || budgetValue <= 0) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("El presupuesto mensual es requerido para categorías de gastos y debe ser mayor a 0")
                                }
                                return@AppPrimaryButton
                            }
                        }
                        
                        val descripcionValue = description.takeIf { it.isNotBlank() }
                        val presupuestoFinal = budgetValue?.toLong()
                        
                        if (isEditMode && categoryId != null) {
                            viewModel.updateCategory(
                                categoryId = categoryId,
                                nombre = categoryName.trim(),
                                icono = selectedEmoji,
                                tipo = tipoString,
                                presupuestoMensual = presupuestoFinal,
                                descripcion = descripcionValue
                            )
                        } else {
                            viewModel.createCategory(
                                nombre = categoryName.trim(),
                                icono = selectedEmoji,
                                tipo = tipoString,
                                presupuestoMensual = presupuestoFinal,
                                descripcion = descripcionValue
                            )
                        }
                    },
                    enabled = !isSaving
                )
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            TextButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Cancelar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        if (isEditMode && categoryDetailState is CategoryDetailState.Loading && !hasInitializedForm) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

private enum class CategoryTypeOption(val label: String) {
    INCOME("Ingresos"),
    EXPENSE("Gastos")
}

private data class CategoryPreset(
    val name: String,
    val emoji: String,
    val type: CategoryTypeOption
)

@Composable
private fun PresetPickerRow(
    presets: List<CategoryPreset>,
    selectedName: String,
    onPresetSelected: (CategoryPreset) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        presets.forEach { preset ->
            val isSelected = selectedName == preset.name
            Surface(
                modifier = Modifier
                    .height(44.dp)
                    .clickable { onPresetSelected(preset) },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                border = if (isSelected)
                    BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                else
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .height(44.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = preset.emoji)
                    Text(
                        text = preset.name,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

private fun parseBudgetInput(input: String): Double? {
    if (input.isBlank()) return null
    val normalized = input.replace(",", "").replace(".", "")
    return normalized.toDoubleOrNull()
}
