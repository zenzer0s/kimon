@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui.analyze.tabs

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
        SimpleDateFormat("MM / dd / yyyy", Locale.getDefault()).format(selectedCalendar.time)
    }

    val totalSectionsGroup = 3
    val todayFocusShapesGroup = ListItemDefaults.segmentedShapes(index = 0, count = totalSectionsGroup)
    val focusByTagShapesGroup = ListItemDefaults.segmentedShapes(index = 1, count = totalSectionsGroup)
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

        // 2. Material 3 Expressive Segmented Group (Today's Focus, Focus Time by Tag, Daily Timeline)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(3.5.dp)
        ) {
            // --- Card 1: Today's Focus ---
            Surface(
                shape = todayFocusShapesGroup.shape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
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
                            label = stringResource(R.string.label_total_focus),
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
                            label = stringResource(R.string.label_total_sessions),
                            value = stats.totalSessions.toString()
                        )
                    }
                }
            }

            // --- Card 2: Focus Time by Tag ---
            Surface(
                shape = focusByTagShapesGroup.shape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(11.dp)
                ) {
                    AnalyzeCardHeader(
                        icon = R.drawable.ic_tag,
                        title = stringResource(R.string.title_focus_time_by_tag)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (stats.tagDistributions.isEmpty()) {
                        AnalyzeEmptyState(
                            icon = R.drawable.ic_tag,
                            message = stringResource(R.string.empty_no_focus_sessions_day),
                            actionText = stringResource(R.string.action_start_focus),
                            onActionClick = onNavigateToFocus
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            stats.tagDistributions.forEach { item ->
                                val tagColor = remember(item.tag?.colorHex) {
                                    try {
                                        Color(android.graphics.Color.parseColor(item.tag?.colorHex ?: "#7C4DFF"))
                                    } catch (e: Exception) {
                                        Color(0xFF7C4DFF)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(tagColor)
                                            )
                                            Text(
                                                text = item.tag?.name ?: "Untagged",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 13.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        Text(
                                            text = AnalyzeViewModel.formatDuration(item.totalDurationSeconds),
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.5.sp
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

            // --- Card 3: Daily Timeline ---
            Surface(
                shape = dailyTimelineShapesGroup.shape,
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
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            stats.timelineSessions.forEach { item ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = item.tag?.name ?: item.session.sessionType.replace("_", " "),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 13.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
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
                                            color = MaterialTheme.colorScheme.secondary
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
