@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui.analyze.tabs

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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenzeros.kimon.R
import com.zenzeros.kimon.ui.analyze.components.AnalyzeCardHeader
import com.zenzeros.kimon.ui.analyze.components.AnalyzeNavigationHeader
import com.zenzeros.kimon.ui.analyze.components.MetricTileCard
import com.zenzeros.kimon.ui.analyze.components.horizontalSegmentedShape
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Locale

@Composable
fun YearTab(
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }

    val totalSectionsGroup = 2
    val yearlyFocusShapes = ListItemDefaults.segmentedShapes(index = 0, count = totalSectionsGroup)
    val heatMapShapes = ListItemDefaults.segmentedShapes(index = 1, count = totalSectionsGroup)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // 1. Navigation Header: [ Left: Compact Year Pill ] ... [ Right: ButtonGroup with < and > ]
        AnalyzeNavigationHeader(
            onPreviousClick = { selectedYear -= 1 },
            onNextClick = { selectedYear += 1 }
        ) {
            Text(
                text = selectedYear.toString(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
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

        // 2. Material 3 Expressive Segmented 2-Card Group (Yearly Focus & Heat Map)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(3.5.dp)
        ) {
            // --- Card 1: Yearly Focus ---
            Surface(
                shape = yearlyFocusShapes.shape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(11.dp)
                ) {
                    AnalyzeCardHeader(
                        icon = R.drawable.ic_trending_up,
                        title = "Yearly Focus"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 1: Hero Full-Width Total Focus Time
                    MetricTileCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        icon = R.drawable.ic_focus,
                        iconTint = MaterialTheme.colorScheme.primary,
                        iconBg = MaterialTheme.colorScheme.primaryContainer,
                        cardBg = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                        cardBorder = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        valueColor = MaterialTheme.colorScheme.primary,
                        label = "Total Focus Time",
                        value = "10h 3m"
                    )

                    Spacer(modifier = Modifier.height(3.5.dp))

                    // Row 2: [ Sessions ] [ Avg Session ]
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
                            icon = R.drawable.ic_check,
                            iconTint = MaterialTheme.colorScheme.secondary,
                            iconBg = MaterialTheme.colorScheme.secondaryContainer,
                            cardBg = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f),
                            cardBorder = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                            valueColor = MaterialTheme.colorScheme.secondary,
                            label = "Sessions",
                            value = "4"
                        )

                        MetricTileCard(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            shape = horizontalSegmentedShape(index = 1, count = 2),
                            icon = R.drawable.ic_trophy,
                            iconTint = MaterialTheme.colorScheme.tertiary,
                            iconBg = MaterialTheme.colorScheme.tertiaryContainer,
                            cardBg = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f),
                            cardBorder = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                            valueColor = MaterialTheme.colorScheme.tertiary,
                            label = "Avg Session",
                            value = "2h 31m"
                        )
                    }

                    Spacer(modifier = Modifier.height(3.5.dp))

                    // Row 3: [ Focus Days ] [ Best Streak ]
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
                            icon = R.drawable.ic_calendar,
                            iconTint = MaterialTheme.colorScheme.primary,
                            iconBg = MaterialTheme.colorScheme.primaryContainer,
                            cardBg = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                            cardBorder = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            valueColor = MaterialTheme.colorScheme.primary,
                            label = "Focus Days",
                            value = "2d"
                        )

                        MetricTileCard(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            shape = horizontalSegmentedShape(index = 1, count = 2),
                            icon = R.drawable.ic_streak,
                            iconTint = MaterialTheme.colorScheme.tertiary,
                            iconBg = MaterialTheme.colorScheme.tertiaryContainer,
                            cardBg = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f),
                            cardBorder = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                            valueColor = MaterialTheme.colorScheme.tertiary,
                            label = "Best Streak",
                            value = "1d"
                        )
                    }

                    Spacer(modifier = Modifier.height(3.5.dp))

                    // Row 4: [ Best Day ] [ Best Week ] [ Best Month ]
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
                            icon = R.drawable.ic_bolt,
                            iconTint = MaterialTheme.colorScheme.primary,
                            iconBg = MaterialTheme.colorScheme.primaryContainer,
                            cardBg = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                            cardBorder = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            valueColor = MaterialTheme.colorScheme.primary,
                            label = "Best Day",
                            value = "10h"
                        )

                        MetricTileCard(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            shape = horizontalSegmentedShape(index = 1, count = 3),
                            icon = R.drawable.ic_target,
                            iconTint = MaterialTheme.colorScheme.secondary,
                            iconBg = MaterialTheme.colorScheme.secondaryContainer,
                            cardBg = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f),
                            cardBorder = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                            valueColor = MaterialTheme.colorScheme.secondary,
                            label = "Best Week",
                            value = "10h 3m"
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
                            label = "Best Month",
                            value = "10h 3m"
                        )
                    }
                }
            }

            // --- Card 2: Heat Map ---
            Surface(
                shape = heatMapShapes.shape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                YearlyHeatMapContent(year = selectedYear)
            }
        }
    }
}

