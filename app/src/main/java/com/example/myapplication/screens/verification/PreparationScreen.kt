package com.example.myapplication.screens.verification

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType.Companion.PrimaryEditable
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.viewmodels.verification.VerificationViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun PreparationScreen(
    viewModel: VerificationViewModel = hiltViewModel(),
    onNext: () -> Unit,
) {
    // Собираем состояния полей из ViewModel
    val protocolNumber by viewModel.protocolNumber.collectAsStateWithLifecycle()
    val deviceNumber by viewModel.deviceNumber.collectAsStateWithLifecycle()
    val deviceType by viewModel.deviceType.collectAsStateWithLifecycle()
    val deviceModel by viewModel.deviceModel.collectAsStateWithLifecycle()
    val lowerRange by viewModel.lowerRange.collectAsStateWithLifecycle()
    val upperRange by viewModel.upperRange.collectAsStateWithLifecycle()
    val pointCount by viewModel.pointCount.collectAsStateWithLifecycle()
    val transformFunction by viewModel.transformFunction.collectAsStateWithLifecycle()
    val registryNumber by viewModel.registryNumber.collectAsStateWithLifecycle()
    val accuracyClass by viewModel.accuracyClass.collectAsStateWithLifecycle()
    val deviceLoaded by viewModel.deviceLoaded.collectAsStateWithLifecycle()

    // Автоподстановка данных при изменении номера прибора
    LaunchedEffect(deviceNumber) {
        if (deviceNumber.isNotBlank() && deviceNumber.isNotEmpty()) {
            delay(500) // Антодребезг
            viewModel.tryLoadExistingDevice(deviceNumber)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Основной контент экрана
        PreparationStep(
            protocolNumber = protocolNumber,
            deviceNumber = deviceNumber,
            deviceType = deviceType,
            deviceModel = deviceModel,
            lowerRange = lowerRange,
            upperRange = upperRange,
            pointCount = pointCount,
            transformFunction = transformFunction,
            registryNumber = registryNumber,
            accuracyClass = accuracyClass,
            deviceLoaded = deviceLoaded,
            onProtocolNumberChange = { viewModel.updateProtocolNumber(it) },
            onDeviceNumberChange = { viewModel.updateDeviceNumber(it) },
            onDeviceTypeChange = { viewModel.updateDeviceType(it) },
            onDeviceModelChange = { viewModel.updateDeviceModel(it) },
            onLowerRangeChange = { viewModel.updateLowerRange(it) },
            onUpperRangeChange = { viewModel.updateUpperRange(it) },
            onPointCountChange = { viewModel.updatePointCount(it) },
            onTransformFunctionChange = { viewModel.updateTransformFunction(it) },
            onRegistryNumberChange = { viewModel.updateRegistryNumber(it) },
            onAccuracyClassChange = { viewModel.updateAccuracyClass(it) },
            onNext = {
                viewModel.saveCurrentDevice() // Сохраняем прибор перед переходом
                onNext()
            }
        )

        // Кнопка для просмотра истории приборов

    }
}

@Composable
private fun PreparationStep(
    protocolNumber: String,
    deviceNumber: String,
    deviceType: String,
    deviceModel: String,
    lowerRange: String,
    upperRange: String,
    pointCount: String,
    transformFunction: String,
    registryNumber: String,
    accuracyClass: String,
    deviceLoaded: Boolean,
    onProtocolNumberChange: (String) -> Unit,
    onDeviceNumberChange: (String) -> Unit,
    onDeviceTypeChange: (String) -> Unit,
    onDeviceModelChange: (String) -> Unit,
    onLowerRangeChange: (String) -> Unit,
    onUpperRangeChange: (String) -> Unit,
    onPointCountChange: (String) -> Unit,
    onTransformFunctionChange: (String) -> Unit,
    onRegistryNumberChange: (String) -> Unit,
    onAccuracyClassChange: (String) -> Unit,
    onNext: () -> Unit
) {
    // Текущее активное поле формы
    var currentField by remember { mutableIntStateOf(0) }
    // Состояние для скролла LazyColumn
    val listState = rememberLazyListState()
    // Менеджер фокуса для управления клавиатурой
    val focusManager = LocalFocusManager.current
    // Корутина для анимаций
    val coroutineScope = rememberCoroutineScope()
    
    // Конфигурация всех полей формы
    val fields = listOf(
        FieldConfig("№ протокола", protocolNumber, onProtocolNumberChange, KeyboardType.Number),
        FieldConfig("№ прибора", deviceNumber, onDeviceNumberChange, KeyboardType.Number),
        FieldConfig("Вид прибора", deviceType, onDeviceTypeChange),
        FieldConfig("Тип прибора", deviceModel, onDeviceModelChange),
        FieldConfig("НПИ", lowerRange, onLowerRangeChange, KeyboardType.Number),
        FieldConfig("ВПИ", upperRange, onUpperRangeChange, KeyboardType.Number),
        FieldConfig("Количество точек", pointCount, onPointCountChange, KeyboardType.Number),
        FieldConfig("Функция преобразования", transformFunction, onTransformFunctionChange),
        FieldConfig("Номер в Госреестре СИ", registryNumber, onRegistryNumberChange),
        FieldConfig("Класс точности", accuracyClass, onAccuracyClassChange)
    )
    
    // Проверка, что все поля заполнены
    val allFieldsFilled = fields.all { it.value.isNotBlank() }
    // Список FocusRequester'ов для управления фокусом
    val focusRequesterList = remember { fields.map { FocusRequester() } }
    
    // Обработка события загрузки устройства - перемещаем фокус к первому незаполненному полю или концу списка
    LaunchedEffect(deviceLoaded) {
        if (deviceLoaded) {
            // Ищем индекс первого незаполненного поля
            val nextEmptyFieldIndex = fields.indexOfFirst { it.value.isBlank() }
            // Если все поля заполнены, то перемещаемся к последнему полю
            val targetIndex = if (nextEmptyFieldIndex != -1) nextEmptyFieldIndex else fields.lastIndex
            
            // Обновляем текущее поле и прокручиваем к нему
            currentField = targetIndex
            listState.animateScrollToItem(targetIndex)
            delay(200) // Даем время для обновления интерфейса
            focusRequesterList[targetIndex].requestFocus()
        }
    }

    // Эффект для автоматического скролла и установки фокуса при смене поля
    LaunchedEffect(currentField) {
        snapshotFlow { currentField }.collectLatest { index ->
            // Плавный скролл к текущему полю
            listState.animateScrollToItem(index)
            delay(100) // Небольшая задержка для плавности
            // Установка фокуса на текущее поле
            focusRequesterList[index].requestFocus()
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .imePadding(), // Учет клавиатуры
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Заголовок экрана
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Подготовка к поверке",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                // Индикатор прогресса заполнения
                LinearProgressIndicator(
                    progress = { (currentField + 1).toFloat() / fields.size.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .padding(bottom = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Поля формы с анимациями
        itemsIndexed(fields) { index, config ->
            val isCurrentField = index == currentField
            val isLastField = index == fields.lastIndex

            AnimatedVisibility(
                visible = index <= currentField,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Подсветка текущего поля
                    if (isCurrentField) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(4.dp)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = MaterialTheme.shapes.small
                                )
                        ) {
                            Text(
                                text = "Заполните это поле",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }

                    // Поле ввода
                    if (config.label == "Функция преобразования") {
                        // Выпадающий список для функции преобразования
                        TransformFunctionDropdown(
                            value = config.value,
                            onValueChange = config.onChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequesterList[index])
                        )
                    } else {
                        // Обычное текстовое поле для всех остальных полей
                        OutlinedTextField(
                            value = config.value,
                            onValueChange = { config.onChange(it) },
                            label = { Text(config.label) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequesterList[index]),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = config.keyboardType,
                                imeAction = if (!isLastField) ImeAction.Next else ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = {
                                    // Переход к следующему полю при нажатии Next
                                    currentField = (currentField + 1).coerceAtMost(fields.lastIndex)
                                },
                                onDone = {
                                    focusManager.clearFocus()
                                    if (allFieldsFilled) {
                                        coroutineScope.launch {
                                            // Скролл к кнопке продолжения
                                            listState.animateScrollToItem(fields.size)
                                        }
                                    }
                                }
                            ),
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium,
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    // Индикатор прогресса (текущее поле/всего полей)
                    Text(
                        text = "${index + 1}/${fields.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCurrentField) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }

        // Чек-лист после заполнения всех полей
        item {
            AnimatedVisibility(
                visible = allFieldsFilled,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Text(
                        "Проверьте перед продолжением:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        "1. Убедитесь в целостности корпуса\n2. Проверьте комплектацию",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)
                    )
                }
            }
        }

        // Кнопки навигации
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Кнопка "Назад" (только если не на первом поле)
                if (currentField > 0) {
                    Button(
                        onClick = {
                            currentField = (currentField - 1).coerceAtLeast(0)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text("Назад")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }

                // Кнопка "Далее"/"Продолжить"
                Button(
                    onClick = {
                        if (currentField < fields.lastIndex) {
                            currentField++
                        } else if (allFieldsFilled) {
                            onNext()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = fields[currentField].value.isNotBlank()
                ) {
                    Text(if (currentField < fields.lastIndex) "Далее" else "Продолжить")
                }
            }
        }
    }
}

// Класс конфигурации поля формы
private data class FieldConfig(
    val label: String,                   // Название поля
    val value: String,                   // Текущее значение
    val onChange: (String) -> Unit,      // Обработчик изменения
    val keyboardType: KeyboardType = KeyboardType.Text // Тип клавиатуры
)

// Компонент выпадающего списка для выбора функции преобразования
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransformFunctionDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(
        VerificationViewModel.TRANSFORM_NONE,
        VerificationViewModel.TRANSFORM_75MV,
        VerificationViewModel.TRANSFORM_5A
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text("Функция преобразования") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(PrimaryEditable),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}