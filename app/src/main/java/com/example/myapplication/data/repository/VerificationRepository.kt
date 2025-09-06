package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.VerificationDao
import com.example.myapplication.data.local.entity.VerificationEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface VerificationRepository {
    suspend fun insert(record: VerificationEntity)
    suspend fun saveDevice(record: VerificationEntity) // Добавлено
    suspend fun getByDeviceNumber(deviceNumber: String): VerificationEntity?
    fun getAllRecords(): Flow<List<VerificationEntity>>
    fun getAllVerifications(): Flow<List<VerificationEntity>>
    suspend fun deleteByDeviceNumber(deviceNumber: String)
}

class VerificationRepositoryImpl @Inject constructor(
    private val dao: VerificationDao
) : VerificationRepository {
    override suspend fun insert(record: VerificationEntity) {
        dao.insert(record)
    }

    override fun getAllRecords(): Flow<List<VerificationEntity>> {
        return dao.getAllRecords()
    }

    override fun getAllVerifications(): Flow<List<VerificationEntity>> {
        return dao.getAllVerifications()
    }

    override suspend fun getByDeviceNumber(deviceNumber: String): VerificationEntity? {
        return dao.getByDeviceNumber(deviceNumber)
    }

    override suspend fun saveDevice(record: VerificationEntity) {
        dao.insert(record)
    }

    override suspend fun deleteByDeviceNumber(deviceNumber: String) {
        dao.deleteByDeviceNumber(deviceNumber)
    }
}