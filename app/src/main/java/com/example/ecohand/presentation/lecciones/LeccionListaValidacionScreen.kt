package com.example.ecohand.presentation.lecciones

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecohand.data.local.database.EcoHandDatabase
import com.example.ecohand.data.repository.LeccionRepository
import com.example.ecohand.data.session.UserSession
import kotlinx.coroutines.launch

data class SenaItem(
    val letra: String,
    val descripcion: String,
    var completada: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeccionListaValidacionScreen(
    leccionId: Int,
    senasAValidar: List<SenaItem>,
    onNavigateBack: () -> Unit,
    onNavigateToValidacion: (String, Int) -> Unit,
    onLeccionCompletada: () -> Unit
) {
    val context = LocalContext.current
    // Inicializar señas con su estado guardado
    var senas by remember {
        mutableStateOf(senasAValidar.map { sena ->
            sena.copy(completada = com.example.ecohand.data.session.LeccionValidacionState.estaSenaCompletada(leccionId, sena.letra))
        })
    }
    var showCompletedDialog by remember { mutableStateOf(false) }

    // Actualizar señas cuando regresamos de la validación
    LaunchedEffect(Unit) {
        // Actualizar estado de señas cada vez que entramos a la pantalla
        senas = senasAValidar.map { sena ->
            sena.copy(completada = com.example.ecohand.data.session.LeccionValidacionState.estaSenaCompletada(leccionId, sena.letra))
        }
    }

    // Limpiar estado cuando salimos de la pantalla
    DisposableEffect(Unit) {
        onDispose {
            // Si todas están completadas, limpiar el estado de esta lección
            if (senas.all { it.completada }) {
                com.example.ecohand.data.session.LeccionValidacionState.limpiarLeccion(leccionId)
            }
        }
    }

    val scope = rememberCoroutineScope()
    val database = remember { EcoHandDatabase.getDatabase(context) }
    val leccionRepository = remember {
        LeccionRepository(
            database.leccionDao(),
            database.progresoLeccionDao(),
            database.estadisticasUsuarioDao()
        )
    }
    val userSession = remember { UserSession.getInstance(context) }

    // Calcular progreso
    val senasCompletadas = senas.count { it.completada }
    val senasTotal = senas.size
    val progreso = if (senasTotal > 0) senasCompletadas.toFloat() / senasTotal.toFloat() else 0f
    val todasCompletadas = senasCompletadas == senasTotal

    // Verificar si se completaron todas las señas
    LaunchedEffect(todasCompletadas) {
        if (todasCompletadas && senasTotal > 0 && !showCompletedDialog) {
            // Marcar lección como completada en BD
            scope.launch {
                try {
                    val usuarioId = userSession.getUserId()
                    leccionRepository.completarLeccion(usuarioId, leccionId)
                    showCompletedDialog = true
                } catch (e: Exception) {
                    showCompletedDialog = true
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Práctica de Lección",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "$senasCompletadas de $senasTotal señas completadas",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Barra de progreso
            LinearProgressIndicator(
                progress = { progreso },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

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
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🖐️ Valida las siguientes señas",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Toca cada seña para validarla con la cámara",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Lista de señas
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(senas) { sena ->
                    SenaCard(
                        sena = sena,
                        onClick = {
                            if (!sena.completada) {
                                onNavigateToValidacion(sena.letra, leccionId)
                            }
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Diálogo de lección completada
    if (showCompletedDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🎊",
                        fontSize = 64.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "¡Lección Completada!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50),
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Has completado todas las señas:",
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = senas.joinToString(" • ") { it.letra },
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCompletedDialog = false
                        onLeccionCompletada()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("Finalizar", fontSize = 16.sp)
                }
            }
        )
    }
}

@Composable
fun SenaCard(
    sena: SenaItem,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !sena.completada, onClick = onClick),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (sena.completada) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (sena.completada) 2.dp else 6.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono izquierdo
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        if (sena.completada) {
                            Color(0xFF4CAF50)
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        },
                        RoundedCornerShape(30.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (sena.completada) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completada",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Text(
                        text = sena.letra,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (sena.completada) "✓ ${sena.letra}" else sena.letra,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (sena.completada) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = sena.descripcion,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = if (sena.completada) 0.4f else 0.7f
                    )
                )
                if (sena.completada) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Completada",
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Icono derecho
            if (!sena.completada) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Validar",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

