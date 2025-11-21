package com.example.ecohand.data.repository

import com.example.ecohand.data.local.dao.*
import com.example.ecohand.data.local.entity.*
import java.util.Calendar

class ProgresoRepository(
    private val estadisticasUsuarioDao: EstadisticasUsuarioDao,
    private val progresoLeccionDao: ProgresoLeccionDao,
    private val actividadDiariaDao: ActividadDiariaDao,
    private val logroDao: LogroDao,
    private val logroUsuarioDao: LogroUsuarioDao,
    private val leccionDao: LeccionDao
) {

    // Obtener o crear estadísticas del usuario
    suspend fun getOrCreateEstadisticas(usuarioId: Int): EstadisticasUsuarioEntity {
        return estadisticasUsuarioDao.getEstadisticasByUsuario(usuarioId)
            ?: EstadisticasUsuarioEntity(usuarioId = usuarioId).also {
                estadisticasUsuarioDao.insertEstadisticas(it)
            }
    }

    // Actualizar estadísticas del usuario
    suspend fun updateEstadisticas(estadisticas: EstadisticasUsuarioEntity) {
        estadisticasUsuarioDao.updateEstadisticas(estadisticas)
    }

    // Actualizar estadísticas después de completar una lección
    suspend fun actualizarEstadisticasLeccion(usuarioId: Int, puntos: Int = 100) {
        val estadisticas = getOrCreateEstadisticas(usuarioId)
        val leccionesCompletadas = getLeccionesCompletadas(usuarioId)
        val puntosTotales = progresoLeccionDao.getTotalPuntos(usuarioId) ?: 0

        val estadisticasActualizadas = estadisticas.copy(
            puntosTotal = puntosTotales,
            leccionesCompletadas = leccionesCompletadas,
            ultimaActualizacion = System.currentTimeMillis()
        )
        updateEstadisticas(estadisticasActualizadas)
    }

    // Obtener progreso de lecciones del usuario
    suspend fun getProgresoLecciones(usuarioId: Int): List<ProgresoLeccionEntity> {
        return progresoLeccionDao.getProgresoByUsuario(usuarioId)
    }

    // Obtener total de lecciones
    suspend fun getTotalLecciones(): Int {
        return leccionDao.getTotalLecciones()
    }

    // Obtener lecciones completadas
    suspend fun getLeccionesCompletadas(usuarioId: Int): Int {
        return progresoLeccionDao.getLeccionesCompletadasCount(usuarioId)
    }

    // Obtener actividad semanal
    suspend fun getActividadSemanal(usuarioId: Int): List<ActividadDiariaEntity> {
        val calendar = Calendar.getInstance()

        // Obtener el inicio de la semana (Lunes)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val inicioSemana = calendar.timeInMillis

        // Obtener el fin de la semana (Domingo)
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val finSemana = calendar.timeInMillis

        return actividadDiariaDao.getActividadPorRango(usuarioId, inicioSemana, finSemana)
    }

    // Registrar actividad del día
    suspend fun registrarActividadDiaria(usuarioId: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val hoy = calendar.timeInMillis

        // Verificar si ya existe actividad para hoy
        val actividadExistente = actividadDiariaDao.getActividadPorFecha(usuarioId, hoy)
        if (actividadExistente == null) {
            val nuevaActividad = ActividadDiariaEntity(
                usuarioId = usuarioId,
                fecha = hoy,
                activo = true
            )
            actividadDiariaDao.insertActividad(nuevaActividad)

            // Actualizar días activos
            val estadisticas = getOrCreateEstadisticas(usuarioId)
            val diasActivos = actividadDiariaDao.getDiasActivosCount(usuarioId)

            // Calcular racha
            val racha = calcularRacha(usuarioId)

            val estadisticasActualizadas = estadisticas.copy(
                diasActivos = diasActivos,
                rachaActual = racha,
                rachaMayor = maxOf(estadisticas.rachaMayor, racha),
                ultimaFechaActiva = hoy,
                ultimaActualizacion = System.currentTimeMillis()
            )
            updateEstadisticas(estadisticasActualizadas)
        }
    }

    // Calcular racha actual
    private suspend fun calcularRacha(usuarioId: Int): Int {
        val actividades = actividadDiariaDao.getActividadByUsuario(usuarioId)
        if (actividades.isEmpty()) return 0

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        var racha = 0
        var fechaActual = calendar.timeInMillis

        for (actividad in actividades) {
            if (actividad.fecha == fechaActual) {
                racha++
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                fechaActual = calendar.timeInMillis
            } else {
                break
            }
        }

        return racha
    }

    // Obtener logros del usuario
    suspend fun getLogrosUsuario(usuarioId: Int): List<Pair<LogroEntity, LogroUsuarioEntity>> {
        val logrosUsuario = logroUsuarioDao.getLogrosByUsuario(usuarioId)
        val logros = logroDao.getAllLogros()

        // Si el usuario no tiene logros inicializados, crearlos
        if (logrosUsuario.isEmpty() && logros.isNotEmpty()) {
            logros.forEach { logro ->
                logroUsuarioDao.insertLogroUsuario(
                    LogroUsuarioEntity(
                        usuarioId = usuarioId,
                        logroId = logro.id,
                        obtenido = false
                    )
                )
            }
            return getLogrosUsuario(usuarioId)
        }

        return logrosUsuario.mapNotNull { logroUsuario ->
            val logro = logros.find { it.id == logroUsuario.logroId }
            logro?.let { it to logroUsuario }
        }
    }

    // Verificar y otorgar logros
    suspend fun verificarLogros(usuarioId: Int) {
        val estadisticas = getOrCreateEstadisticas(usuarioId)
        val logrosUsuario = logroUsuarioDao.getLogrosByUsuario(usuarioId)

        logrosUsuario.forEach { logroUsuario ->
            if (!logroUsuario.obtenido) {
                val cumpleRequisito = when (logroUsuario.logroId) {
                    1 -> estadisticas.leccionesCompletadas >= 1 // Primer Paso
                    2 -> estadisticas.rachaActual >= 7 // En Racha
                    3, 4, 5, 6 -> verificarLeccionEspecifica(usuarioId, logroUsuario.logroId - 2)
                    7 -> estadisticas.leccionesCompletadas >= 3 // Estudiante Dedicado
                    8 -> estadisticas.leccionesCompletadas >= 5 // Maestro EcoHand
                    else -> false
                }

                if (cumpleRequisito) {
                    logroUsuarioDao.updateLogroUsuario(
                        logroUsuario.copy(
                            obtenido = true,
                            fechaObtenido = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    private suspend fun verificarLeccionEspecifica(usuarioId: Int, leccionId: Int): Boolean {
        val progreso = progresoLeccionDao.getProgresoByUsuarioAndLeccion(usuarioId, leccionId)
        return progreso?.completada == true
    }
}

