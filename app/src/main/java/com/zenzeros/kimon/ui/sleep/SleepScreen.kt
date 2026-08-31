@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.zenzeros.kimon.ui.sleep

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zenzeros.kimon.KimonApplication
import com.zenzeros.kimon.R
import com.zenzeros.kimon.data.local.entity.SleepSessionEntity
import com.zenzeros.kimon.service.sleep.usage.SleepAppUsageEvent
import kotlinx.serialization.json.Json
import com.zenzeros.kimon.ui.analyze.components.AnalyzeCardHeader
import com.zenzeros.kimon.ui.analyze.components.AnalyzeEmptyState
import com.zenzeros.kimon.ui.analyze.components.MetricTileCard
import com.zenzeros.kimon.ui.analyze.components.horizontalSegmentedShape
import com.zenzeros.kimon.ui.theme.CustomColors
import com.zenzeros.kimon.ui.theme.KimonShapeDefaults.cardShape
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun SleepScreen(
    viewModel: SleepViewModel = viewModel(
        factory = SleepViewModel.Factory(
            sleepRepository = (LocalContext.current.applicationContext as KimonApplication).sleepRepository,
            userSettingsRepository = (LocalContext.current.applicationContext as KimonApplication).userSettingsRepository
        )
    ),
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEE, MMM d", Locale.getDefault()) }
    val appUsageJsonParser = remember { Json { ignoreUnknownKeys = true } }

    val totalSectionsGroup = 3
    val heroShapesGroup = ListItemDefaults.segmentedShapes(index = 0, count = totalSectionsGroup)
    val chartShapesGroup = ListItemDefaults.segmentedShapes(index = 1, count = totalSectionsGroup)
    val historyShapesGroup = ListItemDefaults.segmentedShapes(index = 2, count = totalSectionsGroup)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // ==========================================
        // Material 3 Expressive Segmented Group (Hero Card, Weekly Chart, Recent History)
        // ==========================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // 1. Hero Last Night's Sleep Card
            Surface(
                shape = heroShapesGroup.shape,
                color = CustomColors.cardContainerColor,
                border = CustomColors.cardBorder,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val latest = state.latestSession

                AnalyzeCardHeader(
                    icon = R.drawable.ic_moon,
                    title = "Last Night's Sleep",
                    iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                    iconBg = MaterialTheme.colorScheme.primaryContainer,
                    trailingContent = {
                        if (latest != null) {
                            val qualityRating = when {
                                latest.qualityScore >= 85 -> "Optimal"
                                latest.qualityScore >= 75 -> "Good"
                                latest.qualityScore >= 60 -> "Fair"
                                else -> "Low"
                            }

                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                )
                            ) {
                                Text(
                                    text = "$qualityRating • ${latest.qualityScore}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        letterSpacing = (-0.2).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                )

                if (latest != null) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = SleepViewModel.formatDuration(latest.durationMinutes),
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontFamily = com.zenzeros.kimon.ui.theme.LocalAppFonts.current.topBarTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 38.sp,
                                letterSpacing = (-0.8).sp
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        val goalPercentage = ((latest.durationMinutes.toFloat() / state.sleepGoalMinutes.toFloat()) * 100).toInt()
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Time Asleep",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )

                            Text(
                                text = "$goalPercentage% of ${SleepViewModel.formatDuration(state.sleepGoalMinutes.toLong())} goal",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = if (goalPercentage >= 90) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Progress toward goal capsule bar
                        val progressFraction = (latest.durationMinutes.toFloat() / state.sleepGoalMinutes.toFloat()).coerceIn(0f, 1f)
                        val primaryColor = MaterialTheme.colorScheme.primary
                        val secondaryColor = MaterialTheme.colorScheme.secondary
                        val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                        ) {
                            val barWidth = size.width
                            val barHeight = size.height
                            val radius = CornerRadius(barHeight / 2f, barHeight / 2f)

                            // Track
                            drawRoundRect(
                                color = trackColor,
                                size = Size(barWidth, barHeight),
                                cornerRadius = radius
                            )

                            // Fill
                            if (progressFraction > 0) {
                                drawRoundRect(
                                    brush = Brush.horizontalGradient(listOf(primaryColor, secondaryColor)),
                                    size = Size(barWidth * progressFraction, barHeight),
                                    cornerRadius = radius
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Schedule Breakdown (Bedtime -> Wake Up)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        MetricTileCard(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            shape = horizontalSegmentedShape(index = 0, count = 2),
                            icon = R.drawable.ic_moon,
                            iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                            iconBg = MaterialTheme.colorScheme.secondaryContainer,
                            valueColor = MaterialTheme.colorScheme.onSurface,
                            label = stringResource(R.string.sleep_bedtime),
                            value = timeFormat.format(Date(latest.startTimeEpochMs))
                        )

                        MetricTileCard(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            shape = horizontalSegmentedShape(index = 1, count = 2),
                            icon = R.drawable.ic_sunrise,
                            iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                            iconBg = MaterialTheme.colorScheme.tertiaryContainer,
                            valueColor = MaterialTheme.colorScheme.onSurface,
                            label = stringResource(R.string.sleep_wake_up),
                            value = timeFormat.format(Date(latest.endTimeEpochMs))
                        )
                    }

                    // Mid-Sleep App Usage (if recorded)
                    val appUsageEvents = remember(latest.appUsageJson) {
                        try {
                            if (!latest.appUsageJson.isNullOrBlank()) {
                                appUsageJsonParser.decodeFromString<List<SleepAppUsageEvent>>(latest.appUsageJson)
                            } else emptyList()
                        } catch (e: Exception) {
                            emptyList()
                        }
                    }

                    if (appUsageEvents.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_screen_awake),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = stringResource(R.string.sleep_mid_sleep_apps),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            appUsageEvents.forEach { appEvent ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = appEvent.appName,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${timeFormat.format(Date(appEvent.startTimeEpochMs))} – ${timeFormat.format(Date(appEvent.endTimeEpochMs))}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                                    ) {
                                        Text(
                                            text = if (appEvent.durationSeconds >= 60) "${appEvent.durationSeconds / 60}m" else "${appEvent.durationSeconds}s",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "No sleep recorded yet",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Wear your device or keep sleep tracking enabled",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // ==========================================
        // 3. Weekly Sleep Trend Bar Chart (60/120fps Canvas)
        // ==========================================
        Surface(
            shape = chartShapesGroup.shape,
            color = CustomColors.cardContainerColor,
            border = CustomColors.cardBorder,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnalyzeCardHeader(
                    icon = R.drawable.ic_bar_chart,
                    title = stringResource(R.string.sleep_weekly_trend),
                    iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                    iconBg = MaterialTheme.colorScheme.tertiaryContainer,
                    trailingContent = {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Avg:",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                                )
                                Text(
                                    text = SleepViewModel.formatDuration(state.weeklyAverageMinutes),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        letterSpacing = (-0.2).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                )

                val primaryColor = MaterialTheme.colorScheme.primary
                val secondaryColor = MaterialTheme.colorScheme.secondary
                val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f)
                val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                val days = state.weeklyDays

                // Canvas 7-Day Bar Chart
                Column(modifier = Modifier.fillMaxWidth()) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        val chartWidth = size.width
                        val chartHeight = size.height
                        val topPadding = 10.dp.toPx()
                        val bottomPadding = 10.dp.toPx()
                        val usableHeight = chartHeight - topPadding - bottomPadding
                        val yBottom = chartHeight - bottomPadding

                        val maxMins = (state.sleepGoalMinutes * 1.3f).coerceAtLeast(600f)
                        val targetMins = state.sleepGoalMinutes.toFloat()
                        val targetY = yBottom - (targetMins / maxMins) * usableHeight

                        // Target Guideline (dynamic optimal sleep goal)
                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                        drawLine(
                            color = primaryColor.copy(alpha = 0.4f),
                            start = Offset(0f, targetY),
                            end = Offset(chartWidth, targetY),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = dashEffect
                        )

                        val numDays = days.size.coerceAtLeast(1)
                        val slotWidth = chartWidth / numDays
                        val barWidth = (slotWidth * 0.48f).coerceIn(12.dp.toPx(), 28.dp.toPx())

                        days.forEachIndexed { index, day ->
                            val xCenter = (index + 0.5f) * slotWidth
                            val barX = xCenter - (barWidth / 2f)

                            // Background Track
                            drawRoundRect(
                                color = if (day.isToday) primaryColor.copy(alpha = 0.12f) else trackColor,
                                topLeft = Offset(barX, topPadding),
                                size = Size(barWidth, usableHeight),
                                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                            )

                            // Active Duration Bar
                            if (day.durationMinutes > 0) {
                                val fraction = (day.durationMinutes.toFloat() / maxMins).coerceIn(0f, 1f)
                                val barHeight = (fraction * usableHeight).coerceAtLeast(barWidth)
                                val barYTop = yBottom - barHeight

                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        colors = if (day.isToday) listOf(secondaryColor, primaryColor) else listOf(primaryColor, primaryColor.copy(alpha = 0.75f)),
                                        startY = barYTop,
                                        endY = yBottom
                                    ),
                                    topLeft = Offset(barX, barYTop),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Day of Week Labels Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        days.forEach { day ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                Text(
                                    text = day.dayLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 10.sp
                                    ),
                                    color = if (day.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = day.dayNumber,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 9.sp
                                    ),
                                    color = if (day.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 4. Recent Sleep History List
        // ==========================================
        Surface(
            shape = historyShapesGroup.shape,
            color = CustomColors.cardContainerColor,
            border = CustomColors.cardBorder,
            tonalElevation = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnalyzeCardHeader(
                    icon = R.drawable.ic_list,
                    title = stringResource(R.string.sleep_recent_history),
                    iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                    iconBg = MaterialTheme.colorScheme.secondaryContainer
                )

                if (state.recentSessions.isEmpty()) {
                    AnalyzeEmptyState(
                        icon = R.drawable.ic_moon,
                        message = stringResource(R.string.sleep_empty_history)
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        state.recentSessions.forEachIndexed { index, session ->
                            val itemShape = when {
                                state.recentSessions.size == 1 -> cardShape
                                index == 0 -> RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                                index == state.recentSessions.size - 1 -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 14.dp, bottomEnd = 14.dp)
                                else -> RoundedCornerShape(4.dp)
                            }

                            Surface(
                                shape = itemShape,
                                color = CustomColors.innerCardContainerColor,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = CustomColors.innerCardBorderColor
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 11.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Leading Circular Icon
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .background(
                                                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f),
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_moon),
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        // Date and Time Range
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = dateFormat.format(Date(session.endTimeEpochMs)),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 14.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )

                                            Text(
                                                text = "${timeFormat.format(Date(session.startTimeEpochMs))} – ${timeFormat.format(Date(session.endTimeEpochMs))}",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Normal
                                                ),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    // Right Side: Duration & Subtle Source Tag / Delete Action
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.End,
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(
                                                text = SleepViewModel.formatDuration(session.durationMinutes),
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    fontFamily = com.zenzeros.kimon.ui.theme.LocalAppFonts.current.topBarTitle
                                                ),
                                                color = MaterialTheme.colorScheme.primary
                                            )

                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
                                            ) {
                                                Text(
                                                    text = if (session.source == "GOOGLE_SLEEP_API") "Google API" else if (session.source == "HEALTH_CONNECT") "Health Connect" else "Manual",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 9.5.sp,
                                                        fontWeight = FontWeight.Medium
                                                    ),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                viewModel.deleteSession(session)
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_delete),
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}
