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
import android.graphics.Typeface
import android.widget.RemoteViews
import com.zenzeros.kimon.MainActivity
import com.zenzeros.kimon.R
import com.zenzeros.kimon.data.local.KimonDatabase
import com.zenzeros.kimon.domain.usecase.CalculateStreaksUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DateFormatSymbols
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.Locale

class FocusHeatmapWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                for (appWidgetId in appWidgetIds) {
                    updateAppWidgetInternal(context, appWidgetManager, appWidgetId)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_UPDATE_WIDGET) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    val thisWidget = ComponentName(context, FocusHeatmapWidgetProvider::class.java)
                    val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
                    for (appWidgetId in allWidgetIds) {
                        updateAppWidgetInternal(context, appWidgetManager, appWidgetId)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.zenzeros.kimon.widget.ACTION_UPDATE_HEATMAP_WIDGET"
        private val WEEK_DAYS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

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
                updateAppWidgetInternal(context, appWidgetManager, appWidgetId)
            }
        }

        suspend fun updateAppWidgetInternal(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_focus_heatmap)
            val database = KimonDatabase.getInstance(context)
            val zone = ZoneId.systemDefault()
            val today = LocalDate.now(zone)

            val sessions = try {
                database.focusSessionDao().getAllSessionsList()
            } catch (e: Exception) {
                emptyList()
            }

            // Active days set for quick lookup
            val activeDaysSet = mutableSetOf<String>()
            var todayFocusSeconds = 0

            for (session in sessions) {
                if (session.sessionType == "POMODORO" && session.actualDurationSeconds > 0) {
                    val sessionDate = Instant.ofEpochMilli(session.startTimeEpochMs).atZone(zone).toLocalDate()
                    val m = sessionDate.monthValue
                    val d = sessionDate.dayOfMonth
                    val key = "${sessionDate.year}-${if (m < 10) "0$m" else "$m"}-${if (d < 10) "0$d" else "$d"}"
                    activeDaysSet.add(key)
                    if (sessionDate == today) {
                        todayFocusSeconds += session.actualDurationSeconds
                    }
                }
            }

            val streaksResult = CalculateStreaksUseCase().invoke(sessions)

            // Render Heatmap Bitmap (Previous Month + Current Month, matching YearTab)
            val isNight = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            val bitmap = renderYearTabHeatmapBitmap(
                today = today,
                activeDaysSet = activeDaysSet,
                isNight = isNight
            )
            views.setImageViewBitmap(R.id.widget_heatmap_image, bitmap)

            // Update text views
            val todayHours = todayFocusSeconds / 3600
            val todayMins = (todayFocusSeconds % 3600) / 60
            val todayText = if (todayHours > 0) "Today ${todayHours}h ${todayMins}m" else "Today ${todayMins}m"

            views.setTextViewText(R.id.widget_today_badge, todayText)
            views.setTextViewText(R.id.widget_streak_badge, "${streaksResult.currentStreakDays}d Streak")

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

        private fun renderYearTabHeatmapBitmap(
            today: LocalDate,
            activeDaysSet: Set<String>,
            isNight: Boolean
        ): Bitmap {
            val width = 740
            val height = 280
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val monthNames = DateFormatSymbols(Locale.getDefault()).months

            val m2 = today.monthValue
            val y2 = today.year
            val m1 = if (m2 > 1) m2 - 1 else 12
            val y1 = if (m2 > 1) y2 else y2 - 1

            val months = listOf(
                Pair(y1, m1),
                Pair(y2, m2)
            )

            val primaryColor = if (isNight) 0xFFA2C9EB.toInt() else 0xFF386588.toInt()
            val onPrimaryColor = 0xFFFFFFFF.toInt()
            val unfocusedBgColor = if (isNight) 0xFF22262B.toInt() else 0xFFE2E7EC.toInt()
            val onSurfaceVariantColor = if (isNight) 0xFF91969E.toInt() else 0xFF72777F.toInt()
            val onSurfaceColor = if (isNight) 0xFFE2E2E6.toInt() else 0xFF1A1C1E.toInt()

            val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textAlign = Paint.Align.CENTER
                textSize = 15f
            }
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textAlign = Paint.Align.CENTER
                textSize = 21f
                typeface = Typeface.DEFAULT_BOLD
                color = onSurfaceColor
            }
            val weekdayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textAlign = Paint.Align.LEFT
                textSize = 15f
                typeface = Typeface.DEFAULT
                color = onSurfaceVariantColor
            }

            val leftMargin = 50f
            val topHeaderHeight = 36f
            val availableGridWidth = width - leftMargin - 20f
            val monthWidth = availableGridWidth / 2f
            val spacing = 5f
            val cols = 6
            val rows = 7

            val cellWidth = (monthWidth - (cols - 1) * spacing - 14f) / cols
            val cellHeight = (height - topHeaderHeight - (rows - 1) * spacing - 10f) / rows
            val cornerRadius = 6f

            // Draw Weekday labels on left
            for (r in 0 until rows) {
                val dayLabel = WEEK_DAYS[r]
                val top = topHeaderHeight + r * (cellHeight + spacing)
                val textY = top + (cellHeight / 2f) + (textPaint.textSize / 3f)
                canvas.drawText(dayLabel, 4f, textY, weekdayPaint)
            }

            // Draw 2 Months
            months.forEachIndexed { mIndex, (year, month) ->
                val startX = leftMargin + mIndex * monthWidth
                val ym = YearMonth.of(year, month)
                val maxDays = ym.lengthOfMonth()
                val firstDayOfWeek = LocalDate.of(year, month, 1).dayOfWeek.value - 1 // Monday = 0 ... Sunday = 6
                val monthName = monthNames[month - 1]

                // Draw Month Title
                val monthCenterX = startX + (cols * (cellWidth + spacing)) / 2f
                canvas.drawText(monthName, monthCenterX, topHeaderHeight - 10f, titlePaint)

                // Draw Grid
                for (col in 0 until cols) {
                    for (row in 0 until rows) {
                        val slotIndex = col * 7 + row
                        val dayNumber = slotIndex - firstDayOfWeek + 1

                        if (dayNumber in 1..maxDays) {
                            val dayKey = "$year-${if (month < 10) "0$month" else "$month"}-${if (dayNumber < 10) "0$dayNumber" else "$dayNumber"}"
                            val isFocused = activeDaysSet.contains(dayKey)

                            val left = startX + col * (cellWidth + spacing)
                            val top = topHeaderHeight + row * (cellHeight + spacing)
                            val rect = RectF(left, top, left + cellWidth, top + cellHeight)

                            cellPaint.color = if (isFocused) primaryColor else unfocusedBgColor
                            cellPaint.style = Paint.Style.FILL
                            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, cellPaint)

                            // Draw Day Number Text
                            textPaint.color = if (isFocused) onPrimaryColor else onSurfaceVariantColor
                            textPaint.typeface = if (isFocused) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                            val textY = rect.centerY() + (textPaint.textSize / 3f)
                            canvas.drawText(dayNumber.toString(), rect.centerX(), textY, textPaint)
                        }
                    }
                }
            }

            return bitmap
        }
    }
}
