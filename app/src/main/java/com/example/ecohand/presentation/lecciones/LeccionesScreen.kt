package com.example.ecohand.presentation.lecciones

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecohand.data.local.entity.LeccionEntity

@Composable
fun LeccionesScreen(
    onNavigateToDetalle: (Int) -> Unit,
    viewModel: LeccionesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.cargarLecciones()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Lecciones",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Aprende lengua de señas peruanas paso a paso",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Loading
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return
        }
        
        // Agrupar lecciones por categoría
        val leccionesPorCategoria = uiState.lecciones.groupBy { it.nivel }
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            leccionesPorCategoria.forEach { (nivel, lecciones) ->
                item {
                    Text(
                        text = nivel,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )
                }
                
                items(lecciones) { leccion ->
                    LeccionCard(
                        leccion = leccion,
                        isCompletada = uiState.leccionesCompletadas.contains(leccion.id),
                        onClick = {
                            if (!leccion.bloqueada) {
                                onNavigateToDetalle(leccion.id)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LeccionCard(
    leccion: LeccionEntity,
    isCompletada: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = !leccion.bloqueada, onClick = onClick)
            .then(
                if (leccion.bloqueada) Modifier.border(
                    2.dp,
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    RoundedCornerShape(16.dp)
                ) else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (leccion.bloqueada) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            } else if (isCompletada) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (leccion.bloqueada) 0.dp else 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono o emoji
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (leccion.bloqueada) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (leccion.bloqueada) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Bloqueada",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Text(
                        text = leccion.icono ?: "📚",
                        fontSize = 28.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Contenido
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = leccion.titulo,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (leccion.bloqueada) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = leccion.descripcion,
                    fontSize = 13.sp,
                    color = if (leccion.bloqueada) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            
            // Badge completado
            if (isCompletada && !leccion.bloqueada) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        fontSize = 20.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
