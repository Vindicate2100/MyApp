package com.example.myapplication.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.entity.VerificationEntity
import com.example.myapplication.data.repository.VerificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class VerificationViewModel @Inject constructor(
    private val repository: VerificationRepository
) : ViewModel() {

    // Основные поля формы
    val protocolNumber = MutableStateFlow("")
    val deviceNumber = MutableStateFlow("")
    val deviceType = MutableStateFlow("")
    val deviceModel = MutableStateFlow("")
    val lowerRange = MutableStateFlow("")
    val upperRange = MutableStateFlow("")
    val registryNumber = MutableStateFlow("")
    val accuracyClass = MutableStateFlow("")
    val pointCount = MutableStateFlow("")
    val transformFunction = MutableStateFlow("")
    val isPassed = MutableStateFlow(true)

    // Добавляем константы для функций преобразования
    companion object {
        const val TRANSFORM_75MV = "75mV"
        const val TRANSFORM_5A = "5A"
        const val TRANSFORM_NONE = "Нет"
    }

    // Состояния измерений
    private val _measurementGroups = MutableStateFlow<List<MeasurementGroup>>(emptyList())
    val measurementGroups: StateFlow<List<MeasurementGroup>> = _measurementGroups.asStateFlow()


    // Статус сохранения
    private val _saveStatus = MutableStateFlow<SaveStatus>(SaveStatus.Idle)
    val saveStatus: StateFlow<SaveStatus> = _saveStatus.asStateFlow()

    init {
        setupPointCountObserver()
        generateDefaultGroups(pointCount.value.toIntOrNull() ?: 5)
    }

    private fun setupPointCountObserver() {
        viewModelScope.launch {
            pointCount.collect { count ->
                val pointCount = count.toIntOrNull() ?: 5
                if (_measurementGroups.value.firstOrNull()?.measurements?.size != pointCount) {
                    generateDefaultGroups(pointCount)
                }
            }
        }
    }

    fun generateDefaultGroups(pointCount: Int) {
        // Получаем границы диапазона из полей формы
        val lower = lowerRange.value.toFloatOrNull() ?: 0f
        val upper = upperRange.value.toFloatOrNull() ?: 100f

        // Вычисляем шаг между точками (включая границы)
        val step = if (pointCount > 1) (upper - lower) / (pointCount - 1) else 0f

        _measurementGroups.value = listOf(
            MeasurementGroup(
                name = "Основная приведённая погрешность",
                maxAllowedError = 1.5f,
                measurements = List(pointCount) { idx ->
                    VoltageMeasurement(
                        id = idx,
                        scaleMark = lower + idx * step, // Равномерное распределение
                        referenceIncreasing = 0f,
                        referenceDecreasing = 0f,
                        errorIncreasing = 0f,
                        errorDecreasing = 0f,
                        variation = 0f
                    )
                }
            )
        )
    }

    fun updateMeasurement(
        groupIndex: Int,
        measurementIndex: Int,
        newMeasurement: VoltageMeasurement
    ) {
        _measurementGroups.update { groups ->
            groups.mapIndexed { idx, group ->
                if (idx == groupIndex) {
                    val updatedMeasurements = group.measurements.toMutableList().apply {
                        set(measurementIndex, calculateMeasurementErrors(newMeasurement))
                    }
                    group.copy(measurements = updatedMeasurements)
                } else group
            }
        }
    }

    fun saveVerification() {
        viewModelScope.launch {
            _saveStatus.value = SaveStatus.Loading
            try {
                repository.insert(createVerificationEntity())
                _saveStatus.value = SaveStatus.Success
            } catch (e: Exception) {
                _saveStatus.value = SaveStatus.Error(e.message ?: "Ошибка сохранения")
            }
        }
    }

    private fun createVerificationEntity(): VerificationEntity {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return VerificationEntity(
            protocolNumber = protocolNumber.value,
            deviceNumber = deviceNumber.value,
            deviceType = deviceType.value,
            deviceModel = deviceModel.value,
            lowerRange = lowerRange.value,
            upperRange = upperRange.value,
            registryNumber = registryNumber.value,
            accuracyClass = accuracyClass.value,
            pointCount = pointCount.value,
            transformFunction = transformFunction.value,
            verificationDate = dateFormat.format(Date()),
            nextVerificationDate = dateFormat.format(
                Calendar.getInstance().apply { add(Calendar.YEAR, 1) }.time
            ),
            status = if (isPassed.value) "PASSED" else "FAILED",
            measurementResult = buildMeasurementResults()
        )
    }

    private fun buildMeasurementResults(): String {
        return buildString {
            _measurementGroups.value.forEach { group ->
                appendLine("${group.name} (допуск: ${group.maxAllowedError}%):")
                group.measurements.forEach { m ->
                    appendLine(
                        "${"%.1f".format(m.scaleMark)} В: " +
                                "↑${"%.2f".format(m.referenceIncreasing)} (${"%.2f".format(m.errorIncreasing)}%), " +
                                "↓${"%.2f".format(m.referenceDecreasing)} (${"%.2f".format(m.errorDecreasing)}%), " +
                                "Δ=${"%.2f".format(m.variation)}"
                    )
                }
            }
        }
    }

    fun resetForm() {
        protocolNumber.value = ""
        deviceNumber.value = ""
        deviceType.value = ""
        deviceModel.value = ""
        lowerRange.value = ""
        upperRange.value = ""
        registryNumber.value = ""
        accuracyClass.value = ""
        pointCount.value = ""
        transformFunction.value = ""
        isPassed.value = true
        generateDefaultGroups(5)
        _saveStatus.value = SaveStatus.Idle
    }

    sealed class SaveStatus {
        object Idle : SaveStatus()
        object Loading : SaveStatus()
        object Success : SaveStatus()
        data class Error(val message: String) : SaveStatus()
    }

    data class MeasurementGroup(
        val name: String,
        val maxAllowedError: Float,
        val measurements: List<VoltageMeasurement>,
        val completed: Boolean = false
    )

    data class VoltageMeasurement(
        val id: Int,
        val scaleMark: Float,
        val referenceIncreasing: Float,
        val referenceDecreasing: Float,
        val transformedValueInc: Float = 0f,  // Новое поле
        val transformedValueDec: Float = 0f,  // Новое поле
        val errorIncreasing: Float,
        val errorDecreasing: Float,
        val variation: Float
    )

    private fun calculateMeasurementErrors(measurement: VoltageMeasurement): VoltageMeasurement {
        val transform = transformFunction.value
        val (incValue, decValue) = getTransformedOrRawValues(
            measurement.referenceIncreasing,
            measurement.referenceDecreasing,
            transform
        )

        // Получаем преобразованное значение точки измерения
        val transformedScaleMark = when (transform) {
            TRANSFORM_75MV -> {
                val upperRangeValue = upperRange.value.toFloatOrNull() ?: 0f
                (upperRangeValue / 75f) * measurement.scaleMark
            }

            TRANSFORM_5A -> measurement.scaleMark / 5f
            else -> measurement.scaleMark
        }

        return measurement.copy(
            transformedValueInc = incValue,
            transformedValueDec = decValue,
            errorIncreasing = calculateError(incValue, transformedScaleMark),
            errorDecreasing = calculateError(decValue, transformedScaleMark),
            variation = abs(incValue - decValue)
        )
    }

    private fun getTransformedOrRawValues(
        increasing: Float,
        decreasing: Float,
        transform: String
    ): Pair<Float, Float> {
        return when (transform) {
            TRANSFORM_75MV -> {
                val upperRangeValue = upperRange.value.toFloatOrNull() ?: 0f
                Pair(
                    (upperRangeValue / 75f) * increasing,
                    (upperRangeValue / 75f) * decreasing
                )
            }

            TRANSFORM_5A -> Pair(increasing / 5f, decreasing / 5f)
            else -> Pair(increasing, decreasing)
        }
    }

    private fun calculateError(referenceValue: Float, scaleMark: Float): Float {
        return if (scaleMark != 0f) {
            ((referenceValue - scaleMark) / scaleMark) * 100  // Приведенная погрешность в %
        } else {
            0f
        }
    }
}