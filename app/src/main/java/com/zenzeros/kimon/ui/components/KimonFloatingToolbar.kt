@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zenzeros.kimon.R

enum class KimonTab {
    PLAN,
    FOCUS,
    ANALYZE
}

@Composable
fun KimonFloatingToolbar(
    selectedTab: KimonTab,
    onTabSelected: (KimonTab) -> Unit,
    modifier: Modifier = Modifier,
    tabItemWidth: Dp = 80.dp,
    tabItemHeight: Dp = 40.dp,
    pillMargin: Dp = 4.dp
) {
    val totalToolbarWidth = (tabItemWidth * 3) + (pillMargin * 2)
    val totalToolbarHeight = tabItemHeight + (pillMargin * 2)

    val targetPillOffset = (selectedTab.ordinal * tabItemWidth.value).dp

    // Smooth linear horizontal motion
    val animatedPillOffset by animateDpAsState(
        targetValue = targetPillOffset,
        animationSpec = tween(durationMillis = 200, easing = LinearEasing),
        label = "slidingPill"
    )

    // Outer Floating Pill Container (248dp x 48dp)
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 3.dp,
        shadowElevation = 4.dp,
        modifier = modifier.size(width = totalToolbarWidth, height = totalToolbarHeight)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(pillMargin),
            contentAlignment = Alignment.CenterStart
        ) {
            // Inner Symmetrical Active Indicator Pill (80dp x 40dp)
            Box(
                modifier = Modifier
                    .offset(x = animatedPillOffset)
                    .size(width = tabItemWidth, height = tabItemHeight)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    )
            )

            // Symmetrical Row of Toolbar Tab Options: Plan | Focus | Analyze
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ToolbarOptionItem(
                    label = "Plan",
                    selected = selectedTab == KimonTab.PLAN,
                    onClick = { onTabSelected(KimonTab.PLAN) },
                    iconRes = R.drawable.ic_plan,
                    width = tabItemWidth,
                    height = tabItemHeight
                )

                ToolbarOptionItem(
                    label = "Focus",
                    selected = selectedTab == KimonTab.FOCUS,
                    onClick = { onTabSelected(KimonTab.FOCUS) },
                    iconRes = R.drawable.ic_focus,
                    width = tabItemWidth,
                    height = tabItemHeight
                )

                ToolbarOptionItem(
                    label = "Analyze",
                    selected = selectedTab == KimonTab.ANALYZE,
                    onClick = { onTabSelected(KimonTab.ANALYZE) },
                    iconRes = R.drawable.ic_analyze,
                    width = tabItemWidth,
                    height = tabItemHeight
                )
            }
        }
    }
}

@Composable
private fun ToolbarOptionItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    iconRes: Int,
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 200, easing = LinearEasing),
        label = "contentColor"
    )

    Box(
        modifier = modifier
            .size(width = width, height = height)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Icon visible only for the selected active tab
            AnimatedVisibility(
                visible = selected,
                enter = fadeIn(animationSpec = tween(150, easing = LinearEasing)) +
                    expandHorizontally(animationSpec = tween(150, easing = LinearEasing)),
                exit = fadeOut(animationSpec = tween(150, easing = LinearEasing)) +
                    shrinkHorizontally(animationSpec = tween(150, easing = LinearEasing))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = label,
                        modifier = Modifier.size(18.dp),
                        tint = contentColor
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                }
            }

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                ),
                color = contentColor
            )
        }
    }
}
