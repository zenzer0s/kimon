package com.zenzeros.kimon.service.sleep

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.zenzeros.kimon.KimonApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SleepReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "SleepReceiver"
        const val ACTION_RESTART_MONITORING = "com.zenzeros.kimon.service.sleep.ACTION_RESTART_MONITORING"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val appContext = context.applicationContext as? KimonApplication ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.i(TAG, "🔔 [SleepReceiver] Received broadcast intent action: ${intent.action}")

                when (intent.action) {
                    Intent.ACTION_BOOT_COMPLETED,
                    Intent.ACTION_MY_PACKAGE_REPLACED,
                    ACTION_RESTART_MONITORING -> {
                        val isEnabled = appContext.userSettingsRepository.sleepMonitoringEnabled.first()
                        val isScheduled = appContext.userSettingsRepository.sleepScheduledMode.first()

                        if (isEnabled) {
                            if (isScheduled) {
                                Log.i(TAG, "⏰ [SleepReceiver] Rescheduling bedtime alarm after device boot...")
                                SleepAlarmScheduler.scheduleNextBedtimeAlarm(context)
                            } else {
                                Log.i(TAG, "🔄 [SleepReceiver] Continuous mode enabled. Starting CustomSleepService...")
                                CustomSleepService.start(context)
                            }
                        } else {
                            Log.d(TAG, "💤 [SleepReceiver] Sleep monitoring is disabled in settings. Skipping.")
                        }
                    }

                    SleepAlarmScheduler.ACTION_START_SCHEDULED_MONITORING -> {
                        Log.i(TAG, "⏰ [SleepReceiver] Bedtime window reached! Auto-starting CustomSleepService (1h before bedtime)...")
                        CustomSleepService.start(context)
                    }

                    SleepAlarmScheduler.ACTION_STOP_SCHEDULED_MONITORING -> {
                        Log.i(TAG, "⏰ [SleepReceiver] Scheduled window ended. Stopping CustomSleepService...")
                        CustomSleepService.stop(context)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling broadcast in SleepReceiver", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
