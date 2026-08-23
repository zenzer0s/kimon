@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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

    val weekEndCal = remember(selectedWeekStart.timeInMillis) {
        val end = selectedWeekStart.clone() as Calendar
        end.add(Calendar.DAY_OF_YEAR, 6)
        end
    }

    val weekRangeText = remember(selectedWeekStart.timeInMillis, weekEndCal.timeInMillis) {
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        "${dateFormat.format(selectedWeekStart.time)} - ${dateFormat.format(weekEndCal.time)}".uppercase()
    }

    val totalSectionsGroup = 3
    val weeklyFocusShapes = ListItemDefaults.segmentedShapes(index = 0, count = totalSectionsGroup)
    val focusDistributionShapes = ListItemDefaults.segmentedShapes(index = 1, count = totalSectionsGroup)
    val focusTrendsShapes = ListItemDefaults.segmentedShapes(index = 2, count = totalSectionsGroup)

    Column(
        modifier = modifier
            .fillMaxSize()
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
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.4.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1
            )

            Icon(
                painter = painterResource(R.drawable.ic_calendar),
                contentDescription = "Calendar",
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Material 3 Expressive Segmented 3-Card Group
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(3.5.dp)
        ) {
            // --- Card 1: Weekly Focus ---
            Surface(
                shape = weeklyFocusShapes.shape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
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
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        MetricTileCard(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            shape = horizontalSegmentedShape(index = 0, count = 2),
                            icon = R.drawable.ic_focus,
                            iconTint = MaterialTheme.colorScheme.primary,
                            iconBg = MaterialTheme.colorScheme.primaryContainer,
                            cardBg = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                            cardBorder = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
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
                            iconTint = MaterialTheme.colorScheme.secondary,
                            iconBg = MaterialTheme.colorScheme.secondaryContainer,
                            cardBg = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f),
                            cardBorder = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
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
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
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

            // --- Card 3: Focus Trends ---
            Surface(
                shape = focusTrendsShapes.shape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
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

                    Spacer(modifier = Modifier.height(16.dp))

                    val todayDayOfWeekIndex = remember {
                        val cal = Calendar.getInstance()
                        (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
                    }

                    val timelineColor = MaterialTheme.colorScheme.primary
                    val surfaceContainerHigh = MaterialTheme.colorScheme.surfaceContainerHigh

                    // 1. Interactive Nodes Timeline Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(horizontal = 14.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val step = size.width / (WEEK_DAY_LABELS.size - 1)
                            val centerY = size.height / 2

                            // Base track line
                            drawLine(
                                color = timelineColor.copy(alpha = 0.5f),
                                start = androidx.compose.ui.geometry.Offset(0f, centerY),
                                end = androidx.compose.ui.geometry.Offset(size.width, centerY),
                                strokeWidth = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            )

                            // 7 Day Nodes
                            for (i in WEEK_DAY_LABELS.indices) {
                                val x = i * step
                                val isToday = i == todayDayOfWeekIndex
                                val hasFocus = stats.dailyMinutes.getOrElse(i) { 0 } > 0

                                // Outer circle
                                drawCircle(
                                    color = if (hasFocus || isToday) timelineColor else timelineColor.copy(alpha = 0.5f),
                                    radius = if (isToday) 6.dp.toPx() else 4.5.dp.toPx(),
                                    center = androidx.compose.ui.geometry.Offset(x, centerY)
                                )

                                // Inner core
                                drawCircle(
                                    color = if (hasFocus) timelineColor else surfaceContainerHigh,
                                    radius = if (isToday) 2.5.dp.toPx() else 1.8.dp.toPx(),
                                    center = androidx.compose.ui.geometry.Offset(x, centerY)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 2. Day Labels Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        WEEK_DAY_LABELS.forEachIndexed { index, day ->
                            val isToday = index == todayDayOfWeekIndex
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(36.dp)
                            ) {
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.5.sp
                                    ),
                                    color = if (isToday) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )

                                if (isToday) {
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}
