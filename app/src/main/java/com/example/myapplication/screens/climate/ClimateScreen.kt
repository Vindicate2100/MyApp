package com.example.myapplication.screens.climate

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.myapplication.ui.components.common.ActionButtons
import com.example.myapplication.ui.components.climate.InputFields
import com.example.myapplication.ui.components.tables.ClimateDataTable
import com.example.myapplication.viewmodels.climate.ClimateViewModel

// Локальное состояние для хранения вводимых пользователем значений, 
// которые еще не были переданы в ViewModel для валидации или сохранения.
private data class ClimateInputFormState(
    val temperature: String = "",
    val humidity: String = "",
    val pressure: String = ""
)

@Composable
fun ClimateScreen(
    navController: NavController,
    viewModel: ClimateViewModel = hiltViewModel()
) {
    val hasDataToday by viewModel.hasDataToday().collectAsState(initial = false)
    // Состояние для полей ввода, используется локально в Composable
    var inputFormState by remember { mutableStateOf(ClimateInputFormState()) }
    // Состояние валидации из ViewModel
    val validationState by viewModel.validationState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    val climateData by viewModel.climateEntity.collectAsState(emptyList())
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(createGradientBrush())
            .padding(16.dp)
    ) {
        ClimateInputSection(
            temperature = inputFormState.temperature,
            humidity = inputFormState.humidity,
            pressure = inputFormState.pressure,
            onTemperatureChange = { 
                inputFormState = inputFormState.copy(temperature = it)
                if (validationState.temperatureError != null) viewModel.clearValidationErrors() 
            },
            onHumidityChange = { 
                inputFormState = inputFormState.copy(humidity = it) 
                if (validationState.humidityError != null) viewModel.clearValidationErrors()
            },
            onPressureChange = { 
                inputFormState = inputFormState.copy(pressure = it) 
                if (validationState.pressureError != null) viewModel.clearValidationErrors()
            },
            validationState = validationState, // Передаем полное состояние валидации
            onSave = {
                if (viewModel.saveData(
                    temperature = inputFormState.temperature,
                    humidity = inputFormState.humidity,
                    pressure = inputFormState.pressure
                )) {
                    inputFormState = ClimateInputFormState() // Reset form if save was successful
                    Toast.makeText(context, "Данные сохранены", Toast.LENGTH_SHORT).show()
                } else {
                    // Ошибки уже будут отображены через validationState
                    Toast.makeText(context, "Проверьте введенные данные", Toast.LENGTH_SHORT).show()
                }
            },
            onClear = { showDialog = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        DataStatusCard(hasDataToday)

        Spacer(modifier = Modifier.height(16.dp))

        ClimateDataTable(climateData)
    }

    if (showDialog) {
        ClearDataDialog(
            onDismiss = { showDialog = false },
            onConfirm = {
                viewModel.clearTodayData()
                viewModel.clearValidationErrors() // Также очищаем ошибки валидации
                inputFormState = ClimateInputFormState() // Сбрасываем поля ввода
                showDialog = false
                Toast.makeText(context, "Данные удалены", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun ClimateInputSection(
    temperature: String,
    humidity: String,
    pressure: String,
    onTemperatureChange: (String) -> Unit,
    onHumidityChange: (String) -> Unit,
    onPressureChange: (String) -> Unit,
    validationState: ClimateViewModel.ClimateValidationState, // Принимаем состояние валидации
    onSave: () -> Unit,
    onClear: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.9f))
            .padding(16.dp)
    ) {
        InputFields(
            temperature = temperature,
            onTemperatureChange = onTemperatureChange,
            humidity = humidity,
            onHumidityChange = onHumidityChange,
            pressure = pressure,
            onPressureChange = onPressureChange,
            temperatureError = validationState.temperatureError, // Используем ошибки из ViewModel
            humidityError = validationState.humidityError,
            pressureError = validationState.pressureError
        )

        Spacer(modifier = Modifier.height(16.dp))

        ActionButtons(
            onSave = onSave,
            onClear = onClear
        )
    }
}

@Composable
private fun DataStatusCard(hasDataToday: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasDataToday) Color(0xFFE0F2F1) else Color(0xFFFFEBEE)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // Увеличен внутренний отступ
            horizontalArrangement = Arrangement.Center, // Центрирование по горизонтали
            verticalAlignment = Alignment.CenterVertically // Центрирование по вертикали
        ) {
            Icon(
                imageVector = if (hasDataToday) Icons.Default.CheckCircle else Icons.Default.Info,
                contentDescription = null,
                tint = if (hasDataToday) Color(0xFF00C853) else Color(0xFFFF6D00),
                modifier = Modifier.padding(end = 8.dp) // Добавим отступ справа от иконки
            )
            Text(
                text = if (hasDataToday) "Данные за сегодня есть!" else "Нет данных за сегодня.",
                color = if (hasDataToday) Color(0xFF004D40) else Color(0xFFB71C1C),
                textAlign = TextAlign.Center // Выравнивание текста по центру
            )
        }
    }
}

@Composable
private fun ClearDataDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Подтверждение") },
        text = { Text("Удалить все записи за сегодня?") },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text("Да", color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

private fun createGradientBrush() = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF00BCD4),
        Color(0xFF009688)
    )
)
