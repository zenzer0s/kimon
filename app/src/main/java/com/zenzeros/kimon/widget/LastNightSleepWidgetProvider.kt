package com.zenzeros.kimon.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.zenzeros.kimon.MainActivity
import com.zenzeros.kimon.R
import com.zenzeros.kimon.data.local.KimonDatabase
import com.zenzeros.kimon.data.local.entity.SleepSessionEntity
import com.zenzeros.kimon.data.repository.UserSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.min

class LastNightSleepWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_UPDATE_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, LastNightSleepWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (appWidgetId in allWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.zenzeros.kimon.widget.ACTION_UPDATE_SLEEP_WIDGET"

        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, LastNightSleepWidgetProvider::class.java).apply {
                action = ACTION_UPDATE_WIDGET
            }
            context.sendBroadcast(intent)
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                val views = RemoteViews(context.packageName, R.layout.widget_last_night_sleep)
                val database = KimonDatabase.getInstance(context)
                val userSettingsRepo = UserSettingsRepository(context)

                val latestSession: SleepSessionEntity? = try {
                    database.sleepSessionDao().getLatestSession().first()
                } catch (e: Exception) {
                    null
                }

                val sleepGoalMinutes: Int = try {
                    userSettingsRepo.sleepGoalMinutes.first()
                } catch (e: Exception) {
                    480
                }

                val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

                if (latestSession != null && latestSession.durationMinutes > 0) {
                    val durationHours = latestSession.durationMinutes / 60
                    val durationMins = latestSession.durationMinutes % 60
                    val durationText = "${durationHours}h ${durationMins}m"

                    val startTimeStr = timeFormat.format(latestSession.startTimeEpochMs)
                    val endTimeStr = timeFormat.format(latestSession.endTimeEpochMs)
                    val timesText = "$startTimeStr – $endTimeStr"

                    val progressPercent = min(100, ((latestSession.durationMinutes * 100) / sleepGoalMinutes).toInt())
                    val goalHours = sleepGoalMinutes / 60
                    val goalMins = sleepGoalMinutes % 60
                    val goalText = if (goalMins > 0) "Goal: ${goalHours}h ${goalMins}m ($progressPercent%)" else "Goal: ${goalHours}h ($progressPercent%)"

                    val sourceText = when (latestSession.source) {
                        "GOOGLE_SLEEP_API" -> "Google Sleep API"
                        "HEALTH_CONNECT" -> "Health Connect"
                        else -> "Manual"
                    }

                    views.setViewVisibility(R.id.widget_header, View.VISIBLE)
                    views.setViewVisibility(R.id.widget_data_container, View.VISIBLE)
                    views.setViewVisibility(R.id.widget_bottom_container, View.VISIBLE)
                    views.setViewVisibility(R.id.widget_empty_container, View.GONE)

                    views.setTextViewText(R.id.widget_duration, durationText)
                    views.setTextViewText(R.id.widget_times, timesText)
                    views.setTextViewText(R.id.widget_quality_badge, "${latestSession.qualityScore}% Rest")
                    views.setProgressBar(R.id.widget_progress, 100, progressPercent, false)
                    views.setTextViewText(R.id.widget_goal_text, goalText)
                    views.setTextViewText(R.id.widget_source, sourceText)
                } else {
                    views.setViewVisibility(R.id.widget_header, View.VISIBLE)
                    views.setViewVisibility(R.id.widget_data_container, View.GONE)
                    views.setViewVisibility(R.id.widget_bottom_container, View.GONE)
                    views.setViewVisibility(R.id.widget_empty_container, View.VISIBLE)
                    views.setTextViewText(R.id.widget_quality_badge, "Active")
                }

                // Open Kimon app on widget click
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}
