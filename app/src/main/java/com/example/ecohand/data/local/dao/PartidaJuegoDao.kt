package com.example.ecohand.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.ecohand.data.local.entity.PartidaJuegoEntity

@Dao
interface PartidaJuegoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPartida(partida: PartidaJuegoEntity): Long

    @Update
    suspend fun updatePartida(partida: PartidaJuegoEntity)

    @Query("SELECT * FROM partidas_juego WHERE usuarioId = :usuarioId ORDER BY fechaInicio DESC")
    suspend fun getPartidasByUsuario(usuarioId: Int): List<PartidaJuegoEntity>

    @Query("SELECT * FROM partidas_juego WHERE id = :partidaId LIMIT 1")
    suspend fun getPartidaById(partidaId: Int): PartidaJuegoEntity?

    @Query("SELECT COUNT(*) FROM partidas_juego WHERE usuarioId = :usuarioId AND completada = 1")
    suspend fun getPartidasCompletadas(usuarioId: Int): Int

    @Query("SELECT SUM(puntosGanados) FROM partidas_juego WHERE usuarioId = :usuarioId")
    suspend fun getTotalPuntosJuegos(usuarioId: Int): Int?
}

