package com.zenzeros.kimon.service.sleep

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.zenzeros.kimon.KimonApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

object SleepAlarmScheduler {
    private const val TAG = "SleepAlarmScheduler"
    private const val REQUEST_CODE_START = 3001
    private const val REQUEST_CODE_STOP = 3002

    const val ACTION_START_SCHEDULED_MONITORING = "com.zenzeros.kimon.service.sleep.ACTION_START_SCHEDULED"
    const val ACTION_STOP_SCHEDULED_MONITORING = "com.zenzeros.kimon.service.sleep.ACTION_STOP_SCHEDULED"

    fun scheduleNextBedtimeAlarm(context: Context, forceEnable: Boolean = false) {
        val app = context.applicationContext as? KimonApplication ?: return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val isMonitoringEnabled = forceEnable || app.userSettingsRepository.sleepMonitoringEnabled.first()
                val isScheduledMode = app.userSettingsRepository.sleepScheduledMode.first()

                if (!isMonitoringEnabled || !isScheduledMode) {
                    cancelScheduledAlarms(context)
                    return@launch
                }

                val bedtimeHour = app.userSettingsRepository.targetBedtimeHour.first()
                val bedtimeMinute = app.userSettingsRepository.targetBedtimeMinute.first()

                val cal = Calendar.getInstance()
                // Target is 1 hour before bedtime
                cal.set(Calendar.HOUR_OF_DAY, bedtimeHour)
                cal.set(Calendar.MINUTE, bedtimeMinute)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.add(Calendar.HOUR_OF_DAY, -1) // 1 hour before bedtime

                // If this time already passed today, schedule for tomorrow
                if (cal.timeInMillis <= System.currentTimeMillis()) {
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                }

                val triggerTimeMs = cal.timeInMillis

                val startIntent = Intent(context, SleepReceiver::class.java).apply {
                    action = ACTION_START_SCHEDULED_MONITORING
                }
                val startPendingIntent = PendingIntent.getBroadcast(
                    context,
                    REQUEST_CODE_START,
                    startIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                var scheduled = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    try {
                        if (alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                triggerTimeMs,
                                startPendingIntent
                            )
                            scheduled = true
                        }
                    } catch (se: Exception) {
                        Log.w(TAG, "Exact alarm permission rejected by OS, falling back to window alarm", se)
                    }
                }

                if (!scheduled) {
                    try {
                        alarmManager.setWindow(
                            AlarmManager.RTC_WAKEUP,
                            triggerTimeMs,
                            15 * 60 * 1000L, // 15-minute inexact window
                            startPendingIntent
                        )
                        scheduled = true
                    } catch (e: Exception) {
                        Log.w(TAG, "setWindow failed, using setInexact fallback", e)
                        try {
                            alarmManager.setInexactRepeating(
                                AlarmManager.RTC_WAKEUP,
                                triggerTimeMs,
                                AlarmManager.INTERVAL_DAY,
                                startPendingIntent
                            )
                            scheduled = true
                        } catch (fatal: Exception) {
                            Log.e(TAG, "Failed all alarm scheduling methods", fatal)
                        }
                    }
                }

                if (scheduled) {
                    Log.i(
                        TAG,
                        "⏰ [Scheduler] Next sleep tracking auto-start scheduled for: ${cal.time} (1h before $bedtimeHour:${"%02d".format(bedtimeMinute)})"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to schedule sleep alarm", e)
            }
        }
    }

    fun cancelScheduledAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val startIntent = Intent(context, SleepReceiver::class.java).apply {
            action = ACTION_START_SCHEDULED_MONITORING
        }
        val startPendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_START,
            startIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (startPendingIntent != null) {
            alarmManager.cancel(startPendingIntent)
            startPendingIntent.cancel()
        }
        Log.i(TAG, "⏰ [Scheduler] Cancelled scheduled sleep tracking alarms.")
    }
}
