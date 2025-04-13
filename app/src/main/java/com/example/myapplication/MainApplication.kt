package com.example.myapplication

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : Application() {
    // Здесь можно инициализировать библиотеки
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "climate_reminder",
            "Напоминания о климате",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Ежедневное напоминание заполнить климатические данные"
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}