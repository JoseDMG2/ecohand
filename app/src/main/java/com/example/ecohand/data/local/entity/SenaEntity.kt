package com.example.ecohand.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "senas")
data class SenaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val imagenResource: String,
    val categoria: String = "GENERAL",
    val dificultad: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    // ML Detection fields
    val landmarksData: String? = null,  // JSON string with reference landmarks
    val detectionThreshold: Float = 0.85f,  // Minimum confidence for correct detection
    val minConfidence: Float = 0.5f  // Minimum hand detection confidence
)


