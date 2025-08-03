package com.example.healthyapp

import androidx.room.*

@Dao
interface MedicamentoDao {

    @Insert
    suspend fun insertar(medicamento: Medicamento): Long

    @Update
    suspend fun actualizar(medicamento: Medicamento)

    @Delete
    suspend fun eliminar(medicamento: Medicamento)

    @Query("SELECT * FROM Medicamento ORDER BY hora")
    suspend fun obtenerTodos(): List<Medicamento>

    @Query("SELECT * FROM Medicamento WHERE id = :id")
    suspend fun obtenerPorId(id: Int): Medicamento?
}
