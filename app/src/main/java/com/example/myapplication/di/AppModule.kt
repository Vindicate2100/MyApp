package com.example.myapplication.di

import android.content.Context
import androidx.room.Room
import com.example.myapplication.data.dao.ClimateDao
import com.example.myapplication.data.dao.TaskDao
import com.example.myapplication.data.dao.VerificationDao
import com.example.myapplication.data.database.AppDatabase
import com.example.myapplication.data.repository.VerificationRepository
import com.example.myapplication.data.repository.VerificationRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app-database.db"
        )
            .fallbackToDestructiveMigration(false)
            .build()
    }

    // Предоставление DAO
    @Provides
    fun provideTaskDao(database: AppDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    fun provideClimateDao(database: AppDatabase): ClimateDao {
        return database.climateDao()
    }

    @Provides
    fun provideVerificationDao(database: AppDatabase): VerificationDao {
        return database.verificationDao()
    }

    // Предоставление Repository
    @Provides
    fun provideVerificationRepository(dao: VerificationDao): VerificationRepository {
        return VerificationRepositoryImpl(dao)
    }
}