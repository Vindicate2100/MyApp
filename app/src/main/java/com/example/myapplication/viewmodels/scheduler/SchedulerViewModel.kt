package com.example.myapplication.viewmodels.scheduler

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.dao.TaskDao
import com.example.myapplication.data.local.entity.TaskEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class SchedulerViewModel @Inject constructor(
    private val taskDao: TaskDao
) : ViewModel() {

    // Форматы дат с использованием современного java.time API
    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val monthFormatter = DateTimeFormatter.ofPattern("MM.yyyy")

    // Внутренние состояния
    private val _selectedDate = MutableStateFlow(getCurrentDate())
    private val _currentMonth = MutableStateFlow(getCurrentMonth())
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _showAddTaskDialog = MutableStateFlow(false)
    private val _showDeleteConfirmDialog = MutableStateFlow(false)
    private val _taskToDelete = MutableStateFlow<Long?>(null)

    // Публичное UI состояние
    data class UiState(
        val selectedDate: String = "",
        val taskEntities: List<TaskEntity> = emptyList(),
        val markedDates: List<String> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val showAddTaskDialog: Boolean = false,
        val showDeleteConfirmDialog: Boolean = false,
        val taskToDelete: Long? = null
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val tasksFlow: Flow<List<TaskEntity>> = _selectedDate
        .flatMapLatest { date -> taskDao.getTasksByDate(date) }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val markedDatesFlow: Flow<List<String>> = _currentMonth
        .flatMapLatest { month -> taskDao.getDatesWithTasks(month) }

    val uiState: StateFlow<UiState> = combine(
        _selectedDate,
        tasksFlow,
        markedDatesFlow,
        _isLoading,
        _error,
        _showAddTaskDialog,
        _showDeleteConfirmDialog,
        _taskToDelete
    ) { array ->
        // Распаковываем массив значений из combine
        val selectedDate = array[0] as String
        @Suppress("UNCHECKED_CAST")
        val tasks = array[1] as List<TaskEntity>
        @Suppress("UNCHECKED_CAST")
        val markedDates = array[2] as List<String>
        val isLoading = array[3] as Boolean
        val error = array[4] as String?
        val showAddDialog = array[5] as Boolean
        val showDeleteDialog = array[6] as Boolean
        val taskToDelete = array[7] as Long?
        
        UiState(
            selectedDate = selectedDate,
            taskEntities = tasks,
            markedDates = markedDates,
            isLoading = isLoading,
            error = error,
            showAddTaskDialog = showAddDialog,
            showDeleteConfirmDialog = showDeleteDialog,
            taskToDelete = taskToDelete
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState(selectedDate = getCurrentDate())
    )

    // Основные методы
    fun selectDate(date: String) {
        _selectedDate.value = date
    }

    fun setCurrentMonth(monthYear: String) {
        _currentMonth.value = monthYear
    }

    fun addTask(title: String, description: String = "") {
        if (title.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val taskEntity = TaskEntity(
                    title = title,
                    description = description,
                    date = _selectedDate.value,
                    isCompleted = false
                )
                taskDao.insert(taskEntity)
            } catch (e: Exception) {
                _error.value = "Ошибка при добавлении задачи: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleTaskCompletion(taskId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                taskDao.getTaskById(taskId)?.let { task ->
                    taskDao.update(task.copy(isCompleted = !task.isCompleted))
                }
            } catch (e: Exception) {
                _error.value = "Ошибка при обновлении задачи: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                taskDao.deleteById(taskId)
                _taskToDelete.value = null
            } catch (e: Exception) {
                _error.value = "Ошибка при удалении задачи: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
                _showDeleteConfirmDialog.value = false
            }
        }
    }

    // Методы управления диалогами
    fun showAddTaskDialog() {
        _showAddTaskDialog.value = true
    }

    fun hideAddTaskDialog() {
        _showAddTaskDialog.value = false
    }

    fun showDeleteConfirmDialog(taskId: Long) {
        _taskToDelete.value = taskId
        _showDeleteConfirmDialog.value = true
    }

    fun hideDeleteConfirmDialog() {
        _showDeleteConfirmDialog.value = false
    }

    fun clearError() {
        _error.value = null
    }

    // Вспомогательные методы
    private fun getCurrentDate(): String {
        return LocalDate.now().format(dateFormatter)
    }

    private fun getCurrentMonth(): String {
        return LocalDate.now().format(monthFormatter)
    }
} 