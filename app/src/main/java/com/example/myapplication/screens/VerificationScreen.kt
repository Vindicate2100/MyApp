package com.example.myapplication.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.myapplication.screens.verification.MeasurementScreen
import com.example.myapplication.screens.verification.PreparationScreen
import com.example.myapplication.viewmodels.VerificationViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationScreen(
    navController: NavController,
    viewModel: VerificationViewModel = hiltViewModel()
) {
    var currentStep by remember { mutableIntStateOf(0) }
    val steps = listOf("Подготовка", "Измерения", "Результат")
    val saveStatus by viewModel.saveStatus.collectAsState()


    LaunchedEffect(saveStatus) {
        if (saveStatus is VerificationViewModel.SaveStatus.Success) {
            navController.popBackStack()
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
                    onNext = { currentStep++ }
                )

                1 -> MeasurementScreen(
                    onBack = { currentStep-- },
                    onNext = { currentStep++ }
                )

                2 -> ResultScreen(
                    onBack = { currentStep-- },
                    onFinish = { viewModel.saveVerification() }
                )
            }
        }
    }
}