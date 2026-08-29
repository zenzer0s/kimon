package com.zenzeros.kimon.service.sleep.usage

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.util.Log
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SleepAppUsageEvent(
    @SerialName("package_name") val packageName: String,
    @SerialName("app_name") val appName: String,
    @SerialName("start_time_ms") val startTimeEpochMs: Long,
    @SerialName("end_time_ms") val endTimeEpochMs: Long,
    @SerialName("duration_seconds") val durationSeconds: Long
)

object AppUsageHelper {
    private const val TAG = "AppUsageHelper"

    private val IGNORED_PACKAGES = setOf(
        "android",
        "com.android.systemui",
        "com.google.android.apps.nexuslauncher",
        "com.google.android.launcher",
        "com.sec.android.app.launcher",
        "com.miui.home",
        "com.huawei.android.launcher",
        "com.oppo.launcher",
        "com.zenzeros.kimon",
        "com.zenzeros.kimon.dev"
    )

    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun createUsageAccessSettingsIntent(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun getAppUsageDuringInterval(
        context: Context,
        startTimeMs: Long,
        endTimeMs: Long
    ): List<SleepAppUsageEvent> {
        if (!hasUsageStatsPermission(context) || startTimeMs >= endTimeMs) {
            return emptyList()
        }

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return emptyList()

        return try {
            val events = usageStatsManager.queryEvents(startTimeMs, endTimeMs)
            val event = UsageEvents.Event()

            val openEvents = mutableMapOf<String, Long>()
            val resultEvents = mutableListOf<SleepAppUsageEvent>()
            val pm = context.packageManager

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                val pkg = event.packageName ?: continue
                if (IGNORED_PACKAGES.contains(pkg)) continue

                val timestamp = event.timeStamp

                when (event.eventType) {
                    UsageEvents.Event.ACTIVITY_RESUMED -> {
                        openEvents[pkg] = timestamp
                    }
                    UsageEvents.Event.ACTIVITY_PAUSED,
                    UsageEvents.Event.ACTIVITY_STOPPED -> {
                        val start = openEvents.remove(pkg)
                        if (start != null && timestamp > start) {
                            val durationSec = (timestamp - start) / 1000
                            if (durationSec >= 3) { // Filter out micro-transitions (< 3 seconds)
                                val appName = getAppLabel(pm, pkg)
                                resultEvents.add(
                                    SleepAppUsageEvent(
                                        packageName = pkg,
                                        appName = appName,
                                        startTimeEpochMs = start,
                                        endTimeEpochMs = timestamp,
                                        durationSeconds = durationSec
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Close any unclosed dangling open events at endTimeMs
            for ((pkg, start) in openEvents) {
                if (endTimeMs > start) {
                    val durationSec = (endTimeMs - start) / 1000
                    if (durationSec >= 3) {
                        val appName = getAppLabel(pm, pkg)
                        resultEvents.add(
                            SleepAppUsageEvent(
                                packageName = pkg,
                                appName = appName,
                                startTimeEpochMs = start,
                                endTimeEpochMs = endTimeMs,
                                durationSeconds = durationSec
                            )
                        )
                    }
                }
            }

            // Consolidate consecutive events for the same app if separated by < 30 seconds
            consolidateAppEvents(resultEvents)
        } catch (e: Exception) {
            Log.e(TAG, "Error querying app usage events during sleep", e)
            emptyList()
        }
    }

    private fun getAppLabel(pm: PackageManager, packageName: String): String {
        return try {
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getApplicationInfo(packageName, 0)
            }
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName.substringAfterLast('.')
        }
    }

    private fun consolidateAppEvents(events: List<SleepAppUsageEvent>): List<SleepAppUsageEvent> {
        if (events.isEmpty()) return emptyList()

        val sorted = events.sortedBy { it.startTimeEpochMs }
        val consolidated = mutableListOf<SleepAppUsageEvent>()

        var current = sorted[0]
        for (i in 1 until sorted.size) {
            val next = sorted[i]
            if (next.packageName == current.packageName && (next.startTimeEpochMs - current.endTimeEpochMs) <= 30_000L) {
                // Merge
                current = current.copy(
                    endTimeEpochMs = next.endTimeEpochMs,
                    durationSeconds = (next.endTimeEpochMs - current.startTimeEpochMs) / 1000
                )
            } else {
                consolidated.add(current)
                current = next
            }
        }
        consolidated.add(current)

        return consolidated
    }
}
