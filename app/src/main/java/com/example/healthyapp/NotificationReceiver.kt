package com.example.healthyapp

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val nombre = intent.getStringExtra("nombre") ?: "Tu medicamento"
        val id = intent.getIntExtra("id", 0)

        // Acción para confirmar
        val confirmIntent = Intent(context, ConfirmarReceiver::class.java).apply {
            putExtra("id", id)
            putExtra("nombre", nombre)
        }

        val confirmPendingIntent = PendingIntent.getBroadcast(
            context, id, confirmIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Acción para posponer
        val posponerIntent = Intent(context, PosponerReceiver::class.java).apply {
            putExtra("id", id)
            putExtra("nombre", nombre)
        }
        val posponerPendingIntent = PendingIntent.getBroadcast(
            context, id + 1000, posponerIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(context, "med_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Hora de tomar medicamento")
            .setContentText("Toma: $nombre")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(0, "Confirmar", confirmPendingIntent)
            .addAction(0, "Posponer", posponerPendingIntent)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(id, notificationBuilder.build())
    }
}
