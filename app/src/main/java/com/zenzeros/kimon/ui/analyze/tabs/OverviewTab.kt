@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui.analyze.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalToggleButton
import androidx.compose.material3.FilledTonalToggleButtonDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenzeros.kimon.R
import com.zenzeros.kimon.domain.model.OverviewStats
import com.zenzeros.kimon.ui.analyze.AnalyzeViewModel
import com.zenzeros.kimon.ui.analyze.components.AnalyzeCardHeader
import com.zenzeros.kimon.ui.analyze.components.CompactSummaryTile
import com.zenzeros.kimon.ui.analyze.components.MetricTileCard
import com.zenzeros.kimon.ui.analyze.components.horizontalSegmentedShape
import com.zenzeros.kimon.ui.theme.CustomColors
import androidx.compose.runtime.Immutable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val ACTIVITY_LOG_WEEK_DAYS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
private val ActivityDayShape = RoundedCornerShape(4.5.dp)

@Immutable
private data class ActivityLogCell(
    val dayNumber: Int,
    val isToday: Boolean,
    val isFocused: Boolean
)

@Composable
fun OverviewTab(
    stats: OverviewStats = OverviewStats(),
    calendarMonth: Calendar = remember { Calendar.getInstance() },
    onPreviousMonth: () -> Unit = {},
    onNextMonth: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val currentDate = remember {
        SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date()).uppercase()
    }

    val totalSections = 4
    val todayFocusShapes = ListItemDefaults.segmentedShapes(index = 0, count = totalSections)
    val streaksShapes = ListItemDefaults.segmentedShapes(index = 1, count = totalSections)
    val activityLogShapes = ListItemDefaults.segmentedShapes(index = 2, count = totalSections)
    val lifetimeFocusShapes = ListItemDefaults.segmentedShapes(index = 3, count = totalSections)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // --- Section 1: Today's Focus Card ---
        Surface(
            shape = todayFocusShapes.shape,
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
                    title = stringResource(R.string.title_todays_focus),
                    trailingContent = {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                            )
                        ) {
                            Text(
                                text = currentDate,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Horizontal Expressive Segmented Row: [ Focus Time ] [ Sessions ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    MetricTileCard(
                        modifier = Modifier.weight(1f),
                        shape = horizontalSegmentedShape(index = 0, count = 2),
                        icon = R.drawable.ic_focus,
                        iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                        iconBg = MaterialTheme.colorScheme.primaryContainer,
                        valueColor = MaterialTheme.colorScheme.primary,
                        label = stringResource(R.string.label_focus_time),
                        value = AnalyzeViewModel.formatDuration(stats.todayFocusSeconds)
                    )

                    MetricTileCard(
                        modifier = Modifier.weight(1f),
                        shape = horizontalSegmentedShape(index = 1, count = 2),
                        icon = R.drawable.ic_check,
                        iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                        iconBg = MaterialTheme.colorScheme.secondaryContainer,
                        valueColor = MaterialTheme.colorScheme.secondary,
                        label = stringResource(R.string.label_sessions),
                        value = stats.todaySessionsCount.toString()
                    )
                }
            }
        }

        // --- Section 2: Streaks Card ---
        Surface(
            shape = streaksShapes.shape,
            color = CustomColors.cardContainerColor,
            border = CustomColors.cardBorder,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(11.dp)
            ) {
                AnalyzeCardHeader(
                    icon = R.drawable.ic_streak,
                    title = stringResource(R.string.title_streaks),
                    iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                    iconBg = MaterialTheme.colorScheme.tertiaryContainer
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Horizontal Expressive Segmented Row: [ Current Streak ] [ Best Streak ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    MetricTileCard(
                        modifier = Modifier.weight(1f),
                        shape = horizontalSegmentedShape(index = 0, count = 2),
                        icon = R.drawable.ic_streak,
                        iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                        iconBg = MaterialTheme.colorScheme.tertiaryContainer,
                        valueColor = MaterialTheme.colorScheme.tertiary,
                        label = stringResource(R.string.label_current_streak),
                        value = "${stats.currentStreak}d"
                    )

                    MetricTileCard(
                        modifier = Modifier.weight(1f),
                        shape = horizontalSegmentedShape(index = 1, count = 2),
                        icon = R.drawable.ic_star,
                        iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                        iconBg = MaterialTheme.colorScheme.secondaryContainer,
                        valueColor = MaterialTheme.colorScheme.secondary,
                        label = stringResource(R.string.label_best_streak),
                        value = "${stats.bestStreak}d"
                    )
                }
            }
        }

        // --- Section 3: Activity Log Card ---
        Surface(
            shape = activityLogShapes.shape,
            color = CustomColors.cardContainerColor,
            border = CustomColors.cardBorder,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            ActivityLogContent(
                stats = stats,
                calendarMonth = calendarMonth,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth
            )
        }

        // --- Section 4: Lifetime Focus Card ---
        Surface(
            shape = lifetimeFocusShapes.shape,
            color = CustomColors.cardContainerColor,
            border = CustomColors.cardBorder,
            tonalElevation = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            LifetimeFocusContent(stats = stats)
        }
    }
}

