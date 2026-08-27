package com.zenzeros.kimon.ui.settings

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zenzeros.kimon.data.repository.SessionRepository
import com.zenzeros.kimon.data.repository.TagRepository
import com.zenzeros.kimon.data.repository.TaskRepository
import com.zenzeros.kimon.data.repository.UserSettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

fun String.toColor(): Color {
    return when {
        this.isEmpty() || this == "Color.White" || this == "DYNAMIC" -> Color.White
        this.startsWith("Color(") -> {
            try {
                val comma1 = this.indexOf(',')
                val comma2 = this.indexOf(',', comma1 + 1)
                val comma3 = this.indexOf(',', comma2 + 1)
                val comma4 = this.indexOf(',', comma3 + 1)
                val r = this.substringAfter('(').substringBefore(',').trim().toFloat()
                val g = this.slice(comma1 + 1..<comma2).trim().toFloat()
                val b = this.slice(comma2 + 1..<comma3).trim().toFloat()
                val a = this.slice(comma3 + 1..<comma4).trim().toFloat()
                Color(r, g, b, a)
            } catch (_: Exception) {
                Color.White
            }
        }
        else -> {
            try {
                Color(this.toULong())
            } catch (_: Exception) {
                Color.White
            }
        }
    }
}

data class SettingsUiState(
    val workDurationMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val sessionsBeforeLongBreak: Int = 4,
    val dailyGoalMinutes: Int = 120,
    val autoStartBreaks: Boolean = false,
    val autoStartPomodoros: Boolean = false,
    val keepScreenOn: Boolean = false,
    val dndEnabled: Boolean = false,
    val clockStyle: String = "DIAL",
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val mediaVolumeForAlarm: Boolean = false,
    val headphoneMode: Boolean = false,
    val alarmSoundUri: String = "",
    val alarmSoundTitle: String = "Default",
    val themeMode: String = "SYSTEM",
    val themePalette: String = "DYNAMIC",
    val themeColor: Color = Color.White,
    val amoledBlack: Boolean = false,
    val sleepMonitoringEnabled: Boolean = false,
    val healthConnectSyncEnabled: Boolean = false,
    val sleepGoalMinutes: Int = 480
)

