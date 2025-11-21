package com.example.ecohand.data.repository

import com.example.ecohand.data.local.dao.EstadisticasUsuarioDao
import com.example.ecohand.data.local.dao.UserDao
import com.example.ecohand.data.local.entity.EstadisticasUsuarioEntity
import com.example.ecohand.data.local.entity.UserEntity

class PerfilRepository(
    private val userDao: UserDao,
    private val estadisticasUsuarioDao: EstadisticasUsuarioDao
) {
    
    /**
     * Obtiene los datos del usuario por su ID
     */
    suspend fun getUserById(userId: Int): UserEntity? {
        return userDao.getUserById(userId)
    }
    
    /**
     * Obtiene las estadísticas del usuario
     */
    suspend fun getEstadisticasUsuario(userId: Int): EstadisticasUsuarioEntity? {
        return estadisticasUsuarioDao.getEstadisticasByUsuario(userId)
    }
    
    /**
     * Crea estadísticas iniciales para un usuario si no existen
     */
    suspend fun crearEstadisticasIniciales(userId: Int): EstadisticasUsuarioEntity {
        val estadisticas = EstadisticasUsuarioEntity(
            usuarioId = userId,
            puntosTotal = 0,
            rachaActual = 0,
            rachaMayor = 0,
            leccionesCompletadas = 0,
            diasActivos = 0,
            ultimaFechaActiva = null,
            ultimaActualizacion = System.currentTimeMillis()
        )
        estadisticasUsuarioDao.insertEstadisticas(estadisticas)
        return estadisticas
    }
}
