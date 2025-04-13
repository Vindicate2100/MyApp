package com.example.myapplication.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarSection(
    currentMonth: YearMonth,
    selectedDate: String,
    markedDates: List<String>,
    onMonthChange: (YearMonth) -> Unit,
    onDateSelected: (String) -> Unit,
    onDateLongPressed: () -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }
    val monthFormatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy", Locale("ru")) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .shadow(4.dp, shape = RoundedCornerShape(12.dp)),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            CalendarHeader(currentMonth, monthFormatter, onMonthChange)
            DayOfWeekRow()
            CalendarGrid(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                markedDates = markedDates,
                dateFormatter = dateFormatter,
                onDateSelected = onDateSelected,
                onDateLongPressed = onDateLongPressed
            )
        }
    }
}

@Composable
private fun CalendarHeader(
    currentMonth: YearMonth,
    monthFormatter: DateTimeFormatter,
    onMonthChange: (YearMonth) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
        }

        Text(
            text = currentMonth.format(monthFormatter),
            style = MaterialTheme.typography.titleLarge
        )

        IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
        }
    }
}

@Composable
private fun DayOfWeekRow() {
    Row(modifier = Modifier.fillMaxWidth()) {
        listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { day ->
            Text(
                text = day,
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDate: String,
    markedDates: List<String>,
    dateFormatter: DateTimeFormatter,
    onDateSelected: (String) -> Unit,
    onDateLongPressed: () -> Unit
) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value
    val days = remember(currentMonth) { (1..daysInMonth).map { currentMonth.atDay(it) } }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        items((firstDayOfMonth - 1) % 7) {
            Box(modifier = Modifier.aspectRatio(1f))
        }

        items(days.size) { index ->
            val date = days[index]
            CalendarDayCell(
                date = date,
                isSelected = date.format(dateFormatter) == selectedDate,
                hasTasks = markedDates.contains(date.format(dateFormatter)),
                onDateSelected = { onDateSelected(date.format(dateFormatter)) },
                onDateLongPressed = onDateLongPressed
            )
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    isSelected: Boolean,
    hasTasks: Boolean,
    onDateSelected: () -> Unit,
    onDateLongPressed: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else Color.Transparent,
                shape = CircleShape
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onDateSelected() },
                    onLongPress = {
                        onDateSelected()
                        onDateLongPressed()
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                modifier = Modifier.padding(top = 4.dp),
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface
            )

            if (hasTasks) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}