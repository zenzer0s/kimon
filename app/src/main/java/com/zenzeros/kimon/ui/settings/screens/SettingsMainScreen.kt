@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui.settings.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenzeros.kimon.KimonApplication
import com.zenzeros.kimon.R
import com.zenzeros.kimon.service.step.StepCounterService
import com.zenzeros.kimon.ui.components.bouncyScroll
import com.zenzeros.kimon.ui.navigation.KimonNavKey
import com.zenzeros.kimon.ui.settings.SettingsUiState
import com.zenzeros.kimon.ui.theme.CustomColors
import com.zenzeros.kimon.ui.theme.CustomColors.cardBorder
import com.zenzeros.kimon.ui.theme.CustomColors.listItemColors
import com.zenzeros.kimon.ui.theme.CustomColors.switchColors
import com.zenzeros.kimon.ui.theme.CustomColors.topBarColors
import com.zenzeros.kimon.ui.theme.KimonShapeDefaults.bottomListItemShape
import com.zenzeros.kimon.ui.theme.KimonShapeDefaults.middleListItemShape
import com.zenzeros.kimon.ui.theme.KimonShapeDefaults.singleListItemShape
import com.zenzeros.kimon.ui.theme.KimonShapeDefaults.topListItemShape
import com.zenzeros.kimon.ui.theme.LocalAppFonts

data class SettingsNavCategory(
    val key: KimonNavKey,
    val icon: Int,
    val title: Int,
    val subtitle: String
)

