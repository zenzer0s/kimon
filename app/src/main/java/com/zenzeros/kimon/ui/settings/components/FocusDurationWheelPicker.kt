package com.zenzeros.kimon.ui.settings.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenzeros.kimon.R
import com.zenzeros.kimon.ui.theme.CustomColors
import kotlin.math.abs

@Composable
fun FocusDurationWheelPicker(
    totalMinutes: Int,
    onMinutesChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxMinutes: Int = 180,
    minMinutes: Int = 1
) {
    val haptic = LocalHapticFeedback.current
    val currentTotalMinutes by rememberUpdatedState(totalMinutes)

    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    val seconds = 0

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = CustomColors.cardContainerColor,
        tonalElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // --- 1. Hours Wheel Column ---
                WheelColumn(
                    value = hours,
                    range = 0..3,
                    onValueChange = { newHours ->
                        val newTotal = (newHours * 60 + minutes).coerceIn(minMinutes, maxMinutes)
                        if (newTotal != currentTotalMinutes) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onMinutesChanged(newTotal)
                        }
                    }
                )

                // Separator
                Text(
                    text = ":",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Light
                    ),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                )

                // --- 2. Minutes Wheel Column ---
                WheelColumn(
                    value = minutes,
                    range = 0..59,
                    onValueChange = { newMinutes ->
                        val effectiveHours = if (hours == 0 && newMinutes == 0) 0 else hours
                        val calculated = effectiveHours * 60 + newMinutes
                        val newTotal = calculated.coerceIn(minMinutes, maxMinutes)
                        if (newTotal != currentTotalMinutes) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onMinutesChanged(newTotal)
                        }
                    }
                )

                // Separator
                Text(
                    text = ":",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Light
                    ),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                )

                // --- 3. Seconds Column (Fixed 00) ---
                WheelColumn(
                    value = seconds,
                    range = 0..0,
                    onValueChange = {}
                )
            }
        }
    }
}

@Composable
private fun WheelColumn(
    value: Int,
    range: IntProgression,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var dragAccumulator by remember { mutableFloatStateOf(0f) }
    val dragThreshold = 36f

    val minVal = range.first
    val maxVal = range.last
    val canStep = minVal != maxVal

    val prevVal = if (canStep) {
        if (value - 1 < minVal) maxVal else value - 1
    } else value

    val nextVal = if (canStep) {
        if (value + 1 > maxVal) minVal else value + 1
    } else value

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .width(84.dp)
            .pointerInput(value, canStep) {
                if (!canStep) return@pointerInput
                detectVerticalDragGestures(
                    onDragEnd = { dragAccumulator = 0f },
                    onDragCancel = { dragAccumulator = 0f },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        dragAccumulator += dragAmount
                        if (abs(dragAccumulator) >= dragThreshold) {
                            if (dragAccumulator < 0) {
                                // Dragged up -> increment
                                onValueChange(nextVal)
                            } else {
                                // Dragged down -> decrement
                                onValueChange(prevVal)
                            }
                            dragAccumulator = 0f
                        }
                    }
                )
            }
    ) {
        // Upper faint preview number & arrow
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .alpha(if (canStep) 0.35f else 0.08f)
        ) {
            if (canStep) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onValueChange(nextVal)
                    },
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_right),
                        contentDescription = "Increment",
                        modifier = Modifier
                            .rotate(-90f)
                            .height(14.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                Box(Modifier.height(28.dp))
            }

            Text(
                text = String.format("%02d", prevVal),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        // Active center number
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInVertically { height -> height } + fadeIn() togetherWith
                            slideOutVertically { height -> -height } + fadeOut()
                } else {
                    slideInVertically { height -> -height } + fadeIn() togetherWith
                            slideOutVertically { height -> height } + fadeOut()
                }
            },
            label = "WheelNumberAnimation"
        ) { targetVal ->
            Text(
                text = String.format("%02d", targetVal),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // Lower faint preview number & arrow
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .alpha(if (canStep) 0.35f else 0.08f)
        ) {
            Text(
                text = String.format("%02d", nextVal),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (canStep) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onValueChange(prevVal)
                    },
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_right),
                        contentDescription = "Decrement",
                        modifier = Modifier
                            .rotate(90f)
                            .height(14.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                Box(Modifier.height(28.dp))
            }
        }
    }
}
