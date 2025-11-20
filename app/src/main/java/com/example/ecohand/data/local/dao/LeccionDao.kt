package com.example.ecohand.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.ecohand.data.local.entity.LeccionEntity

@Dao
interface LeccionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeccion(leccion: LeccionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lecciones: List<LeccionEntity>)

    @Update
    suspend fun updateLeccion(leccion: LeccionEntity)

    @Query("SELECT * FROM lecciones ORDER BY orden ASC")
    suspend fun getAllLecciones(): List<LeccionEntity>

    @Query("SELECT * FROM lecciones WHERE id = :leccionId LIMIT 1")
    suspend fun getLeccionById(leccionId: Int): LeccionEntity?

    @Query("SELECT COUNT(*) FROM lecciones")
    suspend fun getTotalLecciones(): Int
}

