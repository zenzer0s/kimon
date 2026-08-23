@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui.analyze.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenzeros.kimon.R
import com.zenzeros.kimon.ui.theme.LocalAppFonts
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

internal fun horizontalSegmentedShape(
    index: Int,
    count: Int,
    outerCornerRadius: Dp = 14.dp,
    innerCornerRadius: Dp = 4.dp
): Shape {
    return when {
        count <= 1 -> RoundedCornerShape(outerCornerRadius)
        index == 0 -> RoundedCornerShape(
            topStart = outerCornerRadius,
            bottomStart = outerCornerRadius,
            topEnd = innerCornerRadius,
            bottomEnd = innerCornerRadius
        )
        index == count - 1 -> RoundedCornerShape(
            topStart = innerCornerRadius,
            bottomStart = innerCornerRadius,
            topEnd = outerCornerRadius,
            bottomEnd = outerCornerRadius
        )
        else -> RoundedCornerShape(innerCornerRadius)
    }
}

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
                // Header Row: [ (🕒) Today's Focus ] ... [ AUG 23 ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                                painter = painterResource(R.drawable.ic_focus),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Text(
                            text = "Today's Focus",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_streak),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }

                    Text(
                        text = "Streaks",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

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
        // 1. Header Row: [ (✨) Lifetime Focus ] ... [ OVERVIEW ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                        painter = painterResource(R.drawable.ic_sparkles),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Text(
                    text = "Lifetime Focus",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

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

    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val currentFormattedMonth = monthYearFormat.format(calendarMonth.time)

    val todayCalendar = remember { Calendar.getInstance() }
    val todayDayOfMonth = todayCalendar.get(Calendar.DAY_OF_MONTH)
    val isCurrentMonth = todayCalendar.get(Calendar.MONTH) == calendarMonth.get(Calendar.MONTH) &&
            todayCalendar.get(Calendar.YEAR) == calendarMonth.get(Calendar.YEAR)

    Column(
        modifier = Modifier.padding(11.dp)
    ) {
        // 1. Header Row: [ (📅) Activity Log ]
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

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Month Selector Navigation Row: [ < ] [ August 2026 ] [ > ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable {
                        val newCal = calendarMonth.clone() as Calendar
                        newCal.add(Calendar.MONTH, -1)
                        calendarMonth = newCal
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_left),
                    contentDescription = "Previous Month",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Month-Year Pill Badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                )
            ) {
                Text(
                    text = currentFormattedMonth,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable {
                        val newCal = calendarMonth.clone() as Calendar
                        newCal.add(Calendar.MONTH, 1)
                        calendarMonth = newCal
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = "Next Month",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Weekday Labels: [ SUN  MON  TUE  WED  THU  FRI  SAT ]
        val weekDays = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            weekDays.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.5.sp,
                        letterSpacing = 0.3.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4. Calendar Days Grid (7 columns)
        val cal = calendarMonth.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0 for Sunday
        val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val totalGridSlots = ((firstDayOfWeek + maxDaysInMonth + 6) / 7) * 7

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            for (row in 0 until totalGridSlots / 7) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    for (col in 0 until 7) {
                        val slotIndex = row * 7 + col
                        val dayNumber = slotIndex - firstDayOfWeek + 1

                        if (dayNumber in 1..maxDaysInMonth) {
                            val isToday = isCurrentMonth && dayNumber == todayDayOfMonth
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(28.dp)
                                    .padding(horizontal = 2.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .then(
                                        if (isToday) {
                                            Modifier
                                                .background(MaterialTheme.colorScheme.primaryContainer)
                                                .border(
                                                    width = 1.5.dp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                        } else {
                                            Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.35f))
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayNumber.toString(),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp
                                    ),
                                    color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            // Blank slot
                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(28.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Bottom 3 Metric Tiles: Horizontal Expressive Segmented Group
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // Days Focused
            CompactSummaryTile(
                modifier = Modifier.weight(1f),
                shape = horizontalSegmentedShape(index = 0, count = 3, outerCornerRadius = 10.dp),
                label = "Days Focused",
                value = "0 of 31",
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

@Composable
private fun CompactSummaryTile(
    label: String,
    value: String,
    valueColor: Color,
    cardBg: Color,
    cardBorder: Color,
    shape: Shape,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = shape,
        color = cardBg,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = cardBorder
        ),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 9.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    fontFamily = LocalAppFonts.current.topBarTitle
                ),
                color = valueColor
            )
        }
    }
}

@Composable
internal fun MetricTileCard(
    icon: Int,
    iconTint: Color,
    iconBg: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    cardBg: Color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.45f),
    cardBorder: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f),
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    shape: Shape = RoundedCornerShape(12.dp),
    minLabelLines: Int = 1
) {
    Surface(
        shape = shape,
        color = cardBg,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = cardBorder
        ),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp)
        ) {
            // Centered Circular Icon Badge
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = iconTint
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Label Text (Uniform baseline across 1 or 2 lines)
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    lineHeight = 12.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                minLines = minLabelLines,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Metric Value
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    fontFamily = LocalAppFonts.current.topBarTitle
                ),
                color = valueColor,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
