package com.example.ecohand.presentation.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecohand.data.repository.PerfilRepository
import kotlinx.coroutines.Dispatchers
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
        cargarPerfilInicial()
    }

    private fun cargarPerfilInicial() {
        viewModelScope.launch(Dispatchers.IO) {
            cargarPerfil()
        }
    }

    /**
     * Método público para recargar el perfil
     */
    fun recargarPerfil() {
        viewModelScope.launch(Dispatchers.IO) {
            cargarPerfil()
        }
    }

    /**
     * Carga los datos del perfil del usuario
     */
    private suspend fun cargarPerfil() {
        try {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // Obtener datos del usuario
            val usuario = perfilRepository.getUserById(usuarioId)

            if (usuario == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Usuario no encontrado"
                )
                return
            }

            // Obtener estadísticas actualizadas directamente de la BD
            val estadisticas = perfilRepository.getEstadisticasActualizadas(usuarioId)

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