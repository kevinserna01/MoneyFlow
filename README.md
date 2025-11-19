# 💰 MoneyFlow

Aplicación móvil Android para la gestión de finanzas personales desarrollada con Kotlin y Jetpack Compose.

## 📋 Descripción

MoneyFlow es una aplicación móvil que permite a los usuarios gestionar sus finanzas personales de manera eficiente. La aplicación permite registrar ingresos y gastos, categorizar transacciones, visualizar estadísticas y mantener un control completo sobre el flujo de dinero.

## 🛠️ Tecnologías Utilizadas

### Core
- **Kotlin 2.0.21** - Lenguaje de programación principal
- **Android SDK** - Plataforma de desarrollo móvil
- **Jetpack Compose** - Framework de UI declarativa
- **Material Design 3** - Sistema de diseño moderno

### Arquitectura y Patrones
- **MVVM (Model-View-ViewModel)** - Arquitectura de la aplicación
- **Repository Pattern** - Patrón de repositorio para acceso a datos
- **Navigation Component** - Navegación entre pantallas

### Networking
- **Retrofit 2.9.0** - Cliente HTTP para llamadas a API REST
- **OkHttp 4.12.0** - Cliente HTTP de bajo nivel
- **Gson 2.10.1** - Serialización/deserialización JSON

### Persistencia de Datos
- **DataStore Preferences 1.1.1** - Almacenamiento de preferencias y tokens

### UI y Componentes
- **Vico Charts 1.13.1** - Librería para gráficos y visualización de datos
- **Material Icons Extended** - Iconografía extendida

### Asincronía
- **Kotlin Coroutines 1.7.3** - Programación asíncrona

### Lifecycle
- **Lifecycle ViewModel Compose 2.7.0** - Gestión del ciclo de vida
- **Lifecycle Runtime Compose** - Runtime para Compose

### Testing
- **JUnit 4.13.2** - Framework de testing unitario
- **Espresso 3.7.0** - Framework de testing de UI

## 📦 Requisitos Previos

Antes de comenzar, asegúrate de tener instalado:

- **Android Studio** (versión Hedgehog o superior recomendada)
- **JDK 11** o superior
- **Android SDK** con:
  - `compileSdk = 36`
  - `minSdk = 26`
  - `targetSdk = 36`
- **Gradle 8.13.1** (incluido en el proyecto)
- Conexión a Internet para descargar dependencias

## 🚀 Instalación

### Paso 1: Clonar el Repositorio

```bash
git clone <url-del-repositorio>
cd MoneyFlow
```

### Paso 2: Abrir el Proyecto en Android Studio

1. Abre Android Studio
2. Selecciona **File > Open**
3. Navega hasta la carpeta del proyecto y selecciónala
4. Espera a que Android Studio sincronice el proyecto y descargue las dependencias

### Paso 3: Configurar el SDK de Android

1. Ve a **File > Project Structure > SDK Location**
2. Asegúrate de que el **Android SDK** esté correctamente configurado
3. Verifica que tengas instalado el **Android SDK Platform 36**

### Paso 4: Sincronizar Gradle

1. Android Studio debería sincronizar automáticamente
2. Si no lo hace, haz clic en **File > Sync Project with Gradle Files**
3. Espera a que se descarguen todas las dependencias

### Paso 5: Configurar la URL de la API

Edita el archivo `app/src/main/java/com/example/moneyflow/data/api/ApiConfig.kt`:

```kotlin
object ApiConfig {
    // Cambiar a false para usar el servidor de producción
    // Cambiar a true para usar el servidor local
    private const val USE_LOCAL_SERVER = false
    
    // URLs de los servidores
    private const val LOCAL_URL = "http://10.0.2.2:4000/api/"
    private const val PRODUCTION_URL = "https://moneyflow-backend-taupe.vercel.app/api/"
    
    // URL base seleccionada según la configuración
    const val BASE_URL = if (USE_LOCAL_SERVER) LOCAL_URL else PRODUCTION_URL
}
```

**Configuración de entornos:**
- **Servidor Local** (`USE_LOCAL_SERVER = true`): 
  - Para emulador Android: `http://10.0.2.2:4000/api/`
  - Para dispositivo físico en la misma red: Cambia `LOCAL_URL` a `http://[IP_DEL_SERVIDOR]:4000/api/`
- **Servidor de Producción** (`USE_LOCAL_SERVER = false`):
  - URL de producción: `https://moneyflow-backend-taupe.vercel.app/api/`

**Notas importantes:**
- Cambia el valor de `USE_LOCAL_SERVER` según el entorno que quieras usar
- El servidor local requiere que el backend esté corriendo en tu máquina
- El servidor de producción está disponible en Vercel

### Paso 6: Verificar Permisos

El archivo `AndroidManifest.xml` ya incluye el permiso de Internet necesario:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

### Paso 7: Ejecutar la Aplicación

1. Conecta un dispositivo Android o inicia un emulador
2. Haz clic en el botón **Run** (▶️) en Android Studio
3. Selecciona el dispositivo donde quieres ejecutar la app
4. Espera a que se compile e instale la aplicación

## 📁 Estructura del Proyecto

