package com.zenzeros.kimon.service.sleep

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.SleepClassifyEvent
import com.google.android.gms.location.SleepSegmentEvent
import com.zenzeros.kimon.KimonApplication
import com.zenzeros.kimon.data.local.entity.SleepSessionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SleepReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "SleepReceiver"
        const val ACTION_SLEEP_UPDATE = "com.zenzeros.kimon.ACTION_SLEEP_UPDATE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val appContext = context.applicationContext as? KimonApplication ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (SleepSegmentEvent.hasEvents(intent)) {
                    val sleepSegmentEvents = SleepSegmentEvent.extractEvents(intent)
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                    for (event in sleepSegmentEvents) {
                        val durationMinutes = (event.endTimeMillis - event.startTimeMillis) / (1000 * 60)
                        if (durationMinutes > 15) { // Filter out micro-segments (< 15 mins)
                            // Calculate a reasonable sleep quality score
                            val quality = when (event.status) {
                                SleepSegmentEvent.STATUS_SUCCESSFUL -> 88
                                SleepSegmentEvent.STATUS_MISSING_DATA -> 70
                                else -> 60
                            }

                            val sleepSession = SleepSessionEntity(
                                startTimeEpochMs = event.startTimeMillis,
                                endTimeEpochMs = event.endTimeMillis,
                                durationMinutes = durationMinutes,
                                qualityScore = quality,
                                status = event.status,
                                source = "GOOGLE_SLEEP_API",
                                dateString = dateFormat.format(Date(event.startTimeMillis))
                            )

                            appContext.sleepRepository.recordSession(sleepSession)
                            Log.d(TAG, "Recorded sleep segment: $durationMinutes mins on ${sleepSession.dateString}")
                        }
                    }
                }

                if (SleepClassifyEvent.hasEvents(intent)) {
                    val classifyEvents = SleepClassifyEvent.extractEvents(intent)
                    Log.d(TAG, "Received ${classifyEvents.size} sleep classify events")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing sleep intent", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
