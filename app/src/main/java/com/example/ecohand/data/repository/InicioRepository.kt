package com.example.ecohand.data.repository

import com.example.ecohand.data.local.dao.EstadisticasUsuarioDao
import com.example.ecohand.data.local.dao.LeccionDao
import com.example.ecohand.data.local.dao.ProgresoLeccionDao
import com.example.ecohand.data.local.entity.EstadisticasUsuarioEntity
import com.example.ecohand.data.local.entity.LeccionEntity

class InicioRepository(
    private val estadisticasUsuarioDao: EstadisticasUsuarioDao,
    private val leccionDao: LeccionDao,
    private val progresoLeccionDao: ProgresoLeccionDao
) {
    
    suspend fun getEstadisticasUsuario(usuarioId: Int): EstadisticasUsuarioEntity? {
        return estadisticasUsuarioDao.getEstadisticasByUsuario(usuarioId)
    }
    
    suspend fun getTotalLecciones(): Int {
        return leccionDao.getTotalLecciones()
    }
    
    suspend fun getLeccionesCompletadas(usuarioId: Int): Int {
        return progresoLeccionDao.getLeccionesCompletadasCount(usuarioId)
    }
    
    suspend fun getSiguienteLeccion(usuarioId: Int): LeccionEntity? {
        val todasLasLecciones = leccionDao.getAllLecciones()
        val progreso = progresoLeccionDao.getProgresoByUsuario(usuarioId)
        val completadas = progreso.filter { it.completada }.map { it.leccionId }.toSet()
        
        // Buscar la primera lección no completada
        return todasLasLecciones.firstOrNull { !completadas.contains(it.id) }
    }
}
