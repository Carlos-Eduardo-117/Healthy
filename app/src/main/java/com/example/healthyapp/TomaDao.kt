package com.example.healthyapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TomaDao {
    @Insert
    suspend fun registrarToma(toma: TomaMedicamento)

    @Query("SELECT * FROM TomaMedicamento ORDER BY fechaHora DESC")
    suspend fun obtenerHistorial(): List<TomaMedicamento>
}
