package com.zenzeros.kimon.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sleep_sessions",
    indices = [
        Index("startTimeEpochMs"),
        Index("endTimeEpochMs"),
        Index("dateString")
    ]
)
data class SleepSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTimeEpochMs: Long,
    val endTimeEpochMs: Long,
    val durationMinutes: Long,
    val qualityScore: Int = 80, // 0 - 100
    val status: Int = 0, // 0 = SUCCESSFUL, 1 = MISSING_DATA, 2 = NOT_DETECTED
    val source: String = "GOOGLE_SLEEP_API", // "GOOGLE_SLEEP_API", "HEALTH_CONNECT", "MANUAL"
    val dateString: String, // YYYY-MM-DD
    val syncedToHealthConnect: Boolean = false,
    val notes: String? = null,
    val appUsageJson: String? = null
)
