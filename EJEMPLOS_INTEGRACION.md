# 💡 Ejemplos de Integración - Sistema de Progreso

## 1. Al Completar una Lección

```kotlin
// En tu ViewModel de Lecciones
class LeccionViewModel(
    private val progresoLeccionDao: ProgresoLeccionDao,
    private val estadisticasUsuarioDao: EstadisticasUsuarioDao,
    private val progresoRepository: ProgresoRepository,
    private val usuarioId: Int
) : ViewModel() {
    
    fun completarLeccion(leccionId: Int, puntuacion: Int) {
        viewModelScope.launch {
            try {
                // 1. Registrar progreso de la lección
                val progreso = ProgresoLeccionEntity(
                    usuarioId = usuarioId,
                    leccionId = leccionId,
                    completada = true,
                    puntuacion = puntuacion,
                    intentos = 1,
                    fechaCompletado = System.currentTimeMillis()
                )
                progresoLeccionDao.insertProgreso(progreso)
                
                // 2. Actualizar estadísticas del usuario
                val estadisticas = estadisticasUsuarioDao.getEstadisticasByUsuario(usuarioId)
                estadisticas?.let {
                    estadisticasUsuarioDao.updateEstadisticas(
                        it.copy(
                            puntosTotal = it.puntosTotal + puntuacion,
                            leccionesCompletadas = it.leccionesCompletadas + 1,
                            ultimaActualizacion = System.currentTimeMillis()
                        )
                    )
                }
                
                // 3. Verificar si se desbloquearon logros
                progresoRepository.verificarLogros(usuarioId)
                
                // 4. Mostrar mensaje de éxito
                _uiState.value = _uiState.value.copy(
                    mensajeExito = "¡Lección completada! +$puntuacion puntos"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al guardar progreso: ${e.message}"
                )
            }
        }
    }
}
```

## 2. Inicializar Estadísticas de Nuevo Usuario

```kotlin
// En UserRepository.kt al registrar un usuario
suspend fun registerUser(username: String, email: String, password: String): Result<Long> {
    return try {
        val existingUser = userDao.getUserByEmail(email)
        if (existingUser != null) {
            Result.failure(Exception("El correo electrónico ya está registrado"))
        } else {
            val user = UserEntity(
                username = username,
                email = email,
                password = password
            )
            val userId = userDao.insertUser(user)
            
            // Inicializar estadísticas del nuevo usuario
            val estadisticas = EstadisticasUsuarioEntity(
                usuarioId = userId.toInt(),
                puntosTotal = 0,
                rachaActual = 0,
                rachaMayor = 0,
                leccionesCompletadas = 0,
                diasActivos = 0
            )
            estadisticasUsuarioDao.insertEstadisticas(estadisticas)
            
            // Inicializar logros del usuario
            val logros = logroDao.getAllLogros()
            logros.forEach { logro ->
                logroUsuarioDao.insertLogroUsuario(
                    LogroUsuarioEntity(
                        usuarioId = userId.toInt(),
                        logroId = logro.id,
                        obtenido = false
                    )
                )
            }
            
            Result.success(userId)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

## 3. Mostrar Logro Desbloqueado

```kotlin
// Composable para mostrar un dialog de logro
@Composable
fun LogroDesbloqueadoDialog(
    logro: LogroEntity,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = logro.emoji,
                    fontSize = 64.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "¡Logro Desbloqueado!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = logro.nombre,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = logro.descripcion,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("¡Genial!")
            }
        }
    )
}

// En el ViewModel
private val _logroDesbloqueado = MutableStateFlow<LogroEntity?>(null)
val logroDesbloqueado: StateFlow<LogroEntity?> = _logroDesbloqueado.asStateFlow()

