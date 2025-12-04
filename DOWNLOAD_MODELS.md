# 📥 Descargar Modelos de MediaPipe

Para que la detección de manos y rostro funcione, necesitas descargar los modelos pre-entrenados de MediaPipe.

## Modelos Requeridos

### 1. Hand Landmarker (Detector de Manos)
- **URL:** https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/latest/hand_landmarker.task
- **Tamaño:** ~5 MB
- **Destino:** `app/src/main/assets/hand_landmarker.task`

### 2. Face Landmarker (Detector de Rostro)
- **URL:** https://storage.googleapis.com/mediapipe-models/face_landmarker/face_landmarker/float16/latest/face_landmarker.task
- **Tamaño:** ~5 MB
- **Destino:** `app/src/main/assets/face_landmarker.task`

## Instrucciones de Descarga

### Opción 1: Manual (Recomendado)
1. Descarga los archivos desde las URLs anteriores
2. Colócalos en la carpeta `app/src/main/assets/`
3. Asegúrate de que los nombres sean exactamente:
   - `hand_landmarker.task`
   - `face_landmarker.task`

### Opción 2: Script PowerShell (Automático)
```powershell
# Ejecuta desde la raíz del proyecto
$assetsPath = "app\src\main\assets"

# Descargar Hand Landmarker
Invoke-WebRequest -Uri "https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/latest/hand_landmarker.task" -OutFile "$assetsPath\hand_landmarker.task"

# Descargar Face Landmarker
Invoke-WebRequest -Uri "https://storage.googleapis.com/mediapipe-models/face_landmarker/face_landmarker/float16/latest/face_landmarker.task" -OutFile "$assetsPath\face_landmarker.task"
```

### Opción 3: Script Bash (Linux/Mac)
```bash
# Ejecuta desde la raíz del proyecto
cd app/src/main/assets

# Descargar Hand Landmarker
curl -L -o hand_landmarker.task https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/latest/hand_landmarker.task

# Descargar Face Landmarker
curl -L -o face_landmarker.task https://storage.googleapis.com/mediapipe-models/face_landmarker/face_landmarker/float16/latest/face_landmarker.task
```

## Verificación

Después de descargar, verifica que los archivos estén en la ubicación correcta:
```
app/
  src/
    main/
      assets/
        ├── hand_landmarker.task   ✓
        └── face_landmarker.task   ✓
```

## Notas Importantes

- **No incluyas estos archivos en Git** (son muy grandes ~10MB total)
- Los modelos float16 son más ligeros y rápidos para dispositivos móviles
- Si necesitas mayor precisión, puedes usar modelos float32 (más pesados)
- Los modelos se cargan automáticamente al iniciar la detección

## ¿Por qué no se incluyen automáticamente?

Los modelos de ML son archivos grandes que no se deben incluir en el repositorio Git. 
Cada desarrollador debe descargarlos localmente en su máquina.

