@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.zenzeros.kimon.ui.sleep

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
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
import com.zenzeros.kimon.service.step.StepCounterManager
import com.zenzeros.kimon.ui.analyze.components.AnalyzeCardHeader
import com.zenzeros.kimon.ui.analyze.components.AnalyzeEmptyState
import com.zenzeros.kimon.ui.analyze.components.AnalyzeNavigationHeader
import com.zenzeros.kimon.ui.analyze.components.MetricTileCard
import com.zenzeros.kimon.ui.analyze.components.horizontalSegmentedShape
import com.zenzeros.kimon.ui.components.bouncyScroll
import com.zenzeros.kimon.ui.theme.CustomColors
import com.zenzeros.kimon.ui.theme.KimonShapeDefaults.cardShape
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun SleepScreen(
    viewModel: SleepViewModel = viewModel(
        factory = SleepViewModel.Factory(
            sleepRepository = (LocalContext.current.applicationContext as KimonApplication).sleepRepository,
            userSettingsRepository = (LocalContext.current.applicationContext as KimonApplication).userSettingsRepository,
            stepCounterManager = (LocalContext.current.applicationContext as KimonApplication).stepCounterManager
        )
    ),
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    val stepPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onStepPermissionResult(isGranted)
    }

    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEE, MMM d", Locale.getDefault()) }

    val selectedCalendar = remember(state.selectedDayEpochMs) {
        Calendar.getInstance().apply {
            val epoch = state.selectedDayEpochMs
            if (epoch != null) {
                timeInMillis = epoch
            }
        }
    }

    val isSelectedDayToday = remember(selectedCalendar.timeInMillis) {
        val now = Calendar.getInstance()
        now.get(Calendar.YEAR) == selectedCalendar.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == selectedCalendar.get(Calendar.DAY_OF_YEAR)
    }

    val dayOfWeek = remember(selectedCalendar.timeInMillis, isSelectedDayToday) {
        if (isSelectedDayToday) "TODAY"
        else SimpleDateFormat("EEE", Locale.getDefault()).format(selectedCalendar.time).uppercase()
    }

    val formattedDate = remember(selectedCalendar.timeInMillis) {
        SimpleDateFormat("MM / dd", Locale.getDefault()).format(selectedCalendar.time)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .bouncyScroll()
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Navigation Header: [ Left: Combined Day & Date Pill ] ... [ Right: ButtonGroup with < and > ]
        AnalyzeNavigationHeader(
            onPreviousClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                viewModel.previousDay()
            },
            onNextClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                viewModel.nextDay()
            }
        ) {
            Text(
                text = dayOfWeek,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "•",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )

            Text(
                text = formattedDate,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    letterSpacing = 0.3.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Icon(
                painter = painterResource(R.drawable.ic_calendar),
                contentDescription = "Calendar",
                modifier = Modifier.size(15.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        val showStepCounter = state.isStepCounterEnabled
        val sleepCardShapes = if (showStepCounter) ListItemDefaults.segmentedShapes(index = 0, count = 2) else null
        val stepCardShapes = if (showStepCounter) ListItemDefaults.segmentedShapes(index = 1, count = 2) else null

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Hero Sleep Card
            Surface(
                shape = sleepCardShapes?.shape ?: cardShape,
                color = CustomColors.cardContainerColor,
                border = CustomColors.cardBorder,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val activeSession = state.displayedSession
                    val isDaySelected = state.selectedDayEpochMs != null
                    val heroTitle = when {
                        state.selectedDayLabel != null -> "${state.selectedDayLabel} Sleep"
                        activeSession != null -> "Last Night's Sleep"
                        else -> "Last Night's Sleep"
                    }

                    AnalyzeCardHeader(
                        icon = R.drawable.ic_moon,
                        title = heroTitle,
                        iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                        iconBg = MaterialTheme.colorScheme.primaryContainer,
                        trailingContent = {
                            if (activeSession != null) {
                                val qualityRating = when {
                                    activeSession.qualityScore >= 85 -> "Optimal"
                                    activeSession.qualityScore >= 75 -> "Good"
                                    activeSession.qualityScore >= 60 -> "Fair"
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
                                        text = "$qualityRating • ${activeSession.qualityScore}",
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

                    if (activeSession != null) {
                        val durationMinutes = state.displayedDurationMinutes
                        val startMs = state.displayedStartTimeEpochMs ?: activeSession.startTimeEpochMs
                        val endMs = state.displayedEndTimeEpochMs ?: activeSession.endTimeEpochMs

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = SleepViewModel.formatDuration(durationMinutes),
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontFamily = com.zenzeros.kimon.ui.theme.LocalAppFonts.current.topBarTitle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 38.sp,
                                    letterSpacing = (-0.8).sp
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            val goalPercentage = ((durationMinutes.toFloat() / state.sleepGoalMinutes.toFloat()) * 100).toInt()
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
                            val progressFraction = (durationMinutes.toFloat() / state.sleepGoalMinutes.toFloat()).coerceIn(0f, 1f)
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
                                value = timeFormat.format(Date(startMs))
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
                                value = timeFormat.format(Date(endMs))
                            )
                        }

                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isDaySelected) "No sleep recorded for this day" else "No sleep recorded for last night",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Text(
                                text = if (isDaySelected) "Tap another day or the active day pill to reset" else "Select a day below to view past sleep",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            val latest = state.latestSession
                            if (latest != null && !isDaySelected) {
                                val latestDateStr = remember(latest) {
                                    SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date(latest.endTimeEpochMs))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    onClick = {
                                        val cal = Calendar.getInstance().apply {
                                            timeInMillis = latest.endTimeEpochMs
                                            set(Calendar.HOUR_OF_DAY, 0)
                                            set(Calendar.MINUTE, 0)
                                            set(Calendar.SECOND, 0)
                                            set(Calendar.MILLISECOND, 0)
                                        }
                                        viewModel.selectDay(cal.timeInMillis)
                                    },
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_moon),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "View latest sleep • $latestDateStr",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp
                                            ),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showStepCounter && stepCardShapes != null) {
                // Step Counter Card (Segmented Item 2)
                StepCounterCard(
                    steps = state.todaySteps,
                    goal = state.stepGoal,
                    isSensorAvailable = state.isStepSensorAvailable,
                    hasPermission = state.hasStepPermission,
                    shape = stepCardShapes.shape,
                    onRequestPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            stepPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                        } else {
                            viewModel.onStepPermissionResult(true)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun StepCounterCard(
    steps: Int,
    goal: Int,
    isSensorAvailable: Boolean,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = cardShape
) {
    Surface(
        shape = shape,
        color = CustomColors.cardContainerColor,
        border = CustomColors.cardBorder,
        tonalElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val goalPercentage = if (goal > 0) ((steps.toFloat() / goal.toFloat()) * 100).toInt() else 0
            val formattedSteps = remember(steps) {
                NumberFormat.getNumberInstance(Locale.getDefault()).format(steps)
            }
            val formattedGoal = remember(goal) {
                NumberFormat.getNumberInstance(Locale.getDefault()).format(goal)
            }

            AnalyzeCardHeader(
                icon = R.drawable.ic_steps,
                title = stringResource(R.string.title_step_counter),
                iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                iconBg = MaterialTheme.colorScheme.secondaryContainer,
                trailingContent = {
                    if (hasPermission && isSensorAvailable) {
                        Surface(
                            shape = CircleShape,
                            color = if (goalPercentage >= 100) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                            )
                        ) {
                            Text(
                                text = "$goalPercentage%",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    letterSpacing = (-0.2).sp
                                ),
                                color = if (goalPercentage >= 100) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            )

            if (!isSensorAvailable) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.step_sensor_unavailable),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else if (!hasPermission) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.step_permission_prompt),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    FilledTonalButton(
                        onClick = onRequestPermission,
                        shape = CircleShape
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_steps),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.action_enable_steps),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            } else {
                // Steps Hero Metric
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = formattedSteps,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontFamily = com.zenzeros.kimon.ui.theme.LocalAppFonts.current.topBarTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 38.sp,
                            letterSpacing = (-0.8).sp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.label_steps),
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
                            text = "$goalPercentage% of $formattedGoal goal",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = if (goalPercentage >= 100) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Progress capsule bar
                    val progressFraction = if (goal > 0) (steps.toFloat() / goal.toFloat()).coerceIn(0f, 1f) else 0f
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val tertiaryColor = MaterialTheme.colorScheme.tertiary
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
                                brush = Brush.horizontalGradient(listOf(primaryColor, tertiaryColor)),
                                size = Size(barWidth * progressFraction, barHeight),
                                cornerRadius = radius
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Segmented row for Distance and Calories
                val distanceKm = StepCounterManager.calculateDistanceKm(steps)
                val caloriesKcal = StepCounterManager.calculateCaloriesKcal(steps)

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
                        icon = R.drawable.ic_distance,
                        iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                        iconBg = MaterialTheme.colorScheme.secondaryContainer,
                        valueColor = MaterialTheme.colorScheme.onSurface,
                        label = stringResource(R.string.label_distance),
                        value = String.format(Locale.getDefault(), "%.2f km", distanceKm)
                    )

                    MetricTileCard(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        shape = horizontalSegmentedShape(index = 1, count = 2),
                        icon = R.drawable.ic_streak,
                        iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                        iconBg = MaterialTheme.colorScheme.tertiaryContainer,
                        valueColor = MaterialTheme.colorScheme.onSurface,
                        label = stringResource(R.string.label_calories),
                        value = String.format(Locale.getDefault(), "%d kcal", caloriesKcal)
                    )
                }
            }
        }
    }
}

