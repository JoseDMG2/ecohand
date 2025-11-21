package com.example.ecohand.presentation.lecciones

import android.Manifest
import android.content.pm.PackageManager
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeccionPracticaScreen(
    leccionId: Int,
    onNavigateBack: () -> Unit,
    onLeccionCompletada: () -> Unit,
    viewModel: LeccionesViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    val leccion = uiState.leccionActual
    
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
    
    LaunchedEffect(leccionId) {
        viewModel.cargarLeccion(leccionId)
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }
    
    LaunchedEffect(uiState.showCompletadoDialog) {
        if (uiState.showCompletadoDialog) {
            kotlinx.coroutines.delay(1500)
            viewModel.ocultarDialogCompletado()
            onLeccionCompletada()
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            viewModel.limpiarLeccionActual()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Práctica: ${leccion?.titulo ?: ""}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
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
        if (leccion == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Instrucciones
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
                        text = "Realizar la seña \"${leccion.titulo}\"",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Intento 1 • 5 vidas restantes",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Vista de cámara
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (hasCameraPermission) {
                    CameraPreview(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(3f / 4f)
                            .clip(RoundedCornerShape(16.dp))
                    )
                } else {
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
                                onClick = { launcher.launch(Manifest.permission.CAMERA) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Solicitar permiso")
                            }
                        }
                    }
                }
            }
            
            // Botón evaluar
            Button(
                onClick = {
                    viewModel.completarLeccion(leccionId)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Evaluar la seña",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    
    // Dialog de completado
    if (uiState.showCompletadoDialog) {
        Dialog(onDismissRequest = {}) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icono de éxito
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(40.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✓",
                            fontSize = 48.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "¡COMPLETADO!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Has completado la lección",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun CameraPreview(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
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
                
                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview
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
