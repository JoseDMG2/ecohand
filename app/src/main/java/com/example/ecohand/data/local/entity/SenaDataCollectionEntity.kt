package com.example.ecohand.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity for storing collected sign language data samples.
 * Used for training and improving sign recognition.
 */
@Entity(
    tableName = "sena_data_collection",
    foreignKeys = [
        ForeignKey(
            entity = SenaEntity::class,
            parentColumns = ["id"],
            childColumns = ["senaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("senaId"), Index("usuarioId")]
)
data class SenaDataCollectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val senaId: Int,
    val usuarioId: Int,
    val landmarksData: String,  // JSON string with hand landmarks
    val imageWidth: Int,
    val imageHeight: Int,
    val handedness: String,  // LEFT, RIGHT, UNKNOWN
    val confidence: Float,
    val isValidated: Boolean = false,  // Whether this sample has been verified
    val createdAt: Long = System.currentTimeMillis(),
    val notes: String? = null  // Optional notes about the sample
)
