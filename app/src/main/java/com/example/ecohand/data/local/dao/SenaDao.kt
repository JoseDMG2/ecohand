package com.example.ecohand.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.ecohand.data.local.entity.SenaEntity

@Dao
interface SenaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSena(sena: SenaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(senas: List<SenaEntity>)

    @Update
    suspend fun updateSena(sena: SenaEntity)

    @Query("SELECT * FROM senas ORDER BY RANDOM() LIMIT :cantidad")
    suspend fun getSenasAleatorias(cantidad: Int): List<SenaEntity>

    @Query("SELECT * FROM senas WHERE id = :senaId LIMIT 1")
    suspend fun getSenaById(senaId: Int): SenaEntity?

    @Query("SELECT * FROM senas WHERE nombre = :nombre LIMIT 1")
    suspend fun getSenaByNombre(nombre: String): SenaEntity?

    @Query("SELECT * FROM senas")
    suspend fun getAllSenas(): List<SenaEntity>

    @Query("SELECT COUNT(*) FROM senas")
    suspend fun getTotalSenas(): Int

    @Query("SELECT * FROM senas WHERE landmarksData IS NOT NULL")
    suspend fun getSenasWithLandmarks(): List<SenaEntity>

    @Query("UPDATE senas SET landmarksData = :landmarksData WHERE id = :senaId")
    suspend fun updateLandmarksData(senaId: Int, landmarksData: String)
}


