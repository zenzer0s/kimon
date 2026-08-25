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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenzeros.kimon.R
import com.zenzeros.kimon.domain.model.YearStats
import com.zenzeros.kimon.ui.analyze.AnalyzeViewModel
import com.zenzeros.kimon.ui.analyze.components.AnalyzeCardHeader
import com.zenzeros.kimon.ui.analyze.components.AnalyzeNavigationHeader
import com.zenzeros.kimon.ui.analyze.components.MetricTileCard
import com.zenzeros.kimon.ui.analyze.components.horizontalSegmentedShape
import com.zenzeros.kimon.ui.theme.CustomColors
import androidx.compose.runtime.Immutable
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Locale

private val MONTH_PAIRS = listOf(
    Pair(0, 1),   // Jan, Feb
    Pair(2, 3),   // Mar, Apr
    Pair(4, 5),   // May, Jun
    Pair(6, 7),   // Jul, Aug
    Pair(8, 9),   // Sep, Oct
    Pair(10, 11)  // Nov, Dec
)

private val HEATMAP_WEEK_DAYS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
private val DayCellShape = RoundedCornerShape(5.dp)

@Immutable
private data class MonthGridData(
    val monthIndex: Int,
    val monthName: String,
    val maxDays: Int,
    val firstDayOfWeek: Int,
    val dayIsFocused: BooleanArray
)

@Composable
fun YearTab(
    stats: YearStats = YearStats(),
    selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    onPreviousYear: () -> Unit = {},
    onNextYear: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

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
            onPreviousClick = onPreviousYear,
            onNextClick = onNextYear
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
                color = CustomColors.cardContainerColor,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(11.dp)
                ) {
                    AnalyzeCardHeader(
                        icon = R.drawable.ic_trending_up,
                        title = stringResource(R.string.title_yearly_focus)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 1: Hero Full-Width Total Focus Time
                    MetricTileCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        icon = R.drawable.ic_focus,
                        iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                        iconBg = MaterialTheme.colorScheme.primaryContainer,
                        valueColor = MaterialTheme.colorScheme.primary,
                        label = stringResource(R.string.label_total_focus_time),
                        value = AnalyzeViewModel.formatDuration(stats.totalFocusSeconds)
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
                            iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                            iconBg = MaterialTheme.colorScheme.secondaryContainer,
                            valueColor = MaterialTheme.colorScheme.secondary,
                            label = stringResource(R.string.label_sessions),
                            value = stats.totalSessions.toString()
                        )

                        MetricTileCard(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            shape = horizontalSegmentedShape(index = 1, count = 2),
                            icon = R.drawable.ic_trophy,
                            iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                            iconBg = MaterialTheme.colorScheme.tertiaryContainer,
                            valueColor = MaterialTheme.colorScheme.tertiary,
                            label = stringResource(R.string.label_avg_session),
                            value = AnalyzeViewModel.formatDuration(stats.avgSessionSeconds)
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
                            iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                            iconBg = MaterialTheme.colorScheme.primaryContainer,
                            valueColor = MaterialTheme.colorScheme.primary,
                            label = stringResource(R.string.label_focus_days),
                            value = "${stats.focusDaysCount}d"
                        )

                        MetricTileCard(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            shape = horizontalSegmentedShape(index = 1, count = 2),
                            icon = R.drawable.ic_streak,
                            iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                            iconBg = MaterialTheme.colorScheme.tertiaryContainer,
                            valueColor = MaterialTheme.colorScheme.tertiary,
                            label = stringResource(R.string.label_best_streak),
                            value = "${stats.bestStreakDays}d"
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
                            iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                            iconBg = MaterialTheme.colorScheme.primaryContainer,
                            valueColor = MaterialTheme.colorScheme.primary,
                            label = stringResource(R.string.label_best_day),
                            value = AnalyzeViewModel.formatDuration(stats.bestDaySeconds)
                        )

                        MetricTileCard(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            shape = horizontalSegmentedShape(index = 1, count = 3),
                            icon = R.drawable.ic_target,
                            iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                            iconBg = MaterialTheme.colorScheme.secondaryContainer,
                            valueColor = MaterialTheme.colorScheme.secondary,
                            label = stringResource(R.string.label_best_week),
                            value = AnalyzeViewModel.formatDuration(stats.bestWeekSeconds)
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
                            label = stringResource(R.string.label_best_month),
                            value = AnalyzeViewModel.formatDuration(stats.bestMonthSeconds)
                        )
                    }
                }
            }

            // --- Card 2: Heat Map ---
            Surface(
                shape = heatMapShapes.shape,
                color = CustomColors.cardContainerColor,
                tonalElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                YearlyHeatMapContent(
                    year = selectedYear,
                    activeDaysSet = stats.activeDaysSet
                )
            }
        }
    }
}

