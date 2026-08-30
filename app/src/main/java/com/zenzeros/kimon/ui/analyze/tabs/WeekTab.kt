@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.zenzeros.kimon.ui.analyze.tabs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenzeros.kimon.R
import com.zenzeros.kimon.domain.model.WeekStats
import com.zenzeros.kimon.ui.analyze.AnalyzeViewModel
import com.zenzeros.kimon.ui.analyze.components.AnalyzeCardHeader
import com.zenzeros.kimon.ui.analyze.components.AnalyzeNavigationHeader
import com.zenzeros.kimon.ui.analyze.components.MetricTileCard
import com.zenzeros.kimon.ui.analyze.components.horizontalSegmentedShape
import com.zenzeros.kimon.ui.components.bouncyScroll
import com.zenzeros.kimon.ui.theme.CustomColors
import com.zenzeros.kimon.ui.theme.LocalAppFonts
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val WEEK_DAY_LABELS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

@Composable
fun WeekTab(
    stats: WeekStats = WeekStats(),
    selectedWeekStart: Calendar = remember {
        Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }
    },
    onPreviousWeek: () -> Unit = {},
    onNextWeek: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val totalSections = 3
    val weeklyFocusShapes = ListItemDefaults.segmentedShapes(index = 0, count = totalSections)
    val focusDistributionShapes = ListItemDefaults.segmentedShapes(index = 1, count = totalSections)
    val focusTrendsShapes = ListItemDefaults.segmentedShapes(index = 2, count = totalSections)

    val weekRangeText = remember(selectedWeekStart.timeInMillis) {
        val endCal = selectedWeekStart.clone() as Calendar
        endCal.add(Calendar.DAY_OF_YEAR, 6)

        val monthFormat = SimpleDateFormat("MMM d", Locale.getDefault())
        "${monthFormat.format(selectedWeekStart.time)} - ${monthFormat.format(endCal.time)}"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .bouncyScroll()
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // 1. Navigation Header: [ Left: Combined Week Range Pill ] ... [ Right: ButtonGroup with < and > ]
        AnalyzeNavigationHeader(
            onPreviousClick = onPreviousWeek,
            onNextClick = onNextWeek
        ) {
            Text(
                text = weekRangeText,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 0.4.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1
            )

            Icon(
                painter = painterResource(R.drawable.ic_calendar),
                contentDescription = "Calendar",
                modifier = Modifier.size(15.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Material 3 Expressive Segmented Group
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // --- Card 1: Weekly Focus ---
            Surface(
                shape = weeklyFocusShapes.shape,
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
                        title = stringResource(R.string.title_weekly_focus)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Horizontal Segmented Row: [ Total Time ] [ Sessions ]
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
                            label = stringResource(R.string.label_total_time),
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
                            label = stringResource(R.string.label_sessions),
                            value = stats.totalSessions.toString()
                        )
                    }
                }
            }

            // --- Card 2: Focus Distribution ---
            Surface(
                shape = focusDistributionShapes.shape,
                color = CustomColors.cardContainerColor,
                border = CustomColors.cardBorder,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(11.dp)
                ) {
                    AnalyzeCardHeader(
                        icon = R.drawable.ic_pie_chart,
                        title = stringResource(R.string.title_focus_distribution)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Donut Ring Chart Row: [ Donut ]  [ Breakdown ]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Donut Ring
                        val ringTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
                        val primaryColor = MaterialTheme.colorScheme.primary

                        Box(
                            modifier = Modifier.size(110.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(
                                    color = ringTrackColor,
                                    style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                                )

                                // Draw tag segments if available
                                var startAngle = -90f
                                for (dist in stats.tagDistributions) {
                                    val sweep = dist.percentage * 360f
                                    val segColor = try {
                                        Color(android.graphics.Color.parseColor(dist.tag?.colorHex ?: "#7C4DFF"))
                                    } catch (e: Exception) {
                                        primaryColor
                                    }
                                    drawArc(
                                        color = segColor,
                                        startAngle = startAngle,
                                        sweepAngle = sweep,
                                        useCenter = false,
                                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                    startAngle += sweep
                                }
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = AnalyzeViewModel.formatDuration(stats.totalFocusSeconds),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        fontFamily = LocalAppFonts.current.topBarTitle
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(R.string.label_total),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Right description or tag list
                        if (stats.tagDistributions.isEmpty()) {
                            Text(
                                text = stringResource(R.string.empty_no_focus_data_week),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                stats.tagDistributions.take(3).forEach { item ->
                                    val segColor = remember(item.tag?.colorHex) {
                                        try {
                                            Color(android.graphics.Color.parseColor(item.tag?.colorHex ?: "#7C4DFF"))
                                        } catch (e: Exception) {
                                            primaryColor
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(segColor)
                                            )
                                            Text(
                                                text = item.tag?.name ?: "Focus",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 11.5.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        Text(
                                            text = "${(item.percentage * 100).toInt()}%",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            ),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- Card 3: Focus Trends (Line Graph with X and Y Axis) ---
            Surface(
                shape = focusTrendsShapes.shape,
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
                        icon = R.drawable.ic_trending_up,
                        title = stringResource(R.string.title_focus_trends),
                        iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                        iconBg = MaterialTheme.colorScheme.tertiaryContainer
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val isCurrentWeek = remember(selectedWeekStart.timeInMillis) {
                        val now = Calendar.getInstance()
                        now.firstDayOfWeek = Calendar.MONDAY
                        now.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                        now.get(Calendar.YEAR) == selectedWeekStart.get(Calendar.YEAR) &&
                                now.get(Calendar.WEEK_OF_YEAR) == selectedWeekStart.get(Calendar.WEEK_OF_YEAR)
                    }

                    val todayDayIndex = if (isCurrentWeek) {
                        (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7
                    } else {
                        -1
                    }

                    val rawMaxMinutes = stats.dailyMinutes.maxOrNull() ?: 0
                    val rawMaxHours = (rawMaxMinutes + 59) / 60
                    val stepHours = maxOf(1, (rawMaxHours + 3) / 4)
                    val yLabels = listOf(
                        "${4 * stepHours}h",
                        "${3 * stepHours}h",
                        "${2 * stepHours}h",
                        "${1 * stepHours}h",
                        "0h"
                    )
                    val maxTotalMinutes = 4 * stepHours * 60

                    val primaryColor = MaterialTheme.colorScheme.primary
                    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerHigh

                    // Main Chart Container: [ Y-Axis Labels | Canvas Line Graph ]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 6.dp)
                    ) {
                        // Y-Axis Labels Column (5 points from top to 0h)
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
                            // Line Graph Canvas
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

                                // 2. Calculate Point Coordinates
                                val numPoints = 7
                                val stepX = chartWidth / (numPoints - 1)
                                val points = mutableListOf<Offset>()

                                for (i in 0 until numPoints) {
                                    val mins = stats.dailyMinutes.getOrElse(i) { 0 }
                                    val fraction = (mins.toFloat() / maxTotalMinutes).coerceIn(0f, 1f)
                                    val x = i * stepX
                                    val y = yBottom - (fraction * usableHeight)
                                    points.add(Offset(x, y))
                                }

                                // 3. Build Smooth Cubic Path
                                val strokePath = Path().apply {
                                    if (points.isNotEmpty()) {
                                        moveTo(points[0].x, points[0].y)
                                        for (i in 0 until points.size - 1) {
                                            val p0 = points[i]
                                            val p1 = points[i + 1]
                                            val controlX1 = p0.x + (p1.x - p0.x) / 2f
                                            val controlY1 = p0.y
                                            val controlX2 = p0.x + (p1.x - p0.x) / 2f
                                            val controlY2 = p1.y
                                            cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                                        }
                                    }
                                }

                                // 4. Fill Gradient Under Curve
                                val fillPath = Path().apply {
                                    addPath(strokePath)
                                    lineTo(points.last().x, yBottom)
                                    lineTo(points.first().x, yBottom)
                                    close()
                                }

                                drawPath(
                                    path = fillPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            primaryColor.copy(alpha = 0.28f),
                                            primaryColor.copy(alpha = 0.02f)
                                        ),
                                        startY = yTop,
                                        endY = yBottom
                                    )
                                )

                                // 5. Draw Curve Stroke
                                drawPath(
                                    path = strokePath,
                                    color = primaryColor,
                                    style = Stroke(
                                        width = 2.5.dp.toPx(),
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )

                                // 6. Draw Nodes on Points
                                for (i in points.indices) {
                                    val pt = points[i]
                                    val mins = stats.dailyMinutes.getOrElse(i) { 0 }
                                    val isToday = i == todayDayIndex

                                    if (mins > 0 || isToday) {
                                        // Outer ring
                                        drawCircle(
                                            color = primaryColor,
                                            radius = if (isToday) 5.5.dp.toPx() else 4.dp.toPx(),
                                            center = pt
                                        )
                                        // Inner center
                                        drawCircle(
                                            color = if (isToday) Color.White else surfaceColor,
                                            radius = if (isToday) 2.5.dp.toPx() else 2.dp.toPx(),
                                            center = pt
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // X-Axis Day Labels Row (Aligned with the 7 points)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                WEEK_DAY_LABELS.forEachIndexed { index, day ->
                                    val isToday = index == todayDayIndex
                                    val mins = stats.dailyMinutes.getOrElse(index) { 0 }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.width(32.dp)
                                    ) {
                                        Text(
                                            text = day,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 11.5.sp
                                            ),
                                            color = if (isToday) primaryColor
                                            else if (mins > 0) MaterialTheme.colorScheme.onSurface
                                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            textAlign = TextAlign.Center
                                        )

                                        if (isToday) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(primaryColor)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}
