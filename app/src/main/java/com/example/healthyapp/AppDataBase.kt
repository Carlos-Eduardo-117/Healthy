package com.example.healthyapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Medicamento::class, TomaMedicamento::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicamentoDao(): MedicamentoDao
    abstract fun tomaDao(): TomaDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "medicamentos_db"
                )
                    .fallbackToDestructiveMigration() //Aqui se Recrea la DB si cambian entidades
                    .build()
            }
            return INSTANCE!!
        }

    }
}
