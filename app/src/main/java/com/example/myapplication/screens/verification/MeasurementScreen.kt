package com.example.myapplication.screens.verification

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.viewmodels.VerificationViewModel
import kotlin.math.abs

@Composable
fun MeasurementScreen(
    viewModel: VerificationViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val measurementGroups by viewModel.measurementGroups.collectAsState()
    val pointCount by viewModel.pointCount.collectAsState()
    val transformFunction by viewModel.transformFunction.collectAsState()
    val lowerRange by viewModel.lowerRange.collectAsState() // Получаем как State
    val upperRange by viewModel.upperRange.collectAsState() // Получаем как State
    val saveStatus by viewModel.saveStatus.collectAsState()
    var activeGroupIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(pointCount, lowerRange, upperRange) { // Используем полученные State
        val count = pointCount.toIntOrNull() ?: 5
        val lower = lowerRange.toFloatOrNull() ?: 0f
        val upper = upperRange.toFloatOrNull() ?: 100f

        if (upper > lower && (measurementGroups.isEmpty() ||
                    measurementGroups[0].measurements.size != count)) {
            viewModel.generateDefaultGroups(count)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when (saveStatus) {
            is VerificationViewModel.SaveStatus.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                MeasurementContent(
                    viewModel = viewModel,
                    measurementGroups = measurementGroups,
                    transformFunction = transformFunction,
                    activeGroupIndex = activeGroupIndex,
                    onGroupSelected = { activeGroupIndex = it },
                    onBack = onBack,
                    onNext = onNext
                )
            }
        }
    }
}

@Composable
private fun MeasurementContent(
    viewModel: VerificationViewModel,
    measurementGroups: List<VerificationViewModel.MeasurementGroup>,
    transformFunction: String,
    activeGroupIndex: Int,
    onGroupSelected: (Int) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        InstrumentInfoCard(viewModel)

        if (measurementGroups.isNotEmpty()) {
            GroupTabs(
                groups = measurementGroups,
                selectedIndex = activeGroupIndex,
                onTabSelected = onGroupSelected
            )

            MeasurementInputCard(
                measurements = measurementGroups[activeGroupIndex].measurements,
                onMeasurementUpdate = { index, updated ->
                    viewModel.updateMeasurement(
                        groupIndex = activeGroupIndex,
                        measurementIndex = index,
                        newMeasurement = updated
                    )
                }
            )

            MeasurementResultsTable(
                group = measurementGroups[activeGroupIndex],
                transformFunction = transformFunction
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text("Назад")
                }
                Button(
                    onClick = {
                        viewModel.saveVerification()
                        onNext()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Сохранить")
                }
            }
        }
    }
}

