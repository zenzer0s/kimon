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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenzeros.kimon.R
import com.zenzeros.kimon.ui.analyze.components.AnalyzeCardHeader
import com.zenzeros.kimon.ui.analyze.components.AnalyzeNavigationHeader
import com.zenzeros.kimon.ui.analyze.components.CompactSummaryTile
import com.zenzeros.kimon.ui.analyze.components.MetricTileCard
import com.zenzeros.kimon.ui.analyze.components.horizontalSegmentedShape
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val ACTIVITY_LOG_WEEK_DAYS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

@Composable
fun OverviewTab(
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
        verticalArrangement = Arrangement.spacedBy(3.5.dp)
    ) {
        // --- Section 1: Today's Focus Card ---
        Surface(
            shape = todayFocusShapes.shape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(11.dp)
            ) {
                AnalyzeCardHeader(
                    icon = R.drawable.ic_focus,
                    title = "Today's Focus",
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
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    MetricTileCard(
                        modifier = Modifier.weight(1f),
                        shape = horizontalSegmentedShape(index = 0, count = 2),
                        icon = R.drawable.ic_focus,
                        iconTint = MaterialTheme.colorScheme.primary,
                        iconBg = MaterialTheme.colorScheme.primaryContainer,
                        cardBg = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                        cardBorder = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        valueColor = MaterialTheme.colorScheme.primary,
                        label = "Focus Time",
                        value = "0m"
                    )

                    MetricTileCard(
                        modifier = Modifier.weight(1f),
                        shape = horizontalSegmentedShape(index = 1, count = 2),
                        icon = R.drawable.ic_check,
                        iconTint = MaterialTheme.colorScheme.secondary,
                        iconBg = MaterialTheme.colorScheme.secondaryContainer,
                        cardBg = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f),
                        cardBorder = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                        valueColor = MaterialTheme.colorScheme.secondary,
                        label = "Sessions",
                        value = "0"
                    )
                }
            }
        }

        // --- Section 2: Streaks Card ---
        Surface(
            shape = streaksShapes.shape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(11.dp)
            ) {
                AnalyzeCardHeader(
                    icon = R.drawable.ic_streak,
                    title = "Streaks",
                    iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                    iconBg = MaterialTheme.colorScheme.tertiaryContainer
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Horizontal Expressive Segmented Row: [ Current Streak ] [ Best Streak ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    MetricTileCard(
                        modifier = Modifier.weight(1f),
                        shape = horizontalSegmentedShape(index = 0, count = 2),
                        icon = R.drawable.ic_streak,
                        iconTint = MaterialTheme.colorScheme.tertiary,
                        iconBg = MaterialTheme.colorScheme.tertiaryContainer,
                        cardBg = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f),
                        cardBorder = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                        valueColor = MaterialTheme.colorScheme.tertiary,
                        label = "Current Streak",
                        value = "0d"
                    )

                    MetricTileCard(
                        modifier = Modifier.weight(1f),
                        shape = horizontalSegmentedShape(index = 1, count = 2),
                        icon = R.drawable.ic_star,
                        iconTint = MaterialTheme.colorScheme.secondary,
                        iconBg = MaterialTheme.colorScheme.secondaryContainer,
                        cardBg = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f),
                        cardBorder = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                        valueColor = MaterialTheme.colorScheme.secondary,
                        label = "Best Streak",
                        value = "1d"
                    )
                }
            }
        }

        // --- Section 3: Activity Log Card ---
        Surface(
            shape = activityLogShapes.shape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            ActivityLogContent()
        }

        // --- Section 4: Lifetime Focus Card ---
        Surface(
            shape = lifetimeFocusShapes.shape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            LifetimeFocusContent()
        }
    }
}

