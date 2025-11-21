package com.example.ecohand.presentation.lecciones

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecohand.data.local.database.EcoHandDatabase
import com.example.ecohand.data.local.entity.LeccionEntity
import com.example.ecohand.data.repository.LeccionRepository
import com.example.ecohand.data.session.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LeccionUIState(
    val lecciones: List<LeccionEntity> = emptyList(),
    val leccionesCompletadas: Set<Int> = emptySet(),
    val leccionActual: LeccionEntity? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showCompletadoDialog: Boolean = false
)

class LeccionesViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: LeccionRepository
    private val userSession: UserSession = UserSession.getInstance(application)
    
    private val _uiState = MutableStateFlow(LeccionUIState())
    val uiState: StateFlow<LeccionUIState> = _uiState.asStateFlow()
    
    init {
        val database = EcoHandDatabase.getDatabase(application)
        repository = LeccionRepository(
            database.leccionDao(),
            database.progresoLeccionDao(),
            database.estadisticasUsuarioDao()
        )
        cargarLecciones()
    }
    
    fun cargarLecciones() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val userId = userSession.getUserId()
                val todasLasLecciones = repository.getAllLecciones()
                val progreso = repository.getProgresoUsuario(userId)
                val completadas = progreso.filter { it.completada }.map { it.leccionId }.toSet()
                
                // Determinar qué lecciones están desbloqueadas
                val leccionesConEstado = todasLasLecciones.map { leccion ->
                    val estaCompletada = completadas.contains(leccion.id)
                    val leccionAnteriorCompletada = if (leccion.orden > 1) {
                        val leccionAnterior = todasLasLecciones.find { it.orden == leccion.orden - 1 }
                        leccionAnterior?.let { completadas.contains(it.id) } ?: false
                    } else {
                        true // La primera lección siempre está desbloqueada
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
                _uiState.value = _uiState.value.copy(leccionActual = leccion)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun completarLeccion(leccionId: Int) {
        viewModelScope.launch {
            try {
                val userId = userSession.getUserId()
                repository.completarLeccion(userId, leccionId)
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
        _uiState.value = _uiState.value.copy(leccionActual = null)
    }
}
