# 🚀 Guía de Uso - Sección de Progreso EcoHand

## ✅ Implementación Completada

Se ha implementado exitosamente la sección de **Progreso** en la aplicación EcoHand con todas las funcionalidades solicitadas.

## 📋 Qué Se Implementó

### 1. **Base de Datos** 
- ✅ 6 nuevas tablas en SQLite
- ✅ Datos iniciales (5 lecciones y 8 logros)
- ✅ Relaciones entre tablas con Foreign Keys
- ✅ Actualización automática de estadísticas

### 2. **Interfaz de Usuario**
- ✅ Card de Progreso General con barra de progreso
- ✅ Grid 2x2 de estadísticas (Puntos, Racha, Lecciones, Días Activos)
- ✅ Card de Actividad Semanal (L-D)
- ✅ Card de Logros con estado visual

### 3. **Funcionalidades Automáticas**
- ✅ Registro automático de actividad diaria
- ✅ Cálculo de rachas consecutivas
- ✅ Verificación y desbloqueo de logros
- ✅ Persistencia de sesión de usuario

## 🎯 Cómo Usar

### Al Iniciar Sesión
1. El usuario inicia sesión o se registra
2. La sesión se guarda automáticamente en SharedPreferences
3. Al navegar a "Progreso", se registra la actividad del día

### Visualización del Progreso
- **Progreso General**: Muestra % de lecciones completadas
- **Puntos Total**: Suma de puntuaciones obtenidas
- **Racha**: Días consecutivos activos
- **Lecciones**: Cantidad completada
- **Días Activos**: Total de días de uso
- **Actividad Semanal**: Vista del lunes al domingo actual
- **Logros**: Lista con estado obtenido/bloqueado

## 🔧 Próximos Pasos para Desarrollo

Para que el sistema de progreso funcione completamente, necesitas:

### 1. **Implementar Sistema de Lecciones**
```kotlin
// En LeccionesScreen.kt o donde implementes las lecciones
viewModelScope.launch {
    // Al completar una lección
    val progreso = ProgresoLeccionEntity(
        usuarioId = usuarioId,
        leccionId = leccionId,
        completada = true,
        puntuacion = 85, // 0-100
        intentos = 1,
        fechaCompletado = System.currentTimeMillis()
    )
    progresoLeccionDao.insertProgreso(progreso)
    
    // Actualizar estadísticas
    val estadisticas = estadisticasUsuarioDao.getEstadisticasByUsuario(usuarioId)
    estadisticas?.let {
        estadisticasUsuarioDao.updateEstadisticas(
            it.copy(
                puntosTotal = it.puntosTotal + 85,
                leccionesCompletadas = it.leccionesCompletadas + 1
            )
        )
    }
}
```

### 2. **Llamar a verificarLogros()**
Después de completar acciones importantes:
```kotlin
progresoRepository.verificarLogros(usuarioId)
```

### 3. **Mostrar Notificaciones de Logros**
```kotlin
// Cuando se desbloquea un logro
if (logroDesbloqueado) {
    Toast.makeText(context, "¡Logro desbloqueado: ${logro.nombre}!", Toast.LENGTH_LONG).show()
    // O usar un Dialog personalizado
}
```

## 📊 Datos de Prueba

### Para Probar el Sistema

1. **Crear Usuario de Prueba**:
   - Email: test@test.com
   - Contraseña: test123

2. **Agregar Progreso Manualmente** (opcional para pruebas):
```sql
-- Completar una lección
INSERT INTO progreso_lecciones (usuarioId, leccionId, completada, puntuacion, intentos, fechaCompletado)
VALUES (1, 1, 1, 100, 1, strftime('%s', 'now') * 1000);

-- Actualizar estadísticas
UPDATE estadisticas_usuario 
SET puntosTotal = 100, leccionesCompletadas = 1 
WHERE usuarioId = 1;
```

3. **Agregar Actividades para Racha**:
```sql
-- Agregar actividad de días anteriores
INSERT INTO actividad_diaria (usuarioId, fecha, activo)
VALUES 
  (1, strftime('%s', 'now', '-1 day') * 1000, 1),
  (1, strftime('%s', 'now', '-2 day') * 1000, 1),
  (1, strftime('%s', 'now', '-3 day') * 1000, 1);
```

## 🐛 Solución de Problemas

### Si el progreso no se muestra:
1. Verificar que el usuario esté logueado correctamente
2. Revisar que `UserSession.getUserId()` retorne un ID válido
3. Verificar logs en Logcat para errores

### Si los logros no se desbloquean:
1. Asegurarse de que las estadísticas se actualicen al completar lecciones
2. Llamar manualmente a `progresoRepository.verificarLogros(usuarioId)`

### Si hay errores de compilación:
```bash
# Limpiar y reconstruir
./gradlew clean
./gradlew build

# O en Android Studio:
Build > Clean Project
Build > Rebuild Project
```

## 📁 Archivos Importantes

### Para Modificar Lecciones:
- `EcoHandDatabase.kt` (líneas 65-110) - Datos iniciales de lecciones

### Para Modificar Logros:
- `EcoHandDatabase.kt` (líneas 112-150) - Datos iniciales de logros
- `ProgresoRepository.kt` (líneas 145-180) - Lógica de verificación

### Para Modificar UI:
- `ProgresoScreen.kt` - Toda la interfaz visual
- `ProgresoViewModel.kt` - Lógica y estado

## 🎨 Personalización

### Cambiar Colores:
Editar `Color.kt`:
```kotlin
val NavyBlue = Color(0xFF001F3F) // Tu color preferido
```

### Agregar Más Logros:
1. Agregar en `EcoHandDatabase.kt` en el método `populateDatabase()`
2. Agregar lógica en `ProgresoRepository.kt` en `verificarLogros()`

### Cambiar Emojis:
Editar directamente en la UI o en los datos iniciales.

## ✨ Características Destacadas

- ✅ **Arquitectura MVVM** - Separación clara de responsabilidades
- ✅ **Room Database** - Persistencia robusta
- ✅ **Material Design 3** - UI moderna
- ✅ **Jetpack Compose** - UI declarativa y reactiva
- ✅ **Kotlin Coroutines** - Operaciones asíncronas eficientes
- ✅ **StateFlow** - Gestión de estado reactiva

## 📞 Soporte

Si encuentras problemas:
1. Revisa los logs en Logcat
2. Verifica que la versión de la base de datos sea 2
3. Asegúrate de que todos los archivos estén en su lugar
4. Limpia y reconstruye el proyecto

---

**¡Éxito con tu aplicación EcoHand! 🎉**

