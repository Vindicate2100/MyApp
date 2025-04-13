package com.example.myapplication.screens.verification

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.viewmodels.VerificationViewModel

@Composable
fun PreparationScreen(
    viewModel: VerificationViewModel = hiltViewModel(),
    onNext: () -> Unit
) {
    val protocolNumber by viewModel.protocolNumber.collectAsState()
    val deviceNumber by viewModel.deviceNumber.collectAsState()
    val deviceType by viewModel.deviceType.collectAsState()
    val deviceModel by viewModel.deviceModel.collectAsState()
    val lowerRange by viewModel.lowerRange.collectAsState()
    val upperRange by viewModel.upperRange.collectAsState()
    val pointCount by viewModel.pointCount.collectAsState()
    val transformFunction by viewModel.transformFunction.collectAsState()
    val registryNumber by viewModel.registryNumber.collectAsState()
    val accuracyClass by viewModel.accuracyClass.collectAsState()

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
        onProtocolNumberChange = { viewModel.protocolNumber.value = it },
        onDeviceNumberChange = { viewModel.deviceNumber.value = it },
        onDeviceTypeChange = { viewModel.deviceType.value = it },
        onDeviceModelChange = { viewModel.deviceModel.value = it },
        onLowerRangeChange = { viewModel.lowerRange.value = it },
        onUpperRangeChange = { viewModel.upperRange.value = it },
        onPointCountChange = { viewModel.pointCount.value = it },
        onTransformFunctionChange = { viewModel.transformFunction.value = it },
        onRegistryNumberChange = { viewModel.registryNumber.value = it },
        onAccuracyClassChange = { viewModel.accuracyClass.value = it },
        onNext = onNext
    )
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
    var currentField by remember { mutableIntStateOf(0) }
    val listState = rememberLazyListState()

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

    val allFieldsFilled = fields.all { it.value.isNotBlank() }
    val visibleFields = fields.take(currentField + 1)

    LaunchedEffect(currentField) {
        listState.animateScrollToItem(currentField)
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                "Подготовка к поверке",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(visibleFields) { config ->
            OutlinedTextField(
                value = config.value,
                onValueChange = {
                    config.onChange(it)
                    if (it.isNotBlank() && fields.indexOf(config) == currentField) {
                        currentField = (currentField + 1).coerceAtMost(fields.lastIndex)
                    }
                },
                label = { Text(config.label) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = config.keyboardType)
            )
        }

        item {
            if (allFieldsFilled) {
                Text(
                    "1. Убедитесь в целостности корпуса\n2. Проверьте комплектацию",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Button(
                    onClick = onNext,
                    enabled = allFieldsFilled,
                    modifier = Modifier.width(150.dp)
                ) {
                    Text("Продолжить")
                }
            }
        }
    }
}

private data class FieldConfig(
    val label: String,
    val value: String,
    val onChange: (String) -> Unit,
    val keyboardType: KeyboardType = KeyboardType.Text
)