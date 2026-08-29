package com.zenzeros.kimon.data.backup

import kotlinx.serialization.Serializable

@Serializable
data class KimonBackup(
    val version: Int = 1,
    val appVersion: String = "1.0",
    val backupDateEpochMs: Long = System.currentTimeMillis(),
    val settings: KimonSettingsBackup? = null,
    val tags: List<TagBackup> = emptyList(),
    val focusSessions: List<FocusSessionBackup> = emptyList(),
    val tasks: List<TaskBackup> = emptyList(),
    val sleepSessions: List<SleepSessionBackup> = emptyList()
)

@Serializable
data class KimonSettingsBackup(
    val workDurationMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val sessionsBeforeLongBreak: Int = 4,
    val dailyGoalMinutes: Int = 120,
    val autoStartBreaks: Boolean = false,
    val autoStartPomodoros: Boolean = false,
    val keepScreenOn: Boolean = false,
    val dndEnabled: Boolean = false,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val headphoneMode: Boolean = false,
    val alarmSoundUri: String = "",
    val alarmSoundTitle: String = "Default",
    val themeMode: String = "SYSTEM",
    val themePalette: String = "DYNAMIC",
    val themeColor: String = "Color.White",
    val amoledBlack: Boolean = false,
    val clockStyle: String = "DIAL",
    val dialTickAnimation: Boolean = false,
    val sleepMonitoringEnabled: Boolean = false,
    val healthConnectSyncEnabled: Boolean = false,
    val sleepGoalMinutes: Int = 480
)

@Serializable
data class TagBackup(
    val id: Long = 0,
    val name: String,
    val colorHex: String,
    val iconName: String = "ic_tag",
    val targetDailyMinutes: Int = 0,
    val isArchived: Boolean = false,
    val createdAtEpochMs: Long = 0
)

@Serializable
data class FocusSessionBackup(
    val id: Long = 0,
    val tagId: Long? = null,
    val sessionType: String = "POMODORO",
    val startTimeEpochMs: Long,
    val endTimeEpochMs: Long,
    val targetDurationSeconds: Int,
    val actualDurationSeconds: Int,
    val isCompleted: Boolean = true,
    val notes: String? = null
)

@Serializable
data class TaskBackup(
    val id: Long = 0,
    val title: String,
    val category: String = "Focus",
    val estimatedPomodoros: Int = 1,
    val completedPomodoros: Int = 0,
    val isCompleted: Boolean = false,
    val createdAtEpochMs: Long = 0
)

@Serializable
data class SleepSessionBackup(
    val id: Long = 0,
    val startTimeEpochMs: Long,
    val endTimeEpochMs: Long,
    val durationMinutes: Long,
    val qualityScore: Int = 80,
    val status: Int = 0,
    val source: String = "GOOGLE_SLEEP_API",
    val dateString: String,
    val syncedToHealthConnect: Boolean = false,
    val notes: String? = null
)

data class RestoreSummary(
    val focusSessionsRestored: Int,
    val tagsRestored: Int,
    val tasksRestored: Int,
    val sleepSessionsRestored: Int,
    val settingsRestored: Boolean
)
