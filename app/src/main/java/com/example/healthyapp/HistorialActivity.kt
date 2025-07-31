package com.example.healthyapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class HistorialActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var tomaDao: TomaDao
    private lateinit var medicamentoDao: MedicamentoDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerHistorial)
        recyclerView.layoutManager = LinearLayoutManager(this)

        db = AppDatabase.getDatabase(this)
        tomaDao = db.tomaDao()
        medicamentoDao = db.medicamentoDao()

        lifecycleScope.launch {
            val historial = tomaDao.obtenerHistorial()
            val medicamentos = medicamentoDao.obtenerTodos()
            val mapaNombres = medicamentos.associateBy({ it.id }, { it.nombre })

            runOnUiThread {
                recyclerView.adapter = HistorialAdapter(historial, mapaNombres)
            }
        }
    }
}
