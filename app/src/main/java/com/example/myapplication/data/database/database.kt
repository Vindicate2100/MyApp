package com.example.myapplication.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapplication.data.dao.ClimateDao
import com.example.myapplication.data.dao.TaskDao
import com.example.myapplication.data.dao.VerificationDao
import com.example.myapplication.data.entity.ClimateData
import com.example.myapplication.data.entity.Task
import com.example.myapplication.data.entity.VerificationEntity

@Database(
    entities = [
        ClimateData::class,
        Task::class,
        VerificationEntity::class  // Добавляем новую сущность
    ],
    version = 5,  // Увеличиваем версию БД!
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun climateDao(): ClimateDao
    abstract fun taskDao(): TaskDao
    abstract fun verificationDao(): VerificationDao  // Добавляем новый DAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database.db"
                )
                    .fallbackToDestructiveMigration(false)  // Временное решение для разработки
                    // .addMigrations(MIGRATION_2_3)  // Реальная миграция для продакшна
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}