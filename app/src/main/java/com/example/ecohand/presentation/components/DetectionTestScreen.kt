package com.example.ecohand.presentation.components

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ecohand.ml.FaceDetector
import com.example.ecohand.ml.HandDetector
import com.example.ecohand.utils.toBitmap
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import java.util.concurrent.Executors

/**
 * Pantalla de prueba para detección de manos y rostro
 * Muestra la cámara con overlay de landmarks en verde
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectionTestScreen(
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
    var handsDetected by remember { mutableStateOf(0) }
    var facesDetected by remember { mutableStateOf(0) }

    // Detectores
    val handDetector = remember {
        HandDetector(
            context = context,
            runningMode = RunningMode.LIVE_STREAM,
            maxNumHands = 2
        )
    }

    val faceDetector = remember {
        FaceDetector(
            context = context,
            runningMode = RunningMode.LIVE_STREAM,
            maxNumFaces = 1
        )
    }

    // Inicializar detectores
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }

        val handInit = handDetector.initialize { result, _ ->
            handResults = result
            handsDetected = result.landmarks().size
        }

        val faceInit = faceDetector.initialize { result, _ ->
            faceResults = result
            facesDetected = result.faceLandmarks().size
        }

        detectionStatus = when {
            handInit && faceInit -> "✓ Detectores listos"
            handInit -> "⚠ Solo manos listo"
            faceInit -> "⚠ Solo rostro listo"
            else -> "✗ Error al inicializar"
        }
    }

    // Limpiar recursos
    DisposableEffect(Unit) {
        onDispose {
            handDetector.close()
            faceDetector.close()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detección MediaPipe",
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
            // Estado de detección
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = detectionStatus,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "👐 Manos: $handsDetected",
                            fontSize = 16.sp,
                            color = if (handsDetected > 0) Color(0xFF00C853) else Color.Gray
                        )
                        Text(
                            text = "😊 Rostros: $facesDetected",
                            fontSize = 16.sp,
                            color = if (facesDetected > 0) Color(0xFF00C853) else Color.Gray
                        )
                    }
                }
            }

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
                        CameraPreviewWithDetection(
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

            // Instrucciones
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
                        text = "💡 Instrucciones",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Muestra tus manos frente a la cámara\n" +
                                "• Los puntos y líneas verdes indican detección\n" +
                                "• Puedes mover las manos para probar\n" +
                                "• El rostro se detecta automáticamente",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraPreviewWithDetection(
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
                            processImageProxy(
                                imageProxy = imageProxy,
                                handDetector = handDetector,
                                faceDetector = faceDetector,
                                onImageDimensionsChanged = onImageDimensionsChanged
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
                    Log.e("DetectionTest", "Camera binding failed", e)
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

private fun processImageProxy(
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
        // Rotar según los grados del sensor
        matrix.postRotate(rotationDegrees.toFloat())
        // Voltear horizontalmente solo para cámara frontal
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

        // Liberar bitmap original
        if (rotated != bitmap) {
            bitmap.recycle()
        }

        rotated
    } else {
        // Sin rotación, solo voltear si es cámara frontal
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
