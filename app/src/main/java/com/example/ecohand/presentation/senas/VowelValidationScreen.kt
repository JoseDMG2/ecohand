package com.example.ecohand.presentation.senas

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ecohand.ml.FaceDetector
import com.example.ecohand.ml.HandDetector
import com.example.ecohand.ml.VowelSignValidator
import com.example.ecohand.presentation.components.DetectionOverlay
import com.example.ecohand.utils.toBitmap
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VowelValidationScreen(
    vowel: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    var useFrontCamera by remember { mutableStateOf(true) }
    var handResults by remember { mutableStateOf<HandLandmarkerResult?>(null) }
    var faceResults by remember { mutableStateOf<FaceLandmarkerResult?>(null) }
    var imageWidth by remember { mutableStateOf(640) }
    var imageHeight by remember { mutableStateOf(480) }
    var detectionStatus by remember { mutableStateOf("Inicializando...") }
    var validationResult by remember { mutableStateOf<ValidationState>(ValidationState.Detecting) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Detectores y validador
    val handDetector = remember {
        HandDetector(
            context = context,
            runningMode = RunningMode.LIVE_STREAM,
            maxNumHands = 1 // Solo necesitamos una mano para validar
        )
    }

    val faceDetector = remember {
        FaceDetector(
            context = context,
            runningMode = RunningMode.LIVE_STREAM,
            maxNumFaces = 1
        )
    }

    val vowelValidator = remember { VowelSignValidator() }

    // Inicializar detectores
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }

        val handInit = handDetector.initialize { result, _ ->
            handResults = result
            // Validar la seña automáticamente
            validationResult = validateVowelSign(result, vowel, vowelValidator)
        }

        val faceInit = faceDetector.initialize { result, _ ->
            faceResults = result
        }

        detectionStatus = when {
            handInit && faceInit -> "✓ Detectores listos"
            handInit -> "⚠ Solo manos listo"
            faceInit -> "⚠ Solo rostro listo"
            else -> "✗ Error al inicializar"
        }
    }

    // Mostrar diálogo de éxito cuando se valide correctamente
    LaunchedEffect(validationResult) {
        if (validationResult is ValidationState.Success) {
            showSuccessDialog = true
        }
    }

    // Limpiar recursos
    DisposableEffect(Unit) {
        onDispose {
            handDetector.close()
            faceDetector.close()
            // Limpiar trayectorias de letras con movimiento
            vowelValidator.resetJTrajectory()
            vowelValidator.resetZTrajectory()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Validar Letra $vowel",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { useFrontCamera = !useFrontCamera }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Cambiar cámara"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Estado de validación
            ValidationStatusCard(
                vowel = vowel,
                validationState = validationResult,
                detectionStatus = detectionStatus
            )

            // Vista de cámara con overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (hasCameraPermission) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(3f / 4f)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        CameraPreviewWithValidation(
                            useFrontCamera = useFrontCamera,
                            handDetector = handDetector,
                            faceDetector = faceDetector,
                            onImageDimensionsChanged = { width, height ->
                                imageWidth = width
                                imageHeight = height
                            }
                        )

                        // Overlay de detecciones
                        DetectionOverlay(
                            handResults = handResults,
                            faceResults = faceResults,
                            imageWidth = imageWidth,
                            imageHeight = imageHeight,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    PermissionRequestCard(onRequestPermission = {
                        launcher.launch(Manifest.permission.CAMERA)
                    })
                }
            }

            // Instrucciones específicas para la vocal
            VowelInstructionsCard(vowel = vowel)
        }
    }

    // Diálogo de éxito
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Éxito",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "¡Correcto!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎉",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Has realizado correctamente la seña de la letra $vowel",
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("Continuar")
                }
            }
        )
    }
}

@Composable
private fun ValidationStatusCard(
    vowel: String,
    validationState: ValidationState,
    detectionStatus: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (validationState) {
                is ValidationState.Success -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                is ValidationState.Error -> Color(0xFFF44336).copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.primaryContainer
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Validando Letra $vowel",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = detectionStatus,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (validationState) {
                        is ValidationState.Detecting -> "🔍 Detectando..."
                        is ValidationState.Success -> "✅ ¡Correcto!"
                        is ValidationState.Error -> "❌ ${validationState.message}"
                        is ValidationState.Waiting -> "⏳ Esperando seña..."
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = when (validationState) {
                        is ValidationState.Success -> Color(0xFF4CAF50)
                        is ValidationState.Error -> Color(0xFFF44336)
                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                    }
                )
            }
        }
    }
}

