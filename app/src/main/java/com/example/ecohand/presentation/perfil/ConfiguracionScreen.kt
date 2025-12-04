package com.example.ecohand.presentation.perfil

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecohand.data.local.database.EcoHandDatabase
import com.example.ecohand.data.session.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val userSession = remember { UserSession.getInstance(context) }
    val database = remember { EcoHandDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    var notificacionesActivas by remember { mutableStateOf(true) }
    var sonidoActivo by remember { mutableStateOf(true) }
    var vibracionActiva by remember { mutableStateOf(true) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showVersionDialog by remember { mutableStateOf(false) }
    var showTerminosDialog by remember { mutableStateOf(false) }
    var showPoliticaDialog by remember { mutableStateOf(false) }
    var showCambiarPasswordDialog by remember { mutableStateOf(false) }

    // Estados para cambiar contraseña
    var passwordActual by remember { mutableStateOf("") }
    var passwordNueva by remember { mutableStateOf("") }
    var passwordConfirmar by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var passwordSuccess by remember { mutableStateOf(false) }
    var isChangingPassword by remember { mutableStateOf(false) }

    // Para mostrar/ocultar contraseñas
    var showPasswordActual by remember { mutableStateOf(false) }
    var showPasswordNueva by remember { mutableStateOf(false) }
    var showPasswordConfirmar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Configuración",
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Sección: General
            item {
                Text(
                    text = "General",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                ConfiguracionItemSwitch(
                    icon = Icons.Default.Notifications,
                    titulo = "Notificaciones",
                    subtitulo = "Recibe recordatorios de práctica",
                    checked = notificacionesActivas,
                    onCheckedChange = { notificacionesActivas = it }
                )
            }

            item {
                ConfiguracionItemSwitch(
                    icon = Icons.Default.AccountCircle,
                    titulo = "Sonido",
                    subtitulo = "Efectos de sonido en la app",
                    checked = sonidoActivo,
                    onCheckedChange = { sonidoActivo = it }
                )
            }

            item {
                ConfiguracionItemSwitch(
                    icon = Icons.Default.Phone,
                    titulo = "Vibración",
                    subtitulo = "Vibración al interactuar",
                    checked = vibracionActiva,
                    onCheckedChange = { vibracionActiva = it }
                )
            }


            // Sección: Cuenta
            item {
                Text(
                    text = "Cuenta",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            item {
                ConfiguracionItemClick(
                    icon = Icons.Default.Lock,
                    titulo = "Cambiar Contraseña",
                    subtitulo = "Actualiza tu contraseña",
                    onClick = {
                        showCambiarPasswordDialog = true
                        // Resetear campos
                        passwordActual = ""
                        passwordNueva = ""
                        passwordConfirmar = ""
                        passwordError = null
                        passwordSuccess = false
                    }
                )
            }

            item {
                ConfiguracionItemClick(
                    icon = Icons.Default.Delete,
                    titulo = "Eliminar Cuenta",
                    subtitulo = "Eliminar permanentemente tu cuenta",
                    onClick = { /* TODO: Implementar eliminación de cuenta */ },
                    tintColor = MaterialTheme.colorScheme.error
                )
            }

            // Sección: Sobre
            item {
                Text(
                    text = "Sobre la App",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            item {
                ConfiguracionItemClick(
                    icon = Icons.Default.Info,
                    titulo = "Versión",
                    subtitulo = "1.0.0",
                    onClick = { showVersionDialog = true }
                )
            }

            item {
                ConfiguracionItemClick(
                    icon = Icons.Default.Star,
                    titulo = "Términos y Condiciones",
                    subtitulo = "Lee nuestros términos de uso",
                    onClick = { showTerminosDialog = true }
                )
            }

            item {
                ConfiguracionItemClick(
                    icon = Icons.Default.Star,
                    titulo = "Política de Privacidad",
                    subtitulo = "Lee nuestra política de privacidad",
                    onClick = { showPoliticaDialog = true }
                )
            }

            // Botón Cerrar Sesión
            item {
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Cerrar Sesión",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cerrar Sesión",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Dialog de confirmación para cerrar sesión
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Cerrar Sesión",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("¿Estás seguro que deseas cerrar sesión?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        userSession.clearSession()
                        (context as? Activity)?.finishAffinity()
                    }
                ) {
                    Text("Cerrar Sesión", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog de cambiar contraseña
    if (showCambiarPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isChangingPassword) {
                    showCambiarPasswordDialog = false
                    passwordActual = ""
                    passwordNueva = ""
                    passwordConfirmar = ""
                    passwordError = null
                    passwordSuccess = false
                }
            },
            title = {
                Text(
                    text = "Cambiar Contraseña",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (passwordSuccess) {
                        // Mensaje de éxito
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "✅",
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Contraseña actualizada exitosamente",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        // Campos de contraseña
                        Text(
                            text = "Ingresa tu contraseña actual y la nueva contraseña",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Contraseña actual
                        OutlinedTextField(
                            value = passwordActual,
                            onValueChange = {
                                passwordActual = it
                                passwordError = null
                            },
                            label = { Text("Contraseña Actual") },
                            placeholder = { Text("Ingresa tu contraseña actual") },
                            visualTransformation = if (showPasswordActual)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = { showPasswordActual = !showPasswordActual }) {
                                    Icon(
                                        imageVector = if (showPasswordActual)
                                            Icons.Default.Person
                                        else
                                            Icons.Default.Lock,
                                        contentDescription = if (showPasswordActual)
                                            "Ocultar contraseña"
                                        else
                                            "Mostrar contraseña"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isChangingPassword,
                            singleLine = true
                        )

                        // Nueva contraseña
                        OutlinedTextField(
                            value = passwordNueva,
                            onValueChange = {
                                passwordNueva = it
                                passwordError = null
                            },
                            label = { Text("Nueva Contraseña") },
                            placeholder = { Text("Ingresa la nueva contraseña") },
                            visualTransformation = if (showPasswordNueva)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = { showPasswordNueva = !showPasswordNueva }) {
                                    Icon(
                                        imageVector = if (showPasswordNueva)
                                            Icons.Default.Person
                                        else
                                            Icons.Default.Lock,
                                        contentDescription = if (showPasswordNueva)
                                            "Ocultar contraseña"
                                        else
                                            "Mostrar contraseña"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isChangingPassword,
                            singleLine = true
                        )

                        // Confirmar nueva contraseña
                        OutlinedTextField(
                            value = passwordConfirmar,
                            onValueChange = {
                                passwordConfirmar = it
                                passwordError = null
                            },
                            label = { Text("Confirmar Nueva Contraseña") },
                            placeholder = { Text("Confirma la nueva contraseña") },
                            visualTransformation = if (showPasswordConfirmar)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = { showPasswordConfirmar = !showPasswordConfirmar }) {
                                    Icon(
                                        imageVector = if (showPasswordConfirmar)
                                            Icons.Default.Person
                                        else
                                            Icons.Default.Lock,
                                        contentDescription = if (showPasswordConfirmar)
                                            "Ocultar contraseña"
                                        else
                                            "Mostrar contraseña"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isChangingPassword,
                            singleLine = true
                        )

                        // Mensaje de error
                        passwordError?.let { error ->
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Indicador de progreso
                        if (isChangingPassword) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (passwordSuccess) {
                    TextButton(
                        onClick = {
                            showCambiarPasswordDialog = false
                            passwordActual = ""
                            passwordNueva = ""
                            passwordConfirmar = ""
                            passwordError = null
                            passwordSuccess = false
                        }
                    ) {
                        Text("Cerrar")
                    }
                } else {
                    TextButton(
                        onClick = {
                            // Validaciones
                            when {
                                passwordActual.isEmpty() -> {
                                    passwordError = "Ingresa tu contraseña actual"
                                }
                                passwordNueva.isEmpty() -> {
                                    passwordError = "Ingresa la nueva contraseña"
                                }
                                passwordNueva.length < 6 -> {
                                    passwordError = "La contraseña debe tener al menos 6 caracteres"
                                }
                                passwordConfirmar.isEmpty() -> {
                                    passwordError = "Confirma la nueva contraseña"
                                }
                                passwordNueva != passwordConfirmar -> {
                                    passwordError = "Las contraseñas no coinciden"
                                }
                                passwordActual == passwordNueva -> {
                                    passwordError = "La nueva contraseña debe ser diferente a la actual"
                                }
                                else -> {
                                    // Validar contraseña actual y actualizar
                                    isChangingPassword = true
                                    passwordError = null

                                    scope.launch(Dispatchers.IO) {
                                        try {
                                            val userId = userSession.getUserId()
                                            val user = database.userDao().getUserById(userId)

                                            if (user != null) {
                                                // Verificar contraseña actual
                                                if (user.password == passwordActual) {
                                                    // Actualizar contraseña
                                                    val updatedUser = user.copy(password = passwordNueva)
                                                    database.userDao().updateUser(updatedUser)

                                                    // Mostrar mensaje de éxito
                                                    launch(Dispatchers.Main) {
                                                        passwordSuccess = true
                                                        isChangingPassword = false
                                                    }
                                                } else {
                                                    // Contraseña actual incorrecta
                                                    launch(Dispatchers.Main) {
                                                        passwordError = "La contraseña actual es incorrecta"
                                                        isChangingPassword = false
                                                    }
                                                }
                                            } else {
                                                launch(Dispatchers.Main) {
                                                    passwordError = "Error: Usuario no encontrado"
                                                    isChangingPassword = false
                                                }
                                            }
                                        } catch (e: Exception) {
                                            launch(Dispatchers.Main) {
                                                passwordError = "Error al cambiar contraseña: ${e.message}"
                                                isChangingPassword = false
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        enabled = !isChangingPassword
                    ) {
                        Text("Cambiar")
                    }
                }
            },
            dismissButton = {
                if (!passwordSuccess) {
                    TextButton(
                        onClick = {
                            showCambiarPasswordDialog = false
                            passwordActual = ""
                            passwordNueva = ""
                            passwordConfirmar = ""
                            passwordError = null
                            passwordSuccess = false
                        },
                        enabled = !isChangingPassword
                    ) {
                        Text("Cancelar")
                    }
                }
            }
        )
    }

    // Dialog de información de versión
    if (showVersionDialog) {
        AlertDialog(
            onDismissRequest = { showVersionDialog = false },
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "📱",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "EcoHand",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Versión: 1.0.0",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Text(
                        text = "Fecha de lanzamiento: Noviembre 2024",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "EcoHand es una aplicación educativa para aprender Lengua de Señas Peruana (LSP) de forma interactiva y divertida.",
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Justify
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "© 2024 EcoHand Team",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showVersionDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    // Dialog de Términos y Condiciones
    if (showTerminosDialog) {
        AlertDialog(
            onDismissRequest = { showTerminosDialog = false },
            title = {
                Text(
                    text = "Términos y Condiciones",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    item {
                        Text(
                            text = """
                                TÉRMINOS Y CONDICIONES DE USO DE ECOHAND
                                
                                Última actualización: Noviembre 2024
                                
                                1. ACEPTACIÓN DE LOS TÉRMINOS
                                Al descargar, instalar o usar la aplicación EcoHand, usted acepta estar sujeto a estos Términos y Condiciones. Si no está de acuerdo con estos términos, por favor no use la aplicación.
                                
                                2. USO DE LA APLICACIÓN
                                EcoHand es una aplicación educativa diseñada para el aprendizaje de la Lengua de Señas Peruana (LSP). El usuario se compromete a:
                                • Usar la aplicación solo con fines educativos
                                • No realizar ingeniería inversa del software
                                • No compartir su cuenta con terceros
                                • Proporcionar información veraz al registrarse
                                
                                3. PROPIEDAD INTELECTUAL
                                Todo el contenido de EcoHand, incluyendo pero no limitado a textos, gráficos, logos, videos, y software, es propiedad de EcoHand Team y está protegido por las leyes de propiedad intelectual.
                                
                                4. PRIVACIDAD
                                EcoHand respeta su privacidad. Los datos recopilados se utilizan únicamente para mejorar la experiencia del usuario y el funcionamiento de la aplicación. No compartimos información personal con terceros.
                                
                                5. MODIFICACIONES
                                Nos reservamos el derecho de modificar estos términos en cualquier momento. Los cambios entrarán en vigor inmediatamente después de su publicación en la aplicación.
                                
                                6. LIMITACIÓN DE RESPONSABILIDAD
                                EcoHand se proporciona "tal cual" sin garantías de ningún tipo. No nos hacemos responsables de daños directos o indirectos derivados del uso de la aplicación.
                                
                                7. CONTACTO
                                Para cualquier pregunta sobre estos términos, contáctenos a través de la sección de Ayuda y Soporte en la aplicación.
                            """.trimIndent(),
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Justify
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTerminosDialog = false }) {
                    Text("Aceptar")
                }
            }
        )
    }

    // Dialog de Política de Privacidad
    if (showPoliticaDialog) {
        AlertDialog(
            onDismissRequest = { showPoliticaDialog = false },
            title = {
                Text(
                    text = "Política de Privacidad",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    item {
                        Text(
                            text = """
                                POLÍTICA DE PRIVACIDAD DE ECOHAND
                                
                                Última actualización: Noviembre 2024
                                
                                1. INFORMACIÓN QUE RECOPILAMOS
                                EcoHand recopila la siguiente información:
                                • Información de cuenta: nombre de usuario, correo electrónico
                                • Datos de progreso: lecciones completadas, puntuaciones, logros
                                • Datos de uso: tiempo de uso, frecuencia de acceso
                                
                                2. CÓMO USAMOS SU INFORMACIÓN
                                Utilizamos la información recopilada para:
                                • Proporcionar y mejorar nuestros servicios
                                • Personalizar su experiencia de aprendizaje
                                • Hacer seguimiento de su progreso
                                • Enviar notificaciones relevantes (si está habilitado)
                                • Analizar el uso de la aplicación para mejoras
                                
                                3. ALMACENAMIENTO DE DATOS
                                Sus datos se almacenan de forma segura en el dispositivo local. La información se mantiene encriptada y protegida mediante las medidas de seguridad estándar de la industria.
                                
                                4. COMPARTIR INFORMACIÓN
                                NO compartimos, vendemos ni alquilamos su información personal a terceros. Sus datos son privados y permanecen en su dispositivo.
                                
                                5. SEGURIDAD
                                Implementamos medidas de seguridad técnicas y organizativas para proteger su información contra acceso no autorizado, alteración, divulgación o destrucción.
                                
                                6. DERECHOS DEL USUARIO
                                Usted tiene derecho a:
                                • Acceder a su información personal
                                • Corregir datos inexactos
                                • Eliminar su cuenta y datos asociados
                                • Exportar sus datos de progreso
                                
                                7. DATOS DE MENORES
                                EcoHand puede ser usado por menores de edad bajo supervisión de un adulto. Los padres o tutores son responsables del uso que los menores hagan de la aplicación.
                                
                                8. CAMBIOS A ESTA POLÍTICA
                                Podemos actualizar esta política periódicamente. Le notificaremos sobre cambios significativos mediante la aplicación.
                                
                                9. COOKIES Y TECNOLOGÍAS SIMILARES
                                EcoHand no utiliza cookies. Toda la información se almacena localmente en su dispositivo.
                                
                                10. CONTACTO
                                Si tiene preguntas sobre esta política de privacidad, puede contactarnos a través de la sección de Ayuda y Soporte.
                                
                                Al usar EcoHand, usted acepta esta Política de Privacidad.
                            """.trimIndent(),
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Justify
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPoliticaDialog = false }) {
                    Text("Aceptar")
                }
            }
        )
    }
}

@Composable
fun ConfiguracionItemSwitch(
    icon: ImageVector,
    titulo: String,
    subtitulo: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Icon(
                imageVector = icon,
                contentDescription = titulo,
                tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = titulo,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = subtitulo,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
    }
}

@Composable
fun ConfiguracionItemClick(
    icon: ImageVector,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit,
    tintColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
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
            Icon(
                imageVector = icon,
                contentDescription = titulo,
                tint = tintColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = titulo,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitulo,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Navegar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}