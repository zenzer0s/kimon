package com.zenzeros.kimon.ui.analyze

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zenzeros.kimon.KimonApplication
import com.zenzeros.kimon.R
import com.zenzeros.kimon.ui.analyze.tabs.DayTab
import com.zenzeros.kimon.ui.analyze.tabs.OverviewTab
import com.zenzeros.kimon.ui.analyze.tabs.WeekTab
import com.zenzeros.kimon.ui.analyze.tabs.YearTab
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

enum class AnalyzeTimeRange(val labelRes: Int) {
    OVERVIEW(R.string.analyze_tab_overview),
    DAY(R.string.analyze_tab_day),
    WEEK(R.string.analyze_tab_week),
    YEAR(R.string.analyze_tab_year)
}

private val ANALYZE_RANGES = AnalyzeTimeRange.values()

@Composable
fun AnalyzeScreen(
    viewModel: AnalyzeViewModel = viewModel(
        factory = AnalyzeViewModel.Factory(
            getOverviewStatsUseCase = (LocalContext.current.applicationContext as KimonApplication).getOverviewStatsUseCase,
            getDayStatsUseCase = (LocalContext.current.applicationContext as KimonApplication).getDayStatsUseCase,
            getWeekStatsUseCase = (LocalContext.current.applicationContext as KimonApplication).getWeekStatsUseCase,
            getYearStatsUseCase = (LocalContext.current.applicationContext as KimonApplication).getYearStatsUseCase
        )
    ),
    onNavigateToFocus: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { ANALYZE_RANGES.size }
    )

    val overviewStats by viewModel.overviewStats.collectAsStateWithLifecycle()
    val overviewMonthCal by viewModel.overviewMonthCalendar.collectAsStateWithLifecycle()

    val dayStats by viewModel.dayStats.collectAsStateWithLifecycle()
    val selectedDayCal by viewModel.selectedDayCalendar.collectAsStateWithLifecycle()

    val weekStats by viewModel.weekStats.collectAsStateWithLifecycle()
    val selectedWeekCal by viewModel.selectedWeekStartCalendar.collectAsStateWithLifecycle()

    val yearStats by viewModel.yearStats.collectAsStateWithLifecycle()
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Expressive Segmented Capsule Tab Control with Continuous Sliding Motion
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    val tabCount = ANALYZE_RANGES.size
                    val totalWidth = maxWidth
                    val tabWidth = totalWidth / tabCount
                    val scrollPosition = pagerState.currentPage + pagerState.currentPageOffsetFraction

                    // 1. Continuous Floating Indicator Pill with Spring Physics
                    Box(
                        modifier = Modifier
                            .offset {
                                val xOffsetPx = (tabWidth.toPx() * scrollPosition).roundToInt()
                                IntOffset(x = xOffsetPx, y = 0)
                            }
                            .width(tabWidth)
                            .height(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    )

                    // 2. Interactive Label Layer with Interpolated Color States
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ANALYZE_RANGES.forEachIndexed { index, range ->
                            val distance = (scrollPosition - index).absoluteValue.coerceIn(0f, 1f)
                            val textColor = lerp(
                                MaterialTheme.colorScheme.onPrimaryContainer,
                                MaterialTheme.colorScheme.onSurfaceVariant,
                                distance
                            )
                            val fontWeight = if (distance < 0.5f) FontWeight.Bold else FontWeight.Medium

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(range.labelRes),
                                    fontWeight = fontWeight,
                                    fontSize = 13.sp,
                                    letterSpacing = (-0.2).sp,
                                    color = textColor,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // 2. Smooth Swipeable HorizontalPager with Full Pre-warming for 60/120fps Swiping
        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 3,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp)
            ) {
                when (ANALYZE_RANGES[page]) {
                    AnalyzeTimeRange.OVERVIEW -> {
                        OverviewTab(
                            stats = overviewStats,
                            calendarMonth = overviewMonthCal,
                            onPreviousMonth = { viewModel.previousOverviewMonth() },
                            onNextMonth = { viewModel.nextOverviewMonth() }
                        )
                    }
                    AnalyzeTimeRange.DAY -> {
                        DayTab(
                            stats = dayStats,
                            selectedCalendar = selectedDayCal,
                            onPreviousDay = { viewModel.previousDay() },
                            onNextDay = { viewModel.nextDay() },
                            onNavigateToFocus = onNavigateToFocus
                        )
                    }
                    AnalyzeTimeRange.WEEK -> {
                        WeekTab(
                            stats = weekStats,
                            selectedWeekStart = selectedWeekCal,
                            onPreviousWeek = { viewModel.previousWeek() },
                            onNextWeek = { viewModel.nextWeek() }
                        )
                    }
                    AnalyzeTimeRange.YEAR -> {
                        YearTab(
                            stats = yearStats,
                            selectedYear = selectedYear,
                            onPreviousYear = { viewModel.previousYear() },
                            onNextYear = { viewModel.nextYear() }
                        )
                    }
                }
            }
        }
    }
}
