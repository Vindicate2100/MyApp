package com.example.myapplication.screens.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.myapplication.R.drawable.background // Явный импорт ресурса фона
import com.example.myapplication.ui.components.common.CurrentDateTime
import com.example.myapplication.ui.components.common.MenuButton
import com.example.myapplication.navigation.Screen
import com.example.myapplication.navigation.navigateTo

/**
 * Главный экран приложения.
 * Отображает фоновое изображение, текущее время и кнопки навигации.
 *
 * @param navController Контроллер навигации для перехода между экранами.
 */
@Composable
fun MainScreen(navController: NavController) {
    // Box используется как контейнер, занимающий весь доступный размер экрана.
    // Это позволяет размещать другие элементы поверх него.
    Box(modifier = Modifier.fillMaxSize()) {
        // Фоновое изображение экрана.
        Image(
            painter = painterResource(id = background), // Загрузка изображения из drawable ресурсов
            contentDescription = null, // Описание для доступности (можно добавить, если изображение не чисто декоративное)
            modifier = Modifier.fillMaxSize(), // Растягиваем изображение на весь контейнер Box
            contentScale = ContentScale.Crop // Масштабируем изображение так, чтобы оно заполнило контейнер, обрезая лишнее
        )

        // Полупрозрачная вуаль для затемнения фонового изображения.
        // Это улучшает читаемость элементов на переднем плане.
        Box(
            modifier = Modifier
                .matchParentSize() // Размер этого Box будет таким же, как у родительского Box (т.е. на весь экран)
                .background(Color.Black.copy(alpha = 0.2f)) // Черный цвет с 20% непрозрачностью
        )

        // Отображение текущей даты и времени.
        // Используется кастомный компонент CurrentDateTime.
        CurrentDateTime(
            modifier = Modifier
                .align(Alignment.TopCenter) // Выравниваем компонент по центру верхней части родительского Box
                .padding(top = 32.dp) // Добавляем отступ сверху
        )

        // Контейнер для кнопок навигации.
        // Располагается по центру экрана с отступом снизу.
        Box(
            modifier = Modifier
                .fillMaxSize() // Занимает все доступное пространство (поверх предыдущих Box)
                .padding(bottom = 48.dp), // Отступ снизу, чтобы кнопки не прилипали к краю экрана
            contentAlignment = Alignment.Center // Содержимое этого Box (кнопки) будет выровнено по центру
        ) {
            // Вызов приватной Composable функции для отрисовки навигационных кнопок.
            NavigationButtons(navController)
        }
    }
}

/**
 * Приватная Composable функция, отвечающая за отображение навигационных кнопок.
 *
 * @param navController Контроллер навигации для обработки нажатий на кнопки.
 */
@Composable
private fun NavigationButtons(navController: NavController) {
    // Column располагает дочерние элементы вертикально.
    Column(
        modifier = Modifier
            .widthIn(max = 360.dp) // Ограничиваем максимальную ширину колонки для лучшего вида на широких экранах
            .padding(vertical = 16.dp), // Вертикальные отступы внутри колонки
        horizontalAlignment = Alignment.CenterHorizontally, // Выравниваем дочерние элементы (кнопки) по центру горизонтали
        verticalArrangement = Arrangement.spacedBy(8.dp) // Добавляем фиксированное пространство (8.dp) между кнопками
    ) {
        // Создаем список троек (текст кнопки, иконка, экран назначения).
        // Это позволяет легко добавлять или изменять кнопки.
        listOf(
            Triple("Журнал климатических условий", Icons.Filled.Thermostat, Screen.Climate),
            Triple("Планировщик задач", Icons.Filled.CalendarToday, Screen.Scheduler),
            Triple("Поверка", Icons.Filled.Verified, Screen.Verification),
            Triple("Журнал поверок", Icons.Filled.History, Screen.VerificationJournal),
            Triple("Список приборов", Icons.Filled.History, Screen.DeviceList) // Обратите внимание: иконка History используется дважды
        ).forEach { (text, icon, screen) -> // Итерируемся по списку и деструктурируем каждую тройку
            // Для каждого элемента списка создаем MenuButton.
            MenuButton(
                text = text, // Текст на кнопке
                icon = icon, // Иконка для кнопки
                onClick = { navController.navigateTo(screen) } // Действие при нажатии: навигация на указанный экран
            )
        }
    }
}
