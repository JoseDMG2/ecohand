package com.example.ecohand.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lecciones")
data class LeccionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,
    val descripcion: String,
    val nivel: String,
    val orden: Int,
    val icono: String? = null,
    val bloqueada: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

