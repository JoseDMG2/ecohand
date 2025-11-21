# 🏗️ Arquitectura del Proyecto EcoHand

## 📋 Tabla de Contenidos
1. [Visión General](#visión-general)
2. [Patrón Arquitectónico](#patrón-arquitectónico)
3. [Capas de la Aplicación](#capas-de-la-aplicación)
4. [Estructura del Proyecto](#estructura-del-proyecto)
5. [Flujo de Datos](#flujo-de-datos)
6. [Componentes Principales](#componentes-principales)
7. [Tecnologías y Bibliotecas](#tecnologías-y-bibliotecas)

---

## 🎯 Visión General

EcoHand es una aplicación móvil Android desarrollada con Kotlin y Jetpack Compose que implementa una arquitectura limpia y escalable basada en el patrón **MVVM (Model-View-ViewModel)**. La aplicación está diseñada para enseñar lengua de señas peruanas de manera interactiva y gamificada.

### Características Arquitectónicas Clave
- ✅ **Separación de responsabilidades** clara entre capas
- ✅ **Gestión reactiva del estado** con Kotlin Flows y StateFlow
- ✅ **Persistencia local** con Room Database
- ✅ **Inyección de dependencias manual** con patrón Singleton
- ✅ **Navegación declarativa** con Navigation Compose
- ✅ **UI declarativa** con Jetpack Compose

---

## 🏛️ Patrón Arquitectónico

### MVVM (Model-View-ViewModel)

El proyecto implementa MVVM siguiendo las mejores prácticas de arquitectura Android:

```
┌─────────────────────────────────────────────────────────┐
│                         View                            │
│              (Jetpack Compose Screens)                  │
│  - Observa StateFlow del ViewModel                      │
│  - Renderiza UI basada en estado                        │
│  - Emite eventos de usuario al ViewModel                │
└────────────────┬────────────────────────────────────────┘
                 │
                 │ observa StateFlow
                 │ emite eventos
                 ▼
┌─────────────────────────────────────────────────────────┐
│                      ViewModel                          │
│         (Lógica de presentación y estado)               │
│  - Gestiona el estado de UI (StateFlow)                 │
│  - Procesa eventos de usuario                           │
│  - Interactúa con Repository                            │
│  - Maneja lógica de negocio de UI                       │
└────────────────┬────────────────────────────────────────┘
                 │
                 │ llama métodos
                 │ recibe datos
                 ▼
┌─────────────────────────────────────────────────────────┐
│                     Repository                          │
│              (Gestión de fuentes de datos)              │
│  - Abstrae el acceso a datos                            │
│  - Coordina entre DAOs y sesión                         │
│  - Implementa lógica de negocio de datos                │
└────────────────┬────────────────────────────────────────┘
                 │
                 │ accede a datos
                 │
                 ▼
┌─────────────────────────────────────────────────────────┐
│                  Data Source (Local)                    │
│         (Room DAOs, SharedPreferences)                  │
│  - DAOs: Acceso a base de datos SQLite                  │
│  - UserSession: Gestión de sesión (SharedPreferences)   │
│  - Entidades: Modelos de datos de Room                  │
└─────────────────────────────────────────────────────────┘
```

---

## 📂 Capas de la Aplicación

### 1. Capa de Presentación (Presentation Layer)

**Responsabilidad**: Manejar la interfaz de usuario y la interacción con el usuario.

#### 1.1 Screens (Pantallas)
Componentes Composables que representan las pantallas de la aplicación.

**Ubicación**: `presentation/[modulo]/`

**Pantallas principales**:
- **SplashScreen**: Pantalla de carga inicial
- **LoginScreen**: Autenticación y registro de usuarios
- **MainScreen**: Contenedor principal con Bottom Navigation
- **InicioScreen**: Pantalla de inicio con lecciones destacadas
- **LeccionesScreen**: Lista de lecciones disponibles
- **LeccionDetalleScreen**: Detalles de una lección específica
- **LeccionPracticaScreen**: Práctica interactiva con cámara
- **ProgresoScreen**: Estadísticas y progreso del usuario
- **JuegosScreen**: Minijuegos para practicar
- **PerfilScreen**: Perfil de usuario y configuración

#### 1.2 ViewModels
Gestiona el estado de UI y la lógica de presentación.

**Ubicación**: `presentation/[modulo]/`

**Características**:
- Extienden de `ViewModel` de Android Jetpack
- Mantienen estado con `StateFlow<UiState>`
- Exponen funciones para manejar eventos de usuario
- Interactúan con Repositories para obtener/modificar datos
- Usan `viewModelScope` para operaciones asíncronas con coroutines

**Ejemplo de estructura**:
```kotlin
class LoginViewModel(
    private val userRepository: UserRepository,
    private val userSession: UserSession
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    fun login() {
        viewModelScope.launch {
            // Lógica de login
        }
    }
}
```

**ViewModels disponibles**:
- `LoginViewModel`: Autenticación y registro
- `InicioViewModel`: Gestión de pantalla de inicio
- `LeccionesViewModel`: Gestión de lecciones
- `ProgresoViewModel`: Estadísticas y progreso
- `JuegosViewModel`: Lógica de juegos
- `PerfilViewModel`: Gestión de perfil
- `DiccionarioViewModel`: Diccionario de señas

---

### 2. Capa de Datos (Data Layer)

**Responsabilidad**: Gestionar el acceso y persistencia de datos.

#### 2.1 Entities (Entidades)
Modelos de datos que representan tablas de Room Database.

**Ubicación**: `data/local/entity/`

**Entidades principales**:

```kotlin
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val email: String,
    val password: String,
    val createdAt: Long = System.currentTimeMillis()
)
```

**Todas las entidades**:
- **UserEntity**: Usuarios de la aplicación
- **LeccionEntity**: Lecciones de lengua de señas
- **ProgresoLeccionEntity**: Progreso de usuario en lecciones
- **ActividadDiariaEntity**: Registro de actividad diaria
- **LogroEntity**: Logros disponibles en la app
- **LogroUsuarioEntity**: Logros desbloqueados por usuario
- **EstadisticasUsuarioEntity**: Estadísticas generales del usuario
- **SenaEntity**: Diccionario de señas
- **PartidaJuegoEntity**: Registro de partidas de juegos

#### 2.2 DAOs (Data Access Objects)
Interfaces que definen métodos de acceso a la base de datos.

**Ubicación**: `data/local/dao/`

**Características**:
- Interfaces anotadas con `@Dao`
- Métodos suspendidos para operaciones asíncronas
- Queries SQL con anotaciones Room (`@Query`, `@Insert`, `@Update`, `@Delete`)
- Retornan tipos directos o `Flow<>` para datos reactivos

**Ejemplo**:
```kotlin
@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: UserEntity): Long
    
    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    suspend fun login(email: String, password: String): UserEntity?
}
```

**DAOs disponibles**:
- `UserDao`: Operaciones de usuarios
- `LeccionDao`: Gestión de lecciones
- `ProgresoLeccionDao`: Progreso en lecciones
- `ActividadDiariaDao`: Actividad diaria
- `LogroDao` / `LogroUsuarioDao`: Gestión de logros
- `EstadisticasUsuarioDao`: Estadísticas del usuario
- `SenaDao`: Diccionario de señas
- `PartidaJuegoDao`: Historial de juegos

#### 2.3 Database
Clase principal de Room Database que configura la base de datos.

**Ubicación**: `data/local/database/EcoHandDatabase.kt`

**Características**:
- Anotada con `@Database`
- Implementa patrón Singleton para una única instancia
- Define versión de base de datos (actualmente v4)
- Incluye callback para población inicial de datos
- Provee acceso a todos los DAOs

```kotlin
@Database(
    entities = [
        UserEntity::class,
        LeccionEntity::class,
        // ... otras entidades
    ],
    version = 4,
    exportSchema = false
)
abstract class EcoHandDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    // ... otros DAOs
    
    companion object {
        @Volatile
        private var INSTANCE: EcoHandDatabase? = null
        
        fun getDatabase(context: Context): EcoHandDatabase {
            // Implementación Singleton
        }
    }
}
```

#### 2.4 Repositories
Capa de abstracción entre ViewModels y fuentes de datos.

**Ubicación**: `data/repository/`

**Responsabilidades**:
- Abstraer el origen de los datos (local/remoto)
- Implementar lógica de negocio relacionada con datos
- Coordinar entre múltiples DAOs si es necesario
- Manejar errores y retornar `Result<T>` cuando corresponde

**Ejemplo**:
```kotlin
class UserRepository(private val userDao: UserDao) {
    suspend fun login(email: String, password: String): Result<UserEntity> {
        return try {
            val user = userDao.login(email, password)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Credenciales incorrectas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

**Repositories disponibles**:
- `UserRepository`: Gestión de usuarios y autenticación
- `InicioRepository`: Datos de pantalla de inicio
- `LeccionRepository`: Gestión de lecciones y progreso
- `ProgresoRepository`: Estadísticas y progreso del usuario
- `JuegoRepository`: Lógica de juegos y señas
- `PerfilRepository`: Datos del perfil de usuario
- `DiccionarioRepository`: Diccionario de señas

#### 2.5 Session (Gestión de Sesión)
Maneja la persistencia de la sesión del usuario.

**Ubicación**: `data/session/UserSession.kt`

**Características**:
- Usa `SharedPreferences` para persistir datos de sesión
- Implementa patrón Singleton
- Almacena: userId, username, email, isLoggedIn
- Proporciona métodos para guardar, obtener y limpiar sesión

```kotlin
class UserSession(context: Context) {
    fun saveUserSession(userId: Int, username: String, email: String)
    fun getUserId(): Int
    fun isLoggedIn(): Boolean
    fun clearSession()
}
```

---

### 3. Capa de Navegación (Navigation Layer)

**Responsabilidad**: Gestionar la navegación entre pantallas.

**Ubicación**: `navigation/`

#### 3.1 Archivos principales

**AppNavigation.kt**: Navegación principal de la app
```kotlin
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) { SplashScreen(...) }
        composable(Screen.Login.route) { LoginScreen(...) }
        composable(Screen.Home.route) { MainScreen() }
    }
}
```

**Screen.kt**: Define rutas de navegación
```kotlin
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Home : Screen("home")
    object Inicio : Screen("inicio")
    object Lecciones : Screen("lecciones")
    // ... más pantallas
}
```

**BottomNavItem.kt**: Configuración de Bottom Navigation
```kotlin
data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavItem("Inicio", Icons.Outlined.Home, Icons.Filled.Home, Screen.Inicio.route),
    // ... más items
)
```

#### 3.2 Flujo de navegación

1. **Inicio de la app**: `Splash` → `Login` → `MainScreen`
2. **Bottom Navigation**: Navega entre 5 módulos principales
3. **Navegación con argumentos**: Ej. `leccion_detalle/{leccionId}`
4. **Back Stack management**: Uso de `popUpTo` para control del stack

---

### 4. Capa de UI/Tema (UI Layer)

**Responsabilidad**: Definir el sistema de diseño y temas visuales.

**Ubicación**: `ui/theme/`

#### 4.1 Archivos de tema

**Color.kt**: Define la paleta de colores
```kotlin
val NavyBlue = Color(0xFF001F3F)          // Azul marino principal
val LightNavyBlue = Color(0xFF003366)     // Azul marino claro
val AccentBlue = Color(0xFF0074D9)        // Azul de acento
val DarkNavyBlue = Color(0xFF001529)      // Azul marino oscuro
val LightBlue = Color(0xFF7FDBFF)         // Azul claro
```

**Theme.kt**: Configuración de Material Design 3
- `LightColorScheme`: Tema claro
- `DarkColorScheme`: Tema oscuro
- `EcohandTheme`: Composable principal del tema

**Type.kt**: Tipografía de la aplicación
- Define los estilos de texto usando Material Design 3 Typography

---

## 🔄 Flujo de Datos

### Flujo típico de una operación:

```
1. Usuario interactúa con la UI (View)
   └─> Ejemplo: Click en botón "Iniciar Sesión"

2. La Screen llama a función del ViewModel
   └─> loginViewModel.login()

3. ViewModel actualiza el estado a "cargando"
   └─> _uiState.value = currentState.copy(isLoading = true)

4. ViewModel llama al Repository
   └─> userRepository.login(email, password)

5. Repository ejecuta operación en DAO
   └─> userDao.login(email, password)

6. DAO ejecuta query SQL en Room Database
   └─> SELECT * FROM users WHERE email = ? AND password = ?

7. Room retorna resultado al DAO
   └─> UserEntity? (null si no existe)

8. DAO retorna al Repository
   └─> Repository procesa y retorna Result<UserEntity>

9. Repository retorna al ViewModel
   └─> ViewModel procesa el resultado

10. ViewModel actualiza estado con resultado
    └─> _uiState.value = currentState.copy(isLoading = false, isLoginSuccessful = true)

11. Screen observa cambio de estado (StateFlow)
    └─> UI se recompone automáticamente

12. Usuario ve feedback visual
    └─> Navegación a pantalla principal
```

### Gestión de Estado Reactivo

```kotlin
// En ViewModel
private val _uiState = MutableStateFlow(UiState())
val uiState: StateFlow<UiState> = _uiState.asStateFlow()

// En Screen (Composable)
val uiState by viewModel.uiState.collectAsState()

// Recomposición automática cuando cambia el estado
when {
    uiState.isLoading -> CircularProgressIndicator()
    uiState.isSuccess -> NavigateToHome()
    uiState.error != null -> ShowError(uiState.error)
}
```

---

## 🧩 Componentes Principales

### MainActivity
Punto de entrada de la aplicación.

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EcohandTheme {
                AppNavigation()
            }
        }
    }
}
```

### Base de Datos
- **Nombre**: `ecohand_database`
- **Versión**: 4
- **Estrategia de migración**: `fallbackToDestructiveMigration()`
- **Inicialización**: Poblado automático con datos predeterminados (lecciones, logros, señas)

### Gestión de Dependencias
- **Patrón**: Inyección manual / Factory pattern
- **Singletons**: Database, UserSession, Repositories
- **ViewModels**: Creados con `remember` en composables

---

## 🛠️ Tecnologías y Bibliotecas

### Core
- **Kotlin** 2.0.21 - Lenguaje de programación
- **Android SDK** 24-36 - Plataforma Android
- **Jetpack Compose** - UI declarativa moderna

### Arquitectura
- **ViewModel** - Gestión de estado UI (lifecycle-viewmodel-compose:2.9.2)
- **StateFlow & Flow** - Programación reactiva
- **Coroutines** - Programación asíncrona (kotlinx-coroutines)

### Persistencia
- **Room Database** 2.6.1 - ORM para SQLite
  - `room-runtime`: Runtime de Room
  - `room-ktx`: Extensiones Kotlin y soporte de Coroutines
  - `room-compiler`: Procesador de anotaciones (KSP)
- **SharedPreferences** - Gestión de sesión

### Navegación
- **Navigation Compose** 2.8.4 - Sistema de navegación declarativo

### UI
- **Material Design 3** - Sistema de diseño
- **Compose BOM** - Bill of Materials para Compose
- **CameraX** - Para reconocimiento de señas (práctica de lecciones)
- **Coil** - Carga de imágenes

### Procesamiento
- **KSP** (Kotlin Symbol Processing) - Procesamiento de anotaciones para Room

---

## 📊 Diagrama de Arquitectura Completo

```
┌──────────────────────────────────────────────────────────────────┐
│                          PRESENTATION                             │
├──────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌──────────────┐  ┌─────────────┐             │
│  │   Screens   │  │  ViewModels  │  │ UI/Theme    │             │
│  │  (Compose)  │─▶│ (StateFlow)  │  │ (Material3) │             │
│  └─────────────┘  └──────────────┘  └─────────────┘             │
└────────────────────────┬─────────────────────────────────────────┘
                         │
                         │ Observa/Emite eventos
                         ▼
┌──────────────────────────────────────────────────────────────────┐
│                           DOMAIN                                  │
├──────────────────────────────────────────────────────────────────┤
│  ┌────────────────────────────────────────────────────────────┐  │
│  │                    Repositories                            │  │
│  │  - UserRepository    - LeccionRepository                   │  │
│  │  - ProgresoRepository - JuegoRepository                    │  │
│  └────────────────────────────────────────────────────────────┘  │
└────────────────────────┬─────────────────────────────────────────┘
                         │
                         │ Accede a datos
                         ▼
┌──────────────────────────────────────────────────────────────────┐
│                            DATA                                   │
├──────────────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐           │
│  │   Entities   │  │     DAOs     │  │   Database   │           │
│  │   (Models)   │  │  (Queries)   │  │(Room/SQLite) │           │
│  └──────────────┘  └──────────────┘  └──────────────┘           │
│  ┌──────────────────────────────────────────────────┐           │
│  │            UserSession (SharedPreferences)        │           │
│  └──────────────────────────────────────────────────┘           │
└──────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│                         NAVIGATION                                │
├──────────────────────────────────────────────────────────────────┤
│  AppNavigation, Screens, BottomNavItems                          │
└──────────────────────────────────────────────────────────────────┘
```

---

## 🎯 Ventajas de esta Arquitectura

### ✅ Mantenibilidad
- Código organizado por capas con responsabilidades claras
- Fácil de entender y modificar

### ✅ Testabilidad
- Cada capa puede testearse independientemente
- ViewModels separados de la UI
- Repositories abstraen fuentes de datos

### ✅ Escalabilidad
- Fácil agregar nuevas features sin afectar código existente
- Estructura modular por funcionalidad

### ✅ Reactividad
- UI actualizada automáticamente con StateFlow
- Gestión de estado predecible

### ✅ Separación de Concerns
- UI solo se preocupa de renderizar estado
- ViewModel gestiona lógica de presentación
- Repository gestiona acceso a datos
- DAO ejecuta operaciones de base de datos

---

## 🔜 Posibles Mejoras Futuras

### Inyección de Dependencias
- Implementar **Hilt** o **Koin** para inyección automática
- Eliminar inyección manual de dependencias

### Capa de Domain
- Agregar casos de uso (Use Cases) entre ViewModels y Repositories
- Centralizar lógica de negocio compleja

### Testing
- Unit tests para ViewModels
- Integration tests para Repositories
- UI tests con Compose Testing

### Remote Data
- Implementar Remote Data Source (API REST)
- Sincronización local-remoto
- Migración a Firebase o backend propio

### Modularización
- Separar por features en módulos Gradle
- Crear módulos de core, data, domain, presentation

---

## 📚 Referencias

- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [MVVM Pattern](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93viewmodel)
