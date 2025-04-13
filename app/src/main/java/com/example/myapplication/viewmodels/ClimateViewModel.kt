package com.example.myapplication.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.dao.ClimateDao
import com.example.myapplication.data.entity.ClimateData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Calendar
import javax.inject.Inject

/**
 * ViewModel для работы с климатическими данными
 */
@HiltViewModel
class ClimateViewModel @Inject constructor(
    private val climateDao: ClimateDao
) : ViewModel() {

    // Поток данных для наблюдения за изменениями
    val climateData: Flow<List<ClimateData>> = climateDao.getAll()

    /**
     * Сохранение новых климатических данных
     * @param temperature температура
     * @param humidity влажность
     * @param pressure давление
     */
    fun saveData(temperature: String, humidity: String, pressure: String) {
        viewModelScope.launch {
            climateDao.insert(
                ClimateData(
                    temperature = temperature,
                    humidity = humidity,
                    pressure = pressure
                )
            )
        }
    }

    /**
     * Очистка данных за сегодня
     */
    fun clearTodayData() {
        viewModelScope.launch {
            climateDao.clearTodayData()
        }
    }
    // Проверяет, есть ли данные за сегодня
        fun hasDataToday(): Boolean {
            val (startOfDay, endOfDay) = getStartAndEndOfToday()
            return runBlocking {
                climateDao.getCountForDay(startOfDay, endOfDay) > 0
            }
        }

        // Возвращает начало и конец текущего дня в миллисекундах
        private fun getStartAndEndOfToday(): Pair<Long, Long> {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = calendar.timeInMillis

            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val endOfDay = calendar.timeInMillis - 1

            return Pair(startOfDay, endOfDay)
        }

        // Проверяет, является ли сегодня выходным
        fun isWeekend(): Boolean {
            val calendar = Calendar.getInstance()
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
        }
    }