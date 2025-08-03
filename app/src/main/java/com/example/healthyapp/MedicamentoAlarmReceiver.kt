package com.example.healthyapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.*

class MedicamentoAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val medicamentoId = intent.getIntExtra("medicamento_id", -1)
        val nombre = intent.getStringExtra("medicamento_nombre") ?: ""
        val dosis = intent.getStringExtra("medicamento_dosis") ?: ""
        val intervaloHoras = intent.getIntExtra("medicamento_intervalo", 24)

        if (medicamentoId != -1 && nombre.isNotEmpty()) {
            // Mostrar la notificación
            NotificationHelper.showNotification(context, medicamentoId, nombre, dosis)

            // Programar la siguiente alarma
            scheduleNextAlarm(context, intent, intervaloHoras)
        }
    }

    private fun scheduleNextAlarm(context: Context, originalIntent: Intent, intervaloHoras: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Crear el mismo intent para la siguiente alarma
        val nextIntent = Intent(context, MedicamentoAlarmReceiver::class.java).apply {
            putExtra("medicamento_id", originalIntent.getIntExtra("medicamento_id", -1))
            putExtra("medicamento_nombre", originalIntent.getStringExtra("medicamento_nombre"))
            putExtra("medicamento_dosis", originalIntent.getStringExtra("medicamento_dosis"))
            putExtra("medicamento_intervalo", intervaloHoras)
        }

        val medicamentoId = originalIntent.getIntExtra("medicamento_id", -1)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicamentoId,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calcular tiempo para la siguiente alarma
        val nextAlarmTime = System.currentTimeMillis() + (intervaloHoras * 60 * 60 * 1000L)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextAlarmTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    nextAlarmTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}