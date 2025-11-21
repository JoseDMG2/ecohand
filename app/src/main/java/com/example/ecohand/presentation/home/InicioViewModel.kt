package com.example.ecohand.presentation.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecohand.data.local.database.EcoHandDatabase
import com.example.ecohand.data.local.entity.EstadisticasUsuarioEntity
import com.example.ecohand.data.local.entity.LeccionEntity
import com.example.ecohand.data.repository.InicioRepository
import com.example.ecohand.data.session.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InicioUIState(
    val nombreUsuario: String = "",
    val puntosTotal: Int = 0,
    val leccionesCompletadas: Int = 0,
    val totalLecciones: Int = 0,
    val rachaActual: Int = 0,
    val siguienteLeccion: LeccionEntity? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class InicioViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: InicioRepository
    private val userSession: UserSession = UserSession.getInstance(application)
    
    private val _uiState = MutableStateFlow(InicioUIState())
    val uiState: StateFlow<InicioUIState> = _uiState.asStateFlow()
    
    init {
        val database = EcoHandDatabase.getDatabase(application)
        repository = InicioRepository(
            database.estadisticasUsuarioDao(),
            database.leccionDao(),
            database.progresoLeccionDao()
        )
        cargarDatos()
    }
    
    fun cargarDatos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val userId = userSession.getUserId()
                val username = userSession.getUsername() ?: "Usuario"
                
                // Obtener estadísticas
                val estadisticas = repository.getEstadisticasUsuario(userId)
                    ?: EstadisticasUsuarioEntity(usuarioId = userId)
                
                // Obtener lecciones
                val totalLecciones = repository.getTotalLecciones()
                val leccionesCompletadas = repository.getLeccionesCompletadas(userId)
                
                // Obtener siguiente lección
                val siguienteLeccion = repository.getSiguienteLeccion(userId)
                
                _uiState.value = _uiState.value.copy(
                    nombreUsuario = username,
                    puntosTotal = estadisticas.puntosTotal,
                    leccionesCompletadas = leccionesCompletadas,
                    totalLecciones = totalLecciones,
                    rachaActual = estadisticas.rachaActual,
                    siguienteLeccion = siguienteLeccion,
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
}