class SettingsViewModel(
    private val userSettingsRepository: UserSettingsRepository,
    private val sessionRepository: SessionRepository,
    private val tagRepository: TagRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        combine(
            userSettingsRepository.workDurationMinutes,
            userSettingsRepository.shortBreakMinutes,
            userSettingsRepository.longBreakMinutes,
            userSettingsRepository.sessionsBeforeLongBreak,
            userSettingsRepository.dailyGoalMinutes
        ) { work, sBreak, lBreak, sessions, goal ->
            work to (sBreak to (lBreak to (sessions to goal)))
        },
        combine(
            userSettingsRepository.autoStartBreaks,
            userSettingsRepository.autoStartPomodoros,
            userSettingsRepository.keepScreenOn,
            userSettingsRepository.dndEnabled,
            userSettingsRepository.clockStyle
        ) { aBreaks, aPomodoros, keepScreen, dnd, clockStyle ->
            aBreaks to (aPomodoros to (keepScreen to (dnd to clockStyle)))
        },
        combine(
            userSettingsRepository.soundEnabled,
            userSettingsRepository.vibrationEnabled,
            userSettingsRepository.headphoneMode,
            userSettingsRepository.alarmSoundUri,
            userSettingsRepository.alarmSoundTitle
        ) { sound, vibration, hMode, soundUri, soundTitle ->
            Triple(sound, vibration, Triple(hMode, soundUri, soundTitle))
        },
        combine(
            userSettingsRepository.themeMode,
            userSettingsRepository.themePalette,
            userSettingsRepository.themeColor,
            userSettingsRepository.amoledBlack,
            userSettingsRepository.sleepMonitoringEnabled
        ) { mode, palette, colorStr, amoled, sleepEnabled ->
            Triple(mode, palette, Triple(colorStr, amoled, sleepEnabled))
        },
        combine(
            userSettingsRepository.healthConnectSyncEnabled,
            userSettingsRepository.sleepGoalMinutes
        ) { healthConnectEnabled, sleepGoal ->
            healthConnectEnabled to sleepGoal
        }
    ) { (work, rest1), (aBreaks, rest2), soundGroup, appearanceTriple, (healthConnectEnabled, sleepGoal) ->
        val (sBreak, rest1b) = rest1
        val (lBreak, rest1c) = rest1b
        val (sessions, goal) = rest1c

        val (aPomodoros, rest2b) = rest2
        val (keepScreen, rest2c) = rest2b
        val (dnd, clockStyle) = rest2c

        val (sound, vibration, soundMeta) = soundGroup
        val (hMode, soundUri, soundTitle) = soundMeta
        val (mode, palette, appDetails) = appearanceTriple
        val (colorStr, amoled, sleepEnabled) = appDetails

        SettingsUiState(
            workDurationMinutes = work,
            shortBreakMinutes = sBreak,
            longBreakMinutes = lBreak,
            sessionsBeforeLongBreak = sessions,
            dailyGoalMinutes = goal,
            autoStartBreaks = aBreaks,
            autoStartPomodoros = aPomodoros,
            keepScreenOn = keepScreen,
            dndEnabled = dnd,
            clockStyle = clockStyle,
            soundEnabled = sound,
            vibrationEnabled = vibration,
            mediaVolumeForAlarm = hMode,
            headphoneMode = hMode,
            alarmSoundUri = soundUri,
            alarmSoundTitle = soundTitle,
            themeMode = mode,
            themePalette = palette,
            themeColor = colorStr.toColor(),
            amoledBlack = amoled,
            sleepMonitoringEnabled = sleepEnabled,
            healthConnectSyncEnabled = healthConnectEnabled,
            sleepGoalMinutes = sleepGoal
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setWorkDuration(minutes: Int) = viewModelScope.launch {
        userSettingsRepository.setWorkDurationMinutes(minutes.coerceIn(1, 180))
    }

    fun setShortBreak(minutes: Int) = viewModelScope.launch {
        userSettingsRepository.setShortBreakMinutes(minutes.coerceIn(1, 60))
    }

    fun setLongBreak(minutes: Int) = viewModelScope.launch {
        userSettingsRepository.setLongBreakMinutes(minutes.coerceIn(1, 90))
    }

    fun setSessionsBeforeLongBreak(count: Int) = viewModelScope.launch {
        userSettingsRepository.setSessionsBeforeLongBreak(count.coerceIn(1, 12))
    }

    fun setDailyGoal(minutes: Int) = viewModelScope.launch {
        userSettingsRepository.setDailyGoalMinutes(minutes.coerceIn(15, 720))
    }

    fun toggleAutoStartBreaks(enabled: Boolean) = viewModelScope.launch {
        userSettingsRepository.setAutoStartBreaks(enabled)
    }

    fun toggleAutoStartPomodoros(enabled: Boolean) = viewModelScope.launch {
        userSettingsRepository.setAutoStartPomodoros(enabled)
    }

    fun toggleKeepScreenOn(enabled: Boolean) = viewModelScope.launch {
        userSettingsRepository.setKeepScreenOn(enabled)
    }

    fun toggleDnd(enabled: Boolean) = viewModelScope.launch {
        userSettingsRepository.setDndEnabled(enabled)
    }

    fun setClockStyle(style: String) = viewModelScope.launch {
        userSettingsRepository.setClockStyle(style)
    }

    fun toggleSound(enabled: Boolean) = viewModelScope.launch {
        userSettingsRepository.setSoundEnabled(enabled)
    }

    fun toggleVibration(enabled: Boolean) = viewModelScope.launch {
        userSettingsRepository.setVibrationEnabled(enabled)
    }

    fun toggleMediaVolume(enabled: Boolean) = viewModelScope.launch {
        userSettingsRepository.setHeadphoneMode(enabled)
    }

    fun toggleHeadphoneMode(enabled: Boolean) = viewModelScope.launch {
        userSettingsRepository.setHeadphoneMode(enabled)
    }

    fun setAlarmSound(uri: String, title: String) = viewModelScope.launch {
        userSettingsRepository.setAlarmSound(uri, title)
    }

    fun setThemeMode(mode: String) = viewModelScope.launch {
        userSettingsRepository.setThemeMode(mode)
    }

    fun setThemePalette(palette: String) = viewModelScope.launch {
        userSettingsRepository.setThemePalette(palette)
    }

    fun setThemeColor(color: Color) = viewModelScope.launch {
        userSettingsRepository.setThemeColor(color.toString())
    }

    fun toggleAmoledBlack(enabled: Boolean) = viewModelScope.launch {
        userSettingsRepository.setAmoledBlack(enabled)
    }

    fun toggleSleepMonitoring(enabled: Boolean) = viewModelScope.launch {
        userSettingsRepository.setSleepMonitoringEnabled(enabled)
    }

    fun toggleHealthConnectSync(enabled: Boolean) = viewModelScope.launch {
        userSettingsRepository.setHealthConnectSyncEnabled(enabled)
    }

    fun setSleepGoal(minutes: Int) = viewModelScope.launch {
        userSettingsRepository.setSleepGoalMinutes(minutes.coerceIn(240, 720))
    }

    fun resetAllData() = viewModelScope.launch {
        sessionRepository.clearAllSessions()
        userSettingsRepository.clearAllSettings()
    }

    class Factory(
        private val userSettingsRepository: UserSettingsRepository,
        private val sessionRepository: SessionRepository,
        private val tagRepository: TagRepository,
        private val taskRepository: TaskRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(
                userSettingsRepository = userSettingsRepository,
                sessionRepository = sessionRepository,
                tagRepository = tagRepository,
                taskRepository = taskRepository
            ) as T
        }
    }
}
