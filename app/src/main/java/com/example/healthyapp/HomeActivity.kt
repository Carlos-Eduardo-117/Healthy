package com.example.healthyapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import android.widget.*
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var dao: MedicamentoDao
    private lateinit var rvMedicamentos: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Configurar botón cerrar sesión
        val btnLogout = findViewById<ImageButton>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            cerrarSesion()
        }

        // Configurar otros elementos de la UI
        val btnNuevoMed = findViewById<Button>(R.id.btnNuevoMed)
        rvMedicamentos = findViewById(R.id.rvMedicamentos)

        // Cargar datos del usuario
        val preferences = getSharedPreferences("medPrefs", MODE_PRIVATE)
        val nombre = preferences.getString("name", "")
        val apellido = preferences.getString("lastName", "")
        val nombreCompleto = "$nombre $apellido"

        val tvBienvenida = findViewById<TextView>(R.id.tvBienvenida)
        tvBienvenida.text = "¡Bienvenido, $nombreCompleto!"

        // Configurar botones de navegación
        val btnHistorial = findViewById<Button>(R.id.btnHistorial)
        btnHistorial.setOnClickListener {
            startActivity(Intent(this, HistorialActivity::class.java))
        }

        btnNuevoMed.setOnClickListener {
            startActivity(Intent(this, AddMedicamentoActivity::class.java))
        }

        // Inicializar base de datos
        db = AppDatabase.getDatabase(this)
        dao = db.medicamentoDao()

        cargarMedicamentos()
    }

    override fun onResume() {
        super.onResume()
        cargarMedicamentos()
    }

    private fun cerrarSesion() {
        // Limpiar SharedPreferences
        val prefs = getSharedPreferences("medPrefs", MODE_PRIVATE).edit()
        prefs.clear()
        prefs.apply()

        // Regresar al login y limpiar el stack de actividades
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // Hacer esta función pública para que el adapter pueda acceder
    fun cargarMedicamentos() {
        lifecycleScope.launch {
            val lista = dao.obtenerTodos()
            runOnUiThread {
                rvMedicamentos.layoutManager = LinearLayoutManager(this@HomeActivity)
                rvMedicamentos.adapter = MedicamentoAdapter(lista.toMutableList())
            }
        }
    }
}