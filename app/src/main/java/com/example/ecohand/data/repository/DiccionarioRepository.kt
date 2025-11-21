package com.example.ecohand.data.repository

import com.example.ecohand.data.local.dao.SenaDao
import com.example.ecohand.data.local.entity.SenaEntity

class DiccionarioRepository(
    private val senaDao: SenaDao
) {
    
    /**
     * Obtiene todas las señas ordenadas alfabéticamente
     */
    suspend fun getAllSenas(): List<SenaEntity> {
        return senaDao.getSenasOrdenadas()
    }
    
    /**
     * Busca señas por nombre
     */
    suspend fun searchSenas(query: String): List<SenaEntity> {
        return if (query.isBlank()) {
            senaDao.getSenasOrdenadas()
        } else {
            senaDao.searchSenas(query)
        }
    }
    
    /**
     * Obtiene una seña por ID
     */
    suspend fun getSenaById(id: Int): SenaEntity? {
        return senaDao.getSenaById(id)
    }
}
