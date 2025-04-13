package com.example.myapplication.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.dao.TaskDao
import com.example.myapplication.data.entity.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
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

    // Публичное UI состояние
    data class UiState(
        val selectedDate: String = "",
        val tasks: List<Task> = emptyList(),
        val markedDates: List<String> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeData() {
        viewModelScope.launch {
            combine(
                _selectedDate.flatMapLatest { date ->
                    taskDao.getTasksByDate(date)
                },
                _currentMonth.flatMapLatest { month ->
                    taskDao.getDatesWithTasks(month)
                },
                _isLoading,
                _error
            ) { tasks, markedDates, loading, error ->
                UiState(
                    selectedDate = _selectedDate.value,
                    tasks = tasks,
                    markedDates = markedDates,
                    isLoading = loading,
                    error = error
                )
            }.collect { newState ->
                _uiState.update { newState }
            }
        }
    }

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
                val task = Task(
                    title = title,
                    description = description,
                    date = _selectedDate.value,
                    isCompleted = false
                )
                taskDao.insert(task)
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
            } catch (e: Exception) {
                _error.value = "Ошибка при удалении задачи: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
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