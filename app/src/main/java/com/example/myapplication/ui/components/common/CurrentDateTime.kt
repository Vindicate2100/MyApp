package com.example.myapplication.ui.components.common

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
    val pattern = if (showSeconds) DATE_TIME_PATTERN_WITH_SECONDS else DATE_TIME_PATTERN

    var currentTime by remember(pattern) {
        mutableStateOf(formatCurrentTime(pattern))
    }

    LaunchedEffect(pattern) {
        while (true) {
            delay(1000L)
            currentTime = formatCurrentTime(pattern)
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

private const val DATE_TIME_PATTERN = "dd MMMM yyyy, HH:mm"
private const val DATE_TIME_PATTERN_WITH_SECONDS = "dd MMMM yyyy, HH:mm:ss"

private fun formatCurrentTime(pattern: String): String {
    val formatter = DateTimeFormatter.ofPattern(pattern).withLocale(Locale.getDefault())
    return LocalDateTime.now().format(formatter)
}