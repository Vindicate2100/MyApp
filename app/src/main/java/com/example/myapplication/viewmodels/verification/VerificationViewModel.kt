package com.example.myapplication.viewmodels.verification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.dao.ClimateDao
import com.example.myapplication.data.local.entity.VerificationEntity
import com.example.myapplication.data.repository.VerificationRepository
import com.example.myapplication.services.PdfGeneratorService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class VerificationViewModel @Inject constructor(
    private val repository: VerificationRepository,
    private val climateDao: ClimateDao,
    private val pdfGeneratorService: PdfGeneratorService
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

    // Климатические условия
    val temperature = MutableStateFlow("")
    val humidity = MutableStateFlow("")
    val pressure = MutableStateFlow("")
    
    // Статус поверки
    val verificationStatus = MutableStateFlow("PASSED") // По умолчанию "PASSED"

    // Константы для функций преобразования
    companion object {
        const val TRANSFORM_75MV = "75mV"
        const val TRANSFORM_5A = "5A"
        const val TRANSFORM_NONE = "Нет"
    }

    // Таблица всех сохраненных приборов
    private val _allDevices = MutableStateFlow<Map<String, DeviceInfo>>(emptyMap())
    val allDevices: StateFlow<Map<String, DeviceInfo>> = _allDevices.asStateFlow()

    // Состояния измерений
    private val _measurementGroups = MutableStateFlow<List<MeasurementGroup>>(emptyList())
    val measurementGroups: StateFlow<List<MeasurementGroup>> = _measurementGroups.asStateFlow()

    // Статус сохранения
    private val _saveStatus = MutableStateFlow<SaveStatus>(SaveStatus.Idle)
    val saveStatus: StateFlow<SaveStatus> = _saveStatus.asStateFlow()

    // Событие успешной загрузки существующего устройства
    private val _deviceLoaded = MutableStateFlow(false)
    val deviceLoaded: StateFlow<Boolean> = _deviceLoaded.asStateFlow()

    // Статус генерации PDF
    private val _pdfGenerationStatus = MutableStateFlow<PdfGenerationStatus>(PdfGenerationStatus.Idle)
    val pdfGenerationStatus: StateFlow<PdfGenerationStatus> = _pdfGenerationStatus.asStateFlow()

    // Статус загрузки климатических данных
    private val _climateDataStatus = MutableStateFlow<ClimateDataStatus>(ClimateDataStatus.Idle)
    val climateDataStatus: StateFlow<ClimateDataStatus> = _climateDataStatus.asStateFlow()

    init {
        setupPointCountObserver()
        generateDefaultGroups(pointCount.value.toIntOrNull() ?: 5)
        loadSavedDevices()
    }

    /**
     * Настраивает наблюдатель за изменением количества точек измерения.
     * При изменении количества точек автоматически пересоздает группы измерений.
     */
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

    /**
     * Генерирует группы измерений по умолчанию на основе указанного количества точек.
     * Создает равномерно распределенные точки измерения между нижней и верхней границей диапазона.
     * @param pointCount Количество точек измерения
     */
    fun generateDefaultGroups(pointCount: Int) {
        val lower = lowerRange.value.toFloatOrNull() ?: 0f
        val upper = upperRange.value.toFloatOrNull() ?: 100f
        val step = if (pointCount > 1) (upper - lower) / (pointCount - 1) else 0f

        _measurementGroups.value = listOf(
            MeasurementGroup(
                name = "Основная приведённая погрешность",
                maxAllowedError = 1.5f,
                measurements = List(pointCount) { idx ->
                    VoltageMeasurement(
                        id = idx,
                        scaleMark = lower + idx * step,
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

    /**
     * Пытается загрузить данные существующего прибора по его номеру.
     * Если прибор найден, применяет его данные к текущей форме.
     * @param deviceNumber Номер прибора для поиска
     */
    fun tryLoadExistingDevice(deviceNumber: String) {
        viewModelScope.launch {
            val existing = repository.getByDeviceNumber(deviceNumber)
            if (existing != null) {
                applyVerificationData(existing)
                _deviceLoaded.value = true
                delay(100)
                _deviceLoaded.value = false
            }
        }
    }

    /**
     * Обновляет данные конкретного измерения в указанной группе.
     * @param groupIndex Индекс группы измерений
     * @param measurementIndex Индекс измерения в группе
     * @param newMeasurement Новые данные измерения
     */
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

    /**
     * Сохраняет текущую поверку в базу данных.
     * Обновляет статус сохранения в процессе выполнения операции.
     */
    fun saveVerification() {
        viewModelScope.launch {
            _saveStatus.value = SaveStatus.Loading
            try {
                repository.insert(createVerificationEntity())
                saveCurrentDevice()
                _saveStatus.value = SaveStatus.Success
            } catch (e: Exception) {
                _saveStatus.value = SaveStatus.Error(e.message ?: "Ошибка сохранения")
            }
        }
    }

    /**
     * Создает сущность поверки на основе текущих данных формы.
     * @return VerificationEntity с данными текущей поверки
     */
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
            measurementResult = buildMeasurementResults(),
            documentPaths = "",
            id = UUID.randomUUID().toString()
        )
    }

    /**
     * Формирует строку с результатами измерений для сохранения.
     * @return Строка с отформатированными результатами всех измерений
     */
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

    /**
     * Загружает список всех сохраненных приборов из базы данных.
     * Обновляет _allDevices при изменении данных в репозитории.
     */
    private fun loadSavedDevices() {
        viewModelScope.launch {
            repository.getAllVerifications().collect { verifications ->
                _allDevices.value = verifications.associate { verification ->
                    verification.deviceNumber to verification.toDeviceInfo()
                }
            }
        }
    }

    /**
     * Сохраняет текущий прибор в базу данных.
     * Используется для сохранения промежуточных данных формы.
     */
    fun saveCurrentDevice() {
        viewModelScope.launch {
            try {
                val entity = createVerificationEntity()
                repository.saveDevice(entity)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Удаляет прибор из базы данных по его номеру.
     * @param deviceNumber Номер прибора для удаления
     */
    fun deleteDevice(deviceNumber: String) {
        viewModelScope.launch {
            try {
                repository.deleteByDeviceNumber(deviceNumber)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Применяет данные существующей поверки к текущей форме.
     * @param verification Сущность поверки с данными для применения
     */
    private fun applyVerificationData(verification: VerificationEntity) {
        deviceNumber.value = verification.deviceNumber
        deviceType.value = verification.deviceType
        deviceModel.value = verification.deviceModel
        lowerRange.value = verification.lowerRange
        upperRange.value = verification.upperRange
        registryNumber.value = verification.registryNumber
        accuracyClass.value = verification.accuracyClass
        pointCount.value = verification.pointCount
        transformFunction.value = verification.transformFunction
        isPassed.value = verification.status == "PASSED"
    }

    /**
     * Вычисляет погрешности измерения с учетом функции преобразования.
     * Учитывает различные типы преобразований (75mV, 5A) и рассчитывает приведенную погрешность.
     * @param measurement Измерение, для которого нужно рассчитать погрешности
     * @return Обновленное измерение с рассчитанными погрешностями
     */
    private fun calculateMeasurementErrors(measurement: VoltageMeasurement): VoltageMeasurement {
        val transform = transformFunction.value
        val lowerRangeValue = lowerRange.value.toFloatOrNull() ?: 0f
        val upperRangeValue = upperRange.value.toFloatOrNull() ?: 100f
        val range = upperRangeValue - lowerRangeValue
        
        // Коэффициент обратного преобразования (из mV или A обратно в В)
        val reverseConversionFactor = when (transform) {
            TRANSFORM_75MV -> range / 75f  // Из mV в В
            TRANSFORM_5A -> 5f  // Из А в В
            else -> 1f
        }
        
        // Вычисляем обратно преобразованные значения для столбца "Преобр."
        val transformedValueInc = measurement.referenceIncreasing * reverseConversionFactor
        val transformedValueDec = measurement.referenceDecreasing * reverseConversionFactor
        
        // Рассчитываем ПРИВЕДЕННУЮ погрешность:
        // Приведенная погрешность = ((Преобразованное значение - Точка шкалы) / Диапазон) * 100%
        val errorIncreasing = if (range != 0f) {
            ((transformedValueInc - measurement.scaleMark) / range) * 100f
        } else {
            0f
        }
        
        val errorDecreasing = if (range != 0f) {
            ((transformedValueDec - measurement.scaleMark) / range) * 100f
        } else {
            0f
        }
        
        // Вариация - это абсолютная разница между показаниями при увеличении и уменьшении
        val variation = abs(transformedValueInc - transformedValueDec)
        
        // Отладочная информация для диагностики
        when (transform) {
            TRANSFORM_75MV -> {
                println("""
                DEBUG: 75mV
                Диапазон: $lowerRangeValue-$upperRangeValue В (размер: $range В)
                Эталон (в mV): ${measurement.referenceIncreasing}, ${measurement.referenceDecreasing}
                Коэффициент обратного преобразования: $reverseConversionFactor В/мВ
                Преобразованные значения: $transformedValueInc В, $transformedValueDec В
                Точка шкалы: ${measurement.scaleMark} В
                Приведенная погрешность ↑: ((${transformedValueInc} - ${measurement.scaleMark}) / $range) * 100 = $errorIncreasing%
                Приведенная погрешность ↓: ((${transformedValueDec} - ${measurement.scaleMark}) / $range) * 100 = $errorDecreasing%
                Вариация: $variation В
                """)
            }
            TRANSFORM_5A -> {
                println("""
                DEBUG: 5A
                Диапазон: $lowerRangeValue-$upperRangeValue В (размер: $range В)
                Коэффициент обратного преобразования: $reverseConversionFactor В/А
                Эталон (в A): ${measurement.referenceIncreasing}, ${measurement.referenceDecreasing}
                Преобразованные значения: $transformedValueInc В, $transformedValueDec В
                Точка шкалы: ${measurement.scaleMark} В
                Приведенная погрешность ↑: ((${transformedValueInc} - ${measurement.scaleMark}) / $range) * 100 = $errorIncreasing%
                Приведенная погрешность ↓: ((${transformedValueDec} - ${measurement.scaleMark}) / $range) * 100 = $errorDecreasing%
                Вариация: $variation В
                """)
            }
        }
        
        return measurement.copy(
            transformedValueInc = transformedValueInc,
            transformedValueDec = transformedValueDec,
            errorIncreasing = errorIncreasing,
            errorDecreasing = errorDecreasing,
            variation = variation
        )
    }

    /**
     * Преобразует сущность поверки в информационный объект устройства.
     * @return DeviceInfo с основными параметрами устройства
     */
    private fun VerificationEntity.toDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            deviceNumber = deviceNumber,
            deviceType = deviceType,
            deviceModel = deviceModel,
            lowerRange = lowerRange,
            upperRange = upperRange,
            pointCount = pointCount,
            transformFunction = transformFunction,
            registryNumber = registryNumber,
            accuracyClass = accuracyClass
        )
    }

    /**
     * Определяет единицу измерения на основе типа прибора.
     * @param deviceTypeParam Опциональный тип прибора. Если не указан, используется текущее значение
     * @return Строка с единицей измерения (В, мВ, кВ, А, мА, кА)
     */
    fun getDeviceUnit(deviceTypeParam: String? = null): String {
        val deviceTypeValue = deviceTypeParam ?: deviceType.value
        return when {
            deviceTypeValue.contains("вольтметр", ignoreCase = true) -> "В"
            deviceTypeValue.contains("милливольтметр", ignoreCase = true) -> "мВ"
            deviceTypeValue.contains("киловольтметр", ignoreCase = true) -> "кВ"
            deviceTypeValue.contains("амперметр", ignoreCase = true) -> "А"
            deviceTypeValue.contains("миллиамперметр", ignoreCase = true) -> "мА"
            deviceTypeValue.contains("килоамперметр", ignoreCase = true) -> "кА"
            else -> "В" // По умолчанию вольты
        }
    }
    
    /**
     * Определяет единицу измерения для функции преобразования.
     * @param transformFunctionParam Опциональная функция преобразования. Если не указана, используется текущее значение
     * @return Строка с единицей измерения (мВ, А или единица измерения прибора)
     */
    fun getTransformUnit(transformFunctionParam: String? = null): String {
        val transform = transformFunctionParam ?: transformFunction.value
        return when (transform) {
            TRANSFORM_75MV -> "мВ"
            TRANSFORM_5A -> "А"
            else -> getDeviceUnit()
        }
    }
    
    /**
     * Форматирует значение точки шкалы с учетом функции преобразования.
     * @param measurement Измерение, для которого нужно отформатировать значение
     * @param transformFunctionParam Опциональная функция преобразования
     * @param lowerRangeParam Опциональное значение нижней границы диапазона
     * @param upperRangeParam Опциональное значение верхней границы диапазона
     * @return Отформатированная строка с пересчитанным значением
     */
    fun getFormattedTransformedScaleMark(
        measurement: VoltageMeasurement,
        transformFunctionParam: String? = null,
        lowerRangeParam: String? = null,
        upperRangeParam: String? = null
    ): String {
        val transform = transformFunctionParam ?: transformFunction.value
        
        // Вычисляем пересчитанное значение
        val transformedValue = when (transform) {
            TRANSFORM_75MV -> {
                // Логика преобразования для 75mV
                val lowerValue = lowerRangeParam?.toFloatOrNull() ?: lowerRange.value.toFloatOrNull() ?: 0f
                val upperValue = upperRangeParam?.toFloatOrNull() ?: upperRange.value.toFloatOrNull() ?: 100f
                val range = upperValue - lowerValue
                val position = measurement.scaleMark - lowerValue
                (position / range) * 75f  // пересчет в mV
            }
            TRANSFORM_5A -> measurement.scaleMark / 5f
            else -> measurement.scaleMark
        }
        
        // Форматируем в зависимости от типа преобразования
        return when (transform) {
            TRANSFORM_75MV -> "%.1f".format(transformedValue)
            TRANSFORM_5A -> "%.3f".format(transformedValue)
            else -> "%.2f".format(transformedValue)
        }
    }

    /**
     * Загружает климатические данные за текущий день.
     * Если данные за текущий день отсутствуют, устанавливает статус ошибки.
     */
    fun loadLatestClimateData() {
        viewModelScope.launch {
            _climateDataStatus.value = ClimateDataStatus.Loading
            try {
                val latestData = climateDao.getLatest()
                val currentDate = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val dataDate = Calendar.getInstance().apply {
                    timeInMillis = latestData?.timestamp ?: 0
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                if (latestData == null || dataDate != currentDate) {
                    _climateDataStatus.value = ClimateDataStatus.Error("Отсутствуют климатические данные за текущий день")
                    temperature.value = ""
                    humidity.value = ""
                    pressure.value = ""
                } else {
                    temperature.value = latestData.temperature
                    humidity.value = latestData.humidity
                    pressure.value = latestData.pressure
                    _climateDataStatus.value = ClimateDataStatus.Success
                }
            } catch (e: Exception) {
                _climateDataStatus.value = ClimateDataStatus.Error(e.message ?: "Ошибка загрузки климатических данных")
                temperature.value = ""
                humidity.value = ""
                pressure.value = ""
            }
        }
    }

    /**
     * Статусы загрузки климатических данных.
     */
    sealed class ClimateDataStatus {
        /** Начальное состояние */
        object Idle : ClimateDataStatus()
        /** Процесс загрузки */
        object Loading : ClimateDataStatus()
        /** Успешная загрузка */
        object Success : ClimateDataStatus()
        /** Ошибка загрузки с сообщением */
        data class Error(val message: String) : ClimateDataStatus()
    }

    /**
     * Определяет статус поверки на основе результатов измерений.
     * Проверяет, превышают ли погрешности допустимые значения.
     * Обновляет verificationStatus и isPassed.
     */
    fun determineVerificationStatus() {
        val hasErrors = _measurementGroups.value.any { group ->
            group.hasErrors
        }
        verificationStatus.value = if (hasErrors) "FAILED" else "PASSED"
        isPassed.value = !hasErrors
    }

    /**
     * Генерирует PDF протокол на основе текущих данных поверки.
     * Сохраняет файл на устройстве и обновляет статус генерации.
     */
    fun generateAndSavePdfProtocol() {
        viewModelScope.launch {
            _pdfGenerationStatus.value = PdfGenerationStatus.Loading
            try {
                // Создаем объект данных поверки для PdfGeneratorService
                val verificationData = PdfGeneratorService.VerificationData(
                    protocolNumber = protocolNumber.value,
                    deviceNumber = deviceNumber.value,
                    deviceType = deviceType.value,
                    deviceModel = deviceModel.value,
                    lowerRange = lowerRange.value,
                    upperRange = upperRange.value,
                    registryNumber = registryNumber.value,
                    accuracyClass = accuracyClass.value,
                    temperature = temperature.value,
                    humidity = humidity.value,
                    pressure = pressure.value,
                    transformFunction = transformFunction.value,
                    status = verificationStatus.value,
                    measurements = measurementGroups.value.firstOrNull()?.measurements ?: emptyList()
                )
                
                // Генерируем PDF
                val pdfFile = pdfGeneratorService.generateVerificationProtocol(verificationData)
                
                // Обновляем статус с информацией о файле
                _pdfGenerationStatus.value = PdfGenerationStatus.Success(pdfFile)
                
                // НЕ запускаем напрямую activity для отправки PDF
                // pdfGeneratorService.shareGeneratedPdf(pdfFile)
                
            } catch (e: Exception) {
                _pdfGenerationStatus.value = PdfGenerationStatus.Error(e.message ?: "Ошибка генерации PDF")
            }
        }
    }

    /**
     * Статусы процесса сохранения данных.
     */
    sealed class SaveStatus {
        /** Начальное состояние */
        object Idle : SaveStatus()
        /** Процесс сохранения */
        object Loading : SaveStatus()
        /** Успешное сохранение */
        object Success : SaveStatus()
        /** Ошибка сохранения с сообщением */
        data class Error(val message: String) : SaveStatus()
    }
    
    /**
     * Статусы процесса генерации PDF.
     */
    sealed class PdfGenerationStatus {
        /** Начальное состояние */
        object Idle : PdfGenerationStatus()
        /** Процесс генерации */
        object Loading : PdfGenerationStatus()
        /** Успешная генерация с путем к файлу */
        data class Success(val file: File) : PdfGenerationStatus()
        /** Ошибка генерации с сообщением */
        data class Error(val message: String) : PdfGenerationStatus()
    }

    /**
     * Группа измерений с допустимыми погрешностями.
     * @property name Название группы измерений
     * @property maxAllowedError Максимально допустимая погрешность в процентах
     * @property measurements Список измерений в группе
     * @property completed Флаг завершенности группы измерений
     */
    data class MeasurementGroup(
        val name: String,
        val maxAllowedError: Float,
        val measurements: List<VoltageMeasurement>,
        val completed: Boolean = false
    ) {
        /** Проверяет наличие превышений допустимых погрешностей */
        val hasErrors: Boolean
            get() = measurements.any { 
                abs(it.errorIncreasing) > maxAllowedError || 
                abs(it.errorDecreasing) > maxAllowedError 
            }
        
        /** Максимальная погрешность при увеличении значения */
        val maxErrorIncreasing: Float
            get() = measurements.maxOfOrNull { abs(it.errorIncreasing) } ?: 0f
        
        /** Максимальная погрешность при уменьшении значения */
        val maxErrorDecreasing: Float
            get() = measurements.maxOfOrNull { abs(it.errorDecreasing) } ?: 0f
        
        /** Максимальная вариация показаний */
        val maxVariation: Float
            get() = measurements.maxOfOrNull { it.variation } ?: 0f
    }

    /**
     * Данные одного измерения напряжения.
     * @property id Уникальный идентификатор измерения
     * @property scaleMark Значение точки шкалы
     * @property referenceIncreasing Эталонное значение при увеличении
     * @property referenceDecreasing Эталонное значение при уменьшении
     * @property transformedValueInc Преобразованное значение при увеличении
     * @property transformedValueDec Преобразованное значение при уменьшении
     * @property errorIncreasing Погрешность при увеличении
     * @property errorDecreasing Погрешность при уменьшении
     * @property variation Вариация показаний
     */
    data class VoltageMeasurement(
        val id: Int,
        val scaleMark: Float,
        val referenceIncreasing: Float,
        val referenceDecreasing: Float,
        val transformedValueInc: Float = 0f,
        val transformedValueDec: Float = 0f,
        val errorIncreasing: Float,
        val errorDecreasing: Float,
        val variation: Float
    )

    /**
     * Информация об устройстве.
     * @property deviceNumber Номер устройства
     * @property deviceType Тип устройства
     * @property deviceModel Модель устройства
     * @property lowerRange Нижняя граница диапазона
     * @property upperRange Верхняя граница диапазона
     * @property pointCount Количество точек измерения
     * @property transformFunction Функция преобразования
     * @property registryNumber Номер в госреестре
     * @property accuracyClass Класс точности
     */
    data class DeviceInfo(
        val deviceNumber: String,
        val deviceType: String,
        val deviceModel: String,
        val lowerRange: String,
        val upperRange: String,
        val pointCount: String,
        val transformFunction: String,
        val registryNumber: String,
        val accuracyClass: String
    )

    /**
     * Возвращает путь к директории для сохранения PDF-файлов.
     * @return File объект директории
     */
    fun getPdfDirectory(): File {
        return pdfGeneratorService.getDocumentsDirectory()
    }
    
    /**
     * Возвращает список всех созданных PDF-файлов протоколов.
     * @return Список файлов PDF
     */
    fun getAllPdfFiles(): List<File> {
        return pdfGeneratorService.getAllPdfFiles()
    }

    /**
     * Методы для обновления полей формы
     */
    fun updateProtocolNumber(value: String) {
        protocolNumber.value = value
    }
    
    fun updateDeviceNumber(value: String) {
        deviceNumber.value = value
    }
    
    fun updateDeviceType(value: String) {
        deviceType.value = value
    }
    
    fun updateDeviceModel(value: String) {
        deviceModel.value = value
    }
    
    fun updateLowerRange(value: String) {
        lowerRange.value = value
    }
    
    fun updateUpperRange(value: String) {
        upperRange.value = value
    }
    
    fun updatePointCount(value: String) {
        pointCount.value = value
    }
    
    fun updateTransformFunction(value: String) {
        transformFunction.value = value
    }
    
    fun updateRegistryNumber(value: String) {
        registryNumber.value = value
    }
    
    fun updateAccuracyClass(value: String) {
        accuracyClass.value = value
    }
}