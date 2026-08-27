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
    private const val NOTIFICATION_ID = 2001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Sleep Summaries"
            val descriptionText = "Notifications for last night's sleep duration and schedule"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
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
