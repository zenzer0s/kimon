package com.zenzeros.kimon.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String = "Focus",
    val estimatedPomodoros: Int = 1,
    val completedPomodoros: Int = 0,
    val isCompleted: Boolean = false,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)
