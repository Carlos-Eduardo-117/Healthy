package com.example.healthyapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TomaMedicamento(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val medicamentoId: Int,
    val fechaHora: String
)

