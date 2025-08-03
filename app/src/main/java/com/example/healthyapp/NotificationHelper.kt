package com.example.healthyapp

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.*

object NotificationHelper {

    private const val CHANNEL_ID = "medicamento_channel"
    private const val CHANNEL_NAME = "Recordatorios de Medicamentos"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones para recordar tomar medicamentos"
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleNotification(context: Context, medicamento: Medicamento) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Verificar permisos de alarmas exactas (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // Aquí podrías mostrar un diálogo para que el usuario active los permisos
                return
            }
        }

        // Crear intent para el BroadcastReceiver
        val intent = Intent(context, MedicamentoAlarmReceiver::class.java).apply {
            putExtra("medicamento_id", medicamento.id)
            putExtra("medicamento_nombre", medicamento.nombre)
            putExtra("medicamento_dosis", medicamento.dosis)
            putExtra("medicamento_intervalo", medicamento.intervaloHoras)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicamento.id, // Usar ID del medicamento como request code único
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calcular el tiempo para la próxima alarma
        val calendar = Calendar.getInstance().apply {
            val horaPartes = medicamento.hora.split(":")
            val hora = horaPartes[0].toInt()
            val minutos = horaPartes[1].toInt()

            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minutos)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // Si la hora ya pasó hoy, programar para mañana
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        // Programar alarma repetitiva usando setRepeating
        val intervaloMillis = (medicamento.intervaloHoras ?: 24) * 60 * 60 * 1000L

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Para Android 6+ usar setExactAndAllowWhileIdle para la primera alarma
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                // Para versiones anteriores usar setRepeating
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    intervaloMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Manejar excepción de permisos
            e.printStackTrace()
        }
    }

    fun cancelNotification(context: Context, medicamentoId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MedicamentoAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicamentoId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)

        // También cancelar la notificación mostrada si existe
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(medicamentoId)
    }

    fun showNotification(context: Context, medicamentoId: Int, nombre: String, dosis: String) {
        // Intent para abrir la app cuando se toque la notificación
        val intent = Intent(context, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            medicamentoId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert) // Usando ícono del sistema como fallback
            .setContentTitle("⏰ Hora de tu medicamento")
            .setContentText("$nombre - $dosis")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Es hora de tomar tu medicamento:\n$nombre\nDosis: $dosis"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(medicamentoId, notification)
        } catch (e: SecurityException) {
            // Manejar caso donde no hay permisos
            e.printStackTrace()
        }
    }
}