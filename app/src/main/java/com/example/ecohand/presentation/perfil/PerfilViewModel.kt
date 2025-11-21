package com.example.ecohand.presentation.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecohand.data.repository.PerfilRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PerfilUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    
    // Información del usuario
    val username: String = "",
    val email: String = "",
    val imagenPerfil: String? = null, // Por ahora null, se usará una default
    
    // Estadísticas
    val puntosTotales: Int = 0,
    val leccionesCompletadas: Int = 0,
    val diasRacha: Int = 0
)

class PerfilViewModel(
    private val perfilRepository: PerfilRepository,
    private val usuarioId: Int
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()
    
    init {
        cargarPerfil()
    }
    
    /**
     * Carga los datos del perfil del usuario
     */
    fun cargarPerfil() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                
                // Obtener datos del usuario
                val usuario = perfilRepository.getUserById(usuarioId)
                
                if (usuario == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Usuario no encontrado"
                    )
                    return@launch
                }
                
                // Obtener estadísticas del usuario
                var estadisticas = perfilRepository.getEstadisticasUsuario(usuarioId)
                
                // Si no existen estadísticas, crear unas iniciales
                if (estadisticas == null) {
                    estadisticas = perfilRepository.crearEstadisticasIniciales(usuarioId)
                }
                
                // Actualizar UI
                _uiState.value = PerfilUiState(
                    isLoading = false,
                    username = usuario.username,
                    email = usuario.email,
                    puntosTotales = estadisticas.puntosTotal,
                    leccionesCompletadas = estadisticas.leccionesCompletadas,
                    diasRacha = estadisticas.rachaActual
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar el perfil: ${e.message}"
                )
            }
        }
    }
}
