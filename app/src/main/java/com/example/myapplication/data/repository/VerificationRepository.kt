package com.example.myapplication.data.repository

import com.example.myapplication.data.dao.VerificationDao
import com.example.myapplication.data.entity.VerificationEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface VerificationRepository {
    suspend fun insert(record: VerificationEntity)
    fun getAllRecords(): Flow<List<VerificationEntity>>
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
}