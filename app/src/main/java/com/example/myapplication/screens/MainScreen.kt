package com.example.myapplication.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Verified
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.R.drawable.background
import com.example.myapplication.components.CurrentDateTime
import com.example.myapplication.components.MenuButton

/**
 * Главный экран приложения с навигационными кнопками
 */
@Composable
fun MainScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.2f))
    ) {
        // Фоновое изображение
        Image(
            painter = painterResource(id = background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Контент — текущее время сверху
        CurrentDateTime(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 32.dp)
        )

        // Кнопки по центру
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 48.dp), // немного отступа снизу
            contentAlignment = Alignment.Center
        ) {
            NavigationButtons(navController)
        }
    }
}

@Composable
private fun NavigationButtons(navController: NavController) {
    Column(
        modifier = Modifier
            .widthIn(max = 360.dp)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val buttonSpacing = 8.dp

        listOf(
            Triple("Журнал климатических условий", Icons.Filled.Thermostat, "climate"),
            Triple("Планировщик задач", Icons.Filled.CalendarToday, "scheduler"),
            Triple("Поверка", Icons.Filled.Verified, "verification"),
            Triple("Журнал", Icons.Filled.History, "VerificationJournal")
        ).forEach { (text, icon, route) ->
            MenuButton(
                text = text,
                icon = icon,
                onClick = { navController.navigate(route) }
            )
            Spacer(modifier = Modifier.height(buttonSpacing))
        }
    }
}