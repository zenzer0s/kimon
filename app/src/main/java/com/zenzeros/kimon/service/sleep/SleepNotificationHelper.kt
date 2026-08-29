package com.zenzeros.kimon.service.sleep

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.zenzeros.kimon.MainActivity
import com.zenzeros.kimon.R
import com.zenzeros.kimon.data.local.entity.SleepSessionEntity
import java.text.SimpleDateFormat
import java.util.Locale

object SleepNotificationHelper {

    private const val CHANNEL_ID = "sleep_summary_channel"
    private const val CHANNEL_MONITORING_ID = "sleep_monitoring_channel"
    private const val NOTIFICATION_ID = 2001
    const val MONITORING_NOTIFICATION_ID = 2002

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Summary Channel
            val summaryChannel = NotificationChannel(
                CHANNEL_ID,
                "Sleep Summaries",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for last night's sleep duration and schedule"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(summaryChannel)

            // Ongoing Monitoring Channel (Low Priority / Silent)
            val monitoringChannel = NotificationChannel(
                CHANNEL_MONITORING_ID,
                "Sleep Tracking Active",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Low-power background sleep detection service"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(monitoringChannel)
        }
    }

    fun createMonitoringNotification(context: Context): android.app.Notification {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "sleep")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            MONITORING_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_MONITORING_ID)
            .setSmallIcon(R.drawable.ic_moon)
            .setContentTitle("Sleep Monitoring Active")
            .setContentText("Kimon native engine is analyzing motion & sleep cycles")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    fun sendSleepSummaryNotification(context: Context, session: SleepSessionEntity) {
        if (session.durationMinutes <= 0) return

        // Check POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        createNotificationChannel(context)

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val startTimeStr = timeFormat.format(session.startTimeEpochMs)
        val endTimeStr = timeFormat.format(session.endTimeEpochMs)

        val hours = session.durationMinutes / 60
        val mins = session.durationMinutes % 60
        val durationStr = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"

        val qualityRating = when {
            session.qualityScore >= 85 -> "Optimal"
            session.qualityScore >= 75 -> "Good"
            session.qualityScore >= 60 -> "Fair"
            else -> "Low"
        }

        val title = "Last Night's Sleep: $durationStr"
        val contentText = "Slept at $startTimeStr • Woke up at $endTimeStr"
        val expandedText = "Total Duration: $durationStr ($qualityRating • ${session.qualityScore}%)\n" +
                "Slept at: $startTimeStr\n" +
                "Woke up at: $endTimeStr"

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "sleep")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_moon)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }
}
