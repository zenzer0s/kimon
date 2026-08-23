@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui.pomodoro

import android.graphics.Paint as AndroidPaint
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun FlipCardPomodoroClock(
    remainingSeconds: Int,
    modifier: Modifier = Modifier,
    spacing: Dp = 20.dp
) {
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val availableWidth = maxWidth
        val availableHeight = maxHeight

        // Cards fill available horizontal space (equal margin on left & right).
        // Vertical spacing between top card and bottom card matches the exact margin.
        val cardWidth = availableWidth
        val cardHeight = if (availableHeight > 0.dp) {
            ((availableHeight - spacing) / 2f).coerceAtMost(cardWidth * 0.92f)
        } else {
            240.dp
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterVertically)
        ) {
            // Minutes Vertical Card (Top)
            FlipCardTile(
                targetValue = minutes,
                width = cardWidth,
                height = cardHeight
            )

            // Seconds Vertical Card (Bottom)
            FlipCardTile(
                targetValue = seconds,
                width = cardWidth,
                height = cardHeight
            )
        }
    }
}

@Composable
private fun FlipCardTile(
    targetValue: Int,
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier
) {
    var previousValue by remember { mutableIntStateOf(targetValue) }
    var displayTargetValue by remember { mutableIntStateOf(targetValue) }
    val flipAnimation = remember { Animatable(1f) }

    LaunchedEffect(targetValue) {
        if (targetValue != previousValue) {
            displayTargetValue = targetValue
            flipAnimation.snapTo(0f)
            flipAnimation.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing)
            )
            previousValue = targetValue
        }
    }

    val progress = flipAnimation.value
    val isFirstHalf = progress <= 0.5f

    val cardBg = MaterialTheme.colorScheme.surfaceContainerHigh
    val cardTextColor = MaterialTheme.colorScheme.onSurface
    val grooveColor = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .size(width = width, height = height),
        contentAlignment = Alignment.Center
    ) {
        // 1. Static Background Card
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val halfH = h / 2f
            val corner = 20.dp.toPx()
            val grooveH = 3.dp.toPx()

            // Top Half (New Target Value)
            drawCardHalf(
                text = String.format("%02d", displayTargetValue),
                isTop = true,
                width = w,
                height = h,
                halfHeight = halfH,
                cornerRadius = corner,
                grooveHeight = grooveH,
                cardColor = cardBg,
                textColor = cardTextColor,
                shadowAlpha = 0f
            )

            // Bottom Half (Old Previous Value)
            drawCardHalf(
                text = String.format("%02d", previousValue),
                isTop = false,
                width = w,
                height = h,
                halfHeight = halfH,
                cornerRadius = corner,
                grooveHeight = grooveH,
                cardColor = cardBg,
                textColor = cardTextColor,
                shadowAlpha = 0f
            )
        }

        // 2. Animated 3D Flap
        if (isFirstHalf) {
            // Phase 1: Top flap (showing Old value) flips DOWN from 0° to -90° (Pivot at bottom edge)
            val rotationAngle = -progress * 180f
            val shadowAlpha = (progress * 2f).coerceIn(0f, 0.65f)

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationX = rotationAngle
                        cameraDistance = 18f * density
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.5f)
                    }
            ) {
                val w = size.width
                val h = size.height
                val halfH = h / 2f
                val corner = 20.dp.toPx()
                val grooveH = 3.dp.toPx()

                drawCardHalf(
                    text = String.format("%02d", previousValue),
                    isTop = true,
                    width = w,
                    height = h,
                    halfHeight = halfH,
                    cornerRadius = corner,
                    grooveHeight = grooveH,
                    cardColor = cardBg,
                    textColor = cardTextColor,
                    shadowAlpha = shadowAlpha
                )
            }
        } else {
            // Phase 2: Bottom flap (showing New value) flips DOWN from +90° to 0° (Pivot at top edge)
            val rotationAngle = (1f - progress) * 180f
            val shadowAlpha = ((1f - progress) * 2f).coerceIn(0f, 0.65f)

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationX = rotationAngle
                        cameraDistance = 18f * density
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.5f)
                    }
            ) {
                val w = size.width
                val h = size.height
                val halfH = h / 2f
                val corner = 20.dp.toPx()
                val grooveH = 3.dp.toPx()

                drawCardHalf(
                    text = String.format("%02d", displayTargetValue),
                    isTop = false,
                    width = w,
                    height = h,
                    halfHeight = halfH,
                    cornerRadius = corner,
                    grooveHeight = grooveH,
                    cardColor = cardBg,
                    textColor = cardTextColor,
                    shadowAlpha = shadowAlpha
                )
            }
        }

        // 3. Center Split Groove Divider Line
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val halfH = h / 2f
            val grooveH = 3.5.dp.toPx()

            drawLine(
                color = grooveColor,
                start = Offset(0f, halfH),
                end = Offset(w, halfH),
                strokeWidth = grooveH
            )
        }
    }
}

/**
 * Helper to draw a single top or bottom half of the flip card.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCardHalf(
    text: String,
    isTop: Boolean,
    width: Float,
    height: Float,
    halfHeight: Float,
    cornerRadius: Float,
    grooveHeight: Float,
    cardColor: Color,
    textColor: Color,
    shadowAlpha: Float
) {
    val path = Path().apply {
        if (isTop) {
            addRoundRect(
                RoundRect(
                    rect = Rect(0f, 0f, width, halfHeight - (grooveHeight / 2f)),
                    topLeft = CornerRadius(cornerRadius, cornerRadius),
                    topRight = CornerRadius(cornerRadius, cornerRadius),
                    bottomLeft = CornerRadius.Zero,
                    bottomRight = CornerRadius.Zero
                )
            )
        } else {
            addRoundRect(
                RoundRect(
                    rect = Rect(0f, halfHeight + (grooveHeight / 2f), width, height),
                    topLeft = CornerRadius.Zero,
                    topRight = CornerRadius.Zero,
                    bottomLeft = CornerRadius(cornerRadius, cornerRadius),
                    bottomRight = CornerRadius(cornerRadius, cornerRadius)
                )
            )
        }
    }

    clipPath(path) {
        // Fill card background
        drawRect(color = cardColor)

        // Draw centered bold digit text across the full card
        val canvas = drawContext.canvas.nativeCanvas
        val paint = AndroidPaint().apply {
            isAntiAlias = true
            isSubpixelText = true
            color = textColor.toArgb()
            textSize = height * 0.70f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = AndroidPaint.Align.CENTER
        }

        val fm = paint.fontMetrics
        val textCenterY = (height / 2f) - ((fm.descent + fm.ascent) / 2f)
        canvas.drawText(text, width / 2f, textCenterY, paint)

        // Dynamic 3D lighting shadow
        if (shadowAlpha > 0f) {
            drawRect(color = Color.Black.copy(alpha = shadowAlpha))
        }
    }
}
