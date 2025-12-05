package com.example.ecohand.presentation.senas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VowelSelectionScreen(
    category: String,
    onNavigateBack: () -> Unit,
    onVowelSelected: (String) -> Unit
) {
    // Obtener las señas según la categoría
    val vowels = when (category) {
        "Alfabeto" -> listOf(
            VowelInfo("A", "Pulgar extendido hacia afuera, demás dedos cerrados"),
            VowelInfo("E", "Todos los dedos curvados, pulgar cubriendo puntas"),
            VowelInfo("I", "Solo meñique extendido hacia arriba"),
            VowelInfo("O", "Dedos formando un círculo"),
            VowelInfo("U", "Índice y meñique extendidos hacia arriba"),
            VowelInfo("Z", "Índice extendido trazando una Z (requiere movimiento)")
        )
        "Relaciones Familiares" -> listOf(
            VowelInfo("Amigo", "Coloca una mano arriba y otra abajo, cerca")
        )
        "Números" -> listOf(
            VowelInfo("0", "Todos los dedos curvados formando un círculo, puntas de los dedos tocando la punta del pulgar"),
            VowelInfo("1", "Solo el dedo índice extendido hacia arriba, demás dedos cerrados"),
            VowelInfo("2", "Dedos índice y medio extendidos hacia arriba formando una V"),
            VowelInfo("3", "Dedos índice, medio y anular extendidos hacia arriba"),
            VowelInfo("4", "Los cuatro dedos extendidos hacia arriba, pulgar doblado hacia la palma"),
            VowelInfo("5", "Todos los dedos extendidos y separados, mano abierta completa")
        )
        else -> emptyList() // Otras categorías próximamente
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = category,
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título e instrucciones
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🖐️ $category",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Selecciona la seña que quieres practicar",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Lista de vocales
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(vowels) { vowelInfo ->
                    VowelCard(
                        vowelInfo = vowelInfo,
                        onVowelClick = { onVowelSelected(vowelInfo.letter) }
                    )
                }
            }
        }
    }
}

@Composable
fun VowelCard(
    vowelInfo: VowelInfo,
    onVowelClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onVowelClick,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de la vocal
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(30.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = vowelInfo.letter.take(1),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información de la vocal
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = vowelInfo.letter,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = vowelInfo.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            // Flecha indicadora
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Ir",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

data class VowelInfo(
    val letter: String,
    val description: String
)