@Composable
private fun YearlyHeatMapContent(
    year: Int,
    activeDaysSet: Set<String>
) {
    val currentMonth = remember { Calendar.getInstance().get(Calendar.MONTH) }
    val initialPage = remember(currentMonth) { currentMonth / 2 }

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { MONTH_PAIRS.size }
    )

    // Pre-calculate all 12 months data once off the composition loops using zero-allocation date APIs
    val allMonthsData = remember(year, activeDaysSet) {
        val monthNames = DateFormatSymbols(Locale.getDefault()).months
        (0..11).map { monthIndex ->
            val ym = java.time.YearMonth.of(year, monthIndex + 1)
            val maxDays = ym.lengthOfMonth()
            val firstDayOfWeek = java.time.LocalDate.of(year, monthIndex + 1, 1).dayOfWeek.value - 1 // Monday = 0 ... Sunday = 6
            val m = monthIndex + 1
            val focused = BooleanArray(maxDays + 1)
            for (d in 1..maxDays) {
                val dayKey = "$year-${if (m < 10) "0$m" else "$m"}-${if (d < 10) "0$d" else "$d"}"
                focused[d] = activeDaysSet.contains(dayKey)
            }
            MonthGridData(
                monthIndex = monthIndex,
                monthName = monthNames[monthIndex],
                maxDays = maxDays,
                firstDayOfWeek = firstDayOfWeek,
                dayIsFocused = focused
            )
        }
    }

    Column(
        modifier = Modifier.padding(11.dp)
    ) {
        AnalyzeCardHeader(
            icon = R.drawable.ic_streak,
            title = stringResource(R.string.title_heat_map)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Month-Pair Pager with all 6 pages pre-warmed for 60/120fps swiping
        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 5,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val (firstMonthIndex, secondMonthIndex) = MONTH_PAIRS[page]
            val firstMonthData = allMonthsData[firstMonthIndex]
            val secondMonthData = allMonthsData[secondMonthIndex]

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Left Weekday Labels Column
                Column(
                    modifier = Modifier.padding(top = 26.dp)
                ) {
                    HEATMAP_WEEK_DAYS.forEach { day ->
                        Box(
                            modifier = Modifier
                                .height(21.dp)
                                .padding(end = 4.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 9.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                    }
                }

                // First Month Grid
                MonthHeatMapColumn(
                    monthData = firstMonthData,
                    modifier = Modifier.weight(1f)
                )

                // Second Month Grid
                MonthHeatMapColumn(
                    monthData = secondMonthData,
                    modifier = Modifier.weight(1f)
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
            repeat(MONTH_PAIRS.size) { index ->
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
    monthData: MonthGridData,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
    val unfocusedBgColor = CustomColors.innerCardContainerColor

    val textMeasurer = rememberTextMeasurer()
    val normalTextStyle = MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 9.5.sp,
        lineHeight = 11.sp,
        color = onSurfaceVariantColor
    )
    val focusedTextStyle = MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 9.5.sp,
        lineHeight = 11.sp,
        color = onPrimaryColor
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Month Title
        Text(
            text = monthData.monthName,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // Single high-performance Canvas: 42 slots rendered with 0 Compose nodes in <0.05ms
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(165.dp)
        ) {
            val totalCols = 6
            val totalRows = 7
            val spacingPx = 3.dp.toPx()
            val cellHeightPx = 21.dp.toPx()
            val cornerRadiusPx = 5.dp.toPx()
            val cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)

            val availableWidth = size.width
            val cellWidthPx = (availableWidth - (totalCols - 1) * spacingPx) / totalCols

            for (col in 0 until totalCols) {
                val left = col * (cellWidthPx + spacingPx)
                for (row in 0 until totalRows) {
                    val top = row * (cellHeightPx + spacingPx)
                    val slotIndex = col * 7 + row
                    val dayNumber = slotIndex - monthData.firstDayOfWeek + 1

                    if (dayNumber in 1..monthData.maxDays) {
                        val isFocused = monthData.dayIsFocused[dayNumber]
                        val cellRect = Rect(left, top, left + cellWidthPx, top + cellHeightPx)

                        if (isFocused) {
                            drawRoundRect(
                                color = primaryColor,
                                topLeft = cellRect.topLeft,
                                size = cellRect.size,
                                cornerRadius = cornerRadius
                            )
                        } else {
                            drawRoundRect(
                                color = unfocusedBgColor,
                                topLeft = cellRect.topLeft,
                                size = cellRect.size,
                                cornerRadius = cornerRadius
                            )
                        }

                        val dayStr = dayNumber.toString()
                        val textLayout = textMeasurer.measure(
                            text = dayStr,
                            style = if (isFocused) focusedTextStyle else normalTextStyle
                        )

                        val textX = left + (cellWidthPx - textLayout.size.width) / 2f
                        val textY = top + (cellHeightPx - textLayout.size.height) / 2f

                        drawText(
                            textLayoutResult = textLayout,
                            topLeft = Offset(textX, textY)
                        )
                    }
                }
            }
        }
    }
}
