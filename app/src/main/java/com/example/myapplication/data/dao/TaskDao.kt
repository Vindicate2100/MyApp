package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.entity.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TaskDao {
    // Основные операции CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    // Оптимизированные запросы
    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY isCompleted ASC, createdAt DESC")
    fun getTasksByDate(date: String): Flow<List<Task>>

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
    suspend fun getTaskById(taskId: Long): Task?

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