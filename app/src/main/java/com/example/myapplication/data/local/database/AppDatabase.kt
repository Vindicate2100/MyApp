package com.example.myapplication.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myapplication.data.local.dao.ClimateDao
import com.example.myapplication.data.local.dao.TaskDao
import com.example.myapplication.data.local.dao.VerificationDao
import com.example.myapplication.data.local.entity.ClimateEntity
import com.example.myapplication.data.local.entity.TaskEntity
import com.example.myapplication.data.local.entity.VerificationEntity

@Database(
    entities = [
        ClimateEntity::class,
        TaskEntity::class,
        VerificationEntity::class  // Добавляем новую сущность
    ],
    version = 5,  // Увеличиваем версию БД!
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun climateDao(): ClimateDao
    abstract fun taskDao(): TaskDao
    abstract fun verificationDao(): VerificationDao  // Добавляем новый DAO

}