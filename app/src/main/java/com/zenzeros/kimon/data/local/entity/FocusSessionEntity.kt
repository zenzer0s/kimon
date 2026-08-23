package com.zenzeros.kimon.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "focus_sessions",
    foreignKeys = [
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("tagId"),
        Index("startTimeEpochMs"),
        Index("endTimeEpochMs")
    ]
)
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tagId: Long? = null,
    val sessionType: String = "POMODORO", // POMODORO, SHORT_BREAK, LONG_BREAK
    val startTimeEpochMs: Long,
    val endTimeEpochMs: Long,
    val targetDurationSeconds: Int,
    val actualDurationSeconds: Int,
    val isCompleted: Boolean = true,
    val notes: String? = null
)
