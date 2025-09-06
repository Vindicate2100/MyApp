package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.myapplication.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TaskDao {
    // Основные операции CRUD
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(taskEntity: TaskEntity): Long

    @Update
    suspend fun update(taskEntity: TaskEntity)

    @Delete
    suspend fun delete(taskEntity: TaskEntity)

    // Оптимизированные запросы
    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY isCompleted ASC, createdAt DESC")
    fun getTasksByDate(date: String): Flow<List<TaskEntity>>

    @Query("""
        SELECT DISTINCT date 
        FROM tasks 
        WHERE substr(date, 4, 7) = :monthYear
        ORDER BY date ASC
    """)
    fun getDatesWithTasks(monthYear: String): Flow<List<String>>

    // Безопасные операции
    @Transaction
    @Query("UPDATE tasks SET isCompleted = :isCompleted WHERE id = :taskId")
    suspend fun setTaskCompleted(taskId: Long, isCompleted: Boolean)

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    suspend fun getTaskById(taskId: Long): TaskEntity?

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteById(taskId: Long)

    // Расширенные функции
    @Query("DELETE FROM tasks WHERE isCompleted = 1 AND date < :beforeDate")
    suspend fun cleanupCompletedTasks(beforeDate: String = LocalDate.now().toString())

    @Query("SELECT COUNT(*) FROM tasks WHERE date = :date AND isCompleted = 0")
    fun getActiveTaskCountForDate(date: String): Flow<Int>

    @Query("""
        SELECT COUNT(*) 
        FROM tasks 
        WHERE date BETWEEN :startDate AND :endDate
        AND isCompleted = 0
    """)
    fun getActiveTaskCountForPeriod(startDate: String, endDate: String): Flow<Int>

    // Миграционные/административные запросы
    @Query("UPDATE tasks SET date = :newDate WHERE id = :taskId")
    suspend fun changeTaskDate(taskId: Long, newDate: String)
}