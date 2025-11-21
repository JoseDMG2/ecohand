package com.example.ecohand.presentation.progreso

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecohand.data.local.entity.LogroEntity
import com.example.ecohand.data.repository.ProgresoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class ProgresoUiState(
    val isLoading: Boolean = true,
    val leccionesCompletadas: Int = 0,
    val totalLecciones: Int = 5,
    val porcentajeProgreso: Float = 0f,
    val puntosTotal: Int = 0,
    val rachaActual: Int = 0,
    val leccionesCount: Int = 0,
    val diasActivos: Int = 0,
    val actividadSemanal: List<DiaActividad> = emptyList(),
    val logros: List<LogroConEstado> = emptyList(),
    val errorMessage: String? = null
)

data class DiaActividad(
    val dia: String, // L, M, X, J, V, S, D
    val activo: Boolean,
    val fecha: Long
)

data class LogroConEstado(
    val logro: LogroEntity,
    val obtenido: Boolean,
    val fechaObtenido: Long?
)

class ProgresoViewModel(
    private val progresoRepository: ProgresoRepository,
    private val usuarioId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgresoUiState())
    val uiState: StateFlow<ProgresoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            cargarProgreso()
        }
    }

    suspend fun cargarProgreso() {
        try {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // Registrar actividad del día
            progresoRepository.registrarActividadDiaria(usuarioId)

            // Obtener estadísticas
            val estadisticas = progresoRepository.getOrCreateEstadisticas(usuarioId)

            // Obtener lecciones
            val totalLecciones = progresoRepository.getTotalLecciones()
            val leccionesCompletadas = progresoRepository.getLeccionesCompletadas(usuarioId)
            val porcentaje = if (totalLecciones > 0) {
                (leccionesCompletadas.toFloat() / totalLecciones.toFloat())
            } else 0f

            // Obtener actividad semanal
            val actividadSemanal = obtenerActividadSemanal()

            // Obtener logros
            val logrosData = progresoRepository.getLogrosUsuario(usuarioId)
            val logros = logrosData.map { (logro, logroUsuario) ->
                LogroConEstado(
                    logro = logro,
                    obtenido = logroUsuario.obtenido,
                    fechaObtenido = logroUsuario.fechaObtenido
                )
            }

            // Verificar logros
            progresoRepository.verificarLogros(usuarioId)

            _uiState.value = ProgresoUiState(
                isLoading = false,
                leccionesCompletadas = leccionesCompletadas,
                totalLecciones = totalLecciones,
                porcentajeProgreso = porcentaje,
                puntosTotal = estadisticas.puntosTotal,
                rachaActual = estadisticas.rachaActual,
                leccionesCount = leccionesCompletadas,
                diasActivos = estadisticas.diasActivos,
                actividadSemanal = actividadSemanal,
                logros = logros
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Error al cargar el progreso: ${e.message}"
            )
        }
    }

    private suspend fun obtenerActividadSemanal(): List<DiaActividad> {
        val actividades = progresoRepository.getActividadSemanal(usuarioId)
        val calendar = Calendar.getInstance()

        // Obtener el inicio de la semana (Lunes)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val diasSemana = listOf("L", "M", "X", "J", "V", "S", "D")
        val resultado = mutableListOf<DiaActividad>()

        for (i in 0..6) {
            val fecha = calendar.timeInMillis
            val activo = actividades.any { it.fecha == fecha && it.activo }

            resultado.add(
                DiaActividad(
                    dia = diasSemana[i],
                    activo = activo,
                    fecha = fecha
                )
            )

            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return resultado
    }
}

