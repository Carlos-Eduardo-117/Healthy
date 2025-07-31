package com.example.healthyapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Medicamento(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val dosis: String,
    val hora: String,
    val tomado: Boolean = false
)
