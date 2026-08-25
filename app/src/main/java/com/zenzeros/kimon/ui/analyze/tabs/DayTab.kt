@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui.analyze.tabs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenzeros.kimon.R
import com.zenzeros.kimon.domain.model.DayStats
import com.zenzeros.kimon.ui.analyze.AnalyzeViewModel
import com.zenzeros.kimon.ui.analyze.components.AnalyzeCardHeader
import com.zenzeros.kimon.ui.analyze.components.AnalyzeEmptyState
import com.zenzeros.kimon.ui.analyze.components.AnalyzeNavigationHeader
import com.zenzeros.kimon.ui.analyze.components.MetricTileCard
import com.zenzeros.kimon.ui.analyze.components.horizontalSegmentedShape
import com.zenzeros.kimon.ui.theme.CustomColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DayTab(
    stats: DayStats = DayStats(),
    selectedCalendar: Calendar = remember { Calendar.getInstance() },
    onPreviousDay: () -> Unit = {},
    onNextDay: () -> Unit = {},
    onNavigateToFocus: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val dayOfWeek = remember(selectedCalendar.timeInMillis) {
        SimpleDateFormat("EEE", Locale.getDefault()).format(selectedCalendar.time).uppercase()
    }

    val formattedDate = remember(selectedCalendar.timeInMillis) {
        SimpleDateFormat("MM / dd", Locale.getDefault()).format(selectedCalendar.time)
    }

    val totalSectionsGroup = 3
    val todayFocusShapesGroup = ListItemDefaults.segmentedShapes(index = 0, count = totalSectionsGroup)
    val hourlyFocusShapesGroup = ListItemDefaults.segmentedShapes(index = 1, count = totalSectionsGroup)
    val dailyTimelineShapesGroup = ListItemDefaults.segmentedShapes(index = 2, count = totalSectionsGroup)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // 1. Navigation Header: [ Left: Combined Day & Date Pill ] ... [ Right: ButtonGroup with < and > ]
        AnalyzeNavigationHeader(
            onPreviousClick = onPreviousDay,
            onNextClick = onNextDay
        ) {
            Text(
                text = dayOfWeek,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "•",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Text(
                text = formattedDate,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.5.sp,
                    letterSpacing = 0.3.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Icon(
                painter = painterResource(R.drawable.ic_calendar),
                contentDescription = "Calendar",
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Material 3 Expressive Segmented Group (Today's Focus, Daily Timeline, and Hourly Focus)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.5.dp)
        ) {
            // --- Card 1: Today's Focus ---
            Surface(
                shape = todayFocusShapesGroup.shape,
                color = CustomColors.cardContainerColor,
                border = CustomColors.cardBorder,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(11.dp)
                ) {
                    AnalyzeCardHeader(
                        icon = R.drawable.ic_focus,
                        title = stringResource(R.string.title_todays_focus)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Horizontal Segmented Row: [ Total Focus ] [ Total Sessions ]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        MetricTileCard(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            shape = horizontalSegmentedShape(index = 0, count = 2),
                            icon = R.drawable.ic_focus,
                            iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                            iconBg = MaterialTheme.colorScheme.primaryContainer,
                            valueColor = MaterialTheme.colorScheme.primary,
                            label = stringResource(R.string.label_total_focus),
                            value = AnalyzeViewModel.formatDuration(stats.totalFocusSeconds)
                        )

                        MetricTileCard(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            shape = horizontalSegmentedShape(index = 1, count = 2),
                            icon = R.drawable.ic_bar_chart,
                            iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                            iconBg = MaterialTheme.colorScheme.secondaryContainer,
                            valueColor = MaterialTheme.colorScheme.secondary,
                            label = stringResource(R.string.label_total_sessions),
                            value = stats.totalSessions.toString()
                        )
                    }
                }
            }

            // --- Card 2: Hourly Focus (Bar Graph of 24 lines for 24 hours) ---
            Surface(
                shape = hourlyFocusShapesGroup.shape,
                color = CustomColors.cardContainerColor,
                border = CustomColors.cardBorder,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(11.dp)
                ) {
                    AnalyzeCardHeader(
                        icon = R.drawable.ic_bar_chart,
                        title = stringResource(R.string.title_hourly_focus),
                        iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                        iconBg = MaterialTheme.colorScheme.tertiaryContainer
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val isSelectedDayToday = remember(selectedCalendar.timeInMillis) {
                        val now = Calendar.getInstance()
                        now.get(Calendar.YEAR) == selectedCalendar.get(Calendar.YEAR) &&
                                now.get(Calendar.DAY_OF_YEAR) == selectedCalendar.get(Calendar.DAY_OF_YEAR)
                    }

                    val currentHour = if (isSelectedDayToday) Calendar.getInstance().get(Calendar.HOUR_OF_DAY) else -1

                    val rawMaxMinutes = stats.hourlyMinutes.maxOrNull() ?: 0
                    val maxTotalMinutes = if (rawMaxMinutes <= 60) 60 else (((rawMaxMinutes + 14) / 15) * 15)
                    val stepMinutes = maxTotalMinutes / 4
                    val yLabels = listOf(
                        "${4 * stepMinutes}m",
                        "${3 * stepMinutes}m",
                        "${2 * stepMinutes}m",
                        "${1 * stepMinutes}m",
                        "0m"
                    )

                    val primaryColor = MaterialTheme.colorScheme.primary
                    val secondaryColor = MaterialTheme.colorScheme.secondary
                    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f)

                    // Main Chart Container: [ Y-Axis Labels | Canvas Bar Graph ]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 6.dp)
                    ) {
                        // Y-Axis Labels Column (5 points from top to 0m)
                        Column(
                            modifier = Modifier
                                .height(130.dp)
                                .width(28.dp)
                                .padding(end = 4.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.End
                        ) {
                            yLabels.forEach { label ->
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Chart Area: Canvas + X-Axis
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                            ) {
                                val chartWidth = size.width
                                val chartHeight = size.height
                                val topPadding = 8.dp.toPx()
                                val bottomPadding = 8.dp.toPx()
                                val usableHeight = chartHeight - topPadding - bottomPadding

                                val yTop = topPadding
                                val yBottom = chartHeight - bottomPadding

                                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

                                // 1. Horizontal Y-Axis Grid Lines (5 levels)
                                for (level in 0..4) {
                                    val yLevel = yTop + (level / 4f) * usableHeight
                                    val isBaseLine = level == 4
                                    drawLine(
                                        color = if (isBaseLine) gridColor.copy(alpha = 0.6f) else gridColor,
                                        start = Offset(0f, yLevel),
                                        end = Offset(chartWidth, yLevel),
                                        strokeWidth = if (isBaseLine) 1.5.dp.toPx() else 1.dp.toPx(),
                                        pathEffect = if (isBaseLine) null else dashEffect
                                    )
                                }

                                // 2. 24 Hourly Bar Lines
                                val numBars = 24
                                val barSlotWidth = chartWidth / numBars
                                val barStrokeWidth = (barSlotWidth * 0.45f).coerceIn(3.dp.toPx(), 6.dp.toPx())

                                for (h in 0 until numBars) {
                                    val mins = stats.hourlyMinutes.getOrElse(h) { 0 }
                                    val isCurrent = h == currentHour
                                    val x = (h + 0.5f) * barSlotWidth

                                    // Background full track line
                                    drawLine(
                                        color = if (isCurrent) primaryColor.copy(alpha = 0.15f) else trackColor,
                                        start = Offset(x, yTop),
                                        end = Offset(x, yBottom),
                                        strokeWidth = barStrokeWidth,
                                        cap = StrokeCap.Round
                                    )

                                    // Active Focus Bar
                                    if (mins > 0) {
                                        val fraction = (mins.toFloat() / maxTotalMinutes).coerceIn(0f, 1f)
                                        val barHeight = (fraction * usableHeight).coerceAtLeast(barStrokeWidth)
                                        val barYTop = yBottom - barHeight

                                        drawLine(
                                            brush = Brush.verticalGradient(
                                                colors = if (isCurrent) {
                                                    listOf(secondaryColor, primaryColor)
                                                } else {
                                                    listOf(primaryColor, primaryColor.copy(alpha = 0.75f))
                                                },
                                                startY = barYTop,
                                                endY = yBottom
                                            ),
                                            start = Offset(x, barYTop),
                                            end = Offset(x, yBottom),
                                            strokeWidth = barStrokeWidth,
                                            cap = StrokeCap.Round
                                        )
                                    }

                                    // Current Hour Marker Dot at baseline
                                    if (isCurrent) {
                                        drawCircle(
                                            color = primaryColor,
                                            radius = 2.dp.toPx(),
                                            center = Offset(x, yBottom + 5.dp.toPx())
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // X-Axis Time Labels Row (Key hour markers across 24h)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val timeAxisLabels = listOf("00:00", "06:00", "12:00", "18:00", "23:00")
                                timeAxisLabels.forEach { timeLabel ->
                                    Text(
                                        text = timeLabel,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 10.5.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // --- Card 3: Daily Timeline ---
            Surface(
                shape = dailyTimelineShapesGroup.shape,
                color = CustomColors.cardContainerColor,
                border = CustomColors.cardBorder,
                tonalElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(11.dp)
                ) {
                    AnalyzeCardHeader(
                        icon = R.drawable.ic_calendar,
                        title = stringResource(R.string.title_daily_timeline),
                        iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                        iconBg = MaterialTheme.colorScheme.tertiaryContainer
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (stats.timelineSessions.isEmpty()) {
                        AnalyzeEmptyState(
                            icon = R.drawable.ic_focus,
                            message = stringResource(R.string.empty_no_focus_sessions_day),
                            actionText = stringResource(R.string.action_start_focus),
                            onActionClick = onNavigateToFocus
                        )
                    } else {
                        val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(0.dp),
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            stats.timelineSessions.forEachIndexed { index, item ->
                                val itemShapes = ListItemDefaults.segmentedShapes(
                                    index = index,
                                    count = stats.timelineSessions.size
                                )
                                val isFirst = index == 0
                                val isLast = index == stats.timelineSessions.size - 1
                                val isBreak = item.session.sessionType != "POMODORO"

                                val tagColor = remember(item.tag?.colorHex, isBreak) {
                                    if (isBreak) {
                                        Color(0xFF9E9E9E)
                                    } else {
                                        try {
                                            Color(android.graphics.Color.parseColor(item.tag?.colorHex ?: "#6366F1"))
                                        } catch (e: Exception) {
                                            Color(0xFF6366F1)
                                        }
                                    }
                                }

                                val lineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(IntrinsicSize.Min),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // --- 1. Left Vertical Connected Track & Node ---
                                    Box(
                                        modifier = Modifier
                                            .width(22.dp)
                                            .fillMaxHeight(),
                                        contentAlignment = Alignment.TopCenter
                                    ) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val centerX = size.width / 2f
                                            val centerY = size.height / 2f

                                            // Top connector line
                                            if (!isFirst) {
                                                drawLine(
                                                    color = lineColor,
                                                    start = Offset(centerX, 0f),
                                                    end = Offset(centerX, centerY),
                                                    strokeWidth = 2.dp.toPx()
                                                )
                                            }

                                            // Bottom connector line (dashed if break session)
                                            if (!isLast) {
                                                val dashEffect = if (isBreak) PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f) else null
                                                drawLine(
                                                    color = lineColor,
                                                    start = Offset(centerX, centerY),
                                                    end = Offset(centerX, size.height),
                                                    strokeWidth = 2.dp.toPx(),
                                                    pathEffect = dashEffect
                                                )
                                            }
                                        }

                                        // Node Indicator Badge
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(tagColor.copy(alpha = 0.2f))
                                                .border(
                                                    width = 1.5.dp,
                                                    color = tagColor,
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(tagColor)
                                            )
                                        }
                                    }

                                    // --- 2. Right Segmented Session Card ---
                                    Surface(
                                        shape = itemShapes.shape,
                                        color = CustomColors.innerCardContainerColor,
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = 1.dp,
                                            color = CustomColors.innerCardBorderColor
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .padding(vertical = 1.5.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .clip(CircleShape)
                                                            .background(tagColor)
                                                    )
                                                    Text(
                                                        text = item.tag?.name ?: item.session.sessionType.replace("_", " "),
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontWeight = FontWeight.SemiBold,
                                                            fontSize = 13.sp
                                                        ),
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }

                                                Text(
                                                    text = "${timeFormat.format(Date(item.session.startTimeEpochMs))} - ${timeFormat.format(Date(item.session.endTimeEpochMs))}",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontSize = 11.sp
                                                    ),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            Text(
                                                text = AnalyzeViewModel.formatDuration(item.session.actualDurationSeconds.toLong()),
                                                style = MaterialTheme.typography.labelLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.5.sp
                                                ),
                                                color = if (isBreak) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
