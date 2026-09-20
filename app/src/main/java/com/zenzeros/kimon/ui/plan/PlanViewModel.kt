package com.zenzeros.kimon.ui.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zenzeros.kimon.data.local.entity.TagEntity
import com.zenzeros.kimon.data.local.entity.TaskEntity
import com.zenzeros.kimon.data.repository.TagRepository
import com.zenzeros.kimon.data.repository.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlanViewModel(
    private val taskRepository: TaskRepository,
    private val tagRepository: TagRepository
) : ViewModel() {

    val tasks: StateFlow<List<TaskEntity>> = taskRepository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tags: StateFlow<List<TagEntity>> = tagRepository.getAllActiveTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTask(title: String, category: String = "Focus", pomodoros: Int = 1) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val minOrder = tasks.value.filter { !it.isCompleted }.minOfOrNull { it.displayOrder } ?: 0
            taskRepository.insertTask(
                TaskEntity(
                    title = title.trim(),
                    category = category,
                    estimatedPomodoros = pomodoros,
                    isCompleted = false,
                    displayOrder = minOrder - 1
                )
            )
        }
    }

    fun updateTaskOrder(reorderedTasks: List<TaskEntity>) {
        viewModelScope.launch {
            val updated = reorderedTasks.mapIndexed { index, task ->
                if (task.displayOrder != index) task.copy(displayOrder = index) else task
            }
            taskRepository.updateTasks(updated)
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            taskRepository.updateTask(
                task.copy(isCompleted = !task.isCompleted)
            )
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
        }
    }

    fun clearCompletedTasks() {
        viewModelScope.launch {
            tasks.value.filter { it.isCompleted }.forEach {
                taskRepository.deleteTask(it)
            }
        }
    }

    companion object {
        fun Factory(
            taskRepository: TaskRepository,
            tagRepository: TagRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PlanViewModel(taskRepository, tagRepository) as T
            }
        }
    }
}
