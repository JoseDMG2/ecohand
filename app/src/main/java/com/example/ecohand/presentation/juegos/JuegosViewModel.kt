package com.example.ecohand.presentation.juegos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecohand.data.local.entity.PartidaJuegoEntity
import com.example.ecohand.data.local.entity.SenaEntity
import com.example.ecohand.data.repository.JuegoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class Desafio(
    val sena: SenaEntity,
    val respuesta: String,
    val letrasDisponibles: List<Char>,
    val espaciosRespuesta: List<Char?> = List(respuesta.length) { null },
    val letrasUsadas: List<Boolean> = List(letrasDisponibles.size) { false }
)

data class JuegosUiState(
    val isLoading: Boolean = true,
    val desafioActual: Desafio? = null,
    val numeroDesafio: Int = 1,
    val totalDesafios: Int = 5,
    val partidaId: Int? = null,
    val desafiosCorrectos: Int = 0,
    val puntosGanados: Int = 0,
    val mostrarResultado: Boolean = false,
    val esCorrecto: Boolean = false,
    val juegoCompletado: Boolean = false,
    val errorMessage: String? = null
)

class JuegosViewModel(
    private val juegoRepository: JuegoRepository,
    private val usuarioId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(JuegosUiState())
    val uiState: StateFlow<JuegosUiState> = _uiState.asStateFlow()

    private var senasUsadas = mutableListOf<Int>()
    private var todasLasSenas = listOf<SenaEntity>()

    init {
        iniciarJuego()
    }

    fun iniciarJuego() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Obtener todas las señas
                todasLasSenas = juegoRepository.getAllSenas()
                senasUsadas.clear()

                // Verificar si hay señas disponibles
                if (todasLasSenas.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "No hay señas disponibles. Por favor, verifica la base de datos."
                    )
                    return@launch
                }

                // Crear nueva partida
                val partidaId = juegoRepository.crearPartida(usuarioId)

                // Cargar primer desafío
                val primerDesafio = generarDesafio()
                if (primerDesafio == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al generar desafío"
                    )
                    return@launch
                }

                _uiState.value = JuegosUiState(
                    isLoading = false,
                    partidaId = partidaId.toInt(),
                    numeroDesafio = 1,
                    desafioActual = primerDesafio
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al iniciar juego: ${e.message}"
                )
            }
        }
    }

    private fun generarDesafio(): Desafio? {
        // Verificar que hay señas disponibles
        if (todasLasSenas.isEmpty()) {
            return null
        }

        // Filtrar señas no usadas
        val senasDisponibles = todasLasSenas.filter { it.id !in senasUsadas }

        // Si se acabaron las señas, reiniciar la lista
        val senaParaUsar = if (senasDisponibles.isEmpty()) {
            senasUsadas.clear()
            todasLasSenas.random()
        } else {
            senasDisponibles.random()
        }

        senasUsadas.add(senaParaUsar.id)

        val respuesta = senaParaUsar.nombre.uppercase()

        // Generar letras disponibles
        // Incluir TODAS las letras de la respuesta (incluyendo repetidas)
        val letrasRespuesta = respuesta.toList()

        // Calcular cuántas letras adicionales necesitamos (máximo 8 total)
        val letrasAdicionalesNecesarias = maxOf(0, 8 - letrasRespuesta.size)

        // Generar letras adicionales que NO estén en la respuesta
        val letrasUnicasRespuesta = respuesta.toSet()
        val letrasAdicionales = ('A'..'Z')
            .filter { it !in letrasUnicasRespuesta }
            .shuffled()
            .take(letrasAdicionalesNecesarias)

        // Combinar todas las letras y mezclarlas
        val todasLetras = (letrasRespuesta + letrasAdicionales).shuffled()

        return Desafio(
            sena = senaParaUsar,
            respuesta = respuesta,
            letrasDisponibles = todasLetras
        )
    }

    fun onLetraClick(index: Int) {
        val currentState = _uiState.value
        val desafio = currentState.desafioActual ?: return

        // Si la letra ya fue usada, no hacer nada
        if (desafio.letrasUsadas[index]) return

        // Encontrar el primer espacio vacío
        val primerEspacioVacio = desafio.espaciosRespuesta.indexOfFirst { it == null }

        if (primerEspacioVacio != -1) {
            val letra = desafio.letrasDisponibles[index]
            val nuevosEspacios = desafio.espaciosRespuesta.toMutableList()
            nuevosEspacios[primerEspacioVacio] = letra

            val nuevasLetrasUsadas = desafio.letrasUsadas.toMutableList()
            nuevasLetrasUsadas[index] = true

            val desafioActualizado = desafio.copy(
                espaciosRespuesta = nuevosEspacios,
                letrasUsadas = nuevasLetrasUsadas
            )

            _uiState.value = currentState.copy(desafioActual = desafioActualizado)
        }
    }

    fun onEspacioClick(espacioIndex: Int) {
        val currentState = _uiState.value
        val desafio = currentState.desafioActual ?: return

        val letraEnEspacio = desafio.espaciosRespuesta[espacioIndex] ?: return

        // Encontrar el índice original de esta letra
        val letraIndex = desafio.letrasDisponibles.indexOf(letraEnEspacio)

        if (letraIndex != -1) {
            val nuevosEspacios = desafio.espaciosRespuesta.toMutableList()
            nuevosEspacios[espacioIndex] = null

            val nuevasLetrasUsadas = desafio.letrasUsadas.toMutableList()
            nuevasLetrasUsadas[letraIndex] = false

            val desafioActualizado = desafio.copy(
                espaciosRespuesta = nuevosEspacios,
                letrasUsadas = nuevasLetrasUsadas
            )

            _uiState.value = currentState.copy(desafioActual = desafioActualizado)
        }
    }

    fun verificarRespuesta() {
        val currentState = _uiState.value
        val desafio = currentState.desafioActual ?: return

        // Verificar si todos los espacios están llenos
        if (desafio.espaciosRespuesta.any { it == null }) {
            return
        }

        val respuestaUsuario = desafio.espaciosRespuesta.joinToString("")
        val esCorrecto = respuestaUsuario == desafio.respuesta

        if (esCorrecto) {
            // Respuesta correcta
            val nuevosDesafiosCorrectos = currentState.desafiosCorrectos + 1
            val puntos = 20 // 20 puntos por respuesta correcta

            viewModelScope.launch {
                // Actualizar partida
                currentState.partidaId?.let { partidaId ->
                    val partida = PartidaJuegoEntity(
                        id = partidaId,
                        usuarioId = usuarioId,
                        desafiosCompletados = currentState.numeroDesafio,
                        desafiosCorrectos = nuevosDesafiosCorrectos,
                        desafiosIncorrectos = currentState.numeroDesafio - nuevosDesafiosCorrectos,
                        puntosGanados = currentState.puntosGanados + puntos
                    )
                    juegoRepository.actualizarPartida(partida)
                }
            }

            _uiState.value = currentState.copy(
                mostrarResultado = true,
                esCorrecto = true,
                desafiosCorrectos = nuevosDesafiosCorrectos,
                puntosGanados = currentState.puntosGanados + puntos
            )
        } else {
            // Respuesta incorrecta
            _uiState.value = currentState.copy(
                mostrarResultado = true,
                esCorrecto = false
            )
        }
    }

    fun continuarJuego() {
        val currentState = _uiState.value

        if (currentState.esCorrecto) {
            // Si respondió correctamente, avanzar al siguiente desafío
            if (currentState.numeroDesafio >= currentState.totalDesafios) {
                // Juego completado
                finalizarJuego()
            } else {
                // Siguiente desafío
                val nuevoDesafio = generarDesafio()
                if (nuevoDesafio != null) {
                    _uiState.value = currentState.copy(
                        desafioActual = nuevoDesafio,
                        numeroDesafio = currentState.numeroDesafio + 1,
                        mostrarResultado = false,
                        esCorrecto = false
                    )
                } else {
                    _uiState.value = currentState.copy(
                        errorMessage = "Error al generar el siguiente desafío"
                    )
                }
            }
        } else {
            // Si respondió incorrectamente, limpiar espacios para reintentar
            val desafio = currentState.desafioActual ?: return
            _uiState.value = currentState.copy(
                desafioActual = desafio.copy(
                    espaciosRespuesta = List(desafio.respuesta.length) { null },
                    letrasUsadas = List(desafio.letrasDisponibles.size) { false }
                ),
                mostrarResultado = false,
                esCorrecto = false
            )
        }
    }

    private fun finalizarJuego() {
        viewModelScope.launch {
            val currentState = _uiState.value
            currentState.partidaId?.let { partidaId ->
                juegoRepository.completarPartida(partidaId, usuarioId)
            }

            _uiState.value = currentState.copy(
                juegoCompletado = true,
                mostrarResultado = false
            )
        }
    }

    fun reiniciarJuego() {
        iniciarJuego()
    }
}

