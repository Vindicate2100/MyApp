package com.example.myapplication.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "verification_records")
data class VerificationEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val protocolNumber: String,          // № протокола
    val deviceNumber: String,            // № прибора (бывший serialNumber)
    val deviceType: String,              // Вид прибора
    val deviceModel: String,             // Тип прибора
    val lowerRange: String,              // НПИ
    val upperRange: String,              // ВПИ
    val registryNumber: String,          // Номер в Госреестре СИ
    val accuracyClass: String,           // Класс точности
    val verificationDate: String,        // Формат: "dd.MM.yyyy"
    val nextVerificationDate: String,
    val status: String,                  // "PASSED", "FAILED", "PENDING"
    val measurementResult: String = "",  // Результаты измерений (бывший notes)
    val documentPaths: String = "",      // JSON или разделитель для списка файлов
    val pointCount: String,       // Количество точек
    val transformFunction: String // Функция преобразования
)