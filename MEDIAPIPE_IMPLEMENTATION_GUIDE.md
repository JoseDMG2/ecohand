# 🎯 Guía de Implementación: Detección de Manos y Rostro con MediaPipe

## ✅ Cambios Implementados

### 1. **Dependencias Agregadas**
- ✅ MediaPipe Tasks Vision 0.10.14
- ✅ Configurado en `libs.versions.toml` y `build.gradle.kts`

### 2. **Módulo de Machine Learning** (`app/src/main/java/com/example/ecohand/ml/`)
- ✅ **HandDetector.kt**: Detector de manos con MediaPipe
  - Detecta hasta 2 manos simultáneamente
  - 21 landmarks por mano
  - Modo LIVE_STREAM para tiempo real
  
- ✅ **FaceDetector.kt**: Detector de rostro con MediaPipe
  - Detecta 1 rostro
  - 468 landmarks faciales
  - Modo LIVE_STREAM para tiempo real

### 3. **Componentes de Visualización** (`app/src/main/java/com/example/ecohand/presentation/components/`)
- ✅ **DetectionOverlay.kt**: Canvas para dibujar landmarks
  - Líneas verdes conectando puntos
  - Círculos verdes en vértices
  - Conexiones anatómicas correctas
  
- ✅ **DetectionTestScreen.kt**: Pantalla de prueba completa
  - Preview de cámara
  - Análisis de frames en tiempo real
  - Estadísticas de detección
  - Cambio entre cámara frontal/trasera

### 4. **Navegación**
- ✅ Ruta `Screen.DetectionTest` agregada
- ✅ Integración en `MainNavHost`
- ✅ Botón en pantalla de Perfil para acceder
- ✅ Bottom bar oculto en pantalla de detección

### 5. **Assets y Documentación**
- ✅ Directorio `app/src/main/assets/` creado
- ✅ Guía de descarga de modelos: `DOWNLOAD_MODELS.md`
- ✅ Esta guía de implementación

## 🚀 Pasos Siguientes

### Paso 1: Descargar Modelos de MediaPipe

**IMPORTANTE:** Los modelos NO están incluidos en el código. Debes descargarlos:

#### Opción A: Manual (Recomendado)
1. Descarga los modelos desde:
   - Hand Landmarker: https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/latest/hand_landmarker.task
   - Face Landmarker: https://storage.googleapis.com/mediapipe-models/face_landmarker/face_landmarker/float16/latest/face_landmarker.task

2. Colócalos en: `app/src/main/assets/`
   ```
   app/
     src/
       main/
         assets/
           ├── hand_landmarker.task  (⚠️ REQUERIDO)
           └── face_landmarker.task  (⚠️ REQUERIDO)
   ```

#### Opción B: PowerShell (Automático)
```powershell
# Ejecutar desde la raíz del proyecto
$assetsPath = "app\src\main\assets"

# Descargar Hand Landmarker
Invoke-WebRequest -Uri "https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/latest/hand_landmarker.task" -OutFile "$assetsPath\hand_landmarker.task"

# Descargar Face Landmarker
Invoke-WebRequest -Uri "https://storage.googleapis.com/mediapipe-models/face_landmarker/face_landmarker/float16/latest/face_landmarker.task" -OutFile "$assetsPath\face_landmarker.task"
```

### Paso 2: Sincronizar Gradle
```powershell
# En Android Studio, ejecuta:
# File > Sync Project with Gradle Files
```

O desde terminal:
```powershell
cd "C:\Users\herma\OneDrive\Documentos\Android proyects\ecohand"
.\gradlew build
```

### Paso 3: Ejecutar la Aplicación
1. Conecta un dispositivo Android o inicia un emulador
2. Ejecuta la app desde Android Studio
3. Ve a: **Perfil** → **🧪 Prueba de Detección**
4. Concede permiso de cámara
5. ¡Muestra tus manos y rostro!

## 📱 Uso de la Pantalla de Detección

### Características
- **Vista en tiempo real** de la cámara
- **Overlay verde** con landmarks detectados
- **Contador** de manos y rostros detectados
- **Botón** para cambiar entre cámara frontal/trasera
- **Estado** de los detectores (inicializados/error)

### Interpretación Visual
- 🟢 **Puntos verdes**: Landmarks individuales (vértices)
- 🟢 **Líneas verdes**: Conexiones entre landmarks
- **Manos**: 21 puntos por mano (muñeca, dedos, articulaciones)
- **Rostro**: Contorno facial, ojos, cejas, nariz, boca

### Indicadores de Estado
- ✅ **"✓ Detectores listos"**: Todo funcionando
- ⚠️ **"⚠ Solo X listo"**: Un detector falló
- ❌ **"✗ Error al inicializar"**: Revisar modelos en assets/

## 🔧 Solución de Problemas

### Error: "Model file not found"
**Causa:** Modelos no descargados o mal ubicados
**Solución:** 
1. Verifica que existan: `app/src/main/assets/hand_landmarker.task` y `face_landmarker.task`
2. Verifica los nombres de archivo exactos (sin espacios, minúsculas)
3. Reconstruye el proyecto (Build > Clean Project > Rebuild Project)

### Error: "Failed to initialize detector"
**Causa:** Modelos corruptos o GPU no disponible
**Solución:**
1. Re-descarga los modelos
2. En `HandDetector.kt` y `FaceDetector.kt`, cambia:
   ```kotlin
   .setDelegate(Delegate.GPU)
   ```
   por:
   ```kotlin
   .setDelegate(Delegate.CPU)
   ```

