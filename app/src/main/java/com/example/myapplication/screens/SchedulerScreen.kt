package com.example.myapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.myapplication.components.CalendarSection
import com.example.myapplication.components.TasksListSection
import com.example.myapplication.viewmodels.SchedulerViewModel
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulerScreen(
    navController: NavController,
    viewModel: SchedulerViewModel = hiltViewModel()
) {
    var showDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var taskToDelete by remember { mutableStateOf<Long?>(null) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedDate = uiState.selectedDate
    val tasks = uiState.tasks
    val markedDates = uiState.markedDates

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF00BCD4),
            Color(0xFF009688)
        )
    )

    val monthFormat = remember { DateTimeFormatter.ofPattern("MM.yyyy") }

    LaunchedEffect(currentMonth) {
        viewModel.setCurrentMonth(currentMonth.format(monthFormat))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            contentWindowInsets = WindowInsets(0.dp)
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                CalendarSection(
                    currentMonth = currentMonth,
                    selectedDate = selectedDate,
                    markedDates = markedDates,
                    onMonthChange = { currentMonth = it },
                    onDateSelected = { date -> viewModel.selectDate(date) },
                    onDateLongPressed = { showDialog = true }
                )

                Text(
                    text = "Задачи на $selectedDate",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Обертка для TasksListSection с отступами и скруглением
                Surface(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp), // Явное указание отступов
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    tonalElevation = 2.dp
                ) {
                    TasksListSection(
                        selectedDate = selectedDate,
                        tasks = tasks,
                        onToggleComplete = viewModel::toggleTaskCompletion,
                        onDeleteTask = { taskId ->
                            taskToDelete = taskId
                            showDeleteConfirm = true
                        }
                    )
                }
            }
        }
    }

    // Остальной код с диалогами остается без изменений
    if (showDialog) {
        AddTaskDialog(
            selectedDate = selectedDate,
            onDismiss = { showDialog = false },
            onConfirm = { title, description ->
                viewModel.addTask(title, description)
                showDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar("Задача добавлена")
                }
            }
        )
    }

    if (showDeleteConfirm) {
        ConfirmDeleteDialog(
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                taskToDelete?.let { taskId ->
                    viewModel.deleteTask(taskId)
                    scope.launch {
                        snackbarHostState.showSnackbar("Задача удалена")
                    }
                }
                showDeleteConfirm = false
            }
        )
    }
}

@Composable
private fun ConfirmDeleteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Удалить задачу?") },
        text = { Text("Вы уверены, что хотите удалить эту задачу?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text("Удалить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
private fun AddTaskDialog(
    selectedDate: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая задача") },
        text = {
            Column {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Дата: $selectedDate",
                    modifier = Modifier.padding(vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, description) },
                enabled = title.isNotBlank()
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}