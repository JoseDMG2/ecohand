package com.example.ecohand.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "estadisticas_usuario",
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
data class EstadisticasUsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Int,
    val puntosTotal: Int = 0,
    val rachaActual: Int = 0,
    val rachaMayor: Int = 0,
    val leccionesCompletadas: Int = 0,
    val diasActivos: Int = 0,
    val ultimaFechaActiva: Long? = null,
    val ultimaActualizacion: Long = System.currentTimeMillis()
)

