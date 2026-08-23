package com.zenzeros.kimon.data.repository

import com.zenzeros.kimon.data.local.dao.TaskDao
import com.zenzeros.kimon.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()

    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)

    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)
}
