package com.example.ecohand.data.repository

import com.example.ecohand.data.local.dao.LeccionDao
import com.example.ecohand.data.local.dao.ProgresoLeccionDao
import com.example.ecohand.data.local.entity.LeccionEntity
import com.example.ecohand.data.local.entity.ProgresoLeccionEntity

class LeccionRepository(
    private val leccionDao: LeccionDao,
    private val progresoLeccionDao: ProgresoLeccionDao
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
    
    suspend fun completarLeccion(usuarioId: Int, leccionId: Int, puntuacion: Int = 100, intentos: Int = 1) {
        val progresoExistente = progresoLeccionDao.getProgresoByUsuarioAndLeccion(usuarioId, leccionId)
        
        if (progresoExistente != null) {
            // Actualizar progreso existente
            val progresoActualizado = progresoExistente.copy(
                completada = true,
                fechaCompletado = System.currentTimeMillis(),
                ultimaActualizacion = System.currentTimeMillis(),
                puntuacion = puntuacion,
                intentos = progresoExistente.intentos + intentos
            )
            progresoLeccionDao.updateProgreso(progresoActualizado)
        } else {
            // Crear nuevo progreso
            val nuevoProgreso = ProgresoLeccionEntity(
                usuarioId = usuarioId,
                leccionId = leccionId,
                completada = true,
                puntuacion = puntuacion,
                intentos = intentos,
                fechaCompletado = System.currentTimeMillis()
            )
            progresoLeccionDao.insertProgreso(nuevoProgreso)
        }
    }
    
    suspend fun getLeccionesCompletadasCount(usuarioId: Int): Int {
        return progresoLeccionDao.getLeccionesCompletadasCount(usuarioId)
    }
    
    suspend fun getTotalLecciones(): Int {
        return leccionDao.getTotalLecciones()
    }
}
