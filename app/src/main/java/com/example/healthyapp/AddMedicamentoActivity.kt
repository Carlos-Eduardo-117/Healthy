package com.example.healthyapp

import android.content.Intent
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

        // Crear canal de notificaciones
        NotificationHelper.createNotificationChannel(this)

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

        val tvTituloFormulario = findViewById<TextView>(R.id.tvTituloFormulario)
        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etDosis = findViewById<EditText>(R.id.etDosis)
        val etIntervaloHoras = findViewById<EditText>(R.id.etIntervaloHoras)
        val timePicker = findViewById<TimePicker>(R.id.timePicker)
        timePicker.setIs24HourView(true)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)

        db = AppDatabase.getDatabase(this)
        dao = db.medicamentoDao()

        //Aquí es donde se recibe el medicamentoId (q es el q se va a editar)
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
                        etIntervaloHoras.setText(it.intervaloHoras?.toString() ?: "")
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
                        tvTituloFormulario.text = "Actualizar Medicamento"
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
            val intervaloHoras = etIntervaloHoras.text.toString().toIntOrNull()
            if (intervaloHoras == null || intervaloHoras <= 0) {
                Toast.makeText(this, "Ingresa un intervalo válido", Toast.LENGTH_SHORT).show()
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
                val medicamentoGuardado = if (medicamentoId == null) {
                    //Cacho para registrar
                    val nuevoMedicamento = Medicamento(
                        nombre = nombre,
                        dosis = dosis,
                        hora = hora,
                        intervaloHoras = intervaloHoras
                    )
                    val id = dao.insertar(nuevoMedicamento)
                    nuevoMedicamento.copy(id = id.toInt())

                } else {
                    //Cacho para modificar
                    val medicamentoActualizado = Medicamento(
                        id = medicamentoId!!,
                        nombre = nombre,
                        dosis = dosis,
                        hora = hora,
                        intervaloHoras = intervaloHoras
                    )
                    dao.actualizar(medicamentoActualizado)

                    // Cancelar notificación anterior si existe
                    NotificationHelper.cancelNotification(this@AddMedicamentoActivity, medicamentoId!!)
                    medicamentoActualizado
                }

                // Programar notificación
                NotificationHelper.scheduleNotification(this@AddMedicamentoActivity, medicamentoGuardado)

                runOnUiThread {
                    Toast.makeText(applicationContext, "Medicamento guardado y notificación programada", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
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