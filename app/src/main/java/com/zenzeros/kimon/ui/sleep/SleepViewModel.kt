package com.zenzeros.kimon.ui.sleep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zenzeros.kimon.data.local.entity.SleepSessionEntity
import com.zenzeros.kimon.data.repository.SleepRepository
import com.zenzeros.kimon.data.repository.UserSettingsRepository
import com.zenzeros.kimon.service.step.StepCounterManager
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
    val displayedSession: SleepSessionEntity? = null,
    val displayedDurationMinutes: Long = 0,
    val displayedStartTimeEpochMs: Long? = null,
    val displayedEndTimeEpochMs: Long? = null,
    val isDisplayedSessionFromLastNight: Boolean = false,
    val selectedDayEpochMs: Long? = null,
    val selectedDayLabel: String? = null,
    val recentSessions: List<SleepSessionEntity> = emptyList(),
    val weeklyDays: List<DaySleepStat> = emptyList(),
    val weeklyAverageMinutes: Long = 0,
    val sleepScore: Int = 0,
    val sleepGoalMinutes: Int = 480,
    val isLoading: Boolean = false,
    val todaySteps: Int = 0,
    val stepGoal: Int = 8000,
    val isStepSensorAvailable: Boolean = true,
    val hasStepPermission: Boolean = false,
    val isStepCounterEnabled: Boolean = true
)

private data class SleepSettingsState(
    val goalMinutes: Int,
    val stepGoal: Int,
    val isStepCounterEnabled: Boolean,
    val loading: Boolean,
    val selectedDay: Long?
)

private data class StepTrackingState(
    val steps: Int,
    val hasPermission: Boolean
)

