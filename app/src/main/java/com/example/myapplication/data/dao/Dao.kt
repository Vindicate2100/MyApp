package com.example.myapplication.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.myapplication.data.entity.ClimateData
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) для работы с климатическими данными
 */

@Dao
interface ClimateDao {
    @Insert
    suspend fun insert(data: ClimateData)

    // Получение всех данных, отсортированных по дате (новые сверху)
    @Query("SELECT * FROM climate_data ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ClimateData>>

    // Удаление данных только за текущий день
    @Query("DELETE FROM climate_data WHERE date(timestamp/1000, 'unixepoch') = date('now')")
    suspend fun clearTodayData()

    @Query("SELECT COUNT(*) FROM climate_data WHERE timestamp BETWEEN :start AND :end")
    suspend fun getCountForDay(start: Long, end: Long): Int
}