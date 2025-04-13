package com.example.myapplication.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.viewmodels.ClimateViewModel
import com.example.myapplication.components.InputFields
import com.example.myapplication.components.ActionButtons
import com.example.myapplication.tables.ClimateDataTable

@Composable
fun ClimateScreen(
    navController: NavController,
    viewModel: ClimateViewModel
) {
    // Градиентный фон
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF00BCD4),
            Color(0xFF009688)
        )
    )

    var temperature by remember { mutableStateOf("") }
    var humidity by remember { mutableStateOf("") }
    var pressure by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    val climateData by viewModel.climateData.collectAsState(emptyList())
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
            .padding(16.dp)
    ) {
        // Контейнер для полей ввода с белым фоном
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.9f))
                .padding(16.dp)
        ) {
            InputFields(
                temperature = temperature,
                onTemperatureChange = { temperature = it },
                humidity = humidity,
                onHumidityChange = { humidity = it },
                pressure = pressure,
                onPressureChange = { pressure = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ActionButtons(
                onSave = {
                    viewModel.saveData(temperature, humidity, pressure)
                    temperature = ""; humidity = ""; pressure = ""
                },
                onClear = { showDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Таблица с данными (можно тоже добавить фон при необходимости)
        ClimateDataTable(climateData)
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Подтверждение") },
            text = { Text("Удалить все записи за сегодня?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearTodayData()
                        showDialog = false
                        Toast.makeText(context, "Данные удалены", Toast.LENGTH_SHORT).show()
                    }
                ) { Text("Да", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}