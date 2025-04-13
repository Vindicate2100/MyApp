package com.example.myapplication.components

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Крупный компонент для отображения текущей даты и времени в стиле Material 3
 * @param modifier Модификатор для настройки внешнего вида
 * @param showSeconds Флаг, указывающий нужно ли показывать секунды
 * @param textAlign Выравнивание текста
 * @param bold Сделать текст полужирным
 */
@Composable
fun CurrentDateTime(
    modifier: Modifier = Modifier,
    showSeconds: Boolean = true,
    textAlign: TextAlign? = null,
    bold: Boolean = true
) {
    var currentTime by remember { mutableStateOf(getCurrentDateTime(showSeconds)) }

    LaunchedEffect(showSeconds) {
        while (true) {
            delay(1000L)
            currentTime = getCurrentDateTime(showSeconds)
        }
    }

    Text(
        text = currentTime,
        modifier = modifier,
        style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal
        ),
        fontSize = 28.sp,
        color = LocalContentColor.current.copy(alpha = 0.9f),
        textAlign = textAlign
    )
}

private fun getCurrentDateTime(showSeconds: Boolean): String {
    val formatter = if (showSeconds) {
        DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm:ss")
            .withLocale(Locale.getDefault())
    } else {
        DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm")
            .withLocale(Locale.getDefault())
    }

    return LocalDateTime.now().format(formatter)
}