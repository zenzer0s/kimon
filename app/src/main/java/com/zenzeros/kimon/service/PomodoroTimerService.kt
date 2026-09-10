package com.zenzeros.kimon.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.zenzeros.kimon.MainActivity
import com.zenzeros.kimon.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PomodoroTimerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var countdownJob: Job? = null
    private var targetEndTimeMs: Long = 0L
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val totalSeconds = intent.getIntExtra(EXTRA_TOTAL_SECONDS, 25 * 60)
                val modeLabel = intent.getStringExtra(EXTRA_MODE_LABEL) ?: "Focus"
                startCountdown(totalSeconds, modeLabel)
            }
            ACTION_PAUSE -> pauseCountdown()
            ACTION_RESUME -> resumeCountdown()
            ACTION_STOP -> stopCountdown()
        }
        return START_NOT_STICKY
    }

    private fun startCountdown(totalSeconds: Int, modeLabel: String) {
        targetEndTimeMs = System.currentTimeMillis() + (totalSeconds * 1000L)
        _timerState.value = TimerServiceState(
            remainingSeconds = totalSeconds,
            totalSeconds = totalSeconds,
            modeLabel = modeLabel,
            isRunning = true
        )

        startForeground(NOTIFICATION_ID, buildNotification(_timerState.value))
        scheduleCompletion()
    }

    /**
     * The notification renders its own live countdown via [NotificationCompat.Builder.setUsesChronometer],
     * so the service no longer needs a per-second loop to re-post it. A single suspension until the
     * target end time is all that's required; [PomodoroViewModel.syncWithWallClock] re-aligns on resume.
     */
    private fun scheduleCompletion() {
        countdownJob?.cancel()
        countdownJob = serviceScope.launch {
            val remainingMs = (targetEndTimeMs - System.currentTimeMillis()).coerceAtLeast(0L)
            delay(remainingMs)
            _timerState.value = _timerState.value.copy(remainingSeconds = 0, isRunning = false)
            notificationManager.notify(NOTIFICATION_ID, buildNotification(_timerState.value, isCompleted = true))
            stopSelf()
        }
    }

    private fun pauseCountdown() {
        countdownJob?.cancel()
        if (targetEndTimeMs > 0L) {
            val now = System.currentTimeMillis()
            val newRemaining = ((targetEndTimeMs - now + 999L) / 1000L).coerceAtLeast(0L).toInt()
            _timerState.value = _timerState.value.copy(isRunning = false, remainingSeconds = newRemaining)
        } else {
            _timerState.value = _timerState.value.copy(isRunning = false)
        }
        targetEndTimeMs = 0L
        notificationManager.notify(NOTIFICATION_ID, buildNotification(_timerState.value))
    }

    private fun resumeCountdown() {
        if (_timerState.value.remainingSeconds > 0) {
            targetEndTimeMs = System.currentTimeMillis() + (_timerState.value.remainingSeconds * 1000L)
            _timerState.value = _timerState.value.copy(isRunning = true)
            notificationManager.notify(NOTIFICATION_ID, buildNotification(_timerState.value))
            scheduleCompletion()
        }
    }

    private fun stopCountdown() {
        countdownJob?.cancel()
        targetEndTimeMs = 0L
        _timerState.value = TimerServiceState()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(state: TimerServiceState, isCompleted: Boolean = false): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_timer_title))
            .setSmallIcon(R.drawable.ic_focus)
            .setContentIntent(contentIntent)
            .setOngoing(state.isRunning)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (state.isRunning && !isCompleted) {
            // Let the system tick the countdown down to zero. No per-second notify() needed.
            builder.setContentText(state.modeLabel)
                .setUsesChronometer(true)
                .setChronometerCountDown(true)
                .setWhen(targetEndTimeMs)
                .setShowWhen(true)
        } else {
            val minutes = state.remainingSeconds / 60
            val seconds = state.remainingSeconds % 60
            val timeString = String.format("%02d:%02d", minutes, seconds)
            val contentText = if (isCompleted) {
                getString(R.string.notification_session_completed, state.modeLabel)
            } else {
                getString(R.string.notification_time_remaining, state.modeLabel, timeString)
            }
            builder.setContentText(contentText)
                .setUsesChronometer(false)
                .setShowWhen(false)
        }

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        countdownJob?.cancel()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "kimon_pomodoro_timer_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.zenzeros.kimon.ACTION_START"
        const val ACTION_PAUSE = "com.zenzeros.kimon.ACTION_PAUSE"
        const val ACTION_RESUME = "com.zenzeros.kimon.ACTION_RESUME"
        const val ACTION_STOP = "com.zenzeros.kimon.ACTION_STOP"

        const val EXTRA_TOTAL_SECONDS = "extra_total_seconds"
        const val EXTRA_MODE_LABEL = "extra_mode_label"

        private val _timerState = MutableStateFlow(TimerServiceState())
        val timerState = _timerState.asStateFlow()

        fun startTimer(context: Context, totalSeconds: Int, modeLabel: String) {
            val intent = Intent(context, PomodoroTimerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_TOTAL_SECONDS, totalSeconds)
                putExtra(EXTRA_MODE_LABEL, modeLabel)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (_: Exception) {
                // e.g. ForegroundServiceStartNotAllowedException when launched from background
            }
        }

        // pause/resume/stop only ever deliver to an already-running service; wrap in
        // try/catch because startService() throws if the service has already stopped
        // itself (timer completed) while the app sits in the background.
        fun pauseTimer(context: Context) {
            val intent = Intent(context, PomodoroTimerService::class.java).apply {
                action = ACTION_PAUSE
            }
            try { context.startService(intent) } catch (_: Exception) {}
        }

        fun resumeTimer(context: Context) {
            val intent = Intent(context, PomodoroTimerService::class.java).apply {
                action = ACTION_RESUME
            }
            try { context.startService(intent) } catch (_: Exception) {}
        }

        fun stopTimer(context: Context) {
            val intent = Intent(context, PomodoroTimerService::class.java).apply {
                action = ACTION_STOP
            }
            try { context.startService(intent) } catch (_: Exception) {}
        }
    }
}

data class TimerServiceState(
    val remainingSeconds: Int = 25 * 60,
    val totalSeconds: Int = 25 * 60,
    val modeLabel: String = "Focus",
    val isRunning: Boolean = false
)
