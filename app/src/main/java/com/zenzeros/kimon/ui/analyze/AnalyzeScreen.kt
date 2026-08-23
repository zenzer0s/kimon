@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui.analyze

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalToggleButton
import androidx.compose.material3.FilledTonalToggleButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenzeros.kimon.ui.analyze.tabs.DayTab
import com.zenzeros.kimon.ui.analyze.tabs.OverviewTab

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
    var selectedRange by remember { mutableStateOf(AnalyzeTimeRange.OVERVIEW) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
    ) {
        // 1. Material 3 Expressive ButtonGroup Tabs
        ButtonGroup(
            overflowIndicator = { menuState ->
                ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
        ) {
            val ranges = AnalyzeTimeRange.values()
            ranges.forEachIndexed { index, range ->
                customItem(
                    buttonGroupContent = {
                        val shapes = when (index) {
                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                            ranges.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                        }

                        FilledTonalToggleButton(
                            checked = selectedRange == range,
                            onCheckedChange = { if (it) selectedRange = range },
                            shapes = shapes,
                            colors = FilledTonalToggleButtonDefaults.filledTonalToggleButtonColors(
                                checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = range.label,
                                fontWeight = if (selectedRange == range) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    },
                    menuContent = {}
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 2. Animated Content Routing for Tabs
        AnimatedContent(
            targetState = selectedRange,
            transitionSpec = {
                fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(150))
            },
            label = "analyzeTabContent",
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { range ->
            when (range) {
                AnalyzeTimeRange.OVERVIEW -> {
                    OverviewTab()
                }
                AnalyzeTimeRange.DAY -> {
                    DayTab()
                }
                AnalyzeTimeRange.WEEK -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        // Week Tab Placeholder
                    }
                }
                AnalyzeTimeRange.YEAR -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        // Year Tab Placeholder
                    }
                }
            }
        }
    }
}
