package com.example.moneyflow.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.moneyflow.theme.ExpenseColor
import com.example.moneyflow.theme.IncomeColor
import com.example.moneyflow.ui.viewmodel.StatisticsState
import com.example.moneyflow.ui.viewmodel.StatisticsViewModel
import com.example.moneyflow.utils.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(navController: NavController) {
    val viewModel: StatisticsViewModel = viewModel()
    val estadisticasState by viewModel.estadisticasState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadEstadisticas()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas") },
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
        when (val state = estadisticasState) {
            is StatisticsState.Idle, is StatisticsState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is StatisticsState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Error: ${state.message}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            is StatisticsState.Success -> {
                val estadisticas = state.estadisticas
                val ingresosActual = estadisticas.mesActual.ingresos
                val gastosActual = estadisticas.mesActual.gastos
                val comparacionIngresos = estadisticas.comparacion.ingresos
                val comparacionGastos = estadisticas.comparacion.gastos
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Summary Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Income Card
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = IncomeColor
                            ),
                            shape = RoundedCornerShape(16.dp)
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
                                        color = Color.White.copy(alpha = 0.2f)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.TrendingDown,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Ingresos",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = CurrencyFormatter.formatCOP(ingresosActual.toDouble()),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White
                                )
                                Text(
                                    text = "${if (comparacionIngresos.esPositivo) "+" else ""}${String.format("%.2f", comparacionIngresos.porcentajeCambio)}% este mes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }

                        // Expense Card
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = ExpenseColor
                            ),
                            shape = RoundedCornerShape(16.dp)
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
                                        color = Color.White.copy(alpha = 0.2f)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.TrendingUp,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Gastos",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = CurrencyFormatter.formatCOP(gastosActual.toDouble()),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White
                                )
                                Text(
                                    text = "${if (comparacionGastos.esPositivo) "+" else ""}${String.format("%.2f", comparacionGastos.porcentajeCambio)}% este mes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    // Chart de Barras
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(380.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Comparación Mensual",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Gráfico de barras
                            val maxValue = maxOf(
                                estadisticas.mesActual.ingresos,
                                estadisticas.mesActual.gastos,
                                estadisticas.mesAnterior.ingresos,
                                estadisticas.mesAnterior.gastos
                            ).toFloat()
                            
                            // Obtener colores fuera del Canvas (usar los mismos colores de las cards)
                            val baseLineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            val incomeColorAnterior = IncomeColor.copy(alpha = 0.6f)
                            val expenseColorAnterior = ExpenseColor.copy(alpha = 0.6f)
                            val incomeColorActual = IncomeColor
                            val expenseColorActual = ExpenseColor
                            
                            var selectedBar by remember { mutableStateOf<String?>(null) }
                            
                            Box {
                                Canvas(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight()
                                ) {
                                    val barWidth = size.width / 4f - 20f
                                    val chartHeight = size.height - 80f
                                    val startY = 20f
                                    val spacing = 12f
                                    
                                    // Dibujar barras del mes anterior
                                    val anteriorIngresosHeight = (estadisticas.mesAnterior.ingresos.toFloat() / maxValue) * chartHeight
                                    val anteriorGastosHeight = (estadisticas.mesAnterior.gastos.toFloat() / maxValue) * chartHeight
                                    
                                    // Barra de ingresos mes anterior con sombra
                                    val bar1X = spacing
                                    val bar1Y = startY + chartHeight - anteriorIngresosHeight
                                    drawRoundRect(
                                        color = if (selectedBar == "ingresos_anterior") incomeColorActual else incomeColorAnterior,
                                        topLeft = Offset(bar1X, bar1Y),
                                        size = Size(barWidth, anteriorIngresosHeight),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                                    )
                                    // Sombra
                                    drawRoundRect(
                                        color = Color.Black.copy(alpha = 0.1f),
                                        topLeft = Offset(bar1X + 2f, bar1Y + 2f),
                                        size = Size(barWidth, anteriorIngresosHeight),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                                    )
                                    
                                    // Barra de gastos mes anterior
                                    val bar2X = bar1X + barWidth + spacing
                                    val bar2Y = startY + chartHeight - anteriorGastosHeight
                                    drawRoundRect(
                                        color = if (selectedBar == "gastos_anterior") expenseColorActual else expenseColorAnterior,
                                        topLeft = Offset(bar2X, bar2Y),
                                        size = Size(barWidth, anteriorGastosHeight),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                                    )
                                    // Sombra
                                    drawRoundRect(
                                        color = Color.Black.copy(alpha = 0.1f),
                                        topLeft = Offset(bar2X + 2f, bar2Y + 2f),
                                        size = Size(barWidth, anteriorGastosHeight),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                                    )
                                    
                                    // Dibujar barras del mes actual
                                    val actualIngresosHeight = (estadisticas.mesActual.ingresos.toFloat() / maxValue) * chartHeight
                                    val actualGastosHeight = (estadisticas.mesActual.gastos.toFloat() / maxValue) * chartHeight
                                    
                                    // Barra de ingresos mes actual
                                    val bar3X = bar2X + barWidth + spacing
                                    val bar3Y = startY + chartHeight - actualIngresosHeight
                                    drawRoundRect(
                                        color = if (selectedBar == "ingresos_actual") IncomeColor.copy(alpha = 0.8f) else incomeColorActual,
                                        topLeft = Offset(bar3X, bar3Y),
                                        size = Size(barWidth, actualIngresosHeight),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                                    )
                                    // Sombra
                                    drawRoundRect(
                                        color = Color.Black.copy(alpha = 0.1f),
                                        topLeft = Offset(bar3X + 2f, bar3Y + 2f),
                                        size = Size(barWidth, actualIngresosHeight),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                                    )
                                    
                                    // Barra de gastos mes actual
                                    val bar4X = bar3X + barWidth + spacing
                                    val bar4Y = startY + chartHeight - actualGastosHeight
                                    drawRoundRect(
                                        color = if (selectedBar == "gastos_actual") ExpenseColor.copy(alpha = 0.8f) else expenseColorActual,
                                        topLeft = Offset(bar4X, bar4Y),
                                        size = Size(barWidth, actualGastosHeight),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                                    )
                                    // Sombra
                                    drawRoundRect(
                                        color = Color.Black.copy(alpha = 0.1f),
                                        topLeft = Offset(bar4X + 2f, bar4Y + 2f),
                                        size = Size(barWidth, actualGastosHeight),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                                    )
                                    
                                    // Dibujar línea base
                                    drawLine(
                                        color = baseLineColor,
                                        start = Offset(0f, startY + chartHeight),
                                        end = Offset(size.width, startY + chartHeight),
                                        strokeWidth = 3f
                                    )
                                    
                                    // Mostrar valores en las barras si están seleccionadas
                                    if (selectedBar != null) {
                                        val (label, value) = when (selectedBar) {
                                            "ingresos_anterior" -> "Ingresos\nMes Anterior" to estadisticas.mesAnterior.ingresos
                                            "gastos_anterior" -> "Gastos\nMes Anterior" to estadisticas.mesAnterior.gastos
                                            "ingresos_actual" -> "Ingresos\nMes Actual" to estadisticas.mesActual.ingresos
                                            "gastos_actual" -> "Gastos\nMes Actual" to estadisticas.mesActual.gastos
                                            else -> "" to 0L
                                        }
                                        if (label.isNotEmpty()) {
                                            // Fondo para el texto
                                            drawRoundRect(
                                                color = Color.Black.copy(alpha = 0.7f),
                                                topLeft = Offset(size.width / 2f - 80f, 10f),
                                                size = Size(160f, 50f),
                                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                                            )
                                        }
                                    }
                                }
                                
                                // Tooltip
                                if (selectedBar != null) {
                                    val (label, value) = when (selectedBar) {
                                        "ingresos_anterior" -> "Ingresos Mes Anterior" to estadisticas.mesAnterior.ingresos
                                        "gastos_anterior" -> "Gastos Mes Anterior" to estadisticas.mesAnterior.gastos
                                        "ingresos_actual" -> "Ingresos Mes Actual" to estadisticas.mesActual.ingresos
                                        "gastos_actual" -> "Gastos Mes Actual" to estadisticas.mesActual.gastos
                                        else -> "" to 0L
                                    }
                                    if (label.isNotEmpty()) {
                                        Surface(
                                            modifier = Modifier
                                                .align(Alignment.TopCenter)
                                                .padding(top = 8.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color.Black.copy(alpha = 0.8f)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(12.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = CurrencyFormatter.formatCOP(value.toDouble()),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Leyenda
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(
                                                    IncomeColor.copy(alpha = 0.6f),
                                                    RoundedCornerShape(2.dp)
                                                )
                                        )
                                        Text(
                                            text = "Ingresos",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "Mes Anterior",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                                
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(
                                                    ExpenseColor.copy(alpha = 0.6f),
                                                    RoundedCornerShape(2.dp)
                                                )
                                        )
                                        Text(
                                            text = "Gastos",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "Mes Anterior",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                                
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(
                                                    IncomeColor,
                                                    RoundedCornerShape(2.dp)
                                                )
                                        )
                                        Text(
                                            text = "Ingresos",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "Mes Actual",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                                
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(
                                                    ExpenseColor,
                                                    RoundedCornerShape(2.dp)
                                                )
                                        )
                                        Text(
                                            text = "Gastos",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "Mes Actual",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Gráfico de Torta
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Distribución del Mes Actual",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            val total = estadisticas.mesActual.ingresos + estadisticas.mesActual.gastos
                            val ingresosPorcentaje = if (total > 0) (estadisticas.mesActual.ingresos.toFloat() / total.toFloat()) * 100f else 0f
                            val gastosPorcentaje = if (total > 0) (estadisticas.mesActual.gastos.toFloat() / total.toFloat()) * 100f else 0f
                            
                            // Colores para el gráfico de torta (usar los mismos colores de las cards)
                            val incomeColorActual = IncomeColor
                            val expenseColorActual = ExpenseColor
                            val surfaceColor = MaterialTheme.colorScheme.surface
                            
                            var selectedSegment by remember { mutableStateOf<String?>(null) }
                            
                            Box(
                                modifier = Modifier
                                    .size(250.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                var canvasSize by remember { mutableStateOf(Size.Zero) }
                                
                                Canvas(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .pointerInput(Unit) {
                                            detectTapGestures { tapOffset ->
                                                if (canvasSize != Size.Zero) {
                                                    val centerX = canvasSize.width / 2f
                                                    val centerY = canvasSize.height / 2f
                                                    val radius = minOf(canvasSize.width, canvasSize.height) / 2f - 20f
                                                    val dx = tapOffset.x - centerX
                                                    val dy = tapOffset.y - centerY
                                                    val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                                                    
                                                    if (distance <= radius) {
                                                        val angle = kotlin.math.atan2(dy, dx) * 180f / PI.toFloat() + 180f
                                                        val ingresosAngle = ingresosPorcentaje * 360f / 100f
                                                        
                                                        selectedSegment = if (angle <= ingresosAngle) {
                                                            "ingresos"
                                                        } else {
                                                            "gastos"
                                                        }
                                                    } else {
                                                        selectedSegment = null
                                                    }
                                                }
                                            }
                                        }
                                ) {
                                    canvasSize = size
                                    val centerX = size.width / 2f
                                    val centerY = size.height / 2f
                                    val radius = minOf(size.width, size.height) / 2f - 20f
                                    
                                    // Calcular ángulos
                                    val ingresosAngle = ingresosPorcentaje * 360f / 100f
                                    val gastosAngle = gastosPorcentaje * 360f / 100f
                                    
                                    // Dibujar segmento de ingresos
                                    if (ingresosPorcentaje > 0) {
                                        val path = Path().apply {
                                            moveTo(centerX, centerY)
                                            arcTo(
                                                rect = androidx.compose.ui.geometry.Rect(
                                                    centerX - radius,
                                                    centerY - radius,
                                                    centerX + radius,
                                                    centerY + radius
                                                ),
                                                startAngleDegrees = -90f,
                                                sweepAngleDegrees = ingresosAngle,
                                                forceMoveTo = false
                                            )
                                            close()
                                        }
                                        
                                        val incomeColor = if (selectedSegment == "ingresos") {
                                            IncomeColor.copy(alpha = 0.8f)
                                        } else {
                                            incomeColorActual
                                        }
                                        
                                        drawPath(
                                            path = path,
                                            color = incomeColor,
                                            style = androidx.compose.ui.graphics.drawscope.Fill
                                        )
                                        
                                        // Borde
                                        drawPath(
                                            path = path,
                                            color = Color.White,
                                            style = Stroke(width = 4f)
                                        )
                                    }
                                    
                                    // Dibujar segmento de gastos
                                    if (gastosPorcentaje > 0) {
                                        val path = Path().apply {
                                            moveTo(centerX, centerY)
                                            arcTo(
                                                rect = androidx.compose.ui.geometry.Rect(
                                                    centerX - radius,
                                                    centerY - radius,
                                                    centerX + radius,
                                                    centerY + radius
                                                ),
                                                startAngleDegrees = -90f + ingresosAngle,
                                                sweepAngleDegrees = gastosAngle,
                                                forceMoveTo = false
                                            )
                                            close()
                                        }
                                        
                                        val expenseColor = if (selectedSegment == "gastos") {
                                            ExpenseColor.copy(alpha = 0.8f)
                                        } else {
                                            expenseColorActual
                                        }
                                        
                                        drawPath(
                                            path = path,
                                            color = expenseColor,
                                            style = androidx.compose.ui.graphics.drawscope.Fill
                                        )
                                        
                                        // Borde
                                        drawPath(
                                            path = path,
                                            color = Color.White,
                                            style = Stroke(width = 4f)
                                        )
                                    }
                                    
                                    // Círculo central (donut chart effect)
                                    drawCircle(
                                        color = surfaceColor,
                                        radius = radius * 0.5f,
                                        center = Offset(centerX, centerY)
                                    )
                                }
                                
                                // Información central
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Total",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = CurrencyFormatter.formatCOP(total.toDouble()),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                
                                // Tooltip para el segmento seleccionado
                                if (selectedSegment != null) {
                                    val (label, value, porcentaje) = when (selectedSegment) {
                                        "ingresos" -> Triple(
                                            "Ingresos",
                                            estadisticas.mesActual.ingresos,
                                            ingresosPorcentaje
                                        )
                                        "gastos" -> Triple(
                                            "Gastos",
                                            estadisticas.mesActual.gastos,
                                            gastosPorcentaje
                                        )
                                        else -> Triple("", 0L, 0f)
                                    }
                                    
                                    if (label.isNotEmpty()) {
                                        Surface(
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .padding(bottom = 16.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color.Black.copy(alpha = 0.85f),
                                            shadowElevation = 8.dp
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(16.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = CurrencyFormatter.formatCOP(value.toDouble()),
                                                    style = MaterialTheme.typography.headlineSmall,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = "${String.format("%.1f", porcentaje)}%",
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    color = Color.White.copy(alpha = 0.9f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Leyenda del gráfico de torta
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .background(
                                                incomeColorActual,
                                                RoundedCornerShape(4.dp)
                                            )
                                    )
                                    Column {
                                        Text(
                                            text = "Ingresos",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${String.format("%.1f", ingresosPorcentaje)}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .background(
                                                expenseColorActual,
                                                RoundedCornerShape(4.dp)
                                            )
                                    )
                                    Column {
                                        Text(
                                            text = "Gastos",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${String.format("%.1f", gastosPorcentaje)}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
