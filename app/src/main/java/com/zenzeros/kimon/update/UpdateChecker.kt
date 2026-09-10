package com.zenzeros.kimon.update

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.zenzeros.kimon.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URI

/**
 * Checks GitHub Releases for a newer **stable** build and notifies the user.
 *
 * `/releases/latest` returns only the most recent published, non-draft,
 * non-prerelease release, so anything it returns is by definition "stable".
 * Distribution is via GitHub (no Play Store), so this is how users find updates.
 */
object UpdateChecker {

    private const val TAG = "UpdateChecker"
    private const val LATEST_RELEASE_URL =
        "https://api.github.com/repos/zenzer0s/kimon/releases/latest"

    private const val PREFS = "kimon_update_prefs"
    private const val KEY_LAST_CHECK_MS = "last_check_ms"
    private const val KEY_LAST_NOTIFIED_TAG = "last_notified_tag"
    private const val MIN_INTERVAL_MS = 6L * 60 * 60 * 1000 // 6 hours

    private const val CHANNEL_ID = "kimon_updates_channel"
    private const val NOTIFICATION_ID = 3001

    sealed interface Result {
        data class UpdateAvailable(val version: String, val url: String) : Result
        data class UpToDate(val version: String) : Result
        data object Failed : Result
    }

    /**
     * @param force ignore the throttle and the "already notified this version" guard
     *              (use for a manual "Check for updates" action).
     */
    suspend fun checkForUpdate(context: Context, force: Boolean = false): Result =
        withContext(Dispatchers.IO) {
            val appContext = context.applicationContext
            val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val now = System.currentTimeMillis()

            val currentVersionName = installedVersionName(appContext)
            val currentScore = versionScore(currentVersionName)

            if (!force && now - prefs.getLong(KEY_LAST_CHECK_MS, 0L) < MIN_INTERVAL_MS) {
                return@withContext Result.UpToDate(currentVersionName)
            }
            prefs.edit().putLong(KEY_LAST_CHECK_MS, now).apply()

            val release = fetchLatestRelease() ?: return@withContext Result.Failed
            if (release.draft || release.prerelease) {
                return@withContext Result.UpToDate(currentVersionName)
            }

            val latestScore = versionScore(release.tag)
            if (latestScore <= currentScore || latestScore < 0) {
                return@withContext Result.UpToDate(currentVersionName)
            }

            val version = release.tag.removePrefix("v").removePrefix("V")
            val alreadyNotified = prefs.getString(KEY_LAST_NOTIFIED_TAG, null) == release.tag
            if (!alreadyNotified || force) {
                prefs.edit().putString(KEY_LAST_NOTIFIED_TAG, release.tag).apply()
                notifyUpdate(appContext, version, release)
            }
            Result.UpdateAvailable(version, release.url)
        }

    private data class ReleaseInfo(
        val tag: String,
        val url: String,
        val name: String?,
        val body: String?,
        val prerelease: Boolean,
        val draft: Boolean,
    )

    private fun fetchLatestRelease(): ReleaseInfo? {
        var conn: HttpURLConnection? = null
        return try {
            conn = (URI(LATEST_RELEASE_URL).toURL().openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "Kimon-Android")
            }
            if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                Log.w(TAG, "release check HTTP ${conn.responseCode}")
                return null
            }
            val json = conn.inputStream.bufferedReader().use { it.readText() }
            val o = JSONObject(json)
            ReleaseInfo(
                tag = o.optString("tag_name"),
                url = o.optString("html_url", "https://github.com/zenzer0s/kimon/releases/latest"),
                name = o.optString("name").ifBlank { null },
                body = o.optString("body").ifBlank { null },
                prerelease = o.optBoolean("prerelease", false),
                draft = o.optBoolean("draft", false),
            )
        } catch (e: Exception) {
            Log.w(TAG, "release check failed", e)
            null
        } finally {
            conn?.disconnect()
        }
    }

    private fun installedVersionName(context: Context): String = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0.0.0"
    } catch (e: Exception) {
        "0.0.0"
    }

    /** major.minor.patch -> comparable score. Returns -1 if unparseable. */
    private fun versionScore(raw: String?): Long {
        if (raw.isNullOrBlank()) return -1
        val cleaned = raw.trim().removePrefix("v").removePrefix("V")
        val nums = cleaned.split(".", "-", "+", "_")
            .map { part -> part.takeWhile { it.isDigit() } }
            .filter { it.isNotEmpty() }
            .map { it.toInt() }
        if (nums.isEmpty()) return -1
        val major = nums.getOrElse(0) { 0 }
        val minor = nums.getOrElse(1) { 0 }
        val patch = nums.getOrElse(2) { 0 }
        return major * 1_000_000L + minor * 1_000L + patch
    }

    private fun notifyUpdate(context: Context, version: String, release: ReleaseInfo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.updates_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply { description = context.getString(R.string.updates_channel_desc) }
            )
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            Intent(Intent.ACTION_VIEW, Uri.parse(release.url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.deployed_app_update)
            .setContentTitle(context.getString(R.string.update_available_title, version))
            .setContentText(release.name ?: context.getString(R.string.update_available_body))
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    (release.body?.take(400) ?: context.getString(R.string.update_available_body))
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Permission revoked between the check above and now
        }
    }
}
