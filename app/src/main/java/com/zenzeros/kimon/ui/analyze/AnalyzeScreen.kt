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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenzeros.kimon.ui.analyze.tabs.DayTab
import com.zenzeros.kimon.ui.analyze.tabs.OverviewTab
import com.zenzeros.kimon.ui.analyze.tabs.WeekTab
import com.zenzeros.kimon.ui.analyze.tabs.YearTab
import kotlinx.coroutines.launch

enum class AnalyzeTimeRange(val label: String) {
    OVERVIEW("Overview"),
    DAY("Day"),
    WEEK("Week"),
    YEAR("Year")
}

@Composable
fun AnalyzeScreen(
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val ranges = remember { AnalyzeTimeRange.values() }
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { ranges.size }
    )

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
            ranges.forEachIndexed { index, range ->
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
                            text = range.label,
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

        // 2. Smooth Swipeable HorizontalPager with 14.dp Page Padding
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp)
            ) {
                when (ranges[page]) {
                    AnalyzeTimeRange.OVERVIEW -> {
                        OverviewTab()
                    }
                    AnalyzeTimeRange.DAY -> {
                        DayTab()
                    }
                    AnalyzeTimeRange.WEEK -> {
                        WeekTab()
                    }
                    AnalyzeTimeRange.YEAR -> {
                        YearTab()
                    }
                }
            }
        }
    }
}
