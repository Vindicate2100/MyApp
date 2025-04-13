package com.example.myapplication.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.viewmodels.VerificationViewModel

@Composable
fun ResultScreen(
    viewModel: VerificationViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Экран результатов (заглушка)",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            "Здесь будут отображаться итоговые результаты поверки",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.weight(1f))

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onBack,
                modifier = Modifier.width(200.dp)
            ) {
                Text("Назад")
            }

            Button(
                onClick = onFinish,
                modifier = Modifier.width(200.dp)
            ) {
                Text("Завершить")
            }
        }
    }
}