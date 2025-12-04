# Script para descargar modelos de MediaPipe
# Ejecutar desde la raíz del proyecto

Write-Host "`n🤖 EcoHand - Descargador de Modelos MediaPipe" -ForegroundColor Cyan
Write-Host "================================================`n" -ForegroundColor Cyan

$projectRoot = Get-Location
$assetsPath = Join-Path $projectRoot "app\src\main\assets"

# Verificar que estamos en la raíz del proyecto
if (-not (Test-Path "app\build.gradle.kts")) {
    Write-Host "❌ Error: Este script debe ejecutarse desde la raíz del proyecto EcoHand" -ForegroundColor Red
    Write-Host "   Ubicación actual: $projectRoot" -ForegroundColor Yellow
    Write-Host "   Cambia al directorio correcto y vuelve a ejecutar." -ForegroundColor Yellow
    exit 1
}

# Crear directorio assets si no existe
if (-not (Test-Path $assetsPath)) {
    Write-Host "📁 Creando directorio assets..." -ForegroundColor Yellow
    New-Item -Path $assetsPath -ItemType Directory -Force | Out-Null
    Write-Host "✅ Directorio creado: $assetsPath`n" -ForegroundColor Green
} else {
    Write-Host "✅ Directorio assets existe: $assetsPath`n" -ForegroundColor Green
}

# URLs de los modelos
$handModelUrl = "https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/latest/hand_landmarker.task"
$faceModelUrl = "https://storage.googleapis.com/mediapipe-models/face_landmarker/face_landmarker/float16/latest/face_landmarker.task"

$handModelPath = Join-Path $assetsPath "hand_landmarker.task"
$faceModelPath = Join-Path $assetsPath "face_landmarker.task"

# Función para descargar modelo
function Download-Model {
    param (
        [string]$Url,
        [string]$OutputPath,
        [string]$ModelName
    )

    Write-Host "📥 Descargando $ModelName..." -ForegroundColor Yellow
    Write-Host "   URL: $Url" -ForegroundColor Gray
    Write-Host "   Destino: $OutputPath" -ForegroundColor Gray

    try {
        # Descargar con barra de progreso
        $ProgressPreference = 'SilentlyContinue' # Deshabilitar barra de progreso nativa

        $startTime = Get-Date
        Invoke-WebRequest -Uri $Url -OutFile $OutputPath -UseBasicParsing
        $endTime = Get-Date
        $duration = ($endTime - $startTime).TotalSeconds

        # Verificar tamaño del archivo
        if (Test-Path $OutputPath) {
            $fileSize = (Get-Item $OutputPath).Length / 1MB
            Write-Host "✅ $ModelName descargado exitosamente!" -ForegroundColor Green
            Write-Host "   Tamaño: $([math]::Round($fileSize, 2)) MB" -ForegroundColor Green
            Write-Host "   Tiempo: $([math]::Round($duration, 1)) segundos`n" -ForegroundColor Green
            return $true
        } else {
            Write-Host "❌ Error: El archivo no se creó correctamente`n" -ForegroundColor Red
            return $false
        }
    }
    catch {
        Write-Host "❌ Error al descargar $ModelName" -ForegroundColor Red
        Write-Host "   Mensaje: $($_.Exception.Message)`n" -ForegroundColor Red
        return $false
    }
}

# Verificar si los modelos ya existen
$handExists = Test-Path $handModelPath
$faceExists = Test-Path $faceModelPath

if ($handExists -and $faceExists) {
    Write-Host "⚠️  Los modelos ya existen:" -ForegroundColor Yellow
    Write-Host "   ✅ hand_landmarker.task" -ForegroundColor Green
    Write-Host "   ✅ face_landmarker.task`n" -ForegroundColor Green

    $response = Read-Host "¿Deseas reemplazarlos? (s/n)"
    if ($response -ne "s" -and $response -ne "S") {
        Write-Host "`n✅ Modelos existentes conservados. ¡Listo para usar!" -ForegroundColor Green
        exit 0
    }
    Write-Host ""
}

# Descargar Hand Landmarker
$handSuccess = $false
if (-not $handExists -or $response -eq "s" -or $response -eq "S") {
    $handSuccess = Download-Model -Url $handModelUrl -OutputPath $handModelPath -ModelName "Hand Landmarker"
} else {
    Write-Host "⏭️  Omitiendo Hand Landmarker (ya existe)`n" -ForegroundColor Cyan
    $handSuccess = $true
}

# Descargar Face Landmarker
$faceSuccess = $false
if (-not $faceExists -or $response -eq "s" -or $response -eq "S") {
    $faceSuccess = Download-Model -Url $faceModelUrl -OutputPath $faceModelPath -ModelName "Face Landmarker"
} else {
    Write-Host "⏭️  Omitiendo Face Landmarker (ya existe)`n" -ForegroundColor Cyan
    $faceSuccess = $true
}

# Resumen final
Write-Host "`n" + "="*50 -ForegroundColor Cyan
Write-Host "📊 RESUMEN DE DESCARGA" -ForegroundColor Cyan
Write-Host "="*50 -ForegroundColor Cyan

if ($handSuccess) {
    Write-Host "✅ Hand Landmarker: OK" -ForegroundColor Green
} else {
    Write-Host "❌ Hand Landmarker: FALLÓ" -ForegroundColor Red
}

if ($faceSuccess) {
    Write-Host "✅ Face Landmarker: OK" -ForegroundColor Green
} else {
    Write-Host "❌ Face Landmarker: FALLÓ" -ForegroundColor Red
}

# Verificación final
Write-Host "`n📁 Archivos en assets:" -ForegroundColor Cyan
Get-ChildItem -Path $assetsPath -Filter "*.task" | ForEach-Object {
    $size = $_.Length / 1MB
    Write-Host "   ✅ $($_.Name) - $([math]::Round($size, 2)) MB" -ForegroundColor Green
}

if ($handSuccess -and $faceSuccess) {
    Write-Host "`n🎉 ¡TODOS LOS MODELOS DESCARGADOS EXITOSAMENTE!" -ForegroundColor Green
    Write-Host "`n📝 Próximos pasos:" -ForegroundColor Yellow
    Write-Host "   1. Abre el proyecto en Android Studio" -ForegroundColor White
    Write-Host "   2. Sincroniza Gradle (File → Sync Project with Gradle Files)" -ForegroundColor White
    Write-Host "   3. Ejecuta la app" -ForegroundColor White
    Write-Host "   4. Ve a: Perfil → 🧪 Prueba de Detección" -ForegroundColor White
    Write-Host "`n✨ ¡Listo para detectar manos y rostros!" -ForegroundColor Cyan
} else {
    Write-Host "`n⚠️  ADVERTENCIA: Algunos modelos no se descargaron" -ForegroundColor Yellow
    Write-Host "   Por favor, descárgalos manualmente desde:" -ForegroundColor Yellow
    if (-not $handSuccess) {
        Write-Host "   - Hand: $handModelUrl" -ForegroundColor White
    }
    if (-not $faceSuccess) {
        Write-Host "   - Face: $faceModelUrl" -ForegroundColor White
    }
    Write-Host "`n   Y colócalos en: $assetsPath" -ForegroundColor Yellow
}

Write-Host ""

