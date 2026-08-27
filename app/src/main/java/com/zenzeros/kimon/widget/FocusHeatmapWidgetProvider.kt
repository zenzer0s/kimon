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
        if (intent.action == ACTION_UPDATE_WIDGET ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            intent.action == Intent.ACTION_BOOT_COMPLETED
        ) {
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
        private const val TOTAL_COLS = 10

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, FocusHeatmapWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            if (allWidgetIds.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    for (appWidgetId in allWidgetIds) {
                        updateAppWidgetInternal(context, appWidgetManager, appWidgetId)
                    }
                }
            }
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

            // Render Continuous Calendar Heatmap Bitmap (Zero gap between 31st and 1st)
            val isNight = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            val bitmap = renderContinuousHeatmapBitmap(
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

        private fun renderContinuousHeatmapBitmap(
            today: LocalDate,
            activeDaysSet: Set<String>,
            isNight: Boolean
        ): Bitmap {
            val width = 1000
            val height = 440
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val monthNames = DateFormatSymbols(Locale.getDefault()).months

            // Colors
            val primaryColor = if (isNight) 0xFFA2C9EB.toInt() else 0xFF386588.toInt()
            val onPrimaryTextColor = if (isNight) 0xFF001D32.toInt() else 0xFFFFFFFF.toInt()
            val unfocusedBgColor = if (isNight) 0xFF282C34.toInt() else 0xFFE0E5EC.toInt()
            val unfocusedBorderColor = if (isNight) 0x1EFFFFFF.toInt() else 0x1A000000.toInt()
            val unfocusedTextColor = if (isNight) 0xFFE0E3EB.toInt() else 0xFF2D3139.toInt()
            val futureCellBgColor = if (isNight) 0x0DFFFFFF.toInt() else 0x08000000.toInt()
            val onSurfaceColor = if (isNight) 0xFFFFFFFF.toInt() else 0xFF14171A.toInt()
            val weekdayColor = if (isNight) 0xFF9DA2AC.toInt() else 0xFF656B75.toInt()

            val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textAlign = Paint.Align.CENTER
                textSize = 21f
                typeface = Typeface.DEFAULT_BOLD
            }
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textAlign = Paint.Align.CENTER
                textSize = 26f
                typeface = Typeface.DEFAULT_BOLD
                color = onSurfaceColor
            }
            val weekdayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textAlign = Paint.Align.LEFT
                textSize = 19f
                typeface = Typeface.DEFAULT_BOLD
                color = weekdayColor
            }

            val leftMargin = 62f
            val topHeaderHeight = 44f
            val spacing = 7.5f
            val cols = TOTAL_COLS
            val rows = 7
            val cornerRadius = 9f

            val totalGridWidth = width - leftMargin - 16f
            val cellWidth = (totalGridWidth - (cols - 1) * spacing) / cols
            val cellHeight = (height - topHeaderHeight - (rows - 1) * spacing - 6f) / rows

            // Align weekday labels
            val weekdayMetrics = weekdayPaint.fontMetrics
            val weekdayYOffset = (cellHeight / 2f) - (weekdayMetrics.ascent + weekdayMetrics.descent) / 2f

            for (r in 0 until rows) {
                val dayLabel = WEEK_DAYS[r]
                val top = topHeaderHeight + r * (cellHeight + spacing)
                val textY = top + weekdayYOffset
                canvas.drawText(dayLabel, 6f, textY, weekdayPaint)
            }

            // Calculate start date (Monday of TOTAL_COLS - 1 weeks ago)
            val currentWeekMonday = today.minusDays((today.dayOfWeek.value - 1).toLong())
            val startMonday = currentWeekMonday.minusWeeks((cols - 1).toLong())

            val textMetrics = textPaint.fontMetrics
            val textYOffset = (cellHeight / 2f) - (textMetrics.ascent + textMetrics.descent) / 2f

            // Track columns per month for drawing month titles
            val monthColBounds = mutableMapOf<Pair<Int, Int>, Pair<Int, Int>>() // (year, month) -> (minCol, maxCol)

            // Draw Continuous Grid Cells
            for (col in 0 until cols) {
                val weekMonday = startMonday.plusWeeks(col.toLong())

                for (row in 0 until rows) {
                    val cellDate = weekMonday.plusDays(row.toLong())
                    val ymKey = Pair(cellDate.year, cellDate.monthValue)

                    val existing = monthColBounds[ymKey]
                    if (existing == null) {
                        monthColBounds[ymKey] = Pair(col, col)
                    } else {
                        monthColBounds[ymKey] = Pair(minOf(existing.first, col), maxOf(existing.second, col))
                    }

                    val left = leftMargin + col * (cellWidth + spacing)
                    val top = topHeaderHeight + row * (cellHeight + spacing)
                    val rect = RectF(left, top, left + cellWidth, top + cellHeight)

                    val isToday = cellDate.isEqual(today)
                    val isFuture = cellDate.isAfter(today)

                    if (isFuture) {
                        // Future day cell in the current week
                        cellPaint.color = futureCellBgColor
                        cellPaint.style = Paint.Style.FILL
                        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, cellPaint)
                    } else {
                        val m = cellDate.monthValue
                        val d = cellDate.dayOfMonth
                        val dayKey = "${cellDate.year}-${if (m < 10) "0$m" else "$m"}-${if (d < 10) "0$d" else "$d"}"
                        val isFocused = activeDaysSet.contains(dayKey)

                        // 1. Draw Cell Background
                        cellPaint.color = if (isFocused) primaryColor else unfocusedBgColor
                        cellPaint.style = Paint.Style.FILL
                        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, cellPaint)

                        // 2. Draw Outline (Accent for today, subtle for unfocused)
                        if (isToday && !isFocused) {
                            borderPaint.color = primaryColor
                            borderPaint.strokeWidth = 3f
                            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)
                        } else if (!isFocused) {
                            borderPaint.color = unfocusedBorderColor
                            borderPaint.strokeWidth = 1.5f
                            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)
                        }

                        // 3. Draw Day Number Text
                        textPaint.color = if (isFocused) onPrimaryTextColor else unfocusedTextColor
                        val textY = top + textYOffset
                        canvas.drawText(d.toString(), rect.centerX(), textY, textPaint)
                    }
                }
            }

            // Draw Month Titles above continuous month column spans
            for ((ym, bounds) in monthColBounds) {
                val (year, month) = ym
                val (minCol, maxCol) = bounds
                val spanCount = maxCol - minCol + 1

                // Only draw month name if it covers at least 2 columns
                if (spanCount >= 2) {
                    val monthName = monthNames[month - 1]
                    val startLeft = leftMargin + minCol * (cellWidth + spacing)
                    val endRight = leftMargin + maxCol * (cellWidth + spacing) + cellWidth
                    val monthCenterX = (startLeft + endRight) / 2f
                    canvas.drawText(monthName, monthCenterX, topHeaderHeight - 12f, titlePaint)
                }
            }

            return bitmap
        }
    }
}
