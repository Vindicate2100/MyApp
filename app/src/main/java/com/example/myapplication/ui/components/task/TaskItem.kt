package com.example.myapplication.ui.components.task

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.local.entity.TaskEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TaskItem(
    taskEntity: TaskEntity,
    onToggleComplete: (Long) -> Unit,
    onDeleteTask: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope() // Добавляем scope для корутин

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isPressed) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                    else Color.Transparent
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            isPressed = true
                            scope.launch { // Обернули в корутину
                                onDeleteTask(taskEntity.id)
                                delay(300) // Теперь это внутри корутины
                                isPressed = false
                            }
                        },
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        }
                    )
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TaskTextContent(
                    title = taskEntity.title,
                    description = taskEntity.description,
                    modifier = Modifier.weight(1f)
                )

                TaskToggleButton(
                    isCompleted = taskEntity.isCompleted,
                    onToggle = { onToggleComplete(taskEntity.id) }
                )
            }
        }
    }
}

@Composable
private fun TaskTextContent(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
        if (description.isNotBlank()) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun TaskToggleButton(
    isCompleted: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, contentDesc, tint) = rememberTaskToggleAssets(isCompleted)

    IconButton(
        onClick = onToggle,
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDesc,
            tint = tint
        )
    }
}

@Composable
private fun rememberTaskToggleAssets(
    isCompleted: Boolean
): Triple<ImageVector, String, Color> {
    val colorScheme = MaterialTheme.colorScheme
    return remember(isCompleted) {
        if (isCompleted) {
            Triple(
                Icons.Filled.Done,
                "Завершено",
                colorScheme.primary
            )
        } else {
            Triple(
                Icons.Filled.Circle,
                "Не завершено",
                colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}