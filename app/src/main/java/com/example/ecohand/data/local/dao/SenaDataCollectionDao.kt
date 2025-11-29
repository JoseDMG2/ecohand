package com.example.ecohand.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ecohand.data.local.entity.SenaDataCollectionEntity

@Dao
interface SenaDataCollectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sample: SenaDataCollectionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(samples: List<SenaDataCollectionEntity>)

    @Query("SELECT * FROM sena_data_collection WHERE senaId = :senaId ORDER BY createdAt DESC")
    suspend fun getSamplesBySenaId(senaId: Int): List<SenaDataCollectionEntity>

    @Query("SELECT * FROM sena_data_collection WHERE usuarioId = :usuarioId ORDER BY createdAt DESC")
    suspend fun getSamplesByUsuarioId(usuarioId: Int): List<SenaDataCollectionEntity>

    @Query("SELECT * FROM sena_data_collection WHERE isValidated = :validated ORDER BY createdAt DESC")
    suspend fun getSamplesByValidation(validated: Boolean): List<SenaDataCollectionEntity>

    @Query("SELECT * FROM sena_data_collection ORDER BY createdAt DESC")
    suspend fun getAllSamples(): List<SenaDataCollectionEntity>

    @Query("SELECT COUNT(*) FROM sena_data_collection WHERE senaId = :senaId")
    suspend fun getSampleCountForSena(senaId: Int): Int

    @Query("SELECT COUNT(*) FROM sena_data_collection")
    suspend fun getTotalSampleCount(): Int

    @Query("UPDATE sena_data_collection SET isValidated = :validated WHERE id = :sampleId")
    suspend fun updateValidation(sampleId: Int, validated: Boolean)

    @Query("DELETE FROM sena_data_collection WHERE id = :sampleId")
    suspend fun deleteSample(sampleId: Int)

    @Query("DELETE FROM sena_data_collection WHERE senaId = :senaId")
    suspend fun deleteSamplesForSena(senaId: Int)
}