@Composable
private fun InstrumentInfoCard(viewModel: VerificationViewModel) {
    val protocolNumber by viewModel.protocolNumber.collectAsState()
    val deviceNumber by viewModel.deviceNumber.collectAsState()
    val deviceType by viewModel.deviceType.collectAsState()
    val deviceModel by viewModel.deviceModel.collectAsState()
    val accuracyClass by viewModel.accuracyClass.collectAsState()
    val transformFunction by viewModel.transformFunction.collectAsState()

    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Протокол №$protocolNumber",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "поверки $deviceType $deviceModel №$deviceNumber",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "Класс точности: $accuracyClass" +
                        if (transformFunction != VerificationViewModel.TRANSFORM_NONE)
                            " | Функция: $transformFunction" else "",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun MeasurementInputCard(
    measurements: List<VerificationViewModel.VoltageMeasurement>,
    onMeasurementUpdate: (Int, VerificationViewModel.VoltageMeasurement) -> Unit
) {
    var expandedMeasurementId by remember { mutableIntStateOf(-1) }

    Card(
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Ввод данных измерений",
                style = MaterialTheme.typography.titleMedium
            )

            measurements.forEachIndexed { index, measurement ->
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedMeasurementId = if (expandedMeasurementId == measurement.id) -1 else measurement.id
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${"%.1f".format(measurement.scaleMark)} В",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (expandedMeasurementId == measurement.id)
                                Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expandedMeasurementId == measurement.id)
                                "Свернуть" else "Развернуть"
                        )
                    }

                    if (expandedMeasurementId == measurement.id) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MeasurementTextField(
                                label = "↑ Возрастание (В)",
                                value = measurement.referenceIncreasing,
                                onValueChange = { newValue ->
                                    val updated = measurement.copy(
                                        referenceIncreasing = newValue
                                    )
                                    onMeasurementUpdate(index, updated)
                                }
                            )
                            MeasurementTextField(
                                label = "↓ Убывание (В)",
                                value = measurement.referenceDecreasing,
                                onValueChange = { newValue ->
                                    val updated = measurement.copy(
                                        referenceDecreasing = newValue
                                    )
                                    onMeasurementUpdate(index, updated)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MeasurementResultsTable(
    group: VerificationViewModel.MeasurementGroup,
    transformFunction: String
) {
    val showTransformColumns = transformFunction != VerificationViewModel.TRANSFORM_NONE
    val scrollState = rememberScrollState()

    Card(
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.horizontalScroll(scrollState)
        ) {
            // Заголовок таблицы
            Row(
                modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                // Общие колонки
                TableHeaderCell("Точка (В)")
                TableHeaderCell("Эталон ↑")
                TableHeaderCell("Эталон ↓")

                // Динамические колонки преобразований
                if (showTransformColumns) {
                    val unit = when (transformFunction) {
                        VerificationViewModel.TRANSFORM_75MV -> "mV"
                        else -> "A"
                    }
                    TableHeaderCell("Преобр. ↑ ($unit)")
                    TableHeaderCell("Преобр. ↓ ($unit)")
                }

                // Колонки погрешностей
                TableHeaderCell("Погр. ↑ (%)")
                TableHeaderCell("Погр. ↓ (%)")
                TableHeaderCell("Вариация")
            }

            // Тело таблицы
            group.measurements.forEach { measurement ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Точка измерения (не изменяется)
                    DataCellWithBorder(
                        text = "%.1f".format(measurement.scaleMark),
                        color = if (measurement.scaleMark == group.measurements.first().scaleMark ||
                            measurement.scaleMark == group.measurements.last().scaleMark) {
                            MaterialTheme.colorScheme.primary
                        } else Color.Unspecified
                    )

                    // Оригинальные эталонные значения (в Вольтах)
                    DataCellWithBorder("%.2f В".format(measurement.referenceIncreasing))
                    DataCellWithBorder("%.2f В".format(measurement.referenceDecreasing))

                    // Преобразованные значения (если функция активна)
                    if (showTransformColumns) {
                        DataCellWithBorder("%.2f".format(measurement.transformedValueInc))
                        DataCellWithBorder("%.2f".format(measurement.transformedValueDec))
                    }

                    // Погрешности (уже рассчитаны с учетом преобразований)
                    DataCellWithBorder(
                        text = "%.2f%%".format(measurement.errorIncreasing),
                        color = if (abs(measurement.errorIncreasing) > group.maxAllowedError)
                            Color.Red else Color.Unspecified
                    )
                    DataCellWithBorder(
                        text = "%.2f%%".format(measurement.errorDecreasing),
                        color = if (abs(measurement.errorDecreasing) > group.maxAllowedError)
                            Color.Red else Color.Unspecified
                    )
                    DataCellWithBorder("%.2f".format(measurement.variation))
                }
            }
        }
    }
}

@Composable
private fun DataCellWithBorder(
    text: String,
    color: Color = Color.Unspecified
) {
    Box(
        modifier = Modifier
            .border(1.dp, MaterialTheme.colorScheme.outline)
            .widthIn(80.dp)
            .padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun MeasurementTextField(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    var textValue by remember { mutableStateOf(value.toString()) }

    OutlinedTextField(
        value = textValue,
        onValueChange = {
            if (it.isEmpty() || it.matches(Regex("^-?\\d*\\.?\\d*$"))) {
                textValue = it
                it.toFloatOrNull()?.let(onValueChange)
            }
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        isError = textValue.isNotEmpty() && textValue.toFloatOrNull() == null
    )
}

@Composable
private fun TableHeaderCell(text: String) {
    Box(
        modifier = Modifier
            .width(80.dp)
            .padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun GroupTabs(
    groups: List<VerificationViewModel.MeasurementGroup>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabTitles = groups.map { it.name }
    var selectedTabIndex by remember { mutableIntStateOf(selectedIndex) }

    LaunchedEffect(selectedIndex) {
        selectedTabIndex = selectedIndex
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = {
                        selectedTabIndex = index
                        onTabSelected(index)
                    },
                    text = {
                        Text(
                            text = title,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }
    }
}