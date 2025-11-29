package com.example.ecohand.presentation.admin

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import com.example.ecohand.data.local.database.EcoHandDatabase
import com.example.ecohand.data.local.entity.SenaDataCollectionEntity
import com.example.ecohand.data.local.entity.SenaEntity
import com.example.ecohand.data.session.UserSession
import com.example.ecohand.ml.HandDetectionResult
import com.example.ecohand.ml.HandOverlay
import com.example.ecohand.ml.SignRecognizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataCollectionScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    
    val database = remember { EcoHandDatabase.getDatabase(context) }
    val userSession = remember { UserSession.getInstance(context) }
    val signRecognizer = remember { SignRecognizer(context) }
    
    var senas by remember { mutableStateOf<List<SenaEntity>>(emptyList()) }
    var selectedSena by remember { mutableStateOf<SenaEntity?>(null) }
    var collectionCounts by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }
    var isRecognizerReady by remember { mutableStateOf(false) }
    var currentDetection by remember { mutableStateOf<HandDetectionResult?>(null) }
    var lastBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf<String?>(null) }
    
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
    
    // Initialize
    LaunchedEffect(Unit) {
        // Load senas
        senas = database.senaDao().getAllSenas()
        
        // Load collection counts
        val counts = mutableMapOf<Int, Int>()
        senas.forEach { sena ->
            counts[sena.id] = database.senaDataCollectionDao().getSampleCountForSena(sena.id)
        }
        collectionCounts = counts
        
        // Initialize recognizer
        isRecognizerReady = signRecognizer.initialize()
        
        // Request camera permission
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            signRecognizer.close()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Recoleccion de Datos",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Left panel - Sign list
            Card(
                modifier = Modifier
                    .width(200.dp)
                    .fillMaxHeight()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "Senas",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                    
                    LazyColumn {
                        items(senas) { sena ->
                            val isSelected = selectedSena?.id == sena.id
                            val count = collectionCounts[sena.id] ?: 0
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp)
                                    .clickable { selectedSena = sena },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = sena.nombre,
                                        color = if (isSelected) 
                                            MaterialTheme.colorScheme.onPrimary 
                                        else 
                                            MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    
                                    Text(
                                        text = "$count",
                                        color = if (isSelected) 
                                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) 
                                        else 
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Right panel - Camera and controls
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                if (selectedSena == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Selecciona una sena para recolectar datos",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    // Selected sign info
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Recolectando: ${selectedSena!!.nombre}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Categoria: ${selectedSena!!.categoria}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Muestras: ${collectionCounts[selectedSena!!.id] ?: 0}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                    
                    // Camera view
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        if (hasCameraPermission) {
                            DataCollectionCameraPreview(
                                onFrameAnalyzed = { bitmap ->
                                    lastBitmap = bitmap
                                    scope.launch {
                                        currentDetection = signRecognizer.detectHandLandmarks(bitmap)
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                            
                            // Hand overlay
                            HandOverlay(
                                detectionResult = currentDetection,
                                modifier = Modifier.fillMaxSize(),
                                isMirrored = true
                            )
                            
                            // Detection status
                            val hasHands = currentDetection?.hasHands == true
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (hasHands) Color.Green else Color.Red)
                            )
                            
                            // ML status
                            if (!isRecognizerReady) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(8.dp)
                                        .background(
                                            Color.Black.copy(alpha = 0.7f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "Cargando modelo...",
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Permiso de camara requerido")
                                    Button(
                                        onClick = { launcher.launch(Manifest.permission.CAMERA) }
                                    ) {
                                        Text("Solicitar permiso")
                                    }
                                }
                            }
                        }
                    }
                    
                    // Save message
                    saveMessage?.let { message ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Text(
                                text = message,
                                color = Color.White,
                                modifier = Modifier.padding(12.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        LaunchedEffect(message) {
                            kotlinx.coroutines.delay(2000)
                            saveMessage = null
                        }
                    }
                    
                    // Capture button
                    Button(
                        onClick = {
                            val bitmap = lastBitmap
                            val detection = currentDetection
                            val sena = selectedSena
                            
                            if (bitmap != null && detection?.hasHands == true && sena != null) {
                                isSaving = true
                                scope.launch {
                                    try {
                                        val landmarks = detection.getFirstHand()!!
                                        val landmarksJson = signRecognizer.landmarksToJson(landmarks)
                                        
                                        val sample = SenaDataCollectionEntity(
                                            senaId = sena.id,
                                            usuarioId = userSession.getUserId(),
                                            landmarksData = landmarksJson,
                                            imageWidth = detection.imageWidth,
                                            imageHeight = detection.imageHeight,
                                            handedness = landmarks.handedness.name,
                                            confidence = landmarks.confidence
                                        )
                                        
                                        withContext(Dispatchers.IO) {
                                            database.senaDataCollectionDao().insert(sample)
                                            
                                            // Update count
                                            val newCount = database.senaDataCollectionDao()
                                                .getSampleCountForSena(sena.id)
                                            collectionCounts = collectionCounts + (sena.id to newCount)
                                        }
                                        
                                        saveMessage = "Muestra guardada!"
                                    } catch (e: Exception) {
                                        saveMessage = "Error: ${e.message}"
                                    } finally {
                                        isSaving = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .height(56.dp),
                        enabled = !isSaving && 
                                 currentDetection?.hasHands == true && 
                                 isRecognizerReady,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Capturar Muestra",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    // Set as reference button
                    OutlinedButton(
                        onClick = {
                            val detection = currentDetection
                            val sena = selectedSena
                            
                            if (detection?.hasHands == true && sena != null) {
                                scope.launch {
                                    try {
                                        val landmarks = detection.getFirstHand()!!
                                        val landmarksJson = signRecognizer.landmarksToJson(landmarks)
                                        
                                        withContext(Dispatchers.IO) {
                                            database.senaDao().updateLandmarksData(sena.id, landmarksJson)
                                        }
                                        
                                        saveMessage = "Referencia actualizada para ${sena.nombre}!"
                                    } catch (e: Exception) {
                                        saveMessage = "Error: ${e.message}"
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .height(48.dp),
                        enabled = currentDetection?.hasHands == true && isRecognizerReady,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Establecer como Referencia",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DataCollectionCameraPreview(
    onFrameAnalyzed: (Bitmap) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    
    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
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
            }
            
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(executor) { imageProxy ->
                            val bitmap = imageProxyToBitmap(imageProxy)
                            if (bitmap != null) {
                                onFrameAnalyzed(bitmap)
                            }
                            imageProxy.close()
                        }
                    }
                
                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))
            
            previewView
        },
        modifier = modifier
    )
}

private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
    return try {
        val buffer = imageProxy.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        
        val yuvImage = android.graphics.YuvImage(
            bytes,
            android.graphics.ImageFormat.NV21,
            imageProxy.width,
            imageProxy.height,
            null
        )
        
        val out = java.io.ByteArrayOutputStream()
        yuvImage.compressToJpeg(
            android.graphics.Rect(0, 0, imageProxy.width, imageProxy.height),
            80,
            out
        )
        
        val imageBytes = out.toByteArray()
        android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    } catch (e: Exception) {
        null
    }
}
