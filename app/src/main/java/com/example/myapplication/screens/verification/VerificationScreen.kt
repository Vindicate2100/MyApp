package com.example.myapplication.screens.verification

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.myapplication.viewmodels.verification.VerificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationScreen(
    navController: NavController,
    viewModel: VerificationViewModel = hiltViewModel()
) {
    var currentStep by remember { mutableIntStateOf(0) }
    val steps = listOf("Подготовка", "Измерения", "Результат")
    val saveStatus by viewModel.saveStatus.collectAsState()
    
    // Флаг, указывающий, что нужно завершить процесс поверки
    var shouldFinish by remember { mutableStateOf(false) }

    LaunchedEffect(saveStatus, shouldFinish) {
        // Завершаем только если явно запрошено завершение и сохранение успешно
        if (saveStatus is VerificationViewModel.SaveStatus.Success && shouldFinish) {
            navController.popBackStack()
            shouldFinish = false // Сбрасываем флаг
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Процедура поверки") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            LinearProgressIndicator(
                progress = { (currentStep + 1) / steps.size.toFloat() },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Шаг ${currentStep + 1} из ${steps.size}: ${steps[currentStep]}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            when (currentStep) {
                0 -> PreparationScreen(
                    onNext = { currentStep++ },
                )

                1 -> MeasurementScreen(
                    onBack = { currentStep-- },
                    onNext = { currentStep++ }
                )

                2 -> ResultScreen(
                    onBack = { currentStep-- },
                    onFinish = { 
                        // При завершении сохраняем и устанавливаем флаг для закрытия экрана
                        viewModel.saveVerification() 
                        shouldFinish = true
                    }
                )
            }
        }
    }
}