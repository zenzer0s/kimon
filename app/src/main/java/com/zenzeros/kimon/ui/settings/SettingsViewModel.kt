package com.zenzeros.kimon.ui.settings

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
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val mediaVolumeForAlarm: Boolean = false,
    val headphoneMode: Boolean = false,
    val alarmSoundUri: String = "",
    val alarmSoundTitle: String = "Default",
    val themeMode: String = "SYSTEM",
    val themePalette: String = "DYNAMIC",
    val amoledBlack: Boolean = false
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
            userSettingsRepository.dndEnabled
        ) { aBreaks, aPomodoros, keepScreen, dnd ->
            aBreaks to (aPomodoros to (keepScreen to dnd))
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
            userSettingsRepository.amoledBlack
        ) { mode, palette, amoled ->
            mode to (palette to amoled)
        }
    ) { (work, rest1), (aBreaks, rest2), soundGroup, (mode, rest4) ->
        val (sBreak, rest1b) = rest1
        val (lBreak, rest1c) = rest1b
        val (sessions, goal) = rest1c

        val (aPomodoros, rest2b) = rest2
        val (keepScreen, dnd) = rest2b

        val (sound, vibration, soundMeta) = soundGroup
        val (hMode, soundUri, soundTitle) = soundMeta
        val (palette, amoled) = rest4

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
            soundEnabled = sound,
            vibrationEnabled = vibration,
            mediaVolumeForAlarm = hMode,
            headphoneMode = hMode,
            alarmSoundUri = soundUri,
            alarmSoundTitle = soundTitle,
            themeMode = mode,
            themePalette = palette,
            amoledBlack = amoled
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

    fun toggleAmoledBlack(enabled: Boolean) = viewModelScope.launch {
        userSettingsRepository.setAmoledBlack(enabled)
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