@Composable
private fun VowelInstructionsCard(vowel: String) {
    val instructions = when (vowel.uppercase()) {
        "A" -> "Extiende el pulgar hacia afuera y cierra los demás dedos formando un puño"
        "E" -> "Curva todos los dedos hacia la palma, con el pulgar cubriendo las puntas"
        "I" -> "Extiende solo el meñique hacia arriba, mantén los demás dedos cerrados"
        "O" -> "Forma un círculo con todos los dedos, juntando las puntas"
        "U" -> "Extiende índice y meñique hacia arriba, cierra medio, anular y pulgar"
        "J" -> "Extiende solo el meñique y mueve tu mano formando un arco, desde palma adelante hasta palma atrás"
        "Z" -> "Extiende solo el índice y traza una Z en el aire: línea horizontal → diagonal → línea horizontal"
        else -> "Realiza la seña correspondiente a la vocal seleccionada"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📋 Instrucciones",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = instructions,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun CameraPreviewWithValidation(
    useFrontCamera: Boolean,
    handDetector: HandDetector,
    faceDetector: FaceDetector,
    onImageDimensionsChanged: (Int, Int) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder()
                    .setTargetRotation(previewView.display.rotation)
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                    .setTargetRotation(previewView.display.rotation)
                    .build()
                    .apply {
                        setAnalyzer(cameraExecutor) { imageProxy ->
                            processImageProxyForValidation(
                                imageProxy = imageProxy,
                                handDetector = handDetector,
                                faceDetector = faceDetector,
                                onImageDimensionsChanged = onImageDimensionsChanged,
                                isFrontCamera = useFrontCamera
                            )
                        }
                    }

                val cameraSelector = if (useFrontCamera) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    Log.e("VowelValidation", "Camera binding failed", e)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = Modifier.fillMaxSize(),
        update = { _ ->
            // Actualizar cuando cambie la cámara
        }
    )
}

@Composable
private fun PermissionRequestCard(onRequestPermission: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "📷",
                fontSize = 64.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Permiso de cámara requerido",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Solicitar permiso")
            }
        }
    }
}

private fun processImageProxyForValidation(
    imageProxy: ImageProxy,
    handDetector: HandDetector,
    faceDetector: FaceDetector,
    onImageDimensionsChanged: (Int, Int) -> Unit,
    isFrontCamera: Boolean = true
) {
    val bitmap = imageProxy.toBitmap()
    val rotationDegrees = imageProxy.imageInfo.rotationDegrees

    // Rotar el bitmap según la rotación del sensor
    val rotatedBitmap = if (rotationDegrees != 0) {
        val matrix = Matrix()
        matrix.postRotate(rotationDegrees.toFloat())
        if (isFrontCamera) {
            matrix.postScale(-1f, 1f)
        }

        val rotated = Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )

        if (rotated != bitmap) {
            bitmap.recycle()
        }

        rotated
    } else {
        if (isFrontCamera) {
            val matrix = Matrix()
            matrix.postScale(-1f, 1f)
            val flipped = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                matrix,
                true
            )
            if (flipped != bitmap) {
                bitmap.recycle()
            }
            flipped
        } else {
            bitmap
        }
    }

    val frameTime = System.currentTimeMillis()

    onImageDimensionsChanged(rotatedBitmap.width, rotatedBitmap.height)

    if (handDetector.isReady()) {
        handDetector.detectAsync(rotatedBitmap, frameTime)
    }

    if (faceDetector.isReady()) {
        faceDetector.detectAsync(rotatedBitmap, frameTime)
    }

    imageProxy.close()
}

private fun validateVowelSign(
    handResult: HandLandmarkerResult,
    vowel: String,
    validator: VowelSignValidator
): ValidationState {
    return try {
        if (handResult.landmarks().isEmpty()) {
            return ValidationState.Waiting
        }

        // Manejo especial para la letra J (requiere movimiento)
        if (vowel.uppercase() == "J") {
            val jResult = validator.validateLetterJ(handResult)
            return if (jResult.isValid) {
                ValidationState.Success
            } else {
                ValidationState.Error(jResult.message)
            }
        }

        // Manejo especial para la letra Z (requiere movimiento)
        if (vowel.uppercase() == "Z") {
            val zResult = validator.validateLetterZ(handResult)
            return if (zResult.isValid) {
                ValidationState.Success
            } else {
                ValidationState.Error(zResult.message)
            }
        }

        // Validación para las demás letras (estáticas)
        val isValid = when (vowel.uppercase()) {
            "A" -> validator.validateLetterA(handResult)
            "E" -> validator.validateLetterE(handResult)
            "I" -> validator.validateLetterI(handResult)
            "O" -> validator.validateLetterO(handResult)
            "U" -> validator.validateLetterU(handResult)
            else -> false
        }

        if (isValid) {
            ValidationState.Success
        } else {
            ValidationState.Error("Ajusta la posición de tus dedos")
        }
    } catch (e: Exception) {
        ValidationState.Error("Error en la validación")
    }
}

sealed class ValidationState {
    object Detecting : ValidationState()
    object Waiting : ValidationState()
    object Success : ValidationState()
    data class Error(val message: String) : ValidationState()
}
