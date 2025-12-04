package com.example.ecohand.data.repository

import com.example.ecohand.data.local.dao.EstadisticasUsuarioDao
import com.example.ecohand.data.local.dao.LeccionDao
import com.example.ecohand.data.local.dao.ProgresoLeccionDao
import com.example.ecohand.data.local.entity.LeccionEntity
import com.example.ecohand.data.local.entity.ProgresoLeccionEntity

class LeccionRepository(
    private val leccionDao: LeccionDao,
    private val progresoLeccionDao: ProgresoLeccionDao,
    private val estadisticasUsuarioDao: EstadisticasUsuarioDao
) {
    
    suspend fun getAllLecciones(): List<LeccionEntity> {
        return leccionDao.getAllLecciones()
    }
    
    suspend fun getLeccionById(leccionId: Int): LeccionEntity? {
        return leccionDao.getLeccionById(leccionId)
    }
    
    suspend fun getProgresoLeccion(usuarioId: Int, leccionId: Int): ProgresoLeccionEntity? {
        return progresoLeccionDao.getProgresoByUsuarioAndLeccion(usuarioId, leccionId)
    }
    
    suspend fun getProgresoUsuario(usuarioId: Int): List<ProgresoLeccionEntity> {
        return progresoLeccionDao.getProgresoByUsuario(usuarioId)
    }
    
    suspend fun completarLeccion(usuarioId: Int, leccionId: Int) {
        val progresoExistente = progresoLeccionDao.getProgresoByUsuarioAndLeccion(usuarioId, leccionId)
        
        if (progresoExistente != null) {
            // Actualizar progreso existente
            val progresoActualizado = progresoExistente.copy(
                completada = true,
                fechaCompletado = System.currentTimeMillis(),
                ultimaActualizacion = System.currentTimeMillis(),
                puntuacion = 100
            )
            progresoLeccionDao.updateProgreso(progresoActualizado)
        } else {
            // Crear nuevo progreso
            val nuevoProgreso = ProgresoLeccionEntity(
                usuarioId = usuarioId,
                leccionId = leccionId,
                completada = true,
                puntuacion = 100,
                fechaCompletado = System.currentTimeMillis()
            )
            progresoLeccionDao.insertProgreso(nuevoProgreso)
        }

        // Actualizar estadísticas del usuario
        actualizarEstadisticas(usuarioId)
    }

    // Actualizar estadísticas después de completar una lección
    private suspend fun actualizarEstadisticas(usuarioId: Int) {
        val estadisticas = estadisticasUsuarioDao.getEstadisticasByUsuario(usuarioId)
        if (estadisticas != null) {
            val leccionesCompletadas = getLeccionesCompletadasCount(usuarioId)
            val puntosTotales = progresoLeccionDao.getTotalPuntos(usuarioId) ?: 0

            val estadisticasActualizadas = estadisticas.copy(
                puntosTotal = puntosTotales,
                leccionesCompletadas = leccionesCompletadas,
                ultimaActualizacion = System.currentTimeMillis()
            )
            estadisticasUsuarioDao.updateEstadisticas(estadisticasActualizadas)
        }
    }
    
    suspend fun getLeccionesCompletadasCount(usuarioId: Int): Int {
        return progresoLeccionDao.getLeccionesCompletadasCount(usuarioId)
    }
    
    suspend fun getTotalLecciones(): Int {
        return leccionDao.getTotalLecciones()
    }
}
