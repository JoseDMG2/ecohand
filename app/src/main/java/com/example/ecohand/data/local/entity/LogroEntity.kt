package com.example.ecohand.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "logros")
data class LogroEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val descripcion: String,
    val emoji: String,
    val icono: String? = null,
    val requisito: String, // Descripción del requisito para obtenerlo
    val createdAt: Long = System.currentTimeMillis()
)

