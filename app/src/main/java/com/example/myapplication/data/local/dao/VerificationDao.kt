package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.data.local.entity.VerificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VerificationDao {
    // Получить все записи (сортировка по дате)
    @Query("SELECT * FROM verification_records ORDER BY verificationDate DESC")
    fun getAll(): Flow<List<VerificationEntity>>

    // Добавить запись
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(entity: VerificationEntity)

    // Удалить запись
    @Delete
    suspend fun delete(entity: VerificationEntity)

    // Поиск по серийному номеру
    @Query("SELECT * FROM verification_records WHERE device_Number LIKE :query")
    fun searchBySerial(query: String): Flow<List<VerificationEntity>>

    @Query("SELECT * FROM verification_records ORDER BY verificationDate DESC")
    fun getAllRecords(): Flow<List<VerificationEntity>>

    // Исправляем запрос - используем правильное имя таблицы verification_records
    @Query("SELECT * FROM verification_records")
    fun getAllVerifications(): Flow<List<VerificationEntity>>

    // Добавляем новый метод для поиска по номеру прибора
    @Query("SELECT * FROM verification_records WHERE device_number = :deviceNumber")
    suspend fun getByDeviceNumber(deviceNumber: String): VerificationEntity?

    // Добавляем метод для удаления прибора по его номеру
    @Query("DELETE FROM verification_records WHERE device_number = :deviceNumber")
    suspend fun deleteByDeviceNumber(deviceNumber: String)
}