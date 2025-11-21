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
    val createdAt: Long = System.currentTimeMillis()
)

