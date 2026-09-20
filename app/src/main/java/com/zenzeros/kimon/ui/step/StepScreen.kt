@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.zenzeros.kimon.ui.step

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
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
import com.zenzeros.kimon.ui.analyze.components.AnalyzeCardHeader
import com.zenzeros.kimon.ui.analyze.components.MetricTileCard
import com.zenzeros.kimon.ui.analyze.components.horizontalSegmentedShape
import com.zenzeros.kimon.ui.components.bouncyScroll
import com.zenzeros.kimon.ui.theme.CustomColors
import com.zenzeros.kimon.ui.theme.KimonShapeDefaults.cardShape
import java.text.NumberFormat
import java.util.Locale

@Composable
fun StepScreen(
    viewModel: StepViewModel = viewModel(
        factory = StepViewModel.Factory(
            stepCounterManager = (LocalContext.current.applicationContext as KimonApplication).stepCounterManager,
            userSettingsRepository = (LocalContext.current.applicationContext as KimonApplication).userSettingsRepository
        )
    ),
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .bouncyScroll()
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp)
            .padding(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(2.dp))

        StepCard(
            viewModel = viewModel,
            shape = cardShape
        )
    }
}

@Composable
fun StepCard(
    viewModel: StepViewModel = viewModel(
        factory = StepViewModel.Factory(
            stepCounterManager = (LocalContext.current.applicationContext as KimonApplication).stepCounterManager,
            userSettingsRepository = (LocalContext.current.applicationContext as KimonApplication).userSettingsRepository
        )
    ),
    shape: Shape = cardShape,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val stepPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionResult(isGranted)
    }

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
            val steps = state.todaySteps
            val goal = state.dailyGoal
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
                    if (state.hasPermission && state.isSensorAvailable) {
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

            if (!state.isSensorAvailable) {
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
            } else if (!state.hasPermission) {
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
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                stepPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                            } else {
                                viewModel.onPermissionResult(true)
                            }
                        },
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
                val distanceKm = state.distanceKm
                val caloriesKcal = state.caloriesKcal

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
