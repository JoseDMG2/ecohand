# EcoHand - Aplicación de Aprendizaje de Lengua de Señas Peruanas

## 📱 Descripción
EcoHand es una aplicación móvil educativa desarrollada en Kotlin con Jetpack Compose, enfocada en el aprendizaje lúdico de lengua de señas peruanas. La aplicación proporciona un entorno interactivo para que los usuarios aprendan y practiquen la comunicación mediante señas.

## 🏗️ Arquitectura
El proyecto implementa **MVVM (Model-View-ViewModel)** con las siguientes capas:

### Estructura del Proyecto
```
com.example.ecohand
├── data/
│   ├── local/
│   │   ├── entity/         # Entidades de Room Database
│   │   ├── dao/            # Data Access Objects
│   │   └── database/       # Configuración de base de datos
│   └── repository/         # Repositorios (lógica de datos)
├── presentation/
│   ├── splash/             # Pantalla de carga
│   ├── login/              # Login y registro
│   ├── main/               # Pantalla principal con Bottom Nav
│   ├── home/               # Módulo Inicio
│   ├── lecciones/          # Módulo Lecciones
│   ├── progreso/           # Módulo Progreso
│   ├── juegos/             # Módulo Juegos
│   └── perfil/             # Módulo Perfil
├── navigation/             # Sistema de navegación
└── ui/
    └── theme/              # Tema y colores de la app
```

## 🎨 Diseño
- **Colores principales:** Azul marino (#001F3F)
- **Material Design 3** con Jetpack Compose
- **Tema personalizado** con paleta azul marino

## 🚀 Funcionalidades Implementadas

### ✅ Fase Actual
1. **Pantalla de Carga (Splash Screen)**
   - Animación de entrada
   - Navegación automática al login

2. **Sistema de Login/Registro**
   - Autenticación local con SQLite (Room)
   - Validación de campos
   - Registro de nuevos usuarios
   - Manejo de errores

3. **Navegación Principal**
   - Bottom Navigation Bar con 5 módulos
   - Navegación fluida entre pantallas
   - Estado persistente

4. **Módulos Base** (pantallas en blanco)
   - Inicio
   - Lecciones
   - Progreso
   - Juegos
   - Perfil

## 🛠️ Tecnologías y Bibliotecas

### Core
- **Kotlin** 2.0.21
- **Jetpack Compose** - UI moderna y declarativa
- **Material Design 3** - Sistema de diseño

### Arquitectura MVVM
- **ViewModel** - Gestión de estado UI
- **StateFlow** - Flujo reactivo de datos
- **Coroutines** - Programación asíncrona

### Base de Datos
- **Room** 2.6.1 - SQLite wrapper
- **KSP** - Procesamiento de anotaciones

### Navegación
- **Navigation Compose** 2.8.4 - Navegación entre pantallas

## 📦 Dependencias

```gradle
// Room Database
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")

// Navigation
implementation("androidx.navigation:navigation-compose:2.8.4")

// ViewModel
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.2")
implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.2")
```

## 🗄️ Base de Datos

### Tabla Actual: `users`
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | INTEGER | ID único (auto-increment) |
| username | TEXT | Nombre de usuario |
| email | TEXT | Correo electrónico (único) |
| password | TEXT | Contraseña |
| createdAt | LONG | Timestamp de creación |

Ver diseño completo en [DATABASE_DESIGN.md](DATABASE_DESIGN.md)

## 🎯 Próximas Funcionalidades

### Pendientes de Desarrollo
- [ ] Implementar contenido en módulo de Lecciones
- [ ] Agregar sistema de progreso del usuario
- [ ] Desarrollar juegos interactivos
- [ ] Completar perfil de usuario con estadísticas
- [ ] Añadir catálogo de señas peruanas
- [ ] Implementar reconocimiento de señas (ML)
- [ ] Sistema de logros y recompensas
- [ ] Migración a Firebase (autenticación y Firestore)

## 🔧 Instalación y Configuración

### Requisitos
- Android Studio Hedgehog | 2023.1.1 o superior
- JDK 11 o superior
- Android SDK 24+ (Android 7.0+)
- Compilación con SDK 36

### Pasos
1. Clona el repositorio:
```bash
git clone <repository-url>
```

2. Abre el proyecto en Android Studio

3. Sincroniza las dependencias de Gradle

4. Ejecuta la aplicación en un emulador o dispositivo físico

## 📝 Uso

### Primer Uso
1. La app mostrará la pantalla de carga
2. Automáticamente navegará al login
3. Registra una nueva cuenta con:
   - Nombre de usuario
   - Correo electrónico
   - Contraseña (mínimo 6 caracteres)
4. Una vez registrado, accederás automáticamente

### Navegación
- Usa la **barra inferior** para navegar entre los 5 módulos
- Los módulos mantienen su estado al cambiar entre ellos

## 🔐 Seguridad
- Las contraseñas se almacenan en texto plano (⚠️ temporal, implementar encriptación)
- Base de datos local protegida por el sistema Android
- Validación de campos en el cliente

## 🎨 Personalización de Tema

Los colores se pueden modificar en `ui/theme/Color.kt`:
```kotlin
val NavyBlue = Color(0xFF001F3F)        // Azul marino principal
val AccentBlue = Color(0xFF0074D9)      // Azul de acento
val LightBlue = Color(0xFF7FDBFF)       // Azul claro
```

## 🤝 Contribución
Este es un proyecto educativo. Para contribuir:
1. Crea un fork del proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📄 Licencia
Proyecto educativo - Universidad 2025

## 👥 Autores
- Desarrollo: [Tu nombre]
- Universidad: [Nombre de la Universidad]
- Curso: Desarrollo Móvil 2025-2

## 📞 Contacto
Para preguntas o sugerencias sobre el proyecto, contacta a través del repositorio.