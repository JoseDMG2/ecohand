package com.example.ecohand.presentation.juegos

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun JuegosScreen(viewModel: JuegosViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        } else if (uiState.juegoCompletado) {
            JuegoCompletadoScreen(
                desafiosCorrectos = uiState.desafiosCorrectos,
                totalDesafios = uiState.totalDesafios,
                puntosGanados = uiState.puntosGanados,
                onReiniciar = { viewModel.reiniciarJuego() }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Encabezado con progreso
                EncabezadoJuego(
                    numeroDesafio = uiState.numeroDesafio,
                    totalDesafios = uiState.totalDesafios,
                    puntos = uiState.puntosGanados
                )

                Spacer(modifier = Modifier.height(24.dp))

                uiState.desafioActual?.let { desafio ->
                    // Imagen de la seña
                    ImagenSena(desafio.sena.imagenResource, context)

                    Spacer(modifier = Modifier.height(32.dp))

                    // Espacios de respuesta
                    EspaciosRespuesta(
                        espacios = desafio.espaciosRespuesta,
                        onEspacioClick = { index -> viewModel.onEspacioClick(index) }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Letras disponibles
                    LetrasDisponibles(
                        letras = desafio.letrasDisponibles,
                        letrasUsadas = desafio.letrasUsadas,
                        onLetraClick = { index -> viewModel.onLetraClick(index) }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Botón verificar
                    Button(
                        onClick = { viewModel.verificarRespuesta() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = desafio.espaciosRespuesta.none { it == null },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "VERIFICAR",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Dialog de resultado
        if (uiState.mostrarResultado) {
            ResultadoDialog(
                esCorrecto = uiState.esCorrecto,
                respuestaCorrecta = uiState.desafioActual?.respuesta ?: "",
                onContinuar = { viewModel.continuarJuego() }
            )
        }

        // Mostrar error si existe
        uiState.errorMessage?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text(error)
            }
        }
    }
}

@Composable
fun EncabezadoJuego(
    numeroDesafio: Int,
    totalDesafios: Int,
    puntos: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Desafío $numeroDesafio de $totalDesafios",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                LinearProgressIndicator(
                    progress = { numeroDesafio.toFloat() / totalDesafios.toFloat() },
                    modifier = Modifier
                        .width(150.dp)
                        .padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "⭐",
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$puntos",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ImagenSena(imagenResource: String, context: android.content.Context) {
    Card(
        modifier = Modifier
            .size(250.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        val resourceId = context.resources.getIdentifier(
            imagenResource,
            "drawable",
            context.packageName
        )

        if (resourceId != 0) {
            Image(
                painter = painterResource(id = resourceId),
                contentDescription = "Seña",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text("Imagen no disponible")
            }
        }
    }
}

@Composable
fun EspaciosRespuesta(
    espacios: List<Char?>,
    onEspacioClick: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        espacios.forEachIndexed { index, letra ->
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .padding(4.dp)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .background(
                        color = if (letra != null)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable(enabled = letra != null) {
                        onEspacioClick(index)
                    },
                contentAlignment = Alignment.Center
            ) {
                if (letra != null) {
                    Text(
                        text = letra.toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun LetrasDisponibles(
    letras: List<Char>,
    letrasUsadas: List<Boolean>,
    onLetraClick: (Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Primera fila (4 letras)
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            letras.take(4).forEachIndexed { index, letra ->
                LetraBox(
                    letra = letra,
                    isUsada = letrasUsadas.getOrNull(index) ?: false,
                    onClick = { onLetraClick(index) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Segunda fila (4 letras)
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            letras.drop(4).take(4).forEachIndexed { index, letra ->
                LetraBox(
                    letra = letra,
                    isUsada = letrasUsadas.getOrNull(index + 4) ?: false,
                    onClick = { onLetraClick(index + 4) }
                )
            }
        }
    }
}

@Composable
fun LetraBox(
    letra: Char,
    isUsada: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(70.dp)
            .padding(6.dp)
            .background(
                color = if (isUsada)
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                else
                    MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = !isUsada) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isUsada) "" else letra.toString(),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun ResultadoDialog(
    esCorrecto: Boolean,
    respuestaCorrecta: String,
    onContinuar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (esCorrecto) "✓" else "✗",
                    fontSize = 64.sp,
                    color = if (esCorrecto) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (esCorrecto) "¡Correcto!" else "Incorrecto",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (esCorrecto) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
        },
        text = {
            if (!esCorrecto) {
                Text(
                    text = "La respuesta correcta es: $respuestaCorrecta\nInténtalo de nuevo.",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = "¡Excelente trabajo! +20 puntos",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onContinuar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (esCorrecto) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(if (esCorrecto) "Continuar" else "Reintentar")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun JuegoCompletadoScreen(
    desafiosCorrectos: Int,
    totalDesafios: Int,
    puntosGanados: Int,
    onReiniciar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🏆",
            fontSize = 80.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "¡Juego Completado!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Estadísticas",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    EstadisticaItem("✓", "Correctas", "$desafiosCorrectos/$totalDesafios")
                    EstadisticaItem("⭐", "Puntos", "$puntosGanados")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onReiniciar,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "JUGAR DE NUEVO",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EstadisticaItem(emoji: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, fontSize = 32.sp)
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
