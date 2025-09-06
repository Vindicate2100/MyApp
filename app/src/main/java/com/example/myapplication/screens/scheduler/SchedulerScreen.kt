package com.example.myapplication.screens.scheduler

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.myapplication.data.local.entity.TaskEntity
import com.example.myapplication.ui.components.task.CalendarSection
import com.example.myapplication.ui.components.task.TaskListSection
import com.example.myapplication.viewmodels.scheduler.SchedulerViewModel
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulerScreen(
    navController: NavController,
    viewModel: SchedulerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Обработка ошибок
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearError()
            }
        }
    }

    SchedulerScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onDateSelected = viewModel::selectDate,
        onMonthChanged = viewModel::setCurrentMonth,
        onToggleTaskComplete = viewModel::toggleTaskCompletion,
        onAddTask = viewModel::addTask,
        onDeleteTask = viewModel::deleteTask,
        onShowAddTaskDialog = viewModel::showAddTaskDialog,
        onHideAddTaskDialog = viewModel::hideAddTaskDialog,
        onShowDeleteConfirmDialog = viewModel::showDeleteConfirmDialog,
        onHideDeleteConfirmDialog = viewModel::hideDeleteConfirmDialog
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SchedulerScreenContent(
    uiState: SchedulerViewModel.UiState,
    snackbarHostState: SnackbarHostState,
    onDateSelected: (String) -> Unit,
    onMonthChanged: (String) -> Unit,
    onToggleTaskComplete: (Long) -> Unit,
    onAddTask: (String, String) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onShowAddTaskDialog: () -> Unit,
    onHideAddTaskDialog: () -> Unit,
    onShowDeleteConfirmDialog: (Long) -> Unit,
    onHideDeleteConfirmDialog: () -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    
    val selectedDate = uiState.selectedDate
    val tasks = uiState.taskEntities
    val markedDates = uiState.markedDates
    val isLoading = uiState.isLoading
    val showAddTaskDialog = uiState.showAddTaskDialog
    val showDeleteConfirmDialog = uiState.showDeleteConfirmDialog
    val taskToDelete = uiState.taskToDelete
    val scope = rememberCoroutineScope()
    
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF00BCD4),
            Color(0xFF009688)
        )
    )

    val monthFormat = remember { DateTimeFormatter.ofPattern("MM.yyyy") }

    LaunchedEffect(currentMonth) {
        onMonthChanged(currentMonth.format(monthFormat))
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
                    onDateSelected = { date -> onDateSelected(date) },
                    onDateLongPressed = { onShowAddTaskDialog() }
                )

                SchedulerHeader(selectedDate = selectedDate)

                TaskListContainer(
                    selectedDate = selectedDate,
                    taskEntities = tasks,
                    onToggleComplete = onToggleTaskComplete,
                    onDeleteTask = onShowDeleteConfirmDialog
                )
            }
        }
        
        // Индикатор загрузки
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    // Диалоги
    if (showAddTaskDialog) {
        AddTaskDialog(
            selectedDate = selectedDate,
            onDismiss = { onHideAddTaskDialog() },
            onConfirm = { title, description ->
                onAddTask(title, description)
                scope.launch {
                    snackbarHostState.showSnackbar("Задача добавлена")
                }
            }
        )
    }

    if (showDeleteConfirmDialog && taskToDelete != null) {
        ConfirmDeleteDialog(
            onDismiss = { onHideDeleteConfirmDialog() },
            onConfirm = {
                taskToDelete.let { taskId ->
                    onDeleteTask(taskId)
                    scope.launch {
                        snackbarHostState.showSnackbar("Задача удалена")
                    }
                }
            }
        )
    }
}

@Composable
private fun SchedulerHeader(selectedDate: String) {
    Text(
        text = "Задачи на $selectedDate",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun TaskListContainer(
    selectedDate: String,
    taskEntities: List<TaskEntity>,
    onToggleComplete: (Long) -> Unit,
    onDeleteTask: (Long) -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        tonalElevation = 2.dp
    ) {
        TaskListSection(
            selectedDate = selectedDate,
            taskEntities = taskEntities,
            onToggleComplete = onToggleComplete,
            onDeleteTask = onDeleteTask
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