package com.example.moneyflow.screens

import android.app.Application
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.moneyflow.data.models.CategoryResponse
import com.example.moneyflow.ui.viewmodel.CategoriesState
import com.example.moneyflow.ui.viewmodel.CategoriesViewModel
import com.example.moneyflow.utils.CurrencyFormatter

data class CategoryData(
    val id: String,
    val name: String,
    val icon: String,
    val spent: Double,
    val budget: Double,
    val budgetOriginal: Double? = null,
    val budgetActual: Double? = null,
    val percentage: Int,
    val backgroundColor: Color,
    val tipo: String,
    val sobrepasado: Boolean = false,
    val exceso: Double = 0.0,
    val agotado: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: CategoriesViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as Application
        )
    )
    val categoriesState by viewModel.categoriesState.collectAsState()
    
    // Cargar categorías al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadCategories(includeStats = true)
    }
    
    val refreshFlow = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("refreshCategories", false)
    val shouldRefresh by refreshFlow?.collectAsState() ?: remember { mutableStateOf(false) }

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            viewModel.loadCategories(includeStats = true)
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("refreshCategories", false)
        }
    }

    val rawCategories = when (val state = categoriesState) {
        is CategoriesState.Success -> state.categories
        else -> emptyList()
    }
    val categories = rawCategories.map { it.toCategoryData() }

    // Calcular resumen solo para categorías de gastos (para mostrar en la UI)
    // Para categorías de ingresos, el presupuestoMensual ya está actualizado por el backend
    // Los gastos se restan directamente (no proporcionalmente) empezando por la categoría
    // con mayor presupuesto. Si el presupuesto llega a $0, se genera alerta (agotado: true)
    val gastosCategories = categories.filter { it.tipo.lowercase() == "gastos" }
    val ingresosCategories = categories.filter { it.tipo.lowercase() == "ingresos" }
    
    // Resumen de gastos: suma de gastado y presupuesto de categorías de gastos
    val totalSpent = gastosCategories.sumOf { it.spent }
    val totalBudgetGastos = gastosCategories.sumOf { it.budget }
    val totalPercentageGastos = if (totalBudgetGastos > 0) ((totalSpent / totalBudgetGastos) * 100).toInt() else 0
    
    // Resumen de ingresos: usar presupuesto original y actual de las estadísticas
    val totalPresupuestoOriginalIngresos = ingresosCategories.sumOf { 
        it.budgetOriginal ?: it.budget 
    }
    val totalPresupuestoActualIngresos = ingresosCategories.sumOf { 
        it.budgetActual ?: it.budget 
    }
    val totalGastadoIngresos = ingresosCategories.sumOf { it.spent }
    val totalPorcentajeIngresos = if (totalPresupuestoOriginalIngresos > 0) {
        ((totalGastadoIngresos / totalPresupuestoOriginalIngresos) * 100).toInt()
    } else 0

    var categoryPendingDeletion by remember { mutableStateOf<CategoryData?>(null) }

    if (categoryPendingDeletion != null) {
        AlertDialog(
            onDismissRequest = { categoryPendingDeletion = null },
            title = { Text("Eliminar categoría") },
            text = {
                Text(
                    text = "¿Seguro que deseas eliminar \"${categoryPendingDeletion?.name}\"? Esta acción no se puede deshacer.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        categoryPendingDeletion?.let { viewModel.deleteCategory(it.id) }
                        categoryPendingDeletion = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryPendingDeletion = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categorías") },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_category") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Agregar categoría")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Summary Card
            // Mostrar resumen diferente según el tipo de categorías
            if (gastosCategories.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.9f)
                    ),
                    elevation = CardDefaults.cardElevation(8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                              Text(
                                  text = "Total Gastado",
                                  style = MaterialTheme.typography.bodySmall,
                                  color = Color.Gray
                              )
                              Text(
                                  text = CurrencyFormatter.formatCOP(totalSpent),
                                  style = MaterialTheme.typography.headlineMedium,
                                  color = Color.White
                              )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                              Text(
                                  text = "Presupuesto Total",
                                  style = MaterialTheme.typography.bodySmall,
                                  color = Color.Gray
                              )
                              Text(
                                  text = CurrencyFormatter.formatCOP(totalBudgetGastos),
                                  style = MaterialTheme.typography.headlineMedium,
                                  color = Color.White
                              )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Uso general",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "$totalPercentageGastos%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = (totalPercentageGastos / 100f).coerceAtMost(1f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = Color.White.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            } else if (ingresosCategories.isNotEmpty()) {
                // Resumen para categorías de ingresos
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.9f)
                    ),
                    elevation = CardDefaults.cardElevation(8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                              Text(
                                  text = "Presupuesto Original",
                                  style = MaterialTheme.typography.bodySmall,
                                  color = Color.Gray
                              )
                              Text(
                                  text = CurrencyFormatter.formatCOP(totalPresupuestoOriginalIngresos),
                                  style = MaterialTheme.typography.headlineMedium,
                                  color = Color.White
                              )
                            }
                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "$totalPorcentajeIngresos% usado",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (totalPorcentajeIngresos > 70) 
                                        MaterialTheme.colorScheme.error 
                                    else 
                                        Color.Gray
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val categoriasAgotadas = ingresosCategories.count { it.agotado }
                        if (categoriasAgotadas > 0) {
                            Text(
                                text = "⚠️ $categoriasAgotadas categoría(s) agotada(s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text(
                                text = "Porcentaje calculado sobre presupuesto original",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Categories List
            when (val state = categoriesState) {
                is CategoriesState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is CategoriesState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Error al cargar categorías",
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
                is CategoriesState.Success -> {
                    if (categories.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay categorías. Agrega una nueva.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(categories) { category ->
                                CategoryCard(
                                    category = category,
                                    onEdit = {
                                        val encodedId = Uri.encode(category.id)
                                        navController.navigate("add_category?categoryId=$encodedId")
                                    },
                                    onDelete = { categoryPendingDeletion = category }
                                )
                            }
                        }
                    }
                }
                is CategoriesState.Idle -> {
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

@Composable
fun CategoryCard(
    category: CategoryData,
    onEdit: (CategoryData) -> Unit,
    onDelete: (CategoryData) -> Unit
) {
    var actionsExpanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = category.backgroundColor
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = category.icon, style = MaterialTheme.typography.headlineSmall)
                        }
                    }

                      Column {
                          Text(
                              text = category.name,
                              style = MaterialTheme.typography.bodyLarge
                          )
                          val budgetFormatted = CurrencyFormatter.formatCOP(category.budget)
                          if (category.tipo.lowercase() == "gastos") {
                              // Para gastos: mostrar "gastado de presupuesto"
                              val spentFormatted = CurrencyFormatter.formatCOP(category.spent)
                              Text(
                                  text = "$spentFormatted de $budgetFormatted",
                                  style = MaterialTheme.typography.bodySmall,
                                  color = MaterialTheme.colorScheme.onSurfaceVariant
                              )
                          } else {
                              // Para ingresos: mostrar solo el presupuesto original
                              Row(
                                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                                  verticalAlignment = Alignment.CenterVertically
                              ) {
                                  val originalFormatted = category.budgetOriginal?.let { 
                                      CurrencyFormatter.formatCOP(it) 
                                  } ?: budgetFormatted
                                  Text(
                                      text = "Presupuesto: $originalFormatted",
                                      style = MaterialTheme.typography.bodySmall,
                                      color = if (category.agotado)
                                          MaterialTheme.colorScheme.error
                                      else
                                          MaterialTheme.colorScheme.onSurfaceVariant
                                  )
                                  if (category.agotado) {
                                      Surface(
                                          shape = RoundedCornerShape(4.dp),
                                          color = MaterialTheme.colorScheme.errorContainer
                                      ) {
                                          Text(
                                              text = "⚠️ Agotado",
                                              style = MaterialTheme.typography.labelSmall,
                                              color = MaterialTheme.colorScheme.error,
                                              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                          )
                                      }
                                  }
                              }
                          }
                      }
                }

                Box {
                    IconButton(onClick = { actionsExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opciones"
                        )
                    }
                    DropdownMenu(
                        expanded = actionsExpanded,
                        onDismissRequest = { actionsExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar") },
                            onClick = {
                                actionsExpanded = false
                                onEdit(category)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar") },
                            onClick = {
                                actionsExpanded = false
                                onDelete(category)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Solo mostrar estadísticas para categorías de gastos
            if (category.tipo.lowercase() == "gastos") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${category.percentage}% usado",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (category.sobrepasado || category.percentage > 70)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (category.sobrepasado) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Text(
                                    text = "⚠️ Excedido",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    val remaining = if (category.sobrepasado) {
                        val excesoFormatted = CurrencyFormatter.formatCOP(category.exceso)
                        Text(
                            text = "+$excesoFormatted exceso",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        val remainingAmount = CurrencyFormatter.formatCOP(category.budget - category.spent)
                        Text(
                            text = "$remainingAmount restante",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = (category.percentage / 100f).coerceAtMost(1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (category.sobrepasado || category.percentage > 70)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary,
                    trackColor = if (category.sobrepasado || category.percentage > 70)
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            } else {
                // Para categorías de ingresos: mostrar porcentaje usado y barra de progreso
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${category.percentage}% usado",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (category.agotado || category.percentage > 70)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (category.agotado) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Text(
                                    text = "⚠️ Sin presupuesto",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LinearProgressIndicator(
                        progress = (category.percentage / 100f).coerceAtMost(1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (category.agotado || category.percentage > 70)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary,
                        trackColor = if (category.agotado || category.percentage > 70)
                            MaterialTheme.colorScheme.errorContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

// Función de extensión para convertir CategoryResponse a CategoryData
private fun CategoryResponse.toCategoryData(): CategoryData {
    // IMPORTANTE: Usar datos directamente del backend, no calcular manualmente
    // 
    // Para categorías de GASTOS:
    // - spent: usar estadisticas.gastado (suma de transacciones tipo "gasto")
    // - budget: usar estadisticas.presupuesto o presupuestoMensual (límite para alertas)
    // - agotado: usar estadisticas.agotado (true si presupuesto llegó a $0)
    //
    // Para categorías de INGRESOS:
    // - spent: usar estadisticas.gastado (total de gastos del usuario)
    // - budgetOriginal: usar estadisticas.presupuestoOriginal (suma de todas las transacciones de ingreso)
    // - budgetActual: usar estadisticas.presupuestoActual (presupuesto después de restar gastos)
    // - budget: usar budgetOriginal para mostrar (o presupuestoMensual si no hay stats)
    // - percentage: usar estadisticas.porcentajeUsado (calculado sobre presupuestoOriginal)
    //   Los gastos se restan DIRECTAMENTE (no proporcionalmente) empezando por la categoría
    //   con mayor presupuesto. Si el presupuesto llega a $0, se genera alerta (agotado: true)
    
    val isGastos = tipo.lowercase() == "gastos"
    
    val spent = if (isGastos) {
        // Para gastos: usar estadisticas.gastado de la API
        estadisticas?.gastado?.toDouble() ?: 0.0
    } else {
        // Para ingresos: usar estadisticas.gastado (total de gastos del usuario)
        estadisticas?.gastado?.toDouble() ?: 0.0
    }
    
    val budgetOriginal = if (isGastos) {
        // Para gastos: no aplica presupuesto original
        null
    } else {
        // Para ingresos: usar presupuestoOriginal de estadísticas
        estadisticas?.presupuestoOriginal?.toDouble()
    }
    
    val budgetActual = if (isGastos) {
        // Para gastos: no aplica presupuesto actual
        null
    } else {
        // Para ingresos: usar presupuestoActual de estadísticas
        estadisticas?.presupuestoActual?.toDouble()
    }
    
    val budget = if (isGastos) {
        // Para gastos: usar presupuesto de estadísticas o presupuestoMensual
        estadisticas?.presupuesto?.toDouble() ?: presupuestoMensual.toDouble()
    } else {
        // Para ingresos: usar presupuestoOriginal si está disponible, sino presupuestoMensual
        budgetOriginal ?: presupuestoMensual.toDouble()
    }
    
    val percentage = if (isGastos) {
        // Para gastos: usar porcentaje de estadísticas
        estadisticas?.porcentajeUsado?.toInt() 
            ?: if (budget > 0) ((spent / budget) * 100).toInt() else 0
    } else {
        // Para ingresos: usar porcentajeUsado de estadísticas (calculado sobre presupuestoOriginal)
        estadisticas?.porcentajeUsado?.toInt() ?: 0
    }
    
    // Asignar colores según el tipo
    val backgroundColor = when (tipo.lowercase()) {
        "gastos" -> when (nombre.lowercase()) {
            "comida" -> Color(0xFFFFEDD5)
            "transporte" -> Color(0xFFDBEAFE)
            "entretenimiento" -> Color(0xFFF3E8FF)
            "salud" -> Color(0xFFFEE2E2)
            "educación" -> Color(0xFFE0E7FF)
            "compras" -> Color(0xFFFFF7ED)
            else -> Color(0xFFF3F4F6)
        }
        "ingresos" -> when (nombre.lowercase()) {
            "salario" -> Color(0xFFD1FAE5)
            else -> Color(0xFFE0F2FE)
        }
        else -> Color(0xFFF3F4F6)
    }
    
    return CategoryData(
        id = id,
        name = nombre,
        icon = icono,
        spent = spent,
        budget = budget,
        budgetOriginal = budgetOriginal,
        budgetActual = budgetActual,
        percentage = percentage,
        backgroundColor = backgroundColor,
        tipo = tipo,
        sobrepasado = estadisticas?.sobrepasado ?: false,
        exceso = estadisticas?.exceso?.toDouble() ?: 0.0,
        agotado = estadisticas?.agotado ?: false
    )
}
