package com.example.ecohand.presentation.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecohand.data.local.database.EcoHandDatabase
import com.example.ecohand.data.local.entity.LogroEntity
import com.example.ecohand.data.local.entity.LogroUsuarioEntity
import com.example.ecohand.data.session.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisLogrosScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userSession = remember { UserSession.getInstance(context) }
    val database = remember { EcoHandDatabase.getDatabase(context) }

    var logros by remember { mutableStateOf<List<Pair<LogroEntity, LogroUsuarioEntity>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var logrosObtenidos by remember { mutableStateOf(0) }
    var totalLogros by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            val userId = userSession.getUserId()
            val logrosUsuario = database.logroUsuarioDao().getLogrosByUsuario(userId)
            val todosLogros = database.logroDao().getAllLogros()

            // Si el usuario no tiene logros inicializados, crearlos
            if (logrosUsuario.isEmpty() && todosLogros.isNotEmpty()) {
                todosLogros.forEach { logro ->
                    database.logroUsuarioDao().insertLogroUsuario(
                        LogroUsuarioEntity(
                            usuarioId = userId,
                            logroId = logro.id,
                            obtenido = false
                        )
                    )
                }
                val nuevosLogrosUsuario = database.logroUsuarioDao().getLogrosByUsuario(userId)
                logros = nuevosLogrosUsuario.mapNotNull { logroUsuario ->
                    val logro = todosLogros.find { it.id == logroUsuario.logroId }
                    logro?.let { it to logroUsuario }
                }
            } else {
                logros = logrosUsuario.mapNotNull { logroUsuario ->
                    val logro = todosLogros.find { it.id == logroUsuario.logroId }
                    logro?.let { it to logroUsuario }
                }
            }

            logrosObtenidos = logros.count { it.second.obtenido }
            totalLogros = logros.size
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mis Logros",
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
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Resumen de logros
                item {
                    LogrosResumenCard(
                        logrosObtenidos = logrosObtenidos,
                        totalLogros = totalLogros
                    )
                }

                // Título
                item {
                    Text(
                        text = "Todos los Logros",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Lista de logros
                if (logros.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "📋",
                                    fontSize = 48.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No hay logros disponibles",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(logros) { (logro, logroUsuario) ->
                        LogroCard(
                            logro = logro,
                            obtenido = logroUsuario.obtenido,
                            fechaObtenido = logroUsuario.fechaObtenido
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun LogrosResumenCard(
    logrosObtenidos: Int,
    totalLogros: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🏆",
                fontSize = 64.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tu Progreso",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$logrosObtenidos de $totalLogros logros obtenidos",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Barra de progreso
            LinearProgressIndicator(
                progress = { if (totalLogros > 0) logrosObtenidos.toFloat() / totalLogros.toFloat() else 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${if (totalLogros > 0) ((logrosObtenidos.toFloat() / totalLogros.toFloat()) * 100).toInt() else 0}% completado",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun LogroCard(
    logro: LogroEntity,
    obtenido: Boolean,
    fechaObtenido: Long?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (obtenido)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (obtenido) 4.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji del logro
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (obtenido)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = logro.emoji,
                    fontSize = 32.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información del logro
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = logro.nombre,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (obtenido)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = logro.descripcion,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Requisito: ${logro.requisito}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )

                if (obtenido && fechaObtenido != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Obtenido: ${formatDate(fechaObtenido)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Indicador de obtenido
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (obtenido)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (obtenido) {
                    Text(
                        text = "✓",
                        fontSize = 18.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "🔒",
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val format = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
    return format.format(date)
}
