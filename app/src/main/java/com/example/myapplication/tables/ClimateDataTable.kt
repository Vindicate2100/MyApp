package com.example.myapplication.tables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.entity.ClimateData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Компонент таблицы с данными
 */
@Composable
fun ClimateDataTable(
    data: List<ClimateData>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        if (data.isEmpty()) {
            EmptyState()
        } else {
            Column {
                // Шапка таблицы
                TableHeader()

                // Тело таблицы
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(data) { item ->
                        ClimateDataRow(item)
                        TableDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Нет данных для отображения",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun TableHeader() {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val dividerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.9f)

            TableCell("Дата", 1f, MaterialTheme.typography.titleSmall)
            VerticalDivider(
                color = dividerColor,
                thickness = 1.dp,
                modifier = Modifier.height(28.dp)
            )
            TableCell("Температура", 1f, MaterialTheme.typography.titleSmall)
            VerticalDivider(
                color = dividerColor,
                thickness = 1.dp,
                modifier = Modifier.height(28.dp)
            )
            TableCell("Влажность", 1f, MaterialTheme.typography.titleSmall)
            VerticalDivider(
                color = dividerColor,
                thickness = 1.dp,
                modifier = Modifier.height(28.dp)
            )
            TableCell("Давление", 1f, MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun ClimateDataRow(data: ClimateData) {
    val dividerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableCell(formatDate(data.timestamp), 1f)
        VerticalDivider(
            color = dividerColor,
            thickness = 1.dp,
            modifier = Modifier.height(24.dp)
        )
        TableCell("${data.temperature} °C", 1f)
        VerticalDivider(
            color = dividerColor,
            thickness = 1.dp,
            modifier = Modifier.height(24.dp)
        )
        TableCell("${data.humidity}%", 1f)
        VerticalDivider(
            color = dividerColor,
            thickness = 1.dp,
            modifier = Modifier.height(24.dp)
        )
        TableCell("${data.pressure} hPa", 1f)
    }
}

@Composable
private fun RowScope.TableCell(
    text: String,
    weight: Float,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = textStyle,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun TableDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 12.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
    )
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        .format(Date(timestamp))
}