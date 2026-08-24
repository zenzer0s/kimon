package com.zenzeros.kimon.ui.pomodoro

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zenzeros.kimon.data.local.entity.FocusSessionEntity
import com.zenzeros.kimon.data.local.entity.TagEntity
import com.zenzeros.kimon.data.repository.SessionRepository
import com.zenzeros.kimon.data.repository.TagRepository
import com.zenzeros.kimon.data.repository.UserSettingsRepository
import com.zenzeros.kimon.service.PomodoroTimerService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PomodoroViewModel(
    private val appContext: Context,
    private val sessionRepository: SessionRepository,
    private val tagRepository: TagRepository,
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PomodoroUiState())
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()

    val allTags: StateFlow<List<TagEntity>> = tagRepository.getAllActiveTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var timerJob: Job? = null
    private var sessionStartTimeMs: Long = 0L

    init {
        viewModelScope.launch {
            combine(
                userSettingsRepository.workDurationMinutes,
                userSettingsRepository.shortBreakMinutes,
                userSettingsRepository.longBreakMinutes,
                userSettingsRepository.sessionsBeforeLongBreak
            ) { work, sBreak, lBreak, totalSessions ->
                listOf(work, sBreak, lBreak, totalSessions)
            }.collect { (work, sBreak, lBreak, totalSessions) ->
                _uiState.update { state ->
                    val durationMinutes = when (state.currentMode) {
                        PomodoroMode.FOCUS -> work
                        PomodoroMode.SHORT_BREAK -> sBreak
                        PomodoroMode.LONG_BREAK -> lBreak
                    }
                    val durationSecs = durationMinutes * 60
                    if (state.timerStatus == TimerStatus.IDLE) {
                        state.copy(
                            remainingSeconds = durationSecs,
                            totalSeconds = durationSecs,
                            totalDailySessions = totalSessions
                        )
                    } else {
                        state.copy(
                            totalDailySessions = totalSessions
                        )
                    }
                }
            }
        }
    }

    fun selectTag(tag: TagEntity?) {
        _uiState.update { it.copy(selectedTag = tag) }
    }

    fun createTag(name: String, colorHex: String) {
        viewModelScope.launch {
            val newTagId = tagRepository.createTag(name = name, colorHex = colorHex)
            val createdTag = TagEntity(id = newTagId, name = name, colorHex = colorHex)
            _uiState.update { it.copy(selectedTag = createdTag) }
        }
    }

    fun deleteTag(tag: TagEntity) {
        viewModelScope.launch {
            if (_uiState.value.selectedTag?.id == tag.id) {
                _uiState.update { it.copy(selectedTag = null) }
            }
            tagRepository.deleteTag(tag)
        }
    }

    fun setClockStyle(style: ClockStyle) {
        _uiState.update { it.copy(clockStyle = style) }
    }

    fun setMode(mode: PomodoroMode, context: Context? = null) {
        pauseTimer(context)
        viewModelScope.launch {
            val durationMinutes = when (mode) {
                PomodoroMode.FOCUS -> userSettingsRepository.workDurationMinutes.first()
                PomodoroMode.SHORT_BREAK -> userSettingsRepository.shortBreakMinutes.first()
                PomodoroMode.LONG_BREAK -> userSettingsRepository.longBreakMinutes.first()
            }
            val durationSecs = durationMinutes * 60
            _uiState.update {
                it.copy(
                    currentMode = mode,
                    timerStatus = TimerStatus.IDLE,
                    remainingSeconds = durationSecs,
                    totalSeconds = durationSecs
                )
            }
        }
    }

    private var dndActivatedByTimer: Boolean = false

    private fun applyDndIfEnabled(context: Context?) {
        val ctx = context ?: appContext
        viewModelScope.launch {
            val isDndEnabled = userSettingsRepository.dndEnabled.first()
            if (isDndEnabled && _uiState.value.currentMode == PomodoroMode.FOCUS) {
                val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager
                if (notificationManager != null && notificationManager.isNotificationPolicyAccessGranted) {
                    try {
                        notificationManager.setInterruptionFilter(android.app.NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                        dndActivatedByTimer = true
                    } catch (e: Exception) {
                        // SecurityException or unsupported
                    }
                }
            }
        }
    }

    private fun restoreDnd(context: Context?) {
        if (dndActivatedByTimer) {
            val ctx = context ?: appContext
            val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager
            if (notificationManager != null && notificationManager.isNotificationPolicyAccessGranted) {
                try {
                    notificationManager.setInterruptionFilter(android.app.NotificationManager.INTERRUPTION_FILTER_ALL)
                } catch (e: Exception) {
                    // Ignored
                }
            }
            dndActivatedByTimer = false
        }
    }

    private fun playAlarmAndVibration() {
        val ctx = appContext
        viewModelScope.launch {
            val sound = userSettingsRepository.soundEnabled.first()
            val vibration = userSettingsRepository.vibrationEnabled.first()
            val headphoneOnly = userSettingsRepository.headphoneMode.first()
            val customSoundUri = userSettingsRepository.alarmSoundUri.first()

            if (vibration) {
                try {
                    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val manager = ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                        manager?.defaultVibrator
                    } else {
                        @Suppress("DEPRECATION")
                        ctx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    }

                    if (vibrator != null) {
                        val audioAttributes = AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(
                                VibrationEffect.createWaveform(
                                    longArrayOf(0, 500, 250, 500),
                                    -1
                                ),
                                audioAttributes
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(longArrayOf(0, 500, 250, 500), -1)
                        }
                    }
                } catch (e: Exception) {
                    // Ignored
                }
            }

            if (sound) {
                try {
                    val audioManager = ctx.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                    val isHeadphonesConnected = audioManager?.let { am ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val devices = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                            devices.any {
                                it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                                it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                                it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                                it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                                it.type == AudioDeviceInfo.TYPE_USB_HEADSET
                            }
                        } else {
                            @Suppress("DEPRECATION")
                            am.isWiredHeadsetOn || am.isBluetoothA2dpOn
                        }
                    } ?: false

                    if (headphoneOnly && isHeadphonesConnected) {
                        // When headphones are connected: play on USAGE_MEDIA (STREAM_MUSIC) to stay inside headphones
                        val alertUri = if (customSoundUri.isNotEmpty()) {
                            Uri.parse(customSoundUri)
                        } else {
                            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        }
                        MediaPlayer().apply {
                            setAudioAttributes(
                                AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build()
                            )
                            setDataSource(ctx, alertUri)
                            prepare()
                            start()
                            setOnCompletionListener { release() }
                        }
                    } else {
                        // When headphones are not connected (or headphone mode is off): play on standard alarm stream
                        val alertUri = if (customSoundUri.isNotEmpty()) {
                            Uri.parse(customSoundUri)
                        } else {
                            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                        }
                        val ringtone = RingtoneManager.getRingtone(ctx, alertUri)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            ringtone?.audioAttributes = AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                        }
                        ringtone?.play()
                    }
                } catch (e: Exception) {
                    // Ignored
                }
            }
        }
    }

    fun startTimer(context: Context? = null) {
        if (_uiState.value.timerStatus == TimerStatus.RUNNING) return

        if (_uiState.value.timerStatus == TimerStatus.IDLE) {
            sessionStartTimeMs = System.currentTimeMillis()
        }

        val currentState = _uiState.value
        _uiState.update { it.copy(timerStatus = TimerStatus.RUNNING) }

        applyDndIfEnabled(context)

        context?.let { ctx ->
            PomodoroTimerService.startTimer(
                context = ctx,
                totalSeconds = currentState.remainingSeconds,
                modeLabel = currentState.currentMode.label
            )
        }

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0 && _uiState.value.timerStatus == TimerStatus.RUNNING) {
                delay(1000)
                _uiState.update {
                    it.copy(remainingSeconds = (it.remainingSeconds - 1).coerceAtLeast(0))
                }
            }

            if (_uiState.value.remainingSeconds <= 0) {
                onTimerFinished(context)
            }
        }
    }

    fun pauseTimer(context: Context? = null) {
        timerJob?.cancel()
        restoreDnd(context)
        _uiState.update { it.copy(timerStatus = TimerStatus.PAUSED) }
        context?.let { ctx ->
            PomodoroTimerService.pauseTimer(ctx)
        }
    }

    fun stopTimer(context: Context? = null) {
        timerJob?.cancel()
        restoreDnd(context)
        context?.let { ctx ->
            PomodoroTimerService.stopTimer(ctx)
        }

        val currentState = _uiState.value
        val actualSeconds = currentState.totalSeconds - currentState.remainingSeconds

        // Save session if elapsed >= 30 seconds
        if (actualSeconds >= 30 && sessionStartTimeMs > 0) {
            saveSession(
                startTimeMs = sessionStartTimeMs,
                endTimeMs = System.currentTimeMillis(),
                targetDurationSeconds = currentState.totalSeconds,
                actualDurationSeconds = actualSeconds,
                isCompleted = false
            )
        }

        sessionStartTimeMs = 0L
        _uiState.update {
            it.copy(
                timerStatus = TimerStatus.IDLE,
                remainingSeconds = it.totalSeconds
            )
        }
    }

    private fun onTimerFinished(context: Context? = null) {
        timerJob?.cancel()
        restoreDnd(context)
        context?.let { ctx ->
            PomodoroTimerService.stopTimer(ctx)
        }

        val currentState = _uiState.value
        val endTime = System.currentTimeMillis()

        saveSession(
            startTimeMs = sessionStartTimeMs.takeIf { it > 0 } ?: (endTime - currentState.totalSeconds * 1000L),
            endTimeMs = endTime,
            targetDurationSeconds = currentState.totalSeconds,
            actualDurationSeconds = currentState.totalSeconds,
            isCompleted = true
        )

        sessionStartTimeMs = 0L

        playAlarmAndVibration()

        // Auto switch mode: FOCUS -> SHORT_BREAK (or LONG_BREAK)
        viewModelScope.launch {
            val sessionsBeforeLong = userSettingsRepository.sessionsBeforeLongBreak.first()
            val nextMode = when (currentState.currentMode) {
                PomodoroMode.FOCUS -> {
                    val nextSessionIndex = currentState.currentSessionIndex + 1
                    if (nextSessionIndex % sessionsBeforeLong == 0) PomodoroMode.LONG_BREAK else PomodoroMode.SHORT_BREAK
                }
                PomodoroMode.SHORT_BREAK, PomodoroMode.LONG_BREAK -> PomodoroMode.FOCUS
            }

            val nextDurationMinutes = when (nextMode) {
                PomodoroMode.FOCUS -> userSettingsRepository.workDurationMinutes.first()
                PomodoroMode.SHORT_BREAK -> userSettingsRepository.shortBreakMinutes.first()
                PomodoroMode.LONG_BREAK -> userSettingsRepository.longBreakMinutes.first()
            }
            val nextDurationSecs = nextDurationMinutes * 60

            _uiState.update {
                it.copy(
                    currentMode = nextMode,
                    timerStatus = TimerStatus.IDLE,
                    remainingSeconds = nextDurationSecs,
                    totalSeconds = nextDurationSecs,
                    currentSessionIndex = if (currentState.currentMode == PomodoroMode.FOCUS) it.currentSessionIndex + 1 else it.currentSessionIndex
                )
            }

            // Check auto-start settings and start next mode automatically
            val shouldAutoStart = when (nextMode) {
                PomodoroMode.FOCUS -> userSettingsRepository.autoStartPomodoros.first()
                PomodoroMode.SHORT_BREAK, PomodoroMode.LONG_BREAK -> userSettingsRepository.autoStartBreaks.first()
            }

            if (shouldAutoStart) {
                startTimer(context)
            }
        }
    }

    private fun saveSession(
        startTimeMs: Long,
        endTimeMs: Long,
        targetDurationSeconds: Int,
        actualDurationSeconds: Int,
        isCompleted: Boolean
    ) {
        val currentState = _uiState.value
        val mode = currentState.currentMode
        viewModelScope.launch {
            sessionRepository.recordSession(
                FocusSessionEntity(
                    tagId = currentState.selectedTag?.id,
                    sessionType = when (mode) {
                        PomodoroMode.FOCUS -> "POMODORO"
                        PomodoroMode.SHORT_BREAK -> "SHORT_BREAK"
                        PomodoroMode.LONG_BREAK -> "LONG_BREAK"
                    },
                    startTimeEpochMs = startTimeMs,
                    endTimeEpochMs = endTimeMs,
                    targetDurationSeconds = targetDurationSeconds,
                    actualDurationSeconds = actualDurationSeconds,
                    isCompleted = isCompleted
                )
            )
        }
    }

    companion object {
        fun Factory(
            appContext: Context,
            sessionRepository: SessionRepository,
            tagRepository: TagRepository,
            userSettingsRepository: UserSettingsRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PomodoroViewModel(
                    appContext,
                    sessionRepository,
                    tagRepository,
                    userSettingsRepository
                ) as T
            }
        }
    }
}
