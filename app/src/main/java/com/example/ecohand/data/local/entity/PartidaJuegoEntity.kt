package com.example.ecohand.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "partidas_juego",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("usuarioId")]
)
data class PartidaJuegoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Int,
    val desafiosCompletados: Int = 0,
    val desafiosCorrectos: Int = 0,
    val desafiosIncorrectos: Int = 0,
    val puntosGanados: Int = 0,
    val completada: Boolean = false,
    val fechaInicio: Long = System.currentTimeMillis(),
    val fechaFin: Long? = null
)

