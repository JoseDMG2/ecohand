package com.example.ecohand.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ecohand.data.local.entity.LogroEntity

@Dao
interface LogroDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogro(logro: LogroEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logros: List<LogroEntity>)

    @Query("SELECT * FROM logros ORDER BY id ASC")
    suspend fun getAllLogros(): List<LogroEntity>

    @Query("SELECT * FROM logros WHERE id = :logroId LIMIT 1")
    suspend fun getLogroById(logroId: Int): LogroEntity?
}

