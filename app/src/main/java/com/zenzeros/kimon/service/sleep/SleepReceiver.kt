package com.zenzeros.kimon.service.sleep

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.SleepClassifyEvent
import com.google.android.gms.location.SleepSegmentEvent
import com.zenzeros.kimon.KimonApplication
import com.zenzeros.kimon.data.local.entity.SleepSessionEntity
import com.zenzeros.kimon.service.sleep.usage.AppUsageHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SleepReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "SleepReceiver"
        private val json = Json { ignoreUnknownKeys = true }
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val appContext = context.applicationContext as? KimonApplication ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.i(TAG, "[SleepReceiver] Received broadcast intent action: ${intent.action}")

                // 1. Handle Google Play Services Sleep Segment Events (Morning wake-up summary)
                if (SleepSegmentEvent.hasEvents(intent)) {
                    val segmentEvents = SleepSegmentEvent.extractEvents(intent)
                    Log.i(TAG, "[SleepReceiver] Extracted ${segmentEvents.size} SleepSegmentEvents from Google Play Services")

                    for (event in segmentEvents) {
                        val startTime = event.startTimeMillis
                        val endTime = event.endTimeMillis
                        val status = event.status
                        val durationMinutes = ((endTime - startTime) / (1000 * 60)).coerceAtLeast(1)

                        Log.i(
                            TAG,
                            "[SleepSegment] Start: $startTime, End: $endTime (${durationMinutes}m), Status: $status"
                        )

                        // Discard if sleep segment is invalid, unconfirmed, or unrealistic duration (30 mins to 16 hours)
                        if (status != SleepSegmentEvent.STATUS_SUCCESSFUL && status != SleepSegmentEvent.STATUS_MISSING_DATA) {
                            Log.w(TAG, "[SleepSegment] Skipping unconfirmed sleep segment status: $status")
                            continue
                        }
                        if (durationMinutes < 30 || durationMinutes > 960) {
                            Log.w(TAG, "[SleepSegment] Skipping unrealistic sleep segment duration: ${durationMinutes}m (status: $status)")
                            continue
                        }
                        if (startTime >= endTime || endTime > System.currentTimeMillis() + 60000) {
                            Log.w(TAG, "[SleepSegment] Skipping invalid timestamps start: $startTime, end: $endTime")
                            continue
                        }

                        // Calculate sleep score based on duration and status
                        val baseScore = when {
                            durationMinutes in 420..540 -> 95 // 7 - 9 hours optimal
                            durationMinutes in 360..600 -> 88 // 6 - 10 hours
                            durationMinutes in 300..660 -> 78
                            durationMinutes in 240..720 -> 68
                            else -> 55
                        }
                        val statusPenalty = if (status == SleepSegmentEvent.STATUS_MISSING_DATA) 10 else 0
                        val finalScore = (baseScore - statusPenalty).coerceIn(35, 100)

                        // Query App Usage during the sleep interval if enabled
                        val isAppUsageEnabled = appContext.userSettingsRepository.appUsageAccessEnabled.first()
                        val appUsageEvents = if (isAppUsageEnabled && AppUsageHelper.hasUsageStatsPermission(context)) {
                            AppUsageHelper.getAppUsageDuringInterval(context, startTime, endTime)
                        } else {
                            emptyList()
                        }

                        val appUsageJsonString = if (appUsageEvents.isNotEmpty()) {
                            json.encodeToString(appUsageEvents)
                        } else null

                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val session = SleepSessionEntity(
                            startTimeEpochMs = startTime,
                            endTimeEpochMs = endTime,
                            durationMinutes = durationMinutes,
                            qualityScore = finalScore,
                            status = status,
                            source = "GOOGLE_SLEEP_API",
                            dateString = dateFormat.format(Date(endTime)),
                            notes = if (status == SleepSegmentEvent.STATUS_MISSING_DATA) "Google Sleep API • Missing Data" else "Google Sleep API",
                            appUsageJson = appUsageJsonString
                        )

                        val id = appContext.sleepRepository.recordSession(session, sendNotification = true)
                        Log.i(TAG, "[SleepReceiver] Successfully saved SleepSession (ID=$id, Duration=${durationMinutes}m, Quality=$finalScore%)")
                    }
                }

                // 2. Handle Google Play Services Periodic Sleep Classify Events (every 10 min)
                if (SleepClassifyEvent.hasEvents(intent)) {
                    val classifyEvents = SleepClassifyEvent.extractEvents(intent)
                    for (classify in classifyEvents) {
                        Log.d(
                            TAG,
                            "[SleepClassify] Time: ${classify.timestampMillis}, Conf: ${classify.confidence}%, Motion: ${classify.motion}, Light: ${classify.light}"
                        )
                    }
                }

                // 3. Handle System Boot / App Update re-registration
                when (intent.action) {
                    Intent.ACTION_BOOT_COMPLETED,
                    Intent.ACTION_MY_PACKAGE_REPLACED -> {
                        val isEnabled = appContext.userSettingsRepository.sleepMonitoringEnabled.first()
                        if (isEnabled) {
                            Log.i(TAG, "[SleepReceiver] Re-registering Google Sleep API updates after boot/update...")
                            appContext.sleepMonitorManager.startSleepMonitoring()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "[SleepReceiver] Error handling broadcast in SleepReceiver", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
