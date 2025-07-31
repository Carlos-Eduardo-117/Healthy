package com.example.healthyapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
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

        val btnNuevoMed = findViewById<Button>(R.id.btnNuevoMed)
        rvMedicamentos = findViewById(R.id.rvMedicamentos)

        val btnHistorial = findViewById<Button>(R.id.btnHistorial)
        btnHistorial.setOnClickListener {
            startActivity(Intent(this, HistorialActivity::class.java))
        }

        btnNuevoMed.setOnClickListener {
            startActivity(Intent(this, AddMedicamentoActivity::class.java))
        }

        db = AppDatabase.getDatabase(this)
        dao = db.medicamentoDao()

        cargarMedicamentos()
    }

    override fun onResume() {
        super.onResume()
        cargarMedicamentos()
    }

    fun cargarMedicamentos() {
        lifecycleScope.launch {
            val lista = dao.obtenerTodos()
            runOnUiThread {
                rvMedicamentos.layoutManager = LinearLayoutManager(this@HomeActivity)
                rvMedicamentos.adapter = MedicamentoAdapter(lista)
            }
        }
    }
}
