package com.example.moneyflow.screens

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.moneyflow.data.repository.UsuarioRepository
import com.example.moneyflow.theme.ExpenseColor
import com.example.moneyflow.theme.IncomeColor
import com.example.moneyflow.utils.CurrencyFormatter
import com.example.moneyflow.ui.viewmodel.ProfileViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as Application
        )
    )
    val userFullName by viewModel.userFullName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val userId by viewModel.userId.collectAsState()
    val resumenState by viewModel.resumenState.collectAsState()
    
    // Cargar resumen al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadResumen()
    }
    
    val displayName = if (userFullName.isNotBlank()) userFullName else "Usuario"
    val displayEmail = if (userEmail.isNotBlank()) userEmail else "Sin correo"
    val initials = displayName.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercaseChar().toString() }
        .joinToString("")
        .ifBlank { "?" }
    
    // Obtener datos del resumen
    val resumen = when (val state = resumenState) {
        is com.example.moneyflow.ui.viewmodel.ResumenState.Success -> state.resumen
        else -> null
    }
    
    val balance = resumen?.balance?.toDouble() ?: 0.0
    val ingresos = resumen?.totalIngresos?.toDouble() ?: 0.0
    val gastos = resumen?.totalGastos?.toDouble() ?: 0.0
    
    // Estados para configuración
    var showSettings by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var biometricEnabled by remember { mutableStateOf(false) }
    
    // Estados para cambio de contraseña
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    val currentPasswordDisplay = "********" // Solo lectura, siempre muestra asteriscos
    var showCurrentPassword by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    var showNewPassword by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var isChangingPassword by remember { mutableStateOf(false) }
    
    val usuarioRepository = remember { UsuarioRepository() }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
        ) {
            // Profile Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .offset(y = (-64).dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFF97316),
                                        Color(0xFFEA580C)
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.White
                            )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Text(
                            text = displayEmail,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider()

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Balance",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                              Text(
                                  text = CurrencyFormatter.formatCOP(balance),
                                  style = MaterialTheme.typography.bodyLarge
                              )
                        }

                        VerticalDivider(modifier = Modifier.height(40.dp))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Ingresos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                              Text(
                                  text = CurrencyFormatter.formatCOP(ingresos),
                                  style = MaterialTheme.typography.bodyLarge,
                                  color = IncomeColor
                              )
                        }

                        VerticalDivider(modifier = Modifier.height(40.dp))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Gastos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                              Text(
                                  text = CurrencyFormatter.formatCOP(gastos),
                                  style = MaterialTheme.typography.bodyLarge,
                                  color = ExpenseColor
                              )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider()

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Botón de Configuración
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSettings = !showSettings },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Configuración",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "Ajustes y preferencias",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = if (showSettings) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Opciones de configuración expandibles
                    if (showSettings) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Notificaciones
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Notificaciones",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Recibir alertas y recordatorios",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { notificationsEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Cambiar Contraseña
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    showChangePasswordDialog = true
                                    newPassword = ""
                                    confirmPassword = ""
                                    passwordError = null
                                    showCurrentPassword = false
                                    showNewPassword = false
                                    showConfirmPassword = false
                                },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Password,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Cambiar Contraseña",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Actualizar tu contraseña de acceso",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Autenticación Biométrica
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Autenticación Biométrica",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Usar huella dactilar o reconocimiento facial",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = biometricEnabled,
                                onCheckedChange = { biometricEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Moneda
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { /* Navigate to currency selection */ },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CurrencyExchange,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Moneda",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "COP - Peso Colombiano",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Política de Privacidad
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { /* Navigate to privacy policy */ },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PrivacyTip,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Política de Privacidad",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Cómo manejamos tus datos",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Dialog para cambiar contraseña
            if (showChangePasswordDialog) {
                AlertDialog(
                    onDismissRequest = { 
                        showChangePasswordDialog = false
                        newPassword = ""
                        confirmPassword = ""
                        passwordError = null
                        showCurrentPassword = false
                        showNewPassword = false
                        showConfirmPassword = false
                    },
                    title = { Text("Cambiar Contraseña") },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Contraseña actual (solo lectura, muestra asteriscos)
                            OutlinedTextField(
                                value = currentPasswordDisplay,
                                onValueChange = { }, // No editable
                                label = { Text("Contraseña Actual") },
                                enabled = false,
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(
                                        onClick = { showCurrentPassword = !showCurrentPassword }
                                    ) {
                                        Icon(
                                            imageVector = if (showCurrentPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (showCurrentPassword) "Ocultar contraseña" else "Mostrar contraseña",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            
                            // Nueva contraseña
                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = { 
                                    newPassword = it
                                    passwordError = null
                                },
                                label = { Text("Nueva Contraseña") },
                                modifier = Modifier.fillMaxWidth(),
                                visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(
                                        onClick = { showNewPassword = !showNewPassword }
                                    ) {
                                        Icon(
                                            imageVector = if (showNewPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (showNewPassword) "Ocultar contraseña" else "Mostrar contraseña",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                isError = passwordError != null
                            )
                            
                            // Confirmar nueva contraseña
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { 
                                    confirmPassword = it
                                    passwordError = null
                                },
                                label = { Text("Confirmar Nueva Contraseña") },
                                modifier = Modifier.fillMaxWidth(),
                                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(
                                        onClick = { showConfirmPassword = !showConfirmPassword }
                                    ) {
                                        Icon(
                                            imageVector = if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (showConfirmPassword) "Ocultar contraseña" else "Mostrar contraseña",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                isError = passwordError != null,
                                supportingText = if (passwordError != null) {
                                    { Text(passwordError!!, color = MaterialTheme.colorScheme.error) }
                                } else null
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                // Validaciones
                                if (newPassword.isBlank()) {
                                    passwordError = "La nueva contraseña es requerida"
                                    return@Button
                                }
                                if (newPassword.length < 6) {
                                    passwordError = "La contraseña debe tener al menos 6 caracteres"
                                    return@Button
                                }
                                if (newPassword != confirmPassword) {
                                    passwordError = "Las contraseñas no coinciden"
                                    return@Button
                                }
                                
                                // Cambiar contraseña
                                isChangingPassword = true
                                passwordError = null
                                
                                CoroutineScope(Dispatchers.Main).launch {
                                    val currentUserId = userId
                                    if (currentUserId == null) {
                                        passwordError = "No se encontró el ID de usuario"
                                        isChangingPassword = false
                                        return@launch
                                    }
                                    
                                    usuarioRepository.cambiarContraseña(
                                        id = currentUserId,
                                        nuevaContraseña = newPassword,
                                        confirmarContraseña = confirmPassword
                                    ).onSuccess {
                                        // Éxito
                                        showChangePasswordDialog = false
                                        newPassword = ""
                                        confirmPassword = ""
                                        passwordError = null
                                        isChangingPassword = false
                                    }.onFailure { error ->
                                        passwordError = error.message ?: "Error al cambiar la contraseña"
                                        isChangingPassword = false
                                    }
                                }
                            },
                            enabled = !isChangingPassword && newPassword.isNotBlank() && confirmPassword.isNotBlank()
                        ) {
                            if (isChangingPassword) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Confirmar")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { 
                                showChangePasswordDialog = false
                                newPassword = ""
                                confirmPassword = ""
                                passwordError = null
                                showCurrentPassword = false
                                showNewPassword = false
                                showConfirmPassword = false
                            }
                        ) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            // Logout Button
            OutlinedButton(
                onClick = { navController.navigate("login") {
                    popUpTo("dashboard") { inclusive = true }
                } },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión")
            }
        }
    }
}