@Composable
fun SettingsMainScreen(
    state: SettingsUiState,
    onNavigate: (KimonNavKey) -> Unit,
    onBack: () -> Unit,
    onToggleSleepMonitoring: (Boolean) -> Unit = {},
    onToggleStepCounter: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()
    val kimonApp = remember { context.applicationContext as KimonApplication }
    val stepCounterManager = kimonApp.stepCounterManager
    val isSensorAvailable = remember { stepCounterManager.isSensorAvailable() }

    val sleepPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val activityRecognitionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions[Manifest.permission.ACTIVITY_RECOGNITION] == true
        } else true

        if (activityRecognitionGranted) {
            kimonApp.sleepMonitorManager.startSleepMonitoring(
                onSuccess = { onToggleSleepMonitoring(true) },
                onFailure = {
                    Toast.makeText(context, "Failed to start Google Sleep API: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                    onToggleSleepMonitoring(false)
                }
            )
        } else {
            Toast.makeText(context, "Physical Activity permission required for Sleep Tracking", Toast.LENGTH_LONG).show()
            onToggleSleepMonitoring(false)
        }
    }

    val handleToggleSleepMonitoring = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        val target = !state.sleepMonitoringEnabled
        if (target) {
            if (!kimonApp.sleepMonitorManager.hasPermission()) {
                val permissions = buildList {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        add(Manifest.permission.ACTIVITY_RECOGNITION)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                sleepPermissionsLauncher.launch(permissions.toTypedArray())
            } else {
                kimonApp.sleepMonitorManager.startSleepMonitoring(
                    onSuccess = { onToggleSleepMonitoring(true) },
                    onFailure = { onToggleSleepMonitoring(false) }
                )
            }
        } else {
            kimonApp.sleepMonitorManager.stopSleepMonitoring(
                onSuccess = { onToggleSleepMonitoring(false) }
            )
        }
    }

    val stepPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onToggleStepCounter(true)
            StepCounterService.start(context)
            Toast.makeText(context, "Step counter enabled", Toast.LENGTH_SHORT).show()
        } else {
            onToggleStepCounter(false)
            StepCounterService.stop(context)
            Toast.makeText(context, "Activity recognition permission required to track steps", Toast.LENGTH_LONG).show()
        }
    }

    val handleToggleStepCounter = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        if (!isSensorAvailable) {
            Toast.makeText(context, context.getString(R.string.step_sensor_unavailable), Toast.LENGTH_SHORT).show()
        } else {
            val target = !state.stepCounterEnabled
            if (target) {
                if (!stepCounterManager.hasPermission()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        stepPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                    } else {
                        onToggleStepCounter(true)
                        StepCounterService.start(context)
                    }
                } else {
                    onToggleStepCounter(true)
                    StepCounterService.start(context)
                }
            } else {
                onToggleStepCounter(false)
                StepCounterService.stop(context)
            }
        }
    }

    val categories = remember {
        listOf(
            SettingsNavCategory(
                key = KimonNavKey.TimerSettings,
                icon = R.drawable.ic_focus,
                title = R.string.settings_section_timer,
                subtitle = "Durations, auto-start & display"
            ),
            SettingsNavCategory(
                key = KimonNavKey.AlarmSettings,
                icon = R.drawable.ic_alarm_sound,
                title = R.string.settings_section_sound,
                subtitle = "Alarm sound, vibration & volume"
            ),
            SettingsNavCategory(
                key = KimonNavKey.AppearanceSettings,
                icon = R.drawable.palette,
                title = R.string.settings_section_appearance,
                subtitle = "Theme, dynamic color & Nothing OS"
            ),
            SettingsNavCategory(
                key = KimonNavKey.BackupSettings,
                icon = R.drawable.ic_backup,
                title = R.string.settings_section_data,
                subtitle = "Backup, restore & reset data"
            ),
            SettingsNavCategory(
                key = KimonNavKey.AboutSettings,
                icon = R.drawable.ic_profile,
                title = R.string.settings_section_about,
                subtitle = "App info, donations & developer"
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.title_settings),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = LocalAppFonts.current.topBarTitle,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                },
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = onBack,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = listItemColors.containerColor
                        ),
                        modifier = Modifier.padding(start = 12.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_chevron_left),
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = topBarColors
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .bouncyScroll()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Spacer(Modifier.height(6.dp))

            // Main Settings Categories Card (Focus, Sound & Vibration, Appearance & Theme)
            categories.forEachIndexed { index, cat ->
                val shape: Shape = when (index) {
                    0 -> topListItemShape
                    categories.lastIndex -> bottomListItemShape
                    else -> middleListItemShape
                }

                Surface(
                    shape = shape,
                    color = listItemColors.containerColor,
                    border = CustomColors.cardBorder,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onNavigate(cat.key)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(cat.icon),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = stringResource(cat.title),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = cat.subtitle,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 12.5.sp,
                                    lineHeight = 16.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            painter = painterResource(R.drawable.ic_chevron_right),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Activity and Tracking Section
            Text(
                text = "Activity and tracking",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 0.4.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 6.dp, bottom = 4.dp)
            )

            // Item 1: Sleep tracking
            Surface(
                shape = topListItemShape,
                color = listItemColors.containerColor,
                border = CustomColors.cardBorder,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onNavigate(KimonNavKey.SleepSettings)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (state.sleepMonitoringEnabled) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceContainerHighest,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_moon),
                            contentDescription = null,
                            tint = if (state.sleepMonitoringEnabled) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = stringResource(R.string.settings_sleep_master_title),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_chevron_right),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(22.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        )

                        Switch(
                            checked = state.sleepMonitoringEnabled,
                            onCheckedChange = { handleToggleSleepMonitoring() },
                            thumbContent = {
                                if (state.sleepMonitoringEnabled) {
                                    Icon(
                                        painter = painterResource(R.drawable.check),
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(R.drawable.clear),
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                }
                            },
                            colors = switchColors
                        )
                    }
                }
            }

            // Item 2: Step counter
            Surface(
                shape = bottomListItemShape,
                color = listItemColors.containerColor,
                border = CustomColors.cardBorder,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onNavigate(KimonNavKey.StepSettings)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (state.stepCounterEnabled && isSensorAvailable) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceContainerHighest,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_steps),
                            contentDescription = null,
                            tint = if (state.stepCounterEnabled && isSensorAvailable) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = stringResource(R.string.title_step_counter),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_chevron_right),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(22.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        )

                        Switch(
                            checked = state.stepCounterEnabled && isSensorAvailable,
                            enabled = isSensorAvailable,
                            onCheckedChange = { handleToggleStepCounter() },
                            thumbContent = {
                                if (state.stepCounterEnabled && isSensorAvailable) {
                                    Icon(
                                        painter = painterResource(R.drawable.check),
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(R.drawable.clear),
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                }
                            },
                            colors = switchColors
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}