@Composable
private fun LifetimeFocusContent(stats: OverviewStats) {
    Column(
        modifier = Modifier.padding(11.dp)
    ) {
        AnalyzeCardHeader(
            icon = R.drawable.ic_sparkles,
            title = stringResource(R.string.title_lifetime_focus),
            trailingContent = {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.badge_overview),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 3 Metric Tiles: Horizontal Expressive Segmented Group
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
                shape = horizontalSegmentedShape(index = 0, count = 3),
                icon = R.drawable.ic_focus,
                iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                iconBg = MaterialTheme.colorScheme.primaryContainer,
                valueColor = MaterialTheme.colorScheme.primary,
                label = stringResource(R.string.label_total_focus_time),
                value = AnalyzeViewModel.formatDuration(stats.lifetimeFocusSeconds),
                minLabelLines = 2
            )

            MetricTileCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = horizontalSegmentedShape(index = 1, count = 3),
                icon = R.drawable.ic_list,
                iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                iconBg = MaterialTheme.colorScheme.secondaryContainer,
                valueColor = MaterialTheme.colorScheme.secondary,
                label = stringResource(R.string.label_total_sessions),
                value = stats.lifetimeSessionsCount.toString(),
                minLabelLines = 2
            )

            MetricTileCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = horizontalSegmentedShape(index = 2, count = 3),
                icon = R.drawable.ic_calendar,
                iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                iconBg = MaterialTheme.colorScheme.tertiaryContainer,
                valueColor = MaterialTheme.colorScheme.tertiary,
                label = stringResource(R.string.label_focus_days),
                value = "${stats.lifetimeFocusDays}d",
                minLabelLines = 2
            )
        }
    }
}

