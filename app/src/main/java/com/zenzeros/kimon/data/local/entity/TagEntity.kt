package com.zenzeros.kimon.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: String,
    val iconName: String = "ic_tag",
    val targetDailyMinutes: Int = 0,
    val isArchived: Boolean = false,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)
