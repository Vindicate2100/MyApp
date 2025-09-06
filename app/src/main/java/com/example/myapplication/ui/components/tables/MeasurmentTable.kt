package com.example.myapplication.ui.components.tables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.viewmodels.verification.VerificationViewModel
import kotlin.math.abs

@Composable
fun MeasurementTable(
    viewModel: VerificationViewModel,
    group: VerificationViewModel.MeasurementGroup
) {
    val transformFunction by viewModel.transformFunction.collectAsState()
    val showTransformColumns = transformFunction != VerificationViewModel.TRANSFORM_NONE
    val scrollState = rememberScrollState()
    
    // Получаем тип устройства для определения единиц измерения
    val deviceType by viewModel.deviceType.collectAsState()
    val lowerRange by viewModel.lowerRange.collectAsState()
    val upperRange by viewModel.upperRange.collectAsState()
    
    // Получаем единицы измерения и другие данные из ViewModel, передавая собранные значения
    val deviceUnit = viewModel.getDeviceUnit(deviceType)
    val transformUnit = viewModel.getTransformUnit(transformFunction)

    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.horizontalScroll(scrollState)) {
            // Заголовок таблицы
            Row(
                modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                TableHeaderCell("Точка ($deviceUnit)")
                
                // Колонка для пересчитанного значения точки
                if (showTransformColumns) {
                    TableHeaderCell("Задано ($transformUnit)")
                }
                
                TableHeaderCell("Эталон ↑")
                TableHeaderCell("Эталон ↓")
                if (showTransformColumns) {
                    TableHeaderCell("Преобр. ↑ ($deviceUnit)")
                    TableHeaderCell("Преобр. ↓ ($deviceUnit)")
                }
                TableHeaderCell("Погр. ↑ (%)")
                TableHeaderCell("Погр. ↓ (%)")
                TableHeaderCell("Вариация")
            }

            // Тело таблицы
            group.measurements.forEach { measurement ->
                val isErrorInc = abs(measurement.errorIncreasing) > group.maxAllowedError
                val isErrorDec = abs(measurement.errorDecreasing) > group.maxAllowedError
                val rowHasError = isErrorInc || isErrorDec
                
                val rowBackgroundColor = if (rowHasError) {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                } else {
                    Color.Unspecified
                }
                
                Row(
                    modifier = Modifier.background(rowBackgroundColor)
                ) {
                    // Точка
                    DataCellWithBorder(
                        text = "%.1f".format(measurement.scaleMark),
                        color = if (measurement.scaleMark == group.measurements.first().scaleMark ||
                            measurement.scaleMark == group.measurements.last().scaleMark) {
                            MaterialTheme.colorScheme.primary
                        } else Color.Unspecified
                    )
                    
                    // Ячейка для пересчитанного значения точки (из В в mV или A)
                    if (showTransformColumns) {
                        val formattedValue = viewModel.getFormattedTransformedScaleMark(
                            measurement = measurement,
                            transformFunctionParam = transformFunction,
                            lowerRangeParam = lowerRange,
                            upperRangeParam = upperRange
                        ) + " " + transformUnit
                        DataCellWithBorder(
                            text = formattedValue,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    
                    // Эталон отображаем как есть - уже в мВ или А
                    DataCellWithBorder("%.2f $transformUnit".format(measurement.referenceIncreasing))
                    DataCellWithBorder("%.2f $transformUnit".format(measurement.referenceDecreasing))
                    
                    if (showTransformColumns) {
                        // Преобразованные значения (из мВ или А обратно в В)
                        DataCellWithBorder("%.2f $deviceUnit".format(measurement.transformedValueInc))
                        DataCellWithBorder("%.2f $deviceUnit".format(measurement.transformedValueDec))
                    }
                    
                    // Погрешности из объекта measurement
                    DataCellWithBorder(
                        text = "%.2f%%".format(measurement.errorIncreasing),
                        color = if (isErrorInc) MaterialTheme.colorScheme.error else Color.Unspecified,
                        fontWeight = if (isErrorInc) FontWeight.Bold else FontWeight.Normal
                    )
                    DataCellWithBorder(
                        text = "%.2f%%".format(measurement.errorDecreasing),
                        color = if (isErrorDec) MaterialTheme.colorScheme.error else Color.Unspecified,
                        fontWeight = if (isErrorDec) FontWeight.Bold else FontWeight.Normal
                    )
                    
                    DataCellWithBorder("%.2f".format(measurement.variation))
                }
            }
            
            // Итоговая строка
            Row(
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                DataCellWithBorder(
                    text = "Макс.",
                    fontWeight = FontWeight.Bold
                )
                
                // Пустая ячейка для колонки пересчитанного значения в итоговой строке
                if (showTransformColumns) {
                    DataCellWithBorder(text = "—", fontWeight = FontWeight.Bold)
                }
                
                DataCellWithBorder(text = "—", fontWeight = FontWeight.Bold)
                DataCellWithBorder(text = "—", fontWeight = FontWeight.Bold)
                
                if (showTransformColumns) {
                    DataCellWithBorder(text = "—", fontWeight = FontWeight.Bold)
                    DataCellWithBorder(text = "—", fontWeight = FontWeight.Bold)
                }
                
                val isMaxErrorInc = group.maxErrorIncreasing > group.maxAllowedError
                val isMaxErrorDec = group.maxErrorDecreasing > group.maxAllowedError
                
                DataCellWithBorder(
                    text = "%.2f%%".format(group.maxErrorIncreasing),
                    color = if (isMaxErrorInc) MaterialTheme.colorScheme.error else Color.Unspecified,
                    fontWeight = FontWeight.Bold
                )
                DataCellWithBorder(
                    text = "%.2f%%".format(group.maxErrorDecreasing),
                    color = if (isMaxErrorDec) MaterialTheme.colorScheme.error else Color.Unspecified,
                    fontWeight = FontWeight.Bold
                )
                DataCellWithBorder(
                    text = "%.2f".format(group.maxVariation),
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Строка результата
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (group.hasErrors) 
                        "Результат: НЕ СООТВЕТСТВУЕТ (допуск ±${group.maxAllowedError}%)" 
                    else 
                        "Результат: СООТВЕТСТВУЕТ (допуск ±${group.maxAllowedError}%)",
                    color = if (group.hasErrors) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun TableHeaderCell(text: String) {
    Box(
        modifier = Modifier
            .width(90.dp)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
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
private fun DataCellWithBorder(
    text: String,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight = FontWeight.Normal
) {
    Box(
        modifier = Modifier
            .border(1.dp, MaterialTheme.colorScheme.outline)
            .width(90.dp)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = fontWeight),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}