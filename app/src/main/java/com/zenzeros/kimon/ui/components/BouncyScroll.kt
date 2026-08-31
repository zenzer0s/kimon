package com.zenzeros.kimon.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * A calm, natural, critically damped overscroll motion engine with progressive resistance and zero oscillation.
 */
fun Modifier.bouncyScroll(
    orientation: Orientation = Orientation.Vertical,
    maxOverscroll: Float = 55f,
    resistance: Float = 0.14f
): Modifier = composed {
    val coroutineScope = rememberCoroutineScope()
    val offset = remember { Animatable(0f) }
    val haptic = LocalHapticFeedback.current

    // Calm, critically-damped spring: smooth, fluid settle with zero wobble or rubber-banding
    val calmSpringSpec = remember {
        spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        )
    }

    val nestedScrollConnection = remember(orientation, maxOverscroll, resistance, haptic, calmSpringSpec) {
        object : NestedScrollConnection {
            private var currentOffset: Float = 0f
            private var hasTriggeredDragHaptic = false

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = if (orientation == Orientation.Vertical) available.y else available.x
                if (abs(currentOffset) > 0.5f) {
                    val isMovingTowardsZero = (currentOffset > 0 && delta < 0) || (currentOffset < 0 && delta > 0)
                    if (isMovingTowardsZero) {
                        val newOffset = currentOffset + delta
                        val sign = if (currentOffset > 0) 1 else -1
                        val crossesZero = (sign > 0 && newOffset < 0) || (sign < 0 && newOffset > 0)
                        val consumedDelta = if (crossesZero) -currentOffset else delta
                        currentOffset = if (crossesZero) 0f else newOffset
                        if (currentOffset == 0f) {
                            hasTriggeredDragHaptic = false
                        }
                        val snapVal = currentOffset
                        coroutineScope.launch {
                            offset.snapTo(snapVal)
                        }
                        return if (orientation == Orientation.Vertical) {
                            Offset(0f, consumedDelta)
                        } else {
                            Offset(consumedDelta, 0f)
                        }
                    }
                } else if (currentOffset != 0f) {
                    currentOffset = 0f
                    hasTriggeredDragHaptic = false
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val delta = if (orientation == Orientation.Vertical) available.y else available.x
                if (source == NestedScrollSource.UserInput && abs(delta) > 0.5f) {
                    if (!hasTriggeredDragHaptic && abs(currentOffset) < 1f) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        hasTriggeredDragHaptic = true
                    }
                    // Progressive resistance: gentle initial pull, smoothly tightens
                    val progress = (abs(currentOffset) / maxOverscroll).coerceIn(0f, 1f)
                    val dynamicResistance = resistance * (1f - progress * 0.65f)
                    currentOffset = (currentOffset + delta * dynamicResistance).coerceIn(-maxOverscroll, maxOverscroll)
                    val snapVal = currentOffset
                    coroutineScope.launch {
                        offset.snapTo(snapVal)
                    }
                    return if (orientation == Orientation.Vertical) Offset(0f, available.y) else Offset(available.x, 0f)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                hasTriggeredDragHaptic = false
                if (abs(currentOffset) > 0.5f) {
                    val consumed = available
                    currentOffset = 0f
                    offset.animateTo(
                        targetValue = 0f,
                        animationSpec = calmSpringSpec
                    )
                    return consumed
                }
                return Velocity.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                hasTriggeredDragHaptic = false
                val velocity = if (orientation == Orientation.Vertical) available.y else available.x
                // Subtle, gentle edge cushion on fling (soft 10-15px cushion max)
                if (abs(velocity) > 250f) {
                    val flingOverscroll = (velocity * 0.008f).coerceIn(-maxOverscroll * 0.35f, maxOverscroll * 0.35f)
                    if (abs(flingOverscroll) > 1.5f) {
                        currentOffset = flingOverscroll
                        offset.snapTo(flingOverscroll)
                    }
                }
                if (abs(currentOffset) > 0.5f || abs(offset.value) > 0.5f) {
                    currentOffset = 0f
                    offset.animateTo(
                        targetValue = 0f,
                        animationSpec = calmSpringSpec
                    )
                } else {
                    currentOffset = 0f
                    offset.snapTo(0f)
                }
                return available
            }
        }
    }

    this
        .nestedScroll(nestedScrollConnection)
        .graphicsLayer {
            if (orientation == Orientation.Vertical) {
                translationY = offset.value
            } else {
                translationX = offset.value
            }
        }
}
