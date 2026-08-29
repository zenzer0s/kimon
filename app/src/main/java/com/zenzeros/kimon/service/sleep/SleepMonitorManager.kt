package com.zenzeros.kimon.service.sleep

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.zenzeros.kimon.KimonApplication
import com.zenzeros.kimon.service.sleep.native.NativeSleepEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class SleepMonitorManager(private val context: Context) {

    companion object {
        private const val TAG = "SleepMonitorManager"
    }

    fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun isNativeEngineAvailable(): Boolean = NativeSleepEngine.isAvailable()

    fun getNativeEngineVersion(): String = NativeSleepEngine.getVersion()

    fun isMonitoringActive(): Boolean = CustomSleepService.isRunning

    fun startSleepMonitoring(onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        val app = context.applicationContext as? KimonApplication
        CoroutineScope(Dispatchers.IO).launch {
            try {
                app?.userSettingsRepository?.setSleepMonitoringEnabled(true)
                val isScheduled = app?.userSettingsRepository?.sleepScheduledMode?.first() ?: true

                if (isScheduled) {
                    Log.i(TAG, "⏰ [SleepMonitorManager] Scheduled Mode enabled. Arming bedtime alarm...")
                    SleepAlarmScheduler.scheduleNextBedtimeAlarm(context, forceEnable = true)

                    // Check if current time is already inside the bedtime window
                    if (isCurrentlyInBedtimeWindow(app)) {
                        Log.i(TAG, "🌙 [SleepMonitorManager] Current time is within bedtime window. Starting CustomSleepService immediately...")
                        CustomSleepService.start(context)
                    }
                } else {
                    Log.i(TAG, "🚀 [SleepMonitorManager] 24/7 Continuous Mode: Starting CustomSleepService...")
                    CustomSleepService.start(context)
                }

                Log.i(TAG, "✅ [SleepMonitorManager] Sleep monitoring initiated. Native Engine: ${NativeSleepEngine.getVersion()}")
                onSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "❌ [SleepMonitorManager] Failed to start sleep monitoring", e)
                onFailure(e)
            }
        }
    }

    fun stopSleepMonitoring(onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        val app = context.applicationContext as? KimonApplication
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.i(TAG, "🛑 [SleepMonitorManager] Stopping sleep monitoring and canceling alarms...")
                app?.userSettingsRepository?.setSleepMonitoringEnabled(false)
                SleepAlarmScheduler.cancelScheduledAlarms(context)
                CustomSleepService.stop(context)
                Log.i(TAG, "✅ [SleepMonitorManager] Sleep monitoring stopped successfully.")
                onSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "❌ [SleepMonitorManager] Error stopping sleep monitoring", e)
                onFailure(e)
            }
        }
    }

    fun checkAndFinalizeMorningSession() {
        if (CustomSleepService.isRunning) {
            val intent = Intent(context, CustomSleepService::class.java).apply {
                action = CustomSleepService.ACTION_FORCE_EVALUATE
            }
            context.startService(intent)
        }
    }

    fun syncMonitoringState() {
        val app = context.applicationContext as? KimonApplication ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val isMonitoringEnabled = app.userSettingsRepository.sleepMonitoringEnabled.first()
                val isScheduled = app.userSettingsRepository.sleepScheduledMode.first()

                if (!isMonitoringEnabled) {
                    Log.i(TAG, "💤 [Sync] Sleep monitoring disabled. Canceling alarms and stopping service.")
                    SleepAlarmScheduler.cancelScheduledAlarms(context)
                    CustomSleepService.stop(context)
                    return@launch
                }

                if (isScheduled) {
                    Log.i(TAG, "⏰ [Sync] Scheduled mode active. Re-arming bedtime alarm...")
                    SleepAlarmScheduler.scheduleNextBedtimeAlarm(context)

                    if (isCurrentlyInBedtimeWindow(app)) {
                        if (!CustomSleepService.isRunning) {
                            Log.i(TAG, "🌙 [Sync] Inside bedtime window. Starting CustomSleepService...")
                            CustomSleepService.start(context)
                        }
                    } else {
                        if (CustomSleepService.isRunning) {
                            Log.i(TAG, "⏰ [Sync] Outside bedtime window. Stopping CustomSleepService...")
                            CustomSleepService.stop(context)
                        }
                    }
                } else {
                    Log.i(TAG, "🚀 [Sync] Continuous mode active. Starting CustomSleepService 24/7...")
                    SleepAlarmScheduler.cancelScheduledAlarms(context)
                    if (!CustomSleepService.isRunning) {
                        CustomSleepService.start(context)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing sleep monitoring state", e)
            }
        }
    }

    private suspend fun isCurrentlyInBedtimeWindow(app: KimonApplication?): Boolean {
        if (app == null) return true
        val bedtimeHour = app.userSettingsRepository.targetBedtimeHour.first()
        val bedtimeMin = app.userSettingsRepository.targetBedtimeMinute.first()

        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        // Window begins 60 minutes before bedtime
        var startMinutes = (bedtimeHour * 60 + bedtimeMin) - 60
        if (startMinutes < 0) startMinutes += 1440

        // Window ends 12 hours after bedtime (e.g. 10 PM -> 10 AM, covering overnight)
        val endMinutes = (bedtimeHour * 60 + bedtimeMin + 720) % 1440

        return if (startMinutes <= endMinutes) {
            currentMinutes in startMinutes..endMinutes
        } else {
            // Window crosses midnight (e.g. 9:00 PM (1260) to 10:00 AM (600))
            currentMinutes >= startMinutes || currentMinutes <= endMinutes
        }
    }
}
