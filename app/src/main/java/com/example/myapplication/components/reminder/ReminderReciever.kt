package com.example.myapplication.components.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.MainApplication
import com.example.myapplication.viewmodels.ClimateViewModel

class DailyReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        // Получаем ViewModel через Hilt
        val app = context.applicationContext as MainApplication
        val viewModel = ViewModelProvider.AndroidViewModelFactory(app).create(ClimateViewModel::class.java)

        // Проверяем условия
        if (viewModel.isWeekend()) {
            Log.d("Reminder", "Сегодня выходной — уведомление не показано")
            return
        }

        if (viewModel.hasDataToday()) {
            Log.d("Reminder", "Данные уже введены за сегодня — уведомление не показано")
            return
        }

        // Если всё ОК — показываем уведомление
        showNotification(context)
    }

    private fun showNotification(context: Context) {
        val channelId = "climate_reminder"
        val title = "Заполните данные о климате"
        val message = "Не забудьте ввести температуру, давление и влажность."

        val pendingIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.let { launchIntent ->
            PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)

        val channel = NotificationChannel(
            channelId,
            "Напоминания о климате",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Ежедневное напоминание заполнить климатические данные"
            enableVibration(true)
            enableLights(true)
        }
        manager.createNotificationChannel(channel)

        manager.notify(1, notification)
    }
}