### Error: "Camera permission denied"
**Causa:** Permiso no concedido
**Solución:** Toca el botón "Solicitar permiso" en la pantalla

### Detección lenta o con lag
**Causa:** Dispositivo con recursos limitados
**Solución:** En `DetectionTestScreen.kt`, reduce FPS:
```kotlin
val imageAnalysis = ImageAnalysis.Builder()
    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
    .setTargetRotation(Surface.ROTATION_0)
    // Agregar:
    .setTargetResolution(Size(640, 480))
    .build()
```

### No se ven líneas verdes
**Causa:** Landmarks no detectados o fuera del canvas
**Solución:**
1. Acerca tus manos/rostro a la cámara
2. Mejora la iluminación
3. Usa cámara frontal (mejor ángulo)

## 🎓 Próximos Pasos (Para Reconocimiento de Señas)

### Fase 1: Recolección de Datos ✅ COMPLETADO
- [x] Detección básica de manos
- [x] Detección básica de rostro
- [x] Visualización de landmarks

### Fase 2: Almacenamiento de Patrones (Siguiente)
1. Crear entidad `PatronSenaEntity` en Room Database:
```kotlin
@Entity(tableName = "patrones_senas")
data class PatronSenaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val senaId: Int, // FK a SenaEntity
    val landmarks: String, // JSON con coordenadas
    val timestamp: Long = System.currentTimeMillis()
)
```

2. Implementar captura de patrones:
   - Botón "Guardar seña" en DetectionTestScreen
   - Serializar landmarks a JSON
   - Almacenar en base de datos

### Fase 3: Clasificación de Señas (Futuro)
1. **Opción A - Reglas basadas en geometría:**
   - Calcular ángulos entre dedos
   - Detectar posiciones relativas
   - Comparar con patrones guardados

2. **Opción B - Machine Learning:**
   - Entrenar modelo TensorFlow Lite
   - Clasificar gestos en tiempo real
   - Mayor precisión para señas complejas

### Fase 4: Integración en Lecciones
- Reemplazar `LeccionPracticaScreen` con detección real
- Validar señas en tiempo real
- Feedback instantáneo al usuario

## 📊 Arquitectura de Detección

```
CameraX (PreviewView)
    ↓
ImageAnalysis (frame-by-frame)
    ↓
Bitmap conversion
    ↓
MediaPipe Detectors (Hand + Face)
    ↓
Landmarks Results
    ↓
DetectionOverlay (Canvas drawing)
    ↓
Visual Feedback (Green lines & points)
```

## 🔐 Permisos Requeridos

Ya configurados en `AndroidManifest.xml`:
- ✅ `android.permission.CAMERA`
- ✅ `android.hardware.camera` (optional)
- ✅ `android.hardware.camera.front` (optional)

## 📝 Notas Técnicas

### Rendimiento
- **GPU acelerado** por defecto (Delegate.GPU)
- **Estrategia KEEP_ONLY_LATEST** para evitar backpressure
- **Procesamiento asíncrono** con LIVE_STREAM mode
- **FPS efectivo**: ~15-30 fps (dependiendo del dispositivo)

### Precisión
- **Hand Landmarker**: 21 puntos por mano
  - 0: Muñeca
  - 1-4: Pulgar
  - 5-8: Índice
  - 9-12: Medio
  - 13-16: Anular
  - 17-20: Meñique

- **Face Landmarker**: 468 puntos faciales
  - Contorno facial: ~35 puntos
  - Ojos: ~32 puntos (16 por ojo)
  - Boca: ~40 puntos
  - Nariz: ~9 puntos
  - Otros: ~352 puntos adicionales

### Optimizaciones Aplicadas
- ✅ Modelos float16 (más ligeros que float32)
- ✅ Procesamiento en hilo separado (executor)
- ✅ Liberación de recursos en onDispose
- ✅ Detección solo cuando detectores están listos

## 🎨 Personalización

### Cambiar color de las líneas
En `DetectionOverlay.kt`:
```kotlin
val lineColor = Color(0xFF00FF00) // Verde brillante
// Cambiar a:
val lineColor = Color(0xFFFF0000) // Rojo
```

### Ajustar grosor de líneas
```kotlin
val lineWidth = 3f
// Cambiar a:
val lineWidth = 5f // Más grueso
```

### Cambiar tamaño de puntos
```kotlin
val pointRadius = 6f
// Cambiar a:
val pointRadius = 8f // Más grande
```

### Detectar más manos
En `HandDetector.kt`:
```kotlin
maxNumHands: Int = 2
// Cambiar a:
maxNumHands: Int = 4 // Detectar hasta 4 manos
```

## ✨ Resultado Esperado

Al abrir la pantalla de prueba, deberías ver:
1. ✅ Vista de cámara en tiempo real
2. ✅ Líneas verdes dibujadas sobre tus manos (si las muestras)
3. ✅ Puntos verdes en articulaciones y dedos
4. ✅ Contorno facial con líneas verdes
5. ✅ Contador actualizado: "👐 Manos: 2" y "😊 Rostros: 1"

**¡La detección está funcionando correctamente!** 🎉

---

## 📞 Contacto y Soporte

Si encuentras problemas:
1. Revisa los logs en Logcat (filtro: `HandDetector`, `FaceDetector`)
2. Verifica que los modelos estén en `app/src/main/assets/`
3. Asegúrate de que la app tenga permiso de cámara
4. Prueba en un dispositivo real (mejor rendimiento que emulador)

---

**Desarrollado con ❤️ para EcoHand**
*Aprendizaje de Lengua de Señas Peruanas*

