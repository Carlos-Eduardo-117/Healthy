package com.example.healthyapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ConfirmarReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val nombre = intent.getStringExtra("nombre") ?: return
        val id = intent.getIntExtra("id", -1)
        val fechaHora = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val db = AppDatabase.getDatabase(context)
        val dao = db.tomaDao()

        CoroutineScope(Dispatchers.IO).launch {
            dao.registrarToma(TomaMedicamento(medicamentoId = id, fechaHora = fechaHora))
        }
    }
}
