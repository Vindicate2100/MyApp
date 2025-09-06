package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "climate_data")
data class ClimateEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val temperature: String,
    val humidity: String,
    val pressure: String,
    val timestamp: Long = System.currentTimeMillis() // Автоматическое сохранение времени
)