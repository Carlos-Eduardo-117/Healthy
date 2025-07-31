package com.example.healthyapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.os.Build

class AddMedicamentoActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var dao: MedicamentoDao
    private val REQUEST_NOTIFICATION_PERMISSION = 1

    private var medicamentoId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_medicamento)

        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etDosis = findViewById<EditText>(R.id.etDosis)
        val timePicker = findViewById<TimePicker>(R.id.timePicker)
        timePicker.setIs24HourView(true)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)

        db = AppDatabase.getDatabase(this)
        dao = db.medicamentoDao()

        //Aki es donde se recibe el medicamentoId (q es el q se va a editar)
        val idRecibido = intent.getIntExtra("medicamentoId", -1)
        if (idRecibido != -1) {
            medicamentoId = idRecibido
            // Cargar medicamento para editar
            lifecycleScope.launch {
                val med = dao.obtenerPorId(medicamentoId!!)
                runOnUiThread {
                    med?.let {
                        etNombre.setText(it.nombre)
                        etDosis.setText(it.dosis)
                        // Parsear hora para el TimePicker
                        val partesHora = it.hora.split(":")
                        if (partesHora.size == 2) {
                            val h = partesHora[0].toIntOrNull() ?: 0
                            val m = partesHora[1].toIntOrNull() ?: 0
                            if (Build.VERSION.SDK_INT >= 23) {
                                timePicker.hour = h
                                timePicker.minute = m
                            } else {
                                timePicker.currentHour = h
                                timePicker.currentMinute = m
                            }
                        }
                        btnGuardar.text = "Actualizar Medicamento"
                    }
                }
            }
        }

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val dosis = etDosis.text.toString().trim()

            if (nombre.isEmpty() || dosis.isEmpty()) {
                Toast.makeText(this, "Llena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //Hay que manejar permiso de notis: Pedir permiso notificaciones si Android 13+
            if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
                return@setOnClickListener
            }

            // Obtener hora del TimePicker
            val hora = if (Build.VERSION.SDK_INT >= 23) {
                String.format("%02d:%02d", timePicker.hour, timePicker.minute)
            } else {
                String.format("%02d:%02d", timePicker.currentHour, timePicker.currentMinute)
            }

            lifecycleScope.launch {
                if (medicamentoId == null) {
                    //Cacho para registrar
                    val nuevoMedicamento = Medicamento(
                        nombre = nombre,
                        dosis = dosis,
                        hora = hora
                    )
                    dao.insertar(nuevoMedicamento)
                } else {
                    //Cacho para modificar
                    val medicamentoActualizado = Medicamento(
                        id = medicamentoId!!,
                        nombre = nombre,
                        dosis = dosis,
                        hora = hora
                    )
                    dao.actualizar(medicamentoActualizado)
                }

                runOnUiThread {
                    Toast.makeText(applicationContext, "Medicamento guardado", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    // Manejo del permiso (por si el usuario responde)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso concedido. Presiona Guardar nuevamente.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No podrás recibir notificaciones", Toast.LENGTH_LONG).show()
            }
        }
    }
}
