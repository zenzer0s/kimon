@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui.analyze

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
        // 1. Material 3 PrimaryTabRow with Full Width & No Text Clipping
        PrimaryTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            indicator = {
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(pagerState.currentPage),
                    width = 38.dp,
                    height = 3.5.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
            },
            divider = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            ANALYZE_RANGES.forEachIndexed { index, range ->
                val selected = pagerState.currentPage == index
                Tab(
                    selected = selected,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    modifier = Modifier.padding(vertical = 4.dp),
                    text = {
                        Text(
                            text = stringResource(range.labelRes),
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            fontSize = 13.sp,
                            letterSpacing = (-0.2).sp,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 2. Smooth Swipeable HorizontalPager with Pre-fetching for 60/120fps Swiping
        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
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
