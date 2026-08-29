package com.zenzeros.kimon.ui.settings

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zenzeros.kimon.KimonApplication
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
    val dialTickAnimation: Boolean = false,
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
    val nothingOsTheme: Boolean = false,
    val sleepMonitoringEnabled: Boolean = false,
    val healthConnectSyncEnabled: Boolean = false,
    val sleepGoalMinutes: Int = 480,
    val sleepScheduledMode: Boolean = true,
    val targetBedtimeHour: Int = 23,
    val targetBedtimeMinute: Int = 0,
    val targetWakeHour: Int = 7,
    val targetWakeMinute: Int = 0,
    val appUsageAccessEnabled: Boolean = true
)

class SettingsViewModel(
    private val application: Application? = null,
    private val userSettingsRepository: UserSettingsRepository,
    private val sessionRepository: SessionRepository,
    private val tagRepository: TagRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private data class TimerSettingsGroup(
        val work: Int,
        val sBreak: Int,
        val lBreak: Int,
        val sessions: Int,
        val goal: Int
    )

    private data class AutomationSettingsGroup(
        val aBreaks: Boolean,
        val aPomodoros: Boolean,
        val keepScreen: Boolean,
        val dnd: Boolean,
        val clockStyle: String,
        val dialTick: Boolean
    )

    private data class SoundSettingsGroup(
        val sound: Boolean,
        val vibration: Boolean,
        val headphone: Boolean,
        val uri: String,
        val title: String
    )

    private data class SleepAndThemeGroup(
        val themeMode: String,
        val themePalette: String,
        val themeColor: String,
        val amoledBlack: Boolean,
        val nothingOsTheme: Boolean,
        val sleepMonitoring: Boolean,
        val healthConnect: Boolean,
        val sleepGoal: Int,
        val scheduledMode: Boolean,
        val bHour: Int,
        val bMin: Int,
        val wHour: Int,
        val wMin: Int,
        val appUsage: Boolean
    )

    val uiState: StateFlow<SettingsUiState> = combine(
        combine(
            userSettingsRepository.workDurationMinutes,
            userSettingsRepository.shortBreakMinutes,
            userSettingsRepository.longBreakMinutes,
            userSettingsRepository.sessionsBeforeLongBreak,
            userSettingsRepository.dailyGoalMinutes
        ) { work, sBreak, lBreak, sessions, goal ->
            TimerSettingsGroup(work, sBreak, lBreak, sessions, goal)
        },
        combine(
            userSettingsRepository.autoStartBreaks,
            userSettingsRepository.autoStartPomodoros,
            userSettingsRepository.keepScreenOn,
            userSettingsRepository.dndEnabled,
            combine(
                userSettingsRepository.clockStyle,
                userSettingsRepository.dialTickAnimation
            ) { clockStyle, dialTick -> Pair(clockStyle, dialTick) }
        ) { aBreaks, aPomodoros, keepScreen, dnd, (clockStyle, dialTick) ->
            AutomationSettingsGroup(aBreaks, aPomodoros, keepScreen, dnd, clockStyle, dialTick)
        },
        combine(
            userSettingsRepository.soundEnabled,
            userSettingsRepository.vibrationEnabled,
            userSettingsRepository.headphoneMode,
            userSettingsRepository.alarmSoundUri,
            userSettingsRepository.alarmSoundTitle
        ) { sound, vibration, hMode, soundUri, soundTitle ->
            SoundSettingsGroup(sound, vibration, hMode, soundUri, soundTitle)
        },
        combine(
            combine(
                userSettingsRepository.themeMode,
                userSettingsRepository.themePalette,
                userSettingsRepository.themeColor,
                userSettingsRepository.amoledBlack,
                userSettingsRepository.nothingOsTheme
            ) { mode, palette, color, amoled, nothingOs ->
                Tuple5(mode, palette, color, amoled, nothingOs)
            },
            combine(
                userSettingsRepository.sleepMonitoringEnabled,
                userSettingsRepository.healthConnectSyncEnabled,
                userSettingsRepository.sleepGoalMinutes,
                userSettingsRepository.sleepScheduledMode,
                userSettingsRepository.targetBedtimeHour
            ) { sleep, hc, goal, sched, bh ->
                Tuple5(sleep, hc, goal, sched, bh)
            },
            combine(
                userSettingsRepository.targetBedtimeMinute,
                userSettingsRepository.targetWakeHour,
                userSettingsRepository.targetWakeMinute,
                userSettingsRepository.appUsageAccessEnabled
            ) { bm, wh, wm, usage ->
                Tuple4(bm, wh, wm, usage)
            }
        ) { (mode, palette, color, amoled, nothingOs), (sleep, hc, goal, sched, bh), (bm, wh, wm, usage) ->
            SleepAndThemeGroup(mode, palette, color, amoled, nothingOs, sleep, hc, goal, sched, bh, bm, wh, wm, usage)
        }
    ) { timer, auto, sound, appGroup ->
        SettingsUiState(
            workDurationMinutes = timer.work,
            shortBreakMinutes = timer.sBreak,
            longBreakMinutes = timer.lBreak,
            sessionsBeforeLongBreak = timer.sessions,
            dailyGoalMinutes = timer.goal,
            autoStartBreaks = auto.aBreaks,
            autoStartPomodoros = auto.aPomodoros,
            keepScreenOn = auto.keepScreen,
            dndEnabled = auto.dnd,
            clockStyle = auto.clockStyle,
            dialTickAnimation = auto.dialTick,
            soundEnabled = sound.sound,
            vibrationEnabled = sound.vibration,
            mediaVolumeForAlarm = sound.headphone,
            headphoneMode = sound.headphone,
            alarmSoundUri = sound.uri,
            alarmSoundTitle = sound.title,
            themeMode = appGroup.themeMode,
            themePalette = appGroup.themePalette,
            themeColor = appGroup.themeColor.toColor(),
            amoledBlack = appGroup.amoledBlack,
            nothingOsTheme = appGroup.nothingOsTheme,
            sleepMonitoringEnabled = appGroup.sleepMonitoring,
            healthConnectSyncEnabled = appGroup.healthConnect,
            sleepGoalMinutes = appGroup.sleepGoal,
            sleepScheduledMode = appGroup.scheduledMode,
            targetBedtimeHour = appGroup.bHour,
            targetBedtimeMinute = appGroup.bMin,
            targetWakeHour = appGroup.wHour,
            targetWakeMinute = appGroup.wMin,
            appUsageAccessEnabled = appGroup.appUsage
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

    fun toggleDialTickAnimation(enabled: Boolean) = viewModelScope.launch {
        userSettingsRepository.setDialTickAnimation(enabled)
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

    fun toggleNothingOsTheme(enabled: Boolean) = viewModelScope.launch {
        userSettingsRepository.setNothingOsTheme(enabled)
    }

    fun toggleSleepMonitoring(enabled: Boolean) = viewModelScope.launch {
        userSettingsRepository.setSleepMonitoringEnabled(enabled)
        (application as? KimonApplication)?.sleepMonitorManager?.syncMonitoringState()
    }

    fun toggleHealthConnectSync(enabled: Boolean) = viewModelScope.launch {
        userSettingsRepository.setHealthConnectSyncEnabled(enabled)
    }

    fun setSleepGoal(minutes: Int) = viewModelScope.launch {
        userSettingsRepository.setSleepGoalMinutes(minutes.coerceIn(240, 720))
    }

    fun toggleSleepScheduledMode(enabled: Boolean) = viewModelScope.launch {
        userSettingsRepository.setSleepScheduledMode(enabled)
        (application as? KimonApplication)?.sleepMonitorManager?.syncMonitoringState()
    }

    fun setTargetBedtime(hour: Int, minute: Int) = viewModelScope.launch {
        userSettingsRepository.setTargetBedtime(hour, minute)
        (application as? KimonApplication)?.sleepMonitorManager?.syncMonitoringState()
    }

    fun setTargetWakeTime(hour: Int, minute: Int) = viewModelScope.launch {
        userSettingsRepository.setTargetWakeTime(hour, minute)
    }

    fun toggleAppUsageAccess(enabled: Boolean) = viewModelScope.launch {
        userSettingsRepository.setAppUsageAccessEnabled(enabled)
    }

    fun resetAllData() = viewModelScope.launch {
        sessionRepository.clearAllSessions()
        userSettingsRepository.clearAllSettings()
    }

    private data class Tuple5<A, B, C, D, E>(
        val a: A,
        val b: B,
        val c: C,
        val d: D,
        val e: E
    )

    private data class Tuple4<A, B, C, D>(
        val a: A,
        val b: B,
        val c: C,
        val d: D
    )

    private data class SleepConfigGroup(
        val healthConnectEnabled: Boolean,
        val sleepGoal: Int,
        val scheduledMode: Boolean,
        val bedtimeHour: Int,
        val bedtimeMinute: Int
    )

    class Factory(
        private val application: Application? = null,
        private val userSettingsRepository: UserSettingsRepository,
        private val sessionRepository: SessionRepository,
        private val tagRepository: TagRepository,
        private val taskRepository: TaskRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(
                application = application,
                userSettingsRepository = userSettingsRepository,
                sessionRepository = sessionRepository,
                tagRepository = tagRepository,
                taskRepository = taskRepository
            ) as T
        }
    }
}
