package com.example.ecohand.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.ecohand.data.local.entity.LogroUsuarioEntity

@Dao
interface LogroUsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogroUsuario(logroUsuario: LogroUsuarioEntity): Long

    @Update
    suspend fun updateLogroUsuario(logroUsuario: LogroUsuarioEntity)

    @Query("SELECT * FROM logros_usuario WHERE usuarioId = :usuarioId")
    suspend fun getLogrosByUsuario(usuarioId: Int): List<LogroUsuarioEntity>

    @Query("SELECT * FROM logros_usuario WHERE usuarioId = :usuarioId AND obtenido = 1")
    suspend fun getLogrosObtenidos(usuarioId: Int): List<LogroUsuarioEntity>

    @Query("SELECT * FROM logros_usuario WHERE usuarioId = :usuarioId AND logroId = :logroId LIMIT 1")
    suspend fun getLogroUsuario(usuarioId: Int, logroId: Int): LogroUsuarioEntity?
}

