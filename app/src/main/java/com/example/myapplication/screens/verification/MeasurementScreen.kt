package com.example.myapplication.screens.verification

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.ui.components.tables.MeasurementTable
import com.example.myapplication.viewmodels.verification.VerificationViewModel

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

            MeasurementTable(
                viewModel = viewModel,
                group = measurementGroups[activeGroupIndex]
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
                    onClick = onNext,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Далее")
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
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Протокол
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = "Протокол",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = " №$protocolNumber",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Устройство
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeviceUnknown,
                    contentDescription = "Устройство",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = " $deviceType $deviceModel №$deviceNumber",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Класс точности + функция преобразования
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.StarRate,
                    contentDescription = "Класс точности",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp)
                )
                if (transformFunction != VerificationViewModel.TRANSFORM_NONE) {
                    Text(
                        text = " $accuracyClass | $transformFunction",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                } else {
                    Text(
                        text = " $accuracyClass",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
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

                    AnimatedVisibility(visible = expandedMeasurementId == measurement.id) {
                        Column(
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .animateContentSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Показываем подсказку: что нужно сделать
                            Text(
                                text = "Приложите ${"%.1f".format(measurement.scaleMark)} В:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            MeasurementTextField(
                                label = "↑ Возрастание (В)",
                                value = measurement.referenceIncreasing.takeIf { it != 0f },
                                onValueChange = { newValue ->
                                    val updated = measurement.copy(
                                        referenceIncreasing = newValue ?: 0f
                                    )
                                    onMeasurementUpdate(index, updated)
                                }
                            )

                            MeasurementTextField(
                                label = "↓ Убывание (В)",
                                value = measurement.referenceDecreasing.takeIf { it != 0f },
                                onValueChange = { newValue ->
                                    val updated = measurement.copy(
                                        referenceDecreasing = newValue ?: 0f
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
private fun MeasurementTextField(
    label: String,
    value: Float?,
    onValueChange: (Float?) -> Unit
) {
    var textValue by remember(value) {
        mutableStateOf(value?.toString() ?: "")
    }

    OutlinedTextField(
        value = textValue,
        onValueChange = {
            if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                textValue = it
                val floatValue = it.toFloatOrNull()
                onValueChange(floatValue)
            }
        },
        label = { Text(label) },
        placeholder = { Text("Введите значение") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        isError = textValue.isNotEmpty() && textValue.toFloatOrNull() == null,
        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
    )

    if (textValue.isNotEmpty() && textValue.toFloatOrNull() == null) {
        Text(
            text = "Неверное значение",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
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