@Composable
private fun ActivityLogContent(
    stats: OverviewStats,
    calendarMonth: Calendar,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val currentFormattedMonth = remember(calendarMonth.timeInMillis) {
        SimpleDateFormat("MMM", Locale.getDefault()).format(calendarMonth.time).uppercase()
    }

    val todayCalendar = remember { Calendar.getInstance() }
    val todayDayOfMonth = todayCalendar.get(Calendar.DAY_OF_MONTH)
    val isCurrentMonth = todayCalendar.get(Calendar.MONTH) == calendarMonth.get(Calendar.MONTH) &&
            todayCalendar.get(Calendar.YEAR) == calendarMonth.get(Calendar.YEAR)

    Column(
        modifier = Modifier.padding(11.dp)
    ) {
        // Single Combined Header Row: [ (📅) Activity Log ] ... [ [ AUG ] [ < > ] ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Title with Icon Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_calendar),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Text(
                    text = stringResource(R.string.title_activity_log),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Right: [ Month Pill (No icon) ] [ < > Connected ButtonGroup ]
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Month Pill (without icon, compact)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = CustomColors.innerCardContainerColor,
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = CustomColors.innerCardBorderColor
                    )
                ) {
                    Text(
                        text = currentFormattedMonth,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }

                // < > Connected ButtonGroup
                ButtonGroup(
                    overflowIndicator = { menuState ->
                        ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
                    },
                    modifier = Modifier.wrapContentWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                ) {
                    customItem(
                        buttonGroupContent = {
                            FilledTonalToggleButton(
                                checked = false,
                                onCheckedChange = { onPreviousMonth() },
                                shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
                                colors = FilledTonalToggleButtonDefaults.filledTonalToggleButtonColors(
                                    containerColor = CustomColors.innerCardContainerColor,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = CustomColors.innerCardBorderColor
                                ),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.size(width = 34.dp, height = 30.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_chevron_left),
                                    contentDescription = "Previous Month",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        },
                        menuContent = {}
                    )

                    customItem(
                        buttonGroupContent = {
                            FilledTonalToggleButton(
                                checked = false,
                                onCheckedChange = { onNextMonth() },
                                shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                                colors = FilledTonalToggleButtonDefaults.filledTonalToggleButtonColors(
                                    containerColor = CustomColors.innerCardContainerColor,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = CustomColors.innerCardBorderColor
                                ),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.size(width = 34.dp, height = 30.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_chevron_right),
                                    contentDescription = "Next Month",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        },
                        menuContent = {}
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Precompute the entire 7x6 month grid once outside composition rendering
        val gridRows = remember(calendarMonth.timeInMillis, stats.monthActiveDays, isCurrentMonth, todayDayOfMonth) {
            val cal = calendarMonth.clone() as Calendar
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val firstDayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
            val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

            (0 until 7).map { row ->
                (0 until 6).map { col ->
                    val slotIndex = col * 7 + row
                    val dayNumber = slotIndex - firstDayOfWeek + 1
                    if (dayNumber in 1..maxDaysInMonth) {
                        ActivityLogCell(
                            dayNumber = dayNumber,
                            isToday = isCurrentMonth && dayNumber == todayDayOfMonth,
                            isFocused = stats.monthActiveDays.contains(dayNumber)
                        )
                    } else {
                        null
                    }
                }
            }
        }

        val maxDaysInMonth = remember(calendarMonth.timeInMillis) {
            val cal = calendarMonth.clone() as Calendar
            cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        }

        val primaryColor = MaterialTheme.colorScheme.primary
        val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
        val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
        val onPrimaryContainerColor = MaterialTheme.colorScheme.onPrimaryContainer
        val surfaceContainerColor = CustomColors.innerCardContainerColor
        val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

        val todayBorder = remember(primaryColor) { androidx.compose.foundation.BorderStroke(1.2.dp, primaryColor) }
        val focusedBorder = remember(primaryColor) { androidx.compose.foundation.BorderStroke(1.dp, primaryColor) }

        // Vertical Weekday Calendar Heatmap (7 Rows: Mon-Sun x 6 Columns)
        Column(
            verticalArrangement = Arrangement.spacedBy(2.5.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 26.dp)
        ) {
            gridRows.forEachIndexed { row, cols ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Left Weekday Label
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = ACTIVITY_LOG_WEEK_DAYS[row],
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 9.sp
                            ),
                            color = onSurfaceVariantColor.copy(alpha = 0.7f)
                        )
                    }

                    // 6 Columns of Day Cells (1:1 Compact Squares)
                    cols.forEach { cell ->
                        if (cell != null) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(ActivityDayShape)
                                    .then(
                                        when {
                                            cell.isToday -> Modifier
                                                .background(primaryContainerColor)
                                                .border(todayBorder, ActivityDayShape)
                                            cell.isFocused -> Modifier
                                                .background(primaryColor)
                                                .border(focusedBorder, ActivityDayShape)
                                            else -> Modifier.background(surfaceContainerColor)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cell.dayNumber.toString(),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (cell.isToday || cell.isFocused) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 9.sp
                                    ),
                                    color = when {
                                        cell.isFocused -> onPrimaryColor
                                        cell.isToday -> onPrimaryContainerColor
                                        else -> onSurfaceVariantColor
                                    }
                                )
                            }
                        } else {
                            // Blank slot maintaining perfect square aspect ratio
                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bottom 3 Metric Tiles: Horizontal Expressive Segmented Group
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Days Focused
            CompactSummaryTile(
                modifier = Modifier.weight(1f),
                shape = horizontalSegmentedShape(index = 0, count = 3, outerCornerRadius = 10.dp),
                label = stringResource(R.string.label_days_focused),
                value = "${stats.monthDaysFocused} of $maxDaysInMonth",
                valueColor = MaterialTheme.colorScheme.primary,
                cardBg = CustomColors.innerCardContainerColor,
                cardBorder = CustomColors.innerCardBorderColor
            )

            // Avg Focus Day
            CompactSummaryTile(
                modifier = Modifier.weight(1f),
                shape = horizontalSegmentedShape(index = 1, count = 3, outerCornerRadius = 10.dp),
                label = stringResource(R.string.label_avg_focus_day),
                value = AnalyzeViewModel.formatDuration(stats.monthAvgDailySeconds),
                valueColor = MaterialTheme.colorScheme.tertiary,
                cardBg = CustomColors.innerCardContainerColor,
                cardBorder = CustomColors.innerCardBorderColor
            )

            // Total Focus
            CompactSummaryTile(
                modifier = Modifier.weight(1f),
                shape = horizontalSegmentedShape(index = 2, count = 3, outerCornerRadius = 10.dp),
                label = stringResource(R.string.label_total_focus),
                value = AnalyzeViewModel.formatDuration(stats.monthTotalFocusSeconds),
                valueColor = MaterialTheme.colorScheme.secondary,
                cardBg = CustomColors.innerCardContainerColor,
                cardBorder = CustomColors.innerCardBorderColor
            )
        }
    }
}
