package com.zenzeros.kimon.service.sleep

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.SleepSegmentRequest
import com.zenzeros.kimon.KimonApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SleepMonitorManager(private val context: Context) {

    companion object {
        private const val TAG = "SleepMonitorManager"
        private const val SLEEP_REQUEST_CODE = 4040
    }

    fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun getSleepPendingIntent(): PendingIntent {
        val intent = Intent(context, SleepReceiver::class.java)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getBroadcast(context, SLEEP_REQUEST_CODE, intent, flags)
    }

    fun startSleepMonitoring(onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        val app = context.applicationContext as? KimonApplication
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!hasPermission()) {
                    val err = SecurityException("Missing ACTIVITY_RECOGNITION permission")
                    Log.e(TAG, "[SleepMonitorManager] Cannot start Google Sleep API: $err")
                    onFailure(err)
                    return@launch
                }

                Log.i(TAG, "[SleepMonitorManager] Registering Google Play Services Sleep API updates...")
                val request = SleepSegmentRequest.getDefaultSleepSegmentRequest()
                val pendingIntent = getSleepPendingIntent()
                val client = ActivityRecognition.getClient(context)

                client.requestSleepSegmentUpdates(pendingIntent, request)
                    .addOnSuccessListener {
                        Log.i(TAG, "[SleepMonitorManager] Google Play Services Sleep API successfully registered!")
                        CoroutineScope(Dispatchers.IO).launch {
                            app?.userSettingsRepository?.setSleepMonitoringEnabled(true)
                        }
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "[SleepMonitorManager] Failed to register Google Play Services Sleep API", e)
                        onFailure(e)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "[SleepMonitorManager] Error starting sleep monitoring", e)
                onFailure(e)
            }
        }
    }

    fun stopSleepMonitoring(onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        val app = context.applicationContext as? KimonApplication
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.i(TAG, "[SleepMonitorManager] Removing Google Play Services Sleep API updates...")
                val pendingIntent = getSleepPendingIntent()
                val client = ActivityRecognition.getClient(context)

                client.removeSleepSegmentUpdates(pendingIntent)
                    .addOnSuccessListener {
                        Log.i(TAG, "[SleepMonitorManager] Google Sleep API updates successfully removed.")
                        CoroutineScope(Dispatchers.IO).launch {
                            app?.userSettingsRepository?.setSleepMonitoringEnabled(false)
                        }
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "[SleepMonitorManager] Error unregistering Google Sleep API", e)
                        onFailure(e)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "[SleepMonitorManager] Error stopping sleep monitoring", e)
                onFailure(e)
            }
        }
    }

    fun syncMonitoringState() {
        val app = context.applicationContext as? KimonApplication ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val isMonitoringEnabled = app.userSettingsRepository.sleepMonitoringEnabled.first()
                if (isMonitoringEnabled && hasPermission()) {
                    Log.i(TAG, "[Sync] Re-syncing Google Sleep API registration...")
                    startSleepMonitoring()
                } else if (!isMonitoringEnabled) {
                    stopSleepMonitoring()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing sleep monitoring state", e)
            }
        }
    }
}

