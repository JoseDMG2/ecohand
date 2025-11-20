package com.example.ecohand.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "progreso_lecciones",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LeccionEntity::class,
            parentColumns = ["id"],
            childColumns = ["leccionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("usuarioId"), Index("leccionId")]
)
data class ProgresoLeccionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Int,
    val leccionId: Int,
    val completada: Boolean = false,
    val puntuacion: Int = 0, // 0-100
    val intentos: Int = 0,
    val fechaInicio: Long = System.currentTimeMillis(),
    val fechaCompletado: Long? = null,
    val ultimaActualizacion: Long = System.currentTimeMillis()
)

