package com.example.healthyapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.*

class PosponerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val nombre = intent.getStringExtra("nombre") ?: "Medicamento"
        val id = intent.getIntExtra("id", 0)

        val newIntent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("nombre", nombre)
            putExtra("id", id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            newIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            add(Calendar.MINUTE, 10) // ⏰ POSPONER 10 MINUTOS
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }
}
