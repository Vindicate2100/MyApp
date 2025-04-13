package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.entity.VerificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VerificationDao {
    // Получить все записи (сортировка по дате)
    @Query("SELECT * FROM verification_records ORDER BY verificationDate DESC")
    fun getAll(): Flow<List<VerificationEntity>>

    // Добавить запись
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: VerificationEntity)

    // Удалить запись
    @Delete
    suspend fun delete(entity: VerificationEntity)

    // Поиск по серийному номеру
    @Query("SELECT * FROM verification_records WHERE deviceNumber LIKE :query")
    fun searchBySerial(query: String): Flow<List<VerificationEntity>>

    @Query("SELECT * FROM verification_records ORDER BY verificationDate DESC")
    fun getAllRecords(): Flow<List<VerificationEntity>>
}