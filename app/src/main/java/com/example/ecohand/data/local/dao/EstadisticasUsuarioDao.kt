package com.example.ecohand.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.ecohand.data.local.entity.EstadisticasUsuarioEntity

@Dao
interface EstadisticasUsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEstadisticas(estadisticas: EstadisticasUsuarioEntity): Long

    @Update
    suspend fun updateEstadisticas(estadisticas: EstadisticasUsuarioEntity)

    @Query("SELECT * FROM estadisticas_usuario WHERE usuarioId = :usuarioId LIMIT 1")
    suspend fun getEstadisticasByUsuario(usuarioId: Int): EstadisticasUsuarioEntity?
}