```
MoneyFlow/
├── app/
│   ├── build.gradle.kts          # Configuración del módulo app
│   ├── proguard-rules.pro        # Reglas de ProGuard
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/example/moneyflow/
│       │   │   ├── MainActivity.kt           # Actividad principal
│       │   │   ├── components/              # Componentes reutilizables
│       │   │   │   ├── AppButton.kt
│       │   │   │   ├── AppTextField.kt
│       │   │   │   └── TransactionCard.kt
│       │   │   ├── data/
│       │   │   │   ├── api/                 # Configuración de API
│       │   │   │   │   ├── ApiConfig.kt
│       │   │   │   │   ├── ApiClient.kt
│       │   │   │   │   └── ApiService.kt
│       │   │   │   ├── local/               # Almacenamiento local
│       │   │   │   │   └── TokenManager.kt
│       │   │   │   ├── models/              # Modelos de datos
│       │   │   │   │   ├── Usuario.kt
│       │   │   │   │   ├── LoginRequest.kt
│       │   │   │   │   ├── LoginResponse.kt
│       │   │   │   │   ├── TransactionRequest.kt
│       │   │   │   │   ├── TransactionResponse.kt
│       │   │   │   │   └── ...
│       │   │   │   └── repository/          # Repositorios
│       │   │   │       ├── UsuarioRepository.kt
│       │   │   │       ├── TransactionRepository.kt
│       │   │   │       └── CategoriaRepository.kt
│       │   │   ├── navigation/             # Navegación
│       │   │   │   └── NavGraph.kt
│       │   │   ├── screens/                # Pantallas de la app
│       │   │   │   ├── LoginScreen.kt
│       │   │   │   ├── RegisterScreen.kt
│       │   │   │   ├── DashboardScreen.kt
│       │   │   │   ├── AddTransactionScreen.kt
│       │   │   │   ├── EditTransactionScreen.kt
│       │   │   │   ├── TransactionsScreen.kt
│       │   │   │   ├── CategoriesScreen.kt
│       │   │   │   ├── StatisticsScreen.kt
│       │   │   │   ├── ProfileScreen.kt
│       │   │   │   └── SettingsScreen.kt
│       │   │   ├── theme/                  # Tema y estilos
│       │   │   │   ├── Color.kt
│       │   │   │   ├── Shape.kt
│       │   │   │   ├── Spacing.kt
│       │   │   │   ├── Theme.kt
│       │   │   │   └── Type.kt
│       │   │   ├── ui/
│       │   │   │   └── viewmodel/          # ViewModels
│       │   │   │       ├── LoginViewModel.kt
│       │   │   │       ├── RegisterViewModel.kt
│       │   │   │       ├── DashboardViewModel.kt
│       │   │   │       └── ...
│       │   │   ├── model/                  # Modelos de dominio
│       │   │   │   └── Transaction.kt
│       │   │   └── utils/                  # Utilidades
│       │   │       └── CurrencyFormatter.kt
│       │   └── res/                        # Recursos
│       │       ├── drawable/
│       │       ├── mipmap-*/
│       │       ├── values/
│       │       └── xml/
│       ├── androidTest/                    # Tests de integración
│       └── test/                          # Tests unitarios
├── gradle/
│   ├── libs.versions.toml                 # Versiones de dependencias
│   └── wrapper/                           # Gradle Wrapper
├── build.gradle.kts                       # Configuración del proyecto
├── settings.gradle.kts                    # Configuración de módulos
├── gradle.properties                      # Propiedades de Gradle
├── API_INTEGRATION.md                     # Documentación de API
└── README.md                              # Este archivo
```

## ⚙️ Configuración Adicional

### Configuración de Red

El proyecto incluye un archivo de configuración de seguridad de red (`network_security_config.xml`) que permite conexiones HTTP en desarrollo. Para producción, asegúrate de usar HTTPS.

### Variables de Entorno

Actualmente, la URL de la API se configura directamente en `ApiConfig.kt`. Para proyectos más grandes, considera usar:

- **BuildConfig** para diferentes variantes (debug/release)
- **Gradle properties** para configuraciones por ambiente
- **Secrets Gradle Plugin** para información sensible

## 🎯 Características Principales

- ✅ **Autenticación de Usuarios**
  - Registro de nuevos usuarios
  - Inicio de sesión con JWT
  - Gestión de tokens persistente

- ✅ **Gestión de Transacciones**
  - Crear, editar y eliminar transacciones
  - Categorización de ingresos y gastos
  - Visualización de historial

- ✅ **Dashboard**
  - Resumen financiero general
  - Balance actual
  - Últimas transacciones

- ✅ **Estadísticas**
  - Gráficos de ingresos y gastos
  - Estadísticas mensuales
  - Análisis por categorías

- ✅ **Categorías**
  - Gestión de categorías personalizadas
  - Estadísticas por categoría

- ✅ **Perfil de Usuario**
  - Información del usuario
  - Cambio de contraseña
  - Configuración de cuenta

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

## 🐛 Debugging

El cliente HTTP tiene logging habilitado. Para ver las peticiones en Logcat:

```
OkHttp: --> POST http://...
OkHttp: Content-Type: application/json
OkHttp: {"correo":"...","contraseña":"..."}
OkHttp: <-- 200 OK
```

Filtra los logs en Android Studio usando el tag: `OkHttp`

## 📱 Build y Release

### Generar APK de Debug

```bash
./gradlew assembleDebug
```

El APK se generará en: `app/build/outputs/apk/debug/app-debug.apk`

### Generar APK de Release

1. Configura la firma en `app/build.gradle.kts`
2. Ejecuta:

```bash
./gradlew assembleRelease
```

El APK se generará en: `app/build/outputs/apk/release/app-release.apk`
