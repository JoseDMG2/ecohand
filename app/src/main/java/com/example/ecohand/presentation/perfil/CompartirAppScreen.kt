package com.example.ecohand.presentation.perfil

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompartirAppScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var showCopiedMessage by remember { mutableStateOf(false) }

    val mensajeCompartir = """
        ¡Hola! 👋
        
        Te invito a descargar EcoHand, una app increíble para aprender Lengua de Señas Peruana (LSP) de forma interactiva y divertida. 🤟
        
        Con EcoHand podrás:
        ✅ Aprender LSP paso a paso
        ✅ Practicar con lecciones interactivas
        ✅ Jugar y ganar puntos
        ✅ Seguir tu progreso
        
        ¡Únete a la comunidad EcoHand y aprende a comunicarte de manera inclusiva! 🌟
        
        #EcoHand #LSP #LenguaDeSeñas #Inclusión
    """.trimIndent()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Compartir App",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
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
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📤",
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Comparte EcoHand",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ayuda a más personas a aprender Lengua de Señas Peruana",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Título
            item {
                Text(
                    text = "Comparte por:",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Botón: Compartir en redes sociales
            item {
                CompartirOpcionCard(
                    icon = Icons.Default.Share,
                    titulo = "Compartir en Redes Sociales",
                    subtitulo = "WhatsApp, Facebook, Twitter y más",
                    onClick = {
                        val intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, mensajeCompartir)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(intent, "Compartir EcoHand"))
                    }
                )
            }

            // Botón: Enviar por mensaje
            item {
                CompartirOpcionCard(
                    icon = Icons.Default.Email,
                    titulo = "Enviar por Mensaje",
                    subtitulo = "SMS o mensajería directa",
                    onClick = {
                        val intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, mensajeCompartir)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(intent, "Enviar mensaje"))
                    }
                )
            }

            // Botón: Copiar enlace
            item {
                CompartirOpcionCard(
                    icon = Icons.Default.Edit,
                    titulo = "Copiar Mensaje",
                    subtitulo = "Copia el mensaje para compartirlo",
                    onClick = {
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                            as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("EcoHand", mensajeCompartir)
                        clipboard.setPrimaryClip(clip)
                        showCopiedMessage = true
                    }
                )
            }

            // Estadísticas de compartir
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "¿Por qué compartir EcoHand?",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        BeneficioItem(
                            emoji = "🤝",
                            texto = "Ayudas a crear una sociedad más inclusiva"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        BeneficioItem(
                            emoji = "📚",
                            texto = "Más personas aprenderán lengua de señas"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        BeneficioItem(
                            emoji = "🌟",
                            texto = "Rompes barreras de comunicación"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        BeneficioItem(
                            emoji = "💪",
                            texto = "Contribuyes a la educación inclusiva"
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Snackbar para mostrar mensaje de copiado
    if (showCopiedMessage) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            showCopiedMessage = false
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = "✓ Mensaje copiado al portapapeles",
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun CompartirOpcionCard(
    icon: ImageVector,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono con fondo circular
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = titulo,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Textos
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = titulo,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitulo,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Flecha
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Navegar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BeneficioItem(
    emoji: String,
    texto: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = emoji,
            fontSize = 24.sp,
            modifier = Modifier.padding(end = 12.dp)
        )
        Text(
            text = texto,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
