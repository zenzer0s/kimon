package com.zenzeros.kimon.service.step

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.zenzeros.kimon.KimonApplication
import com.zenzeros.kimon.MainActivity
import com.zenzeros.kimon.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class StepCounterService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val kimonApp = applicationContext as? KimonApplication
        val initialSteps = kimonApp?.stepCounterManager?.todaySteps?.value ?: 0
        val initialNotification = buildNotification(initialSteps, 8000)

        // Starting a "health" foreground service without ACTIVITY_RECOGNITION throws on
        // Android 14+. Guard so a stale restart (permission revoked) can't crash the process.
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    initialNotification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    initialNotification,
                    0
                )
            } else {
                startForeground(NOTIFICATION_ID, initialNotification)
            }
        } catch (e: Exception) {
            stopSelf()
            return
        }

        if (kimonApp == null || !kimonApp.stepCounterManager.hasPermission()) {
            stopSelf()
            return
        }
        kimonApp.stepCounterManager.startListening()

        serviceScope.launch {
            var lastNotifiedSteps = -1
            var lastNotifiedGoal = -1
            var lastNotifyAt = 0L
            combine(
                kimonApp.stepCounterManager.todaySteps,
                kimonApp.userSettingsRepository.dailyStepGoal
            ) { steps, goal ->
                Pair(steps, goal)
            }.collect { (steps, goal) ->
                // Redrawing the notification churns SystemUI. Skip updates that don't
                // change what the user sees meaningfully: only refresh on a goal change,
                // a >=25 step delta, or once every 2 min at most.
                val now = System.currentTimeMillis()
                val goalChanged = goal != lastNotifiedGoal
                val stepsChanged = kotlin.math.abs(steps - lastNotifiedSteps) >= 25
                val staleEnough = now - lastNotifyAt >= 120_000L
                if (lastNotifiedSteps >= 0 && !goalChanged && !(stepsChanged && staleEnough)) {
                    return@collect
                }
                lastNotifiedSteps = steps
                lastNotifiedGoal = goal
                lastNotifyAt = now
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                notificationManager?.notify(NOTIFICATION_ID, buildNotification(steps, goal))
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val kimonApp = applicationContext as? KimonApplication
        if (kimonApp?.stepCounterManager?.hasPermission() != true) {
            stopSelf()
            return START_NOT_STICKY
        }
        kimonApp.stepCounterManager.startListening()
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        val kimonApp = applicationContext as? KimonApplication
        if (kimonApp?.stepCounterManager?.hasPermission() == true) {
            kimonApp.stepCounterManager.startListening()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        val kimonApp = applicationContext as? KimonApplication
        kimonApp?.stepCounterManager?.stopListening()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.title_step_counter),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.settings_step_sensor_desc)
                setShowBadge(false)
                enableVibration(false)
                enableLights(false)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(steps: Int, goal: Int): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val formattedSteps = NumberFormat.getNumberInstance(Locale.getDefault()).format(steps)
        val formattedGoal = NumberFormat.getNumberInstance(Locale.getDefault()).format(goal)
        val distanceKm = StepCounterManager.calculateDistanceKm(steps)
        val calories = StepCounterManager.calculateCaloriesKcal(steps)

        val title = "$formattedSteps ${getString(R.string.label_steps)}"
        val content = "${getString(R.string.label_distance)}: %.1f km • %d kcal • %s: %s".format(
            Locale.getDefault(),
            distanceKm,
            calories,
            getString(R.string.settings_section_step_goal),
            formattedGoal
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_steps)
            .setContentTitle(title)
            .setContentText(content)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "step_counter_channel"
        // 2001 = sleep summary, 2002 = (legacy) sleep monitoring - keep distinct
        private const val NOTIFICATION_ID = 2003

        fun start(context: Context) {
            val intent = Intent(context, StepCounterService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (_: Exception) {}
        }

        fun stop(context: Context) {
            val intent = Intent(context, StepCounterService::class.java)
            try {
                context.stopService(intent)
            } catch (_: Exception) {}
        }
    }
}