@Composable
private fun YearlyHeatMapContent(year: Int) {
    val monthPairs = listOf(
        Pair(0, 1),   // Jan, Feb
        Pair(2, 3),   // Mar, Apr
        Pair(4, 5),   // May, Jun
        Pair(6, 7),   // Jul, Aug
        Pair(8, 9),   // Sep, Oct
        Pair(10, 11)  // Nov, Dec
    )

    val pagerState = rememberPagerState(
        initialPage = 2, // Default to May-June
        pageCount = { monthPairs.size }
    )

    val monthNames = remember { DateFormatSymbols(Locale.getDefault()).months }
    val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Column(
        modifier = Modifier.padding(11.dp)
    ) {
        AnalyzeCardHeader(
            icon = R.drawable.ic_streak,
            title = "Heat Map"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Month-Pair Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val (firstMonthIndex, secondMonthIndex) = monthPairs[page]

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Weekday Labels Column
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 28.dp)
                ) {
                    weekDays.forEach { day ->
                        Box(
                            modifier = Modifier
                                .height(22.dp)
                                .wrapContentWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 10.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // First Month Grid
                MonthHeatMapColumn(
                    year = year,
                    monthIndex = firstMonthIndex,
                    monthName = monthNames[firstMonthIndex],
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                )

                // Second Month Grid
                MonthHeatMapColumn(
                    year = year,
                    monthIndex = secondMonthIndex,
                    monthName = monthNames[secondMonthIndex],
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Pager Dots Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(monthPairs.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(5.dp)
                        .width(if (isSelected) 16.dp else 5.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun MonthHeatMapColumn(
    year: Int,
    monthIndex: Int,
    monthName: String,
    modifier: Modifier = Modifier
) {
    val cal = remember(year, monthIndex) {
        val c = Calendar.getInstance()
        c.set(Calendar.YEAR, year)
        c.set(Calendar.MONTH, monthIndex)
        c.set(Calendar.DAY_OF_MONTH, 1)
        c
    }

    val maxDays = remember(cal) { cal.getActualMaximum(Calendar.DAY_OF_MONTH) }
    val firstDayOfWeek = remember(cal) {
        (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
    }

    val totalSlots = ((firstDayOfWeek + maxDays + 6) / 7) * 7
    val totalCols = totalSlots / 7

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Month Title
        Text(
            text = monthName,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.5.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 7 Rows (Mon to Sun) x N Columns Grid
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (row in 0 until 7) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (col in 0 until totalCols) {
                        val slotIndex = col * 7 + row
                        val dayNumber = slotIndex - firstDayOfWeek + 1

                        if (dayNumber in 1..maxDays) {
                            val isFocusedDay = (monthIndex == 4 && (dayNumber == 2 || dayNumber == 6))
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .then(
                                        if (isFocusedDay) {
                                            Modifier
                                                .background(MaterialTheme.colorScheme.primary)
                                                .border(
                                                    width = 1.dp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    shape = RoundedCornerShape(6.dp)
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
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isFocusedDay) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 10.sp
                                    ),
                                    color = if (isFocusedDay) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }
        }
    }
}
