package com.example.healthyapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
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

        // Configurar botón regresar
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish() // Regresa a la actividad anterior
        }

        // Configurar botón cerrar sesión
        val btnLogout = findViewById<ImageButton>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            cerrarSesion()
        }


        lifecycleScope.launch {
            val historial = tomaDao.obtenerHistorial()
            val medicamentos = medicamentoDao.obtenerTodos()
            val mapaNombres = medicamentos.associateBy({ it.id }, { it.nombre })

            runOnUiThread {
                recyclerView.adapter = HistorialAdapter(historial, mapaNombres)
            }
        }
    }

    private fun cerrarSesion() {
        // Regresar al login y limpiar el stack de actividades
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
