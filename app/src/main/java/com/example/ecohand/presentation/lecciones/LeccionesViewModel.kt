package com.example.ecohand.presentation.lecciones

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecohand.data.local.database.EcoHandDatabase
import com.example.ecohand.data.local.entity.LeccionEntity
import com.example.ecohand.data.local.entity.SenaEntity
import com.example.ecohand.data.repository.LeccionRepository
import com.example.ecohand.data.session.UserSession
import com.example.ecohand.ml.HandDetectionResult
import com.example.ecohand.ml.SignRecognitionResult
import com.example.ecohand.ml.SignRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Evaluation state for sign practice
 */
data class EvaluationState(
    val intentos: Int = 0,
    val vidas: Int = 5,
    val maxIntentos: Int = 5,
    val ultimaPrecision: Float = 0f,
    val isEvaluating: Boolean = false,
    val lastResult: SignRecognitionResult? = null,
    val handDetectionResult: HandDetectionResult? = null,
    val feedbackMessage: String? = null,
    val isCorrect: Boolean? = null
)

data class LeccionUIState(
    val lecciones: List<LeccionEntity> = emptyList(),
    val leccionesCompletadas: Set<Int> = emptySet(),
    val leccionActual: LeccionEntity? = null,
    val senaActual: SenaEntity? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showCompletadoDialog: Boolean = false,
    val evaluationState: EvaluationState = EvaluationState(),
    val isSignRecognizerReady: Boolean = false
)

class LeccionesViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: LeccionRepository
    private val userSession: UserSession = UserSession.getInstance(application)
    private val signRecognizer: SignRecognizer = SignRecognizer(application)
    private val database = EcoHandDatabase.getDatabase(application)
    
    private val _uiState = MutableStateFlow(LeccionUIState())
    val uiState: StateFlow<LeccionUIState> = _uiState.asStateFlow()
    
    companion object {
        const val PRECISION_THRESHOLD = 0.85f
        const val MAX_INTENTOS = 5
        const val VIDAS_INICIALES = 5
    }
    
    init {
        repository = LeccionRepository(
            database.leccionDao(),
            database.progresoLeccionDao()
        )
        cargarLecciones()
        initializeSignRecognizer()
    }
    
    private fun initializeSignRecognizer() {
        viewModelScope.launch {
            try {
                val success = signRecognizer.initialize()
                _uiState.value = _uiState.value.copy(isSignRecognizerReady = success)
                if (success) {
                    loadReferenceLandmarks()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al inicializar el reconocedor de senas: ${e.message}",
                    isSignRecognizerReady = false
                )
            }
        }
    }
    
    private suspend fun loadReferenceLandmarks() {
        try {
            val senas = database.senaDao().getSenasWithLandmarks()
            senas.forEach { sena ->
                sena.landmarksData?.let { landmarks ->
                    signRecognizer.loadReferenceLandmarks(sena.nombre, landmarks)
                }
            }
        } catch (e: Exception) {
            // Non-fatal error, continue without reference landmarks
        }
    }
    
    fun cargarLecciones() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val userId = userSession.getUserId()
                val todasLasLecciones = repository.getAllLecciones()
                val progreso = repository.getProgresoUsuario(userId)
                val completadas = progreso.filter { it.completada }.map { it.leccionId }.toSet()
                
                val leccionesConEstado = todasLasLecciones.map { leccion ->
                    val estaCompletada = completadas.contains(leccion.id)
                    val leccionAnteriorCompletada = if (leccion.orden > 1) {
                        val leccionAnterior = todasLasLecciones.find { it.orden == leccion.orden - 1 }
                        leccionAnterior?.let { completadas.contains(it.id) } ?: false
                    } else {
                        true
                    }
                    
                    leccion.copy(
                        bloqueada = !estaCompletada && !leccionAnteriorCompletada
                    )
                }
                
                _uiState.value = _uiState.value.copy(
                    lecciones = leccionesConEstado,
                    leccionesCompletadas = completadas,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }
    
    fun cargarLeccion(leccionId: Int) {
        viewModelScope.launch {
            try {
                val leccion = repository.getLeccionById(leccionId)
                _uiState.value = _uiState.value.copy(
                    leccionActual = leccion,
                    evaluationState = EvaluationState()
                )
                loadSenaForLeccion(leccion)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    private suspend fun loadSenaForLeccion(leccion: LeccionEntity?) {
        if (leccion == null) return
        
        try {
            val senas = database.senaDao().getAllSenas()
            val senaMatch = senas.find { sena ->
                sena.categoria.equals(leccion.categoria, ignoreCase = true) ||
                sena.nombre.equals(leccion.titulo, ignoreCase = true)
            } ?: senas.firstOrNull()
            
            _uiState.value = _uiState.value.copy(senaActual = senaMatch)
        } catch (e: Exception) {
            // Continue without a specific sign
        }
    }
    
    fun processFrame(bitmap: Bitmap) {
        if (!_uiState.value.isSignRecognizerReady) return
        
        viewModelScope.launch {
            try {
                val result = signRecognizer.detectHandLandmarks(bitmap)
                val currentState = _uiState.value.evaluationState
                
                _uiState.value = _uiState.value.copy(
                    evaluationState = currentState.copy(
                        handDetectionResult = result
                    )
                )
            } catch (e: Exception) {
                // Silently handle frame processing errors
            }
        }
    }
    
    fun evaluarSena(bitmap: Bitmap) {
        val sena = _uiState.value.senaActual ?: return
        if (!_uiState.value.isSignRecognizerReady) {
            _uiState.value = _uiState.value.copy(
                evaluationState = _uiState.value.evaluationState.copy(
                    feedbackMessage = "El reconocedor de senas no esta listo"
                )
            )
            return
        }
        
        viewModelScope.launch {
            val currentState = _uiState.value.evaluationState
            
            if (currentState.intentos >= MAX_INTENTOS || currentState.vidas <= 0) {
                _uiState.value = _uiState.value.copy(
                    evaluationState = currentState.copy(
                        feedbackMessage = "No tienes mas intentos o vidas"
                    )
                )
                return@launch
            }
            
            _uiState.value = _uiState.value.copy(
                evaluationState = currentState.copy(isEvaluating = true)
            )
            
            try {
                val result = signRecognizer.recognizeSign(bitmap, sena.nombre)
                
                val newIntentos = currentState.intentos + 1
                val newVidas = if (result.isCorrect) currentState.vidas else currentState.vidas - 1
                val feedbackMessage = when {
                    result.isCorrect -> "Excelente! Sena correcta (${(result.confidence * 100).toInt()}%)"
                    result.confidence >= 0.5f -> "Casi... Precision: ${(result.confidence * 100).toInt()}%"
                    result.landmarks == null -> "No se detecto ninguna mano"
                    else -> "Intenta de nuevo. Precision: ${(result.confidence * 100).toInt()}%"
                }
                
                _uiState.value = _uiState.value.copy(
                    evaluationState = EvaluationState(
                        intentos = newIntentos,
                        vidas = newVidas,
                        maxIntentos = MAX_INTENTOS,
                        ultimaPrecision = result.confidence,
                        isEvaluating = false,
                        lastResult = result,
                        handDetectionResult = currentState.handDetectionResult,
                        feedbackMessage = feedbackMessage,
                        isCorrect = result.isCorrect
                    )
                )
                
                if (result.isCorrect) {
                    val leccionId = _uiState.value.leccionActual?.id ?: return@launch
                    completarLeccion(leccionId)
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    evaluationState = currentState.copy(
                        isEvaluating = false,
                        feedbackMessage = "Error al evaluar: ${e.message}"
                    )
                )
            }
        }
    }
    
    fun completarLeccion(leccionId: Int) {
        viewModelScope.launch {
            try {
                val userId = userSession.getUserId()
                val evaluationState = _uiState.value.evaluationState
                val puntuacion = (evaluationState.ultimaPrecision * 100).toInt()
                
                repository.completarLeccion(userId, leccionId, puntuacion, evaluationState.intentos)
                _uiState.value = _uiState.value.copy(showCompletadoDialog = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun ocultarDialogCompletado() {
        _uiState.value = _uiState.value.copy(showCompletadoDialog = false)
    }
    
    fun limpiarLeccionActual() {
        _uiState.value = _uiState.value.copy(
            leccionActual = null,
            senaActual = null,
            evaluationState = EvaluationState()
        )
    }
    
    fun clearFeedback() {
        _uiState.value = _uiState.value.copy(
            evaluationState = _uiState.value.evaluationState.copy(
                feedbackMessage = null,
                isCorrect = null
            )
        )
    }
    
    override fun onCleared() {
        super.onCleared()
        signRecognizer.close()
    }
}