class SleepViewModel(
    private val sleepRepository: SleepRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val stepCounterManager: StepCounterManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _selectedDayEpochMs = MutableStateFlow<Long?>(null)
    private val _hasStepPermission = MutableStateFlow(stepCounterManager.hasPermission())

    init {
        viewModelScope.launch {
            userSettingsRepository.stepCounterEnabled.collect { enabled ->
                if (enabled && stepCounterManager.hasPermission()) {
                    com.zenzeros.kimon.service.step.StepCounterService.start(stepCounterManager.context)
                } else if (!enabled) {
                    com.zenzeros.kimon.service.step.StepCounterService.stop(stepCounterManager.context)
                }
            }
        }
    }

    fun onStepPermissionResult(granted: Boolean) {
        _hasStepPermission.value = granted
        if (granted) {
            com.zenzeros.kimon.service.step.StepCounterService.start(stepCounterManager.context)
        }
    }

    val uiState: StateFlow<SleepUiState> = combine(
        combine(
            sleepRepository.getAllSessions(),
            userSettingsRepository.sleepMonitoringEnabled,
            userSettingsRepository.healthConnectSyncEnabled
        ) { sessions, isMonitoring, isHealthSync ->
            Triple(sessions, isMonitoring, isHealthSync)
        },
        combine(
            userSettingsRepository.sleepGoalMinutes,
            userSettingsRepository.dailyStepGoal,
            userSettingsRepository.stepCounterEnabled,
            _isLoading,
            _selectedDayEpochMs
        ) { goalMinutes, stepGoal, stepEnabled, loading, selectedDay ->
            SleepSettingsState(goalMinutes, stepGoal, stepEnabled, loading, selectedDay)
        },
        combine(
            stepCounterManager.todaySteps,
            _hasStepPermission
        ) { steps, hasPerm ->
            StepTrackingState(steps, hasPerm)
        }
    ) { (sessions, isMonitoring, isHealthSync), settings, stepState ->
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

        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayStartOfDay = todayCal.timeInMillis
        val todayEndOfDay = todayStartOfDay + 86400000L

        // Find session(s) for selected day or for last night (today)
        val daySessions: List<SleepSessionEntity>
        val isFromLastNight: Boolean
        val selectedDayLabel: String?

        if (settings.selectedDay != null) {
            val endOfDay = settings.selectedDay + 86400000L
            // A sleep session belongs to the day it ended (wake-up day), matching `dateString`.
            // Bucketing by both start and end double-counts nights that cross midnight.
            daySessions = sessions.filter {
                it.endTimeEpochMs in (settings.selectedDay + 1)..endOfDay
            }
            val cal = Calendar.getInstance().apply { timeInMillis = settings.selectedDay }
            val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
            val numFormat = SimpleDateFormat("d", Locale.getDefault())
            selectedDayLabel = "${dayFormat.format(cal.time).uppercase()}, ${numFormat.format(cal.time)}"
            isFromLastNight = (settings.selectedDay == todayStartOfDay)
        } else {
            // Default view: session(s) that ended today (last night's sleep)
            daySessions = sessions.filter {
                it.endTimeEpochMs in (todayStartOfDay + 1)..todayEndOfDay
            }
            selectedDayLabel = null
            isFromLastNight = daySessions.isNotEmpty()
        }

        val displayedSession = if (daySessions.isNotEmpty()) {
            daySessions.maxByOrNull { it.durationMinutes } ?: daySessions.first()
        } else null

        val displayedDurationMinutes = daySessions.sumOf { it.durationMinutes }
        val displayedStartTimeEpochMs = daySessions.minOfOrNull { it.startTimeEpochMs }
        val displayedEndTimeEpochMs = daySessions.maxOfOrNull { it.endTimeEpochMs }

        // Calculate sleep score (target based on goalMinutes)
        val score = if (daySessions.isNotEmpty()) {
            daySessions.map { it.qualityScore }.average().toInt()
        } else if (avgMins > 0) {
            val ratio = (avgMins.toFloat() / settings.goalMinutes.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
            (ratio * 100).toInt().coerceIn(40, 98)
        } else 0

        SleepUiState(
            isMonitoringEnabled = isMonitoring,
            isHealthConnectSyncEnabled = isHealthSync,
            hasPermission = sleepRepository.sleepMonitorManager.hasPermission(),
            latestSession = latest,
            displayedSession = displayedSession,
            displayedDurationMinutes = displayedDurationMinutes,
            displayedStartTimeEpochMs = displayedStartTimeEpochMs,
            displayedEndTimeEpochMs = displayedEndTimeEpochMs,
            isDisplayedSessionFromLastNight = isFromLastNight,
            selectedDayEpochMs = settings.selectedDay,
            selectedDayLabel = selectedDayLabel,
            recentSessions = recent,
            weeklyDays = weeklyBreakdown,
            weeklyAverageMinutes = avgMins,
            sleepScore = score,
            sleepGoalMinutes = settings.goalMinutes,
            isLoading = settings.loading,
            todaySteps = stepState.steps,
            stepGoal = settings.stepGoal,
            isStepSensorAvailable = stepCounterManager.isSensorAvailable(),
            hasStepPermission = stepState.hasPermission,
            isStepCounterEnabled = settings.isStepCounterEnabled
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
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = dayCal.timeInMillis
            val endOfDay = startOfDay + 86400000L

            // Attribute each night to its wake-up day only, so a session that crosses
            // midnight is not counted on both days.
            val daySessions = sessions.filter {
                it.endTimeEpochMs in (startOfDay + 1)..endOfDay
            }
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

    fun selectDay(dateEpochMs: Long?) {
        if (_selectedDayEpochMs.value == dateEpochMs) {
            _selectedDayEpochMs.value = null // Toggle off to default/latest
        } else {
            _selectedDayEpochMs.value = dateEpochMs
        }
    }

    fun previousDay() {
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val current = _selectedDayEpochMs.value ?: todayStart
        val cal = Calendar.getInstance().apply {
            timeInMillis = current
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        _selectedDayEpochMs.value = cal.timeInMillis
    }

    fun nextDay() {
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val current = _selectedDayEpochMs.value ?: todayStart
        if (current >= todayStart) {
            return
        }
        val cal = Calendar.getInstance().apply {
            timeInMillis = current
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis >= todayStart) {
            _selectedDayEpochMs.value = null
        } else {
            _selectedDayEpochMs.value = cal.timeInMillis
        }
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
        private val userSettingsRepository: UserSettingsRepository,
        private val stepCounterManager: StepCounterManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SleepViewModel(
                sleepRepository = sleepRepository,
                userSettingsRepository = userSettingsRepository,
                stepCounterManager = stepCounterManager
            ) as T
        }
    }
}
