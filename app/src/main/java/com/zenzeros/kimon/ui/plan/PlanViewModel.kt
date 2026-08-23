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
            taskRepository.insertTask(
                TaskEntity(
                    title = title.trim(),
                    category = category,
                    estimatedPomodoros = pomodoros,
                    isCompleted = false
                )
            )
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
