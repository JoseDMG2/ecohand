package com.example.ecohand.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun InicioScreen(
    onNavigateToLeccion: (Int) -> Unit = {},
    viewModel: InicioViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.cargarDatos()
    }
    
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header: Logo y título
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "EcoHand",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // Tarjeta de Bienvenida
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Aprende Lengua de",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Señas Peruana",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"Conecta con el mundo sin palabras\"",
                    fontSize = 13.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // Saludo personalizado
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "Hola, ${uiState.nombreUsuario}!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Un resumen de tu progreso:",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Tarjetas de Estadísticas (Grid 3 columnas)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Puntos
            StatCard(
                icon = "🏆",
                value = uiState.puntosTotal.toString(),
                label = "Puntos",
                modifier = Modifier.weight(1f)
            )
            
            // Lecciones
            StatCard(
                icon = "📚",
                value = "${uiState.leccionesCompletadas}/${uiState.totalLecciones}",
                label = "Lecciones",
                modifier = Modifier.weight(1f)
            )
            
            // Racha
            StatCard(
                icon = "🔥",
                value = uiState.rachaActual.toString(),
                label = "Racha",
                modifier = Modifier.weight(1f)
            )
        }
        
        // Sección: Continúa aprendiendo
        Text(
            text = "Continúa aprendiendo",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        // Card de Siguiente Lección
        uiState.siguienteLeccion?.let { leccion ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToLeccion(leccion.id) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icono de Play
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Contenido
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = leccion.nivel,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = leccion.titulo,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = leccion.descripcion,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            maxLines = 2
                        )
                    }
                }
            }
        } ?: run {
            // Mensaje cuando no hay lecciones pendientes
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎉",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "¡Felicitaciones!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Has completado todas las lecciones",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun StatCard(
    icon: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
