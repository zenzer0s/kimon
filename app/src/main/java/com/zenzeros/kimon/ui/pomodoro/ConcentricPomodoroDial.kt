@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui.pomodoro

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import android.graphics.Paint as AndroidPaint
import android.graphics.Path as AndroidPath
import android.graphics.RectF as AndroidRectF

@Composable
fun ConcentricPomodoroDial(
    remainingSeconds: Int,
    currentMode: PomodoroMode = PomodoroMode.FOCUS,
    modifier: Modifier = Modifier
) {
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60

    // Continuous monotonic angle without 00/59 rewind glitch
    val targetSecondsAngle = -(remainingSeconds.toFloat() * 6.0f)
    val animatedSecondsAngle by animateFloatAsState(
        targetValue = targetSecondsAngle,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "secondsAngle"
    )

    val targetMinutesAngle = -((remainingSeconds.toFloat() / 60.0f) * 6.0f)
    val animatedMinutesAngle by animateFloatAsState(
        targetValue = targetMinutesAngle,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "minutesAngle"
    )

    // Center display string (Minutes) & Pill display string (Seconds)
    val centerMinutesText = String.format(Locale.getDefault(), "%02d", minutes)
    val pillSecondsText = String.format(Locale.getDefault(), "%02d", seconds)

    // Theme color tokens
    val centerTextColor = MaterialTheme.colorScheme.onBackground.toArgb()
    val pillTextColor = MaterialTheme.colorScheme.onPrimaryContainer.toArgb()
    val pillBorderColor = MaterialTheme.colorScheme.primary.toArgb()
    val pillFillColor = MaterialTheme.colorScheme.primaryContainer.toArgb()
    val tickColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val bgCircleColor = MaterialTheme.colorScheme.surfaceContainerHigh.toArgb()

    // Paints
    val tickPaint = remember {
        AndroidPaint().apply {
            isAntiAlias = true
            strokeCap = AndroidPaint.Cap.ROUND
            style = AndroidPaint.Style.STROKE
        }
    }

    val textPaint = remember {
        AndroidPaint().apply {
            isAntiAlias = true
            isSubpixelText = true
            textAlign = AndroidPaint.Align.CENTER
        }
    }

    val pillBorderPaint = remember {
        AndroidPaint().apply {
            isAntiAlias = true
            style = AndroidPaint.Style.STROKE
            strokeWidth = 2.5f
        }
    }

    val pillBgPaint = remember {
        AndroidPaint().apply {
            isAntiAlias = true
            style = AndroidPaint.Style.FILL
        }
    }

    val bgPaint = remember {
        AndroidPaint().apply {
            isAntiAlias = true
            style = AndroidPaint.Style.FILL
        }
    }

    val cutoutPath = remember { AndroidPath() }
    val pillRectF = remember { AndroidRectF() }

    Box(
        modifier = modifier
            .size(350.dp)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvas = drawContext.canvas.nativeCanvas
            val fMin = minOf(size.width, size.height)
            val radius = fMin / 2.0f
            val centerX = size.width / 2.0f
            val centerY = size.height / 2.0f

            // 1. Draw subtle background circle
            bgPaint.color = bgCircleColor
            bgPaint.alpha = 35
            canvas.drawCircle(centerX, centerY, radius, bgPaint)

            // 2. Setup the Indicator Pill at the 3 o'clock Outer Ring position (radius * 0.85f)
            val pillCenterRadius = radius * 0.85f
            val pillCenterX = centerX + pillCenterRadius
            val pillWidth = radius * 0.26f
            val pillHeight = radius * 0.18f
            val cornerRadius = pillHeight / 2.0f

            pillRectF.set(
                pillCenterX - (pillWidth / 2.0f),
                centerY - (pillHeight / 2.0f),
                pillCenterX + (pillWidth / 2.0f),
                centerY + (pillHeight / 2.0f)
            )

            // 3. Cutout Path for the pill to cleanly remove passing outer ticks and numbers
            cutoutPath.reset()
            cutoutPath.addRoundRect(
                pillRectF,
                cornerRadius,
                cornerRadius,
                AndroidPath.Direction.CW
            )

            canvas.save()
            try {
                canvas.clipOutPath(cutoutPath)
            } catch (e: NoSuchMethodError) {
                @Suppress("DEPRECATION")
                canvas.clipPath(cutoutPath, android.graphics.Region.Op.DIFFERENCE)
            }

            // 4. Draw Concentric Rings with Clockwise polar coordinates and wide spacing
            for (i in 0 until 60) {
                val isMajor = (i % 5 == 0)

                // --- A. Inner Ring: Minutes (00 - 59) ---
                val minAngleDeg = animatedMinutesAngle + (i * 6.0f)
                val minRad = Math.toRadians(minAngleDeg.toDouble())

                val minOuterR = radius * 0.70f
                val minInnerR = if (isMajor) radius * 0.63f else radius * 0.66f

                val minStartX = (centerX + cos(minRad) * minInnerR).toFloat()
                val minStartY = (centerY + sin(minRad) * minInnerR).toFloat()
                val minEndX = (centerX + cos(minRad) * minOuterR).toFloat()
                val minEndY = (centerY + sin(minRad) * minOuterR).toFloat()

                tickPaint.color = tickColor
                tickPaint.strokeWidth = if (isMajor) 3.0f else 1.8f
                tickPaint.alpha = if (isMajor) 220 else 90
                canvas.drawLine(minStartX, minStartY, minEndX, minEndY, tickPaint)

                // Inner numbers for major ticks (00, 05, 10... 55)
                if (isMajor) {
                    val labelR = radius * 0.54f
                    val labelX = (centerX + cos(minRad) * labelR).toFloat()
                    val labelY = (centerY + sin(minRad) * labelR).toFloat()

                    textPaint.color = tickColor
                    textPaint.alpha = 180
                    textPaint.textSize = radius * 0.076f
                    val numStr = String.format(Locale.getDefault(), "%02d", i)
                    val fm = textPaint.fontMetrics
                    canvas.drawText(numStr, labelX, labelY - ((fm.descent + fm.ascent) / 2f), textPaint)
                }

                // --- B. Outer Ring: Seconds (00 - 59) ---
                val secAngleDeg = animatedSecondsAngle + (i * 6.0f)
                val secRad = Math.toRadians(secAngleDeg.toDouble())

                val secOuterR = radius * 0.99f
                val secInnerR = if (isMajor) radius * 0.92f else radius * 0.95f

                val secStartX = (centerX + cos(secRad) * secInnerR).toFloat()
                val secStartY = (centerY + sin(secRad) * secInnerR).toFloat()
                val secEndX = (centerX + cos(secRad) * secOuterR).toFloat()
                val secEndY = (centerY + sin(secRad) * secOuterR).toFloat()

                tickPaint.color = tickColor
                tickPaint.strokeWidth = if (isMajor) 3.5f else 2.0f
                tickPaint.alpha = if (isMajor) 230 else 100
                canvas.drawLine(secStartX, secStartY, secEndX, secEndY, tickPaint)

                // Outer numbers for major ticks (00, 05, 10... 55)
                if (isMajor) {
                    val labelR = radius * 0.85f
                    val labelX = (centerX + cos(secRad) * labelR).toFloat()
                    val labelY = (centerY + sin(secRad) * labelR).toFloat()

                    textPaint.color = tickColor
                    textPaint.alpha = 190
                    textPaint.textSize = radius * 0.082f
                    val numStr = String.format(Locale.getDefault(), "%02d", i)
                    val fm = textPaint.fontMetrics
                    canvas.drawText(numStr, labelX, labelY - ((fm.descent + fm.ascent) / 2f), textPaint)
                }
            }

            canvas.restore()

            // 5. Draw Center Big Bold Minutes (Dead Center)
            textPaint.textSize = radius * 0.44f
            textPaint.color = centerTextColor
            textPaint.alpha = 255
            textPaint.isFakeBoldText = true
            val fontMetrics = textPaint.fontMetrics
            val textCenterY = centerY - ((fontMetrics.descent + fontMetrics.ascent) / 2f)
            canvas.drawText(centerMinutesText, centerX, textCenterY, textPaint)

            // 6. Draw Highlighted Reading Pill Container & Active Seconds
            // Pill Fill Background
            pillBgPaint.color = pillFillColor
            pillBgPaint.alpha = 220
            canvas.drawRoundRect(pillRectF, cornerRadius, cornerRadius, pillBgPaint)

            // Pill Border Stroke
            pillBorderPaint.color = pillBorderColor
            pillBorderPaint.strokeWidth = 3f
            canvas.drawRoundRect(pillRectF, cornerRadius, cornerRadius, pillBorderPaint)

            // Pill Seconds Text (Pixel-perfect centered in capsule)
            textPaint.textSize = radius * 0.125f
            textPaint.color = pillTextColor
            textPaint.alpha = 255
            textPaint.isFakeBoldText = true
            val pillFontMetrics = textPaint.fontMetrics
            val pillTextCenterY = centerY - ((pillFontMetrics.descent + pillFontMetrics.ascent) / 2f)
            canvas.drawText(pillSecondsText, pillCenterX, pillTextCenterY, textPaint)
            textPaint.isFakeBoldText = false
        }
    }
}
