@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
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
 * Adds native Material 3 Expressive spring motion and tactile haptics when scrolling reaches boundaries.
 */
fun Modifier.bouncyScroll(
    orientation: Orientation = Orientation.Vertical,
    maxOverscroll: Float = 220f,
    resistance: Float = 0.28f
): Modifier = composed {
    val coroutineScope = rememberCoroutineScope()
    val offset = remember { Animatable(0f) }
    val haptic = LocalHapticFeedback.current
    val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val fastSpatialSpec = MaterialTheme.motionScheme.fastSpatialSpec<Float>()

    val nestedScrollConnection = remember(orientation, maxOverscroll, resistance, haptic, spatialSpec, fastSpatialSpec) {
        object : NestedScrollConnection {
            private var currentOffset: Float = 0f
            private var hasTriggeredDragHaptic = false

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = if (orientation == Orientation.Vertical) available.y else available.x
                // Only intercept when actively stretched past the boundary to smoothly return to zero
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
                // Only stretch when user is actively dragging past boundary edges
                if (source == NestedScrollSource.UserInput && abs(delta) > 0.5f) {
                    if (!hasTriggeredDragHaptic && abs(currentOffset) < 1f) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        hasTriggeredDragHaptic = true
                    }
                    currentOffset = (currentOffset + delta * resistance).coerceIn(-maxOverscroll, maxOverscroll)
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
                // When released from drag overscroll, spring back using native M3 Expressive spatial motion
                if (abs(currentOffset) > 0.5f) {
                    val consumed = available
                    currentOffset = 0f
                    offset.animateTo(
                        targetValue = 0f,
                        animationSpec = spatialSpec
                    )
                    return consumed
                }
                return Velocity.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                hasTriggeredDragHaptic = false
                val velocity = if (orientation == Orientation.Vertical) available.y else available.x
                // Expressive boundary bounce when fling reaches the end of the scroll container
                if (abs(velocity) > 100f) {
                    val flingOverscroll = (velocity * 0.035f).coerceIn(-maxOverscroll * 0.45f, maxOverscroll * 0.45f)
                    if (abs(flingOverscroll) > 2f) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        currentOffset = flingOverscroll
                        offset.snapTo(flingOverscroll)
                    }
                }
                if (abs(currentOffset) > 0.5f || abs(offset.value) > 0.5f) {
                    currentOffset = 0f
                    offset.animateTo(
                        targetValue = 0f,
                        animationSpec = fastSpatialSpec
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