@Composable
private fun LifetimeFocusContent() {
    Column(
        modifier = Modifier.padding(11.dp)
    ) {
        AnalyzeCardHeader(
            icon = R.drawable.ic_sparkles,
            title = "Lifetime Focus",
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
                        text = "OVERVIEW",
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
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            MetricTileCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = horizontalSegmentedShape(index = 0, count = 3),
                icon = R.drawable.ic_focus,
                iconTint = MaterialTheme.colorScheme.primary,
                iconBg = MaterialTheme.colorScheme.primaryContainer,
                cardBg = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                cardBorder = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                valueColor = MaterialTheme.colorScheme.primary,
                label = "Total Focus Time",
                value = "10h 3m",
                minLabelLines = 2
            )

            MetricTileCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = horizontalSegmentedShape(index = 1, count = 3),
                icon = R.drawable.ic_list,
                iconTint = MaterialTheme.colorScheme.secondary,
                iconBg = MaterialTheme.colorScheme.secondaryContainer,
                cardBg = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f),
                cardBorder = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                valueColor = MaterialTheme.colorScheme.secondary,
                label = "Total Sessions",
                value = "4",
                minLabelLines = 2
            )

            MetricTileCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = horizontalSegmentedShape(index = 2, count = 3),
                icon = R.drawable.ic_calendar,
                iconTint = MaterialTheme.colorScheme.tertiary,
                iconBg = MaterialTheme.colorScheme.tertiaryContainer,
                cardBg = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f),
                cardBorder = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                valueColor = MaterialTheme.colorScheme.tertiary,
                label = "Focus Days",
                value = "2d",
                minLabelLines = 2
            )
        }
    }
}

@Composable
private fun ActivityLogContent() {
    var calendarMonth by remember {
        mutableStateOf(Calendar.getInstance())
    }

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
                    text = "Activity Log",
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
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier.height(30.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentFormattedMonth,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
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
                                onCheckedChange = {
                                    val newCal = calendarMonth.clone() as Calendar
                                    newCal.add(Calendar.MONTH, -1)
                                    calendarMonth = newCal
                                },
                                shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
                                colors = FilledTonalToggleButtonDefaults.filledTonalToggleButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
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
                                onCheckedChange = {
                                    val newCal = calendarMonth.clone() as Calendar
                                    newCal.add(Calendar.MONTH, 1)
                                    calendarMonth = newCal
                                },
                                shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                                colors = FilledTonalToggleButtonDefaults.filledTonalToggleButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
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

        // Vertical Weekday Calendar Heatmap (7 Rows: Mon-Sun x 6 Columns)
        val cal = remember(calendarMonth.timeInMillis) {
            val c = calendarMonth.clone() as Calendar
            c.set(Calendar.DAY_OF_MONTH, 1)
            c
        }
        val firstDayOfWeek = remember(cal) {
            (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Monday = 0 ... Sunday = 6
        }
        val maxDaysInMonth = remember(cal) { cal.getActualMaximum(Calendar.DAY_OF_MONTH) }
        val totalCols = 6

        Column(
            verticalArrangement = Arrangement.spacedBy(2.5.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 26.dp)
        ) {
            for (row in 0 until 7) {
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    // 6 Columns of Day Cells (1:1 Compact Squares)
                    for (col in 0 until totalCols) {
                        val slotIndex = col * 7 + row
                        val dayNumber = slotIndex - firstDayOfWeek + 1

                        if (dayNumber in 1..maxDaysInMonth) {
                            val isToday = isCurrentMonth && dayNumber == todayDayOfMonth
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(4.5.dp))
                                    .then(
                                        if (isToday) {
                                            Modifier
                                                .background(MaterialTheme.colorScheme.primaryContainer)
                                                .border(
                                                    width = 1.2.dp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    shape = RoundedCornerShape(4.5.dp)
                                                )
                                        } else {
                                            Modifier.background(
                                                MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.35f)
                                            )
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayNumber.toString(),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 9.sp
                                    ),
                                    color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
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
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // Days Focused
            CompactSummaryTile(
                modifier = Modifier.weight(1f),
                shape = horizontalSegmentedShape(index = 0, count = 3, outerCornerRadius = 10.dp),
                label = "Days Focused",
                value = "0 of $maxDaysInMonth",
                valueColor = MaterialTheme.colorScheme.primary,
                cardBg = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                cardBorder = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )

            // Avg Focus Day
            CompactSummaryTile(
                modifier = Modifier.weight(1f),
                shape = horizontalSegmentedShape(index = 1, count = 3, outerCornerRadius = 10.dp),
                label = "Avg Focus Day",
                value = "0m",
                valueColor = MaterialTheme.colorScheme.tertiary,
                cardBg = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f),
                cardBorder = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
            )

            // Total Focus
            CompactSummaryTile(
                modifier = Modifier.weight(1f),
                shape = horizontalSegmentedShape(index = 2, count = 3, outerCornerRadius = 10.dp),
                label = "Total Focus",
                value = "0m",
                valueColor = MaterialTheme.colorScheme.secondary,
                cardBg = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f),
                cardBorder = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
            )
        }
    }
}