suspend fun verificarYMostrarLogros() {
    val logrosAnteriores = logroUsuarioDao.getLogrosObtenidos(usuarioId)
    progresoRepository.verificarLogros(usuarioId)
    val logrosNuevos = logroUsuarioDao.getLogrosObtenidos(usuarioId)
    
    // Encontrar logros recién desbloqueados
    val nuevosIds = logrosNuevos.map { it.logroId } - logrosAnteriores.map { it.logroId }.toSet()
    if (nuevosIds.isNotEmpty()) {
        val logroNuevo = logroDao.getLogroById(nuevosIds.first())
        _logroDesbloqueado.value = logroNuevo
    }
}

// En el Screen
val logroDesbloqueado by viewModel.logroDesbloqueado.collectAsState()

logroDesbloqueado?.let { logro ->
    LogroDesbloqueadoDialog(
        logro = logro,
        onDismiss = { viewModel.dismissLogro() }
    )
}
```

## 4. Widget de Racha en HomeScreen

```kotlin
@Composable
fun RachaWidget(racha: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (racha > 0) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🔥",
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Racha Actual",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$racha días",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            if (racha > 0) {
                Text(
                    text = "¡Sigue así!",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "Comienza hoy",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// En HomeScreen
@Composable
fun InicioScreen() {
    val context = LocalContext.current
    val database = EcoHandDatabase.getDatabase(context)
    val userSession = UserSession.getInstance(context)
    val usuarioId = userSession.getUserId()
    
    val estadisticas = remember {
        mutableStateOf<EstadisticasUsuarioEntity?>(null)
    }
    
    LaunchedEffect(usuarioId) {
        withContext(Dispatchers.IO) {
            estadisticas.value = database.estadisticasUsuarioDao()
                .getEstadisticasByUsuario(usuarioId)
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        estadisticas.value?.let { stats ->
            RachaWidget(racha = stats.rachaActual)
        }
        // ... resto del contenido
    }
}
```

## 5. Indicador de Progreso en PerfilScreen

```kotlin
@Composable
fun ProgresoResumen(
    puntosTotal: Int,
    leccionesCompletadas: Int,
    totalLecciones: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    emoji = "⭐",
                    label = "Puntos",
                    value = puntosTotal.toString()
                )
                StatItem(
                    emoji = "📚",
                    label = "Lecciones",
                    value = "$leccionesCompletadas/$totalLecciones"
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = leccionesCompletadas.toFloat() / totalLecciones.toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
fun StatItem(emoji: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, fontSize = 32.sp)
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
```

## 6. Notificación de Recordatorio de Racha

```kotlin
// En un WorkManager o servicio
class RachaReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        val userSession = UserSession.getInstance(applicationContext)
        val userId = userSession.getUserId()
        
        if (userId == -1) return Result.success()
        
        val database = EcoHandDatabase.getDatabase(applicationContext)
        val actividadDao = database.actividadDiariaDao()
        
        // Verificar si el usuario ya inició sesión hoy
        val hoy = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        val actividadHoy = actividadDao.getActividadPorFecha(userId, hoy)
        
        if (actividadHoy == null) {
            // Mostrar notificación
            showReminderNotification()
        }
        
        return Result.success()
    }
    
    private fun showReminderNotification() {
        val notification = NotificationCompat.Builder(applicationContext, "racha_channel")
            .setSmallIcon(R.drawable.ic_fire)
            .setContentTitle("¡No pierdas tu racha! 🔥")
            .setContentText("Practica hoy para mantener tu racha activa")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        
        with(NotificationManagerCompat.from(applicationContext)) {
            notify(1, notification)
        }
    }
}
```

## 7. Sistema de Niveles (Opcional)

```kotlin
// Agregar campo a EstadisticasUsuarioEntity
val nivel: Int = 1

// Función para calcular nivel
fun calcularNivel(puntosTotal: Int): Int {
    return when {
        puntosTotal < 100 -> 1
        puntosTotal < 300 -> 2
        puntosTotal < 600 -> 3
        puntosTotal < 1000 -> 4
        puntosTotal < 1500 -> 5
        else -> 6
    }
}

// Componente de Nivel
@Composable
fun NivelBadge(nivel: Int) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$nivel",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
```

---

**¡Usa estos ejemplos para integrar completamente el sistema de progreso en tu aplicación!** 🎉

