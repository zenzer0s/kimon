package com.zenzeros.kimon.ui.pomodoro

import android.content.Context
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
        context?.let { ctx ->
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
    }

    private fun restoreDnd(context: Context?) {
        if (dndActivatedByTimer) {
            context?.let { ctx ->
                val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager
                if (notificationManager != null && notificationManager.isNotificationPolicyAccessGranted) {
                    try {
                        notificationManager.setInterruptionFilter(android.app.NotificationManager.INTERRUPTION_FILTER_ALL)
                    } catch (e: Exception) {
                        // Ignored
                    }
                }
            }
            dndActivatedByTimer = false
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
            sessionRepository: SessionRepository,
            tagRepository: TagRepository,
            userSettingsRepository: UserSettingsRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PomodoroViewModel(
                    sessionRepository,
                    tagRepository,
                    userSettingsRepository
                ) as T
            }
        }
    }
}
