package com.zenzeros.kimon.ui.sleep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zenzeros.kimon.data.local.entity.SleepSessionEntity
import com.zenzeros.kimon.data.repository.SleepRepository
import com.zenzeros.kimon.data.repository.UserSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DaySleepStat(
    val dayLabel: String,
    val dayNumber: String,
    val durationMinutes: Long,
    val isToday: Boolean,
    val dateEpochMs: Long
)

data class SleepUiState(
    val isMonitoringEnabled: Boolean = false,
    val isHealthConnectSyncEnabled: Boolean = false,
    val hasPermission: Boolean = false,
    val latestSession: SleepSessionEntity? = null,
    val recentSessions: List<SleepSessionEntity> = emptyList(),
    val weeklyDays: List<DaySleepStat> = emptyList(),
    val weeklyAverageMinutes: Long = 0,
    val sleepScore: Int = 0,
    val sleepGoalMinutes: Int = 480,
    val isLoading: Boolean = false
)

class SleepViewModel(
    private val sleepRepository: SleepRepository,
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)

    val uiState: StateFlow<SleepUiState> = combine(
        sleepRepository.getAllSessions(),
        userSettingsRepository.sleepMonitoringEnabled,
        userSettingsRepository.healthConnectSyncEnabled,
        userSettingsRepository.sleepGoalMinutes,
        _isLoading
    ) { sessions, isMonitoring, isHealthSync, goalMinutes, loading ->
        val latest = sessions.firstOrNull()
        val recent = sessions.take(15)

        // Compute 7-day weekly breakdown
        val weeklyBreakdown = computeWeeklyBreakdown(sessions)
        val nonZeroWeekly = weeklyBreakdown.filter { it.durationMinutes > 0 }
        val avgMins = if (nonZeroWeekly.isNotEmpty()) {
            nonZeroWeekly.map { it.durationMinutes }.average().toLong()
        } else {
            latest?.durationMinutes ?: 0
        }

        // Calculate sleep score (target based on goalMinutes)
        val score = latest?.qualityScore ?: if (avgMins > 0) {
            val ratio = (avgMins.toFloat() / goalMinutes.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
            (ratio * 100).toInt().coerceIn(40, 98)
        } else 0

        SleepUiState(
            isMonitoringEnabled = isMonitoring,
            isHealthConnectSyncEnabled = isHealthSync,
            hasPermission = sleepRepository.sleepMonitorManager.hasPermission(),
            latestSession = latest,
            recentSessions = recent,
            weeklyDays = weeklyBreakdown,
            weeklyAverageMinutes = avgMins,
            sleepScore = score,
            sleepGoalMinutes = goalMinutes,
            isLoading = loading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SleepUiState()
    )

    private fun computeWeeklyBreakdown(sessions: List<SleepSessionEntity>): List<DaySleepStat> {
        val list = mutableListOf<DaySleepStat>()
        val cal = Calendar.getInstance()
        val todayDayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val todayYear = cal.get(Calendar.YEAR)

        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val numFormat = SimpleDateFormat("d", Locale.getDefault())

        for (i in 6 downTo 0) {
            val dayCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
            }
            val startOfDay = dayCal.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val endOfDay = startOfDay + 86400000L

            val daySessions = sessions.filter { it.startTimeEpochMs in startOfDay until endOfDay }
            val dayDuration = daySessions.sumOf { it.durationMinutes }

            val isToday = (dayCal.get(Calendar.DAY_OF_YEAR) == todayDayOfYear && dayCal.get(Calendar.YEAR) == todayYear)

            list.add(
                DaySleepStat(
                    dayLabel = dayFormat.format(dayCal.time).uppercase(),
                    dayNumber = numFormat.format(dayCal.time),
                    durationMinutes = dayDuration,
                    isToday = isToday,
                    dateEpochMs = startOfDay
                )
            )
        }
        return list
    }

    fun addManualSession(startMs: Long, endMs: Long, notes: String? = null) = viewModelScope.launch {
        val durationMins = ((endMs - startMs) / (1000 * 60)).coerceAtLeast(1)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val score = when {
            durationMins in 420..540 -> 95 // 7 - 9 hrs optimal
            durationMins in 360..600 -> 85
            durationMins in 300..660 -> 75
            else -> 60
        }

        val session = SleepSessionEntity(
            startTimeEpochMs = startMs,
            endTimeEpochMs = endMs,
            durationMinutes = durationMins,
            qualityScore = score,
            status = 0,
            source = "MANUAL",
            dateString = dateFormat.format(Date(endMs)),
            notes = notes
        )
        sleepRepository.recordSession(session)
    }

    fun deleteSession(session: SleepSessionEntity) = viewModelScope.launch {
        sleepRepository.deleteSession(session)
    }

    fun syncWithHealthConnect() = viewModelScope.launch {
        _isLoading.value = true
        try {
            sleepRepository.syncFromHealthConnect()
            sleepRepository.syncUnsyncedToHealthConnect()
        } finally {
            _isLoading.value = false
        }
    }

    fun clearAllSleepData() = viewModelScope.launch {
        sleepRepository.clearAllSessions()
    }

    fun toggleMonitoring(enabled: Boolean) = viewModelScope.launch {
        userSettingsRepository.setSleepMonitoringEnabled(enabled)
        if (enabled) {
            sleepRepository.sleepMonitorManager.startSleepMonitoring()
        } else {
            sleepRepository.sleepMonitorManager.stopSleepMonitoring()
        }
    }

    companion object {
        fun formatDuration(minutes: Long): String {
            val hrs = minutes / 60
            val mins = minutes % 60
            return when {
                hrs > 0 && mins > 0 -> "${hrs}h ${mins}m"
                hrs > 0 -> "${hrs}h"
                else -> "${mins}m"
            }
        }
    }

    class Factory(
        private val sleepRepository: SleepRepository,
        private val userSettingsRepository: UserSettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SleepViewModel(
                sleepRepository = sleepRepository,
                userSettingsRepository = userSettingsRepository
            ) as T
        }
    }
}
