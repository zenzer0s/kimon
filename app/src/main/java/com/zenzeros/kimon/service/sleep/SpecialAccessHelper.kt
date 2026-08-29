package com.zenzeros.kimon.service.sleep

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import com.zenzeros.kimon.service.sleep.usage.AppUsageHelper

object SpecialAccessHelper {
    private const val TAG = "SpecialAccessHelper"

    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.canScheduleExactAlarms() ?: true
        } else {
            true
        }
    }

    fun openExactAlarmSettings(context: Context) {
        Log.i(TAG, "Opening exact alarm settings for package: ${context.packageName}")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                return
            } catch (e: Exception) {
                Log.w(TAG, "ACTION_REQUEST_SCHEDULE_EXACT_ALARM failed, falling back to app details", e)
            }
        }

        openAppDetailsSettings(context)
    }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: true
        } else {
            true
        }
    }

    fun openBatteryOptimizationSettings(context: Context) {
        Log.i(TAG, "Opening battery optimization settings for package: ${context.packageName}")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                return
            } catch (e: Exception) {
                Log.w(TAG, "ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS failed, trying ignore list", e)
            }

            try {
                val fallback = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallback)
                return
            } catch (e2: Exception) {
                Log.w(TAG, "ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS failed", e2)
            }
        }

        openAppDetailsSettings(context)
    }

    fun hasUsageAccess(context: Context): Boolean {
        return AppUsageHelper.hasUsageStatsPermission(context)
    }

    fun openUsageAccessSettings(context: Context) {
        Log.i(TAG, "Opening usage access settings")
        try {
            val intent = AppUsageHelper.createUsageAccessSettingsIntent()
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.w(TAG, "Usage access settings intent failed, opening app details", e)
            openAppDetailsSettings(context)
        }
    }

    private fun openAppDetailsSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open application details settings", e)
            try {
                val settingsIntent = Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(settingsIntent)
            } catch (ignored: Exception) {}
        }
    }
}
