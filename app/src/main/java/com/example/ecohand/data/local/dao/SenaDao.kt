package com.example.ecohand.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ecohand.data.local.entity.SenaEntity

@Dao
interface SenaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSena(sena: SenaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(senas: List<SenaEntity>)

    @Query("SELECT * FROM senas ORDER BY RANDOM() LIMIT :cantidad")
    suspend fun getSenasAleatorias(cantidad: Int): List<SenaEntity>

    @Query("SELECT * FROM senas WHERE id = :senaId LIMIT 1")
    suspend fun getSenaById(senaId: Int): SenaEntity?

    @Query("SELECT * FROM senas")
    suspend fun getAllSenas(): List<SenaEntity>

    @Query("SELECT COUNT(*) FROM senas")
    suspend fun getTotalSenas(): Int
}

