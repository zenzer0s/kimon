@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui.pomodoro

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import com.zenzeros.kimon.data.local.entity.TagEntity

enum class PomodoroMode(val label: String, val durationMinutes: Int) {
    FOCUS("Focus", 25),
    SHORT_BREAK("Short Break", 5),
    LONG_BREAK("Long Break", 15)
}

enum class TimerStatus {
    IDLE,
    RUNNING,
    PAUSED
}

enum class ClockStyle(val label: String) {
    FLIP_CARD("Flip Card"),
    CONCENTRIC("Concentric")
}

data class PomodoroTask(
    val id: String,
    val title: String,
    val totalSessions: Int = 4,
    val completedSessions: Int = 2
)

data class PomodoroUiState(
    val currentMode: PomodoroMode = PomodoroMode.FOCUS,
    val timerStatus: TimerStatus = TimerStatus.IDLE,
    val clockStyle: ClockStyle = ClockStyle.CONCENTRIC,
    val remainingSeconds: Int = 25 * 60,
    val totalSeconds: Int = 25 * 60,
    val currentSessionIndex: Int = 0,
    val totalDailySessions: Int = 4,
    val currentTask: PomodoroTask? = null,
    val selectedTag: TagEntity? = null,
    val totalFocusMinutesToday: Int = 0,
    val streakDays: Int = 0
) {
    val progress: Float
        get() = if (totalSeconds > 0) (totalSeconds - remainingSeconds).toFloat() / totalSeconds.toFloat() else 0f

    val elapsedSeconds: Int
        get() = totalSeconds - remainingSeconds

    val minutesPart: Int
        get() = remainingSeconds / 60

    val secondsPart: Int
        get() = remainingSeconds % 60

    val formattedTime: String
        get() = String.format("%02d:%02d", minutesPart, secondsPart)
}
