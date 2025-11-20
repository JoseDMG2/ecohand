package com.example.ecohand.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ecohand.data.local.entity.ActividadDiariaEntity

@Dao
interface ActividadDiariaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActividad(actividad: ActividadDiariaEntity): Long

    @Query("SELECT * FROM actividad_diaria WHERE usuarioId = :usuarioId ORDER BY fecha DESC")
    suspend fun getActividadByUsuario(usuarioId: Int): List<ActividadDiariaEntity>

    @Query("SELECT * FROM actividad_diaria WHERE usuarioId = :usuarioId AND fecha >= :fechaInicio AND fecha <= :fechaFin ORDER BY fecha ASC")
    suspend fun getActividadPorRango(usuarioId: Int, fechaInicio: Long, fechaFin: Long): List<ActividadDiariaEntity>

    @Query("SELECT COUNT(DISTINCT fecha) FROM actividad_diaria WHERE usuarioId = :usuarioId AND activo = 1")
    suspend fun getDiasActivosCount(usuarioId: Int): Int

    @Query("SELECT * FROM actividad_diaria WHERE usuarioId = :usuarioId AND fecha = :fecha LIMIT 1")
    suspend fun getActividadPorFecha(usuarioId: Int, fecha: Long): ActividadDiariaEntity?
}

