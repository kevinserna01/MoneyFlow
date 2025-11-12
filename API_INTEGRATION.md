# Integración de API MoneyFlow

Esta documentación describe la implementación de la integración con la API REST de MoneyFlow en la aplicación Android.

## 📁 Estructura de Archivos

```
app/src/main/java/com/example/moneyflow/
├── data/
│   ├── api/
│   │   ├── ApiConfig.kt          # Configuración de URL base
│   │   ├── ApiClient.kt          # Cliente Retrofit con interceptores
│   │   └── ApiService.kt         # Interfaz de API
│   ├── local/
│   │   └── TokenManager.kt       # Gestión de tokens con DataStore
│   ├── models/
│   │   ├── Usuario.kt
│   │   ├── LoginRequest.kt
│   │   ├── LoginResponse.kt
│   │   ├── CreateUsuarioRequest.kt
│   │   ├── UpdateUsuarioRequest.kt
│   │   └── ApiResponse.kt
│   └── repository/
│       └── UsuarioRepository.kt  # Repositorio para llamadas a API
└── ui/
    └── viewmodel/
        ├── LoginViewModel.kt      # ViewModel para Login
        └── RegisterViewModel.kt  # ViewModel para Register
```

## ⚙️ Configuración

### 1. Configurar URL Base de la API

Edita el archivo `app/src/main/java/com/example/moneyflow/data/api/ApiConfig.kt`:

```kotlin
object ApiConfig {
    // Para emulador Android:
    const val BASE_URL = "http://10.0.2.2:4000/api/"
    
    // Para dispositivo físico en la misma red:
    // const val BASE_URL = "http://192.168.1.100:4000/api/"
    
    // Para servidor en producción:
    // const val BASE_URL = "https://api.moneyflow.com/api/"
}
```

**Notas importantes:**
- **Emulador Android**: Usa `10.0.2.2` para referenciar `localhost` de tu máquina
- **Dispositivo físico**: Usa la IP local de tu servidor en la misma red WiFi
- **Producción**: Usa HTTPS con un dominio válido

### 2. Permisos de Internet

Asegúrate de que el archivo `AndroidManifest.xml` tenga el permiso de internet:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## 🔐 Autenticación

La aplicación maneja la autenticación JWT automáticamente:

1. **Login**: Al hacer login exitoso, el token se guarda en:
   - `ApiClient` (en memoria)
   - `TokenManager` / DataStore (persistente)

2. **Peticiones autenticadas**: El interceptor de `ApiClient` agrega automáticamente el header:
   ```
   Authorization: Bearer <token>
   ```

3. **Persistencia**: El token se mantiene entre sesiones usando DataStore.

## 📱 Uso en Pantallas

### LoginScreen

```kotlin
val viewModel: LoginViewModel = viewModel(...)
val loginState by viewModel.loginState

// Manejar estados
LaunchedEffect(loginState) {
    when (loginState) {
        is LoginState.Success -> {
            navController.navigate("dashboard")
        }
        is LoginState.Error -> {
            // Mostrar error
        }
        else -> {}
    }
}

// Llamar login
viewModel.login(email, password)
```

### RegisterScreen

```kotlin
val viewModel: RegisterViewModel = viewModel(...)
val registerState by viewModel.registerState

// Llamar registro
viewModel.register(name, phone, email, password, confirmPassword)
```

## 🔄 Estados de la UI

### LoginState
- `Idle`: Estado inicial
- `Loading`: Cargando
- `Success(LoginResponse)`: Login exitoso
- `Error(String)`: Error con mensaje

### RegisterState
- `Idle`: Estado inicial
- `Loading`: Cargando
- `Success(Usuario)`: Registro exitoso
- `Error(String)`: Error con mensaje

## 🛠️ Endpoints Implementados

1. ✅ `POST /api/usuarios/login` - Login
2. ✅ `POST /api/usuarios` - Crear usuario
3. ✅ `GET /api/usuarios` - Obtener todos los usuarios
4. ✅ `GET /api/usuarios/:id` - Obtener usuario por ID
5. ✅ `PUT /api/usuarios/:id` - Actualizar usuario
6. ✅ `DELETE /api/usuarios/:id` - Eliminar usuario

## 🐛 Debugging

El cliente HTTP tiene logging habilitado. Para ver las peticiones en Logcat:

```
OkHttp: --> POST http://...
OkHttp: Content-Type: application/json
OkHttp: {"correo":"...","contraseña":"..."}
OkHttp: <-- 200 OK
```

## 📝 Próximos Pasos

- [ ] Implementar refresh token
- [ ] Agregar manejo de token expirado
- [ ] Implementar logout
- [ ] Agregar validación de conexión a internet
- [ ] Implementar caché de respuestas

## ⚠️ Notas de Seguridad

1. **HTTPS en producción**: Cambia a HTTPS antes de publicar
2. **Token storage**: Los tokens se guardan en DataStore (seguro)
3. **Validación**: Las validaciones del lado del cliente son complementarias, el backend es la fuente de verdad
