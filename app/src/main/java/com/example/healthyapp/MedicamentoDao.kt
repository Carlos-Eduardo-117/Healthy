package com.example.healthyapp

import androidx.room.*

@Dao
interface MedicamentoDao {
    @Insert
    suspend fun insertar(med: Medicamento)

    @Update
    suspend fun actualizar(med: Medicamento)

    @Delete
    suspend fun eliminar(med: Medicamento)

    @Query("SELECT * FROM Medicamento ORDER BY hora")
    suspend fun obtenerTodos(): List<Medicamento>

    @Query("SELECT * FROM Medicamento WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): Medicamento?

}