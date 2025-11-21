package com.example.ecohand.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.ecohand.data.local.entity.ProgresoLeccionEntity

@Dao
interface ProgresoLeccionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgreso(progreso: ProgresoLeccionEntity): Long

    @Update
    suspend fun updateProgreso(progreso: ProgresoLeccionEntity)

    @Query("SELECT * FROM progreso_lecciones WHERE usuarioId = :usuarioId")
    suspend fun getProgresoByUsuario(usuarioId: Int): List<ProgresoLeccionEntity>

    @Query("SELECT * FROM progreso_lecciones WHERE usuarioId = :usuarioId AND leccionId = :leccionId LIMIT 1")
    suspend fun getProgresoByUsuarioAndLeccion(usuarioId: Int, leccionId: Int): ProgresoLeccionEntity?

    @Query("SELECT COUNT(*) FROM progreso_lecciones WHERE usuarioId = :usuarioId AND completada = 1")
    suspend fun getLeccionesCompletadasCount(usuarioId: Int): Int

    @Query("SELECT SUM(puntuacion) FROM progreso_lecciones WHERE usuarioId = :usuarioId")
    suspend fun getTotalPuntos(usuarioId: Int): Int?
}

