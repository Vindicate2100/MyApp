package com.example.myapplication.viewmodels.climate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.dao.ClimateDao
import com.example.myapplication.data.local.entity.ClimateEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * ViewModel для работы с климатическими данными
 */
@HiltViewModel
class ClimateViewModel @Inject constructor(
    private val climateDao: ClimateDao
) : ViewModel() {

    // Состояние для ошибок валидации
    data class ClimateValidationState(
        val temperatureError: String? = null,
        val humidityError: String? = null,
        val pressureError: String? = null,
        val isValid: Boolean = true
    )

    private val _validationState = MutableStateFlow(ClimateValidationState())
    val validationState: StateFlow<ClimateValidationState> = _validationState.asStateFlow()

    // Поток данных для наблюдения за изменениями
    val climateEntity: Flow<List<ClimateEntity>> = climateDao.getAll()

    /**
     * Валидация введенных данных
     * @param temperature температура
     * @param humidity влажность
     * @param pressure давление
     * @return true, если данные валидны, иначе false
     */
    private fun validateInput(temperature: String, humidity: String, pressure: String): Boolean {
        var isValid = true
        var tempError: String? = null
        var humError: String? = null
        var presError: String? = null

        val tempValue = temperature.toDoubleOrNull()
        val humValue = humidity.toDoubleOrNull()
        val presValue = pressure.toDoubleOrNull()

        if (temperature.isBlank()) {
            tempError = "Температура не может быть пустой"
            isValid = false
        } else if (tempValue == null || tempValue < 15 || tempValue > 30) {
            tempError = "Температура должна быть от 15 до 30"
            isValid = false
        }

        if (humidity.isBlank()) {
            humError = "Влажность не может быть пустой"
            isValid = false
        } else if (humValue == null || humValue < 0 || humValue > 100) {
            humError = "Влажность должна быть от 0 до 100"
            isValid = false
        }

        if (pressure.isBlank()) {
            presError = "Давление не может быть пустым"
            isValid = false
        } else if (presValue == null || presValue < 80 || presValue > 102) {
            presError = "Давление должно быть от 80 до 102"
            isValid = false
        }

        _validationState.update {
            it.copy(
                temperatureError = tempError,
                humidityError = humError,
                pressureError = presError,
                isValid = isValid
            )
        }
        return isValid
    }

    /**
     * Сохранение новых климатических данных
     * @param temperature температура
     * @param humidity влажность
     * @param pressure давление
     * @return true, если данные успешно сохранены, иначе false
     */
    fun saveData(temperature: String, humidity: String, pressure: String): Boolean {
        if (!validateInput(temperature, humidity, pressure)) {
            return false
        }
        viewModelScope.launch {
            climateDao.insert(
                ClimateEntity(
                    temperature = temperature,
                    humidity = humidity,
                    pressure = pressure
                )
            )
        }
        // Сбрасываем состояние ошибок после успешного сохранения
        clearValidationErrors()
        return true
    }

    /**
     * Очистка сообщений об ошибках валидации.
     * Вызывается, когда пользователь начинает вводить новые данные.
     */
    fun clearValidationErrors() {
        _validationState.value = ClimateValidationState()
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
    fun hasDataToday(): Flow<Boolean> {
        val (startOfDay, endOfDay) = getStartAndEndOfToday()
        return flow {
            val count = climateDao.getCountForDay(startOfDay, endOfDay)
            emit(count > 0)
        }.flowOn(Dispatchers.IO)
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
}
