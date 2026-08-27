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

class SleepMonitorManager(private val context: Context) {

    companion object {
        private const val TAG = "SleepMonitorManager"
        private const val REQUEST_CODE_SLEEP = 2048
    }

    private val activityRecognitionClient by lazy {
        ActivityRecognition.getClient(context)
    }

    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(context, SleepReceiver::class.java).apply {
            action = SleepReceiver.ACTION_SLEEP_UPDATE
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getBroadcast(context, REQUEST_CODE_SLEEP, intent, flags)
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

    fun startSleepMonitoring(onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        if (!hasPermission()) {
            val error = SecurityException("ACTIVITY_RECOGNITION permission not granted")
            Log.w(TAG, error.message.orEmpty())
            onFailure(error)
            return
        }

        try {
            val pendingIntent = getPendingIntent()
            activityRecognitionClient.requestSleepSegmentUpdates(
                pendingIntent,
                SleepSegmentRequest.getDefaultSleepSegmentRequest()
            ).addOnSuccessListener {
                Log.d(TAG, "Successfully subscribed to Google Sleep Segment Updates")
                onSuccess()
            }.addOnFailureListener { exception ->
                Log.e(TAG, "Failed to subscribe to Google Sleep Segment Updates", exception)
                onFailure(exception)
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException when requesting sleep updates", e)
            onFailure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error requesting sleep updates", e)
            onFailure(e)
        }
    }

    fun stopSleepMonitoring(onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        try {
            val pendingIntent = getPendingIntent()
            activityRecognitionClient.removeSleepSegmentUpdates(pendingIntent)
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully unregistered Google Sleep Segment Updates")
                    onSuccess()
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Failed to unregister Google Sleep Segment Updates", exception)
                    onFailure(exception)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping sleep updates", e)
            onFailure(e)
        }
    }
}
