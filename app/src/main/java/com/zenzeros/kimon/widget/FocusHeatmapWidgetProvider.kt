package com.zenzeros.kimon.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.widget.RemoteViews
import com.zenzeros.kimon.MainActivity
import com.zenzeros.kimon.R
import com.zenzeros.kimon.data.local.KimonDatabase
import com.zenzeros.kimon.domain.usecase.CalculateStreaksUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

class FocusHeatmapWidgetProvider : AppWidgetProvider() {

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
            val thisWidget = ComponentName(context, FocusHeatmapWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (appWidgetId in allWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.zenzeros.kimon.widget.ACTION_UPDATE_HEATMAP_WIDGET"
        private const val WEEKS_COUNT = 16
        private const val DAYS_IN_WEEK = 7

        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, FocusHeatmapWidgetProvider::class.java).apply {
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
                val views = RemoteViews(context.packageName, R.layout.widget_focus_heatmap)
                val database = KimonDatabase.getInstance(context)
                val zone = ZoneId.systemDefault()
                val today = LocalDate.now(zone)

                val daysTotal = WEEKS_COUNT * DAYS_IN_WEEK
                val startDate = today.minusDays((daysTotal - 1).toLong())
                val startEpochMs = startDate.atStartOfDay(zone).toInstant().toEpochMilli()
                val endEpochMs = System.currentTimeMillis()

                val sessions = try {
                    database.focusSessionDao().getAllSessionsList()
                } catch (e: Exception) {
                    emptyList()
                }

                // Group duration by LocalDate
                val dailyFocusMinutes = mutableMapOf<LocalDate, Int>()
                var todayFocusSeconds = 0

                for (session in sessions) {
                    if (session.sessionType == "POMODORO" && session.actualDurationSeconds > 0) {
                        val sessionDate = Instant.ofEpochMilli(session.startTimeEpochMs).atZone(zone).toLocalDate()
                        dailyFocusMinutes[sessionDate] = (dailyFocusMinutes[sessionDate] ?: 0) + (session.actualDurationSeconds / 60)
                        if (sessionDate == today) {
                            todayFocusSeconds += session.actualDurationSeconds
                        }
                    }
                }

                val streaksResult = CalculateStreaksUseCase().invoke(sessions)

                // Render Heatmap Bitmap
                val isNight = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
                val bitmap = renderHeatmapBitmap(
                    startDate = startDate,
                    dailyFocusMinutes = dailyFocusMinutes,
                    today = today,
                    isNight = isNight
                )
                views.setImageViewBitmap(R.id.widget_heatmap_image, bitmap)

                // Update text views
                val todayHours = todayFocusSeconds / 3600
                val todayMins = (todayFocusSeconds % 3600) / 60
                val todayText = if (todayHours > 0) "Today ${todayHours}h ${todayMins}m" else "Today ${todayMins}m"

                val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
                val startMonth = monthFormat.format(Date(startEpochMs))
                val endMonth = monthFormat.format(Date(endEpochMs))
                val rangeText = "$startMonth – $endMonth ($WEEKS_COUNT Weeks)"

                views.setTextViewText(R.id.widget_today_badge, todayText)
                views.setTextViewText(R.id.widget_streak_badge, "🔥 ${streaksResult.currentStreakDays}d")
                views.setTextViewText(R.id.widget_month_range, rangeText)

                // Click Action to open MainActivity
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    1,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

        private fun renderHeatmapBitmap(
            startDate: LocalDate,
            dailyFocusMinutes: Map<LocalDate, Int>,
            today: LocalDate,
            isNight: Boolean
        ): Bitmap {
            val width = 640
            val height = 240
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val todayIndicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (isNight) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
                style = Paint.Style.FILL
            }

            val cols = WEEKS_COUNT
            val rows = DAYS_IN_WEEK

            val cellGap = 5f
            val totalHorizontalGaps = (cols - 1) * cellGap
            val totalVerticalGaps = (rows - 1) * cellGap

            val cellWidth = (width - totalHorizontalGaps) / cols
            val cellHeight = (height - totalVerticalGaps) / rows
            val cellSize = kotlin.math.min(cellWidth, cellHeight)
            val cornerRadius = 4.5f

            // Color Palette
            val level0 = if (isNight) 0x1EFFFFFF.toInt() else 0x14000000.toInt()
            val level1 = if (isNight) 0x48A2C9EB.toInt() else 0x48386588.toInt()
            val level2 = if (isNight) 0x85A2C9EB.toInt() else 0x85386588.toInt()
            val level3 = if (isNight) 0xC2A2C9EB.toInt() else 0xC2386588.toInt()
            val level4 = if (isNight) 0xFFA2C9EB.toInt() else 0xFF386588.toInt()

            for (col in 0 until cols) {
                for (row in 0 until rows) {
                    val dayOffset = (col * rows + row).toLong()
                    val cellDate = startDate.plusDays(dayOffset)

                    if (cellDate.isAfter(today)) {
                        continue
                    }

                    val minutes = dailyFocusMinutes[cellDate] ?: 0
                    val cellColor = when {
                        minutes <= 0 -> level0
                        minutes < 25 -> level1
                        minutes < 60 -> level2
                        minutes < 120 -> level3
                        else -> level4
                    }

                    val left = col * (cellSize + cellGap)
                    val top = row * (cellSize + cellGap)
                    val rect = RectF(left, top, left + cellSize, top + cellSize)

                    paint.color = cellColor
                    paint.style = Paint.Style.FILL
                    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)

                    // Draw subtle center dot if today
                    if (cellDate == today) {
                        val centerX = rect.centerX()
                        val centerY = rect.centerY()
                        canvas.drawCircle(centerX, centerY, 2f, todayIndicatorPaint)
                    }
                }
            }

            return bitmap
        }
    }
}
