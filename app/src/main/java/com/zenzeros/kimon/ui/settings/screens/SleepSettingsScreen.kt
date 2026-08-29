@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui.settings.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import com.zenzeros.kimon.KimonApplication
import com.zenzeros.kimon.R
import com.zenzeros.kimon.service.health.HealthConnectManager
import com.zenzeros.kimon.service.sleep.usage.AppUsageHelper
import com.zenzeros.kimon.ui.settings.SettingsSwitchItem
import com.zenzeros.kimon.ui.settings.SettingsUiState
import com.zenzeros.kimon.ui.sleep.SleepViewModel
import com.zenzeros.kimon.ui.theme.CustomColors.cardBorder
import com.zenzeros.kimon.ui.theme.CustomColors.cardContainerColor
import com.zenzeros.kimon.ui.theme.CustomColors.listItemColors
import com.zenzeros.kimon.ui.theme.CustomColors.switchColors
import com.zenzeros.kimon.ui.theme.CustomColors.topBarColors
import com.zenzeros.kimon.ui.theme.KimonShapeDefaults.cardShape
import com.zenzeros.kimon.ui.theme.KimonShapeDefaults.segmentedListItemShapes
import com.zenzeros.kimon.ui.theme.LocalAppFonts
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun SleepSettingsScreen(
    state: SettingsUiState,
    onToggleSleepMonitoring: (Boolean) -> Unit,
    onToggleHealthConnectSync: (Boolean) -> Unit,
    onSetSleepGoal: (Int) -> Unit,
    onToggleScheduledMode: (Boolean) -> Unit = {},
    onSetBedtime: (Int, Int) -> Unit = { _, _ -> },
    onSetWakeTime: (Int, Int) -> Unit = { _, _ -> },
    onToggleAppUsageAccess: (Boolean) -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val kimonApp = context.applicationContext as KimonApplication
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // Sleep Monitoring Notification Permission Launcher (Android 13+)
    val sleepPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { notificationGranted ->
        val granted = notificationGranted || (
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )

        if (granted) {
            kimonApp.sleepMonitorManager.startSleepMonitoring(
                onSuccess = { onToggleSleepMonitoring(true) },
                onFailure = {
                    Toast.makeText(context, "Failed to start sleep monitoring: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            Toast.makeText(context, "Notification permission required for sleep tracking status", Toast.LENGTH_LONG).show()
            onToggleSleepMonitoring(false)
        }
    }

    // Health Connect Permission Launcher
    val healthConnectPermissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        if (grantedPermissions.containsAll(HealthConnectManager.PERMISSIONS)) {
            onToggleHealthConnectSync(true)
            coroutineScope.launch {
                kimonApp.sleepRepository.syncFromHealthConnect()
                kimonApp.sleepRepository.syncUnsyncedToHealthConnect()
            }
        } else {
            Toast.makeText(context, "Health Connect permissions were not fully granted", Toast.LENGTH_SHORT).show()
            onToggleHealthConnectSync(false)
        }
    }

    val switchItems = remember(state.sleepMonitoringEnabled, state.healthConnectSyncEnabled, state.appUsageAccessEnabled) {
        listOf(
            SettingsSwitchItem(
                checked = state.sleepMonitoringEnabled,
                icon = R.drawable.ic_moon,
                label = R.string.settings_sleep_monitoring,
                description = R.string.settings_sleep_monitoring_desc,
                onClick = { enabled ->
                    if (enabled) {
                        if (!kimonApp.sleepMonitorManager.hasPermission() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            sleepPermissionsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
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
            ),
            SettingsSwitchItem(
                checked = state.appUsageAccessEnabled,
                icon = R.drawable.ic_screen_awake,
                label = R.string.settings_app_usage_access,
                description = R.string.settings_app_usage_access_desc,
                onClick = { enabled ->
                    if (enabled && !AppUsageHelper.hasUsageStatsPermission(context)) {
                        context.startActivity(AppUsageHelper.createUsageAccessSettingsIntent())
                        Toast.makeText(context, "Please enable Kimon in Usage Access Settings", Toast.LENGTH_LONG).show()
                    }
                    onToggleAppUsageAccess(enabled)
                }
            ),
            SettingsSwitchItem(
                checked = state.healthConnectSyncEnabled,
                icon = R.drawable.ic_sparkles,
                label = R.string.settings_health_connect_sync,
                description = R.string.settings_health_connect_sync_desc,
                onClick = { enabled ->
                    if (enabled) {
                        if (!kimonApp.healthConnectManager.isAvailable()) {
                            Toast.makeText(context, "Health Connect is not available on this device", Toast.LENGTH_LONG).show()
                            onToggleHealthConnectSync(false)
                        } else {
                            healthConnectPermissionLauncher.launch(HealthConnectManager.PERMISSIONS)
                        }
                    } else {
                        onToggleHealthConnectSync(false)
                    }
                }
            )
        )
    }

    val sleepGoalPresets = listOf(420, 450, 480, 510, 540) // 7h, 7.5h, 8h, 8.5h, 9h

    fun formatTime12h(hour: Int, minute: Int): String {
        val amPm = if (hour >= 12) "PM" else "AM"
        val h12 = if (hour % 12 == 0) 12 else hour % 12
        return String.format(Locale.getDefault(), "%d:%02d %s", h12, minute, amPm)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_section_sleep),
                        fontFamily = LocalAppFonts.current.topBarTitle,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
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
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Spacer(Modifier.height(6.dp))

            // ==========================================
            // 1. SECTION: Sleep Goal Setting
            // ==========================================
            Text(
                text = stringResource(R.string.settings_section_sleep_goal),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(start = 6.dp, bottom = 4.dp)
            )

            // Sleep Goal Stepper Item
            SegmentedListItem(
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_bed),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                trailingContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (state.sleepGoalMinutes > 240) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSetSleepGoal(state.sleepGoalMinutes - 30)
                                }
                            },
                            enabled = state.sleepGoalMinutes > 240,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_remove),
                                contentDescription = "Decrease",
                                modifier = Modifier.size(16.dp),
                                tint = if (state.sleepGoalMinutes > 240) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant
                            )
                        }

                        Text(
                            text = SleepViewModel.formatDuration(state.sleepGoalMinutes.toLong()),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                fontFamily = LocalAppFonts.current.topBarTitle
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        IconButton(
                            onClick = {
                                if (state.sleepGoalMinutes < 720) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSetSleepGoal(state.sleepGoalMinutes + 30)
                                }
                            },
                            enabled = state.sleepGoalMinutes < 720,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_add),
                                contentDescription = "Increase",
                                modifier = Modifier.size(16.dp),
                                tint = if (state.sleepGoalMinutes < 720) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                },
                shapes = segmentedListItemShapes(0, 1),
                colors = listItemColors,
                onClick = {}
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.settings_sleep_goal),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.settings_sleep_goal_desc),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.5.sp,
                            lineHeight = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quick Preset Goal Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                sleepGoalPresets.forEach { mins ->
                    val isSelected = state.sleepGoalMinutes == mins
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                        ),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSetSleepGoal(mins)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = SleepViewModel.formatDuration(mins.toLong()),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.5.sp
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // ==========================================
            // 2. SECTION: Bedtime Schedule
            // ==========================================
            Text(
                text = stringResource(R.string.settings_section_sleep_schedule),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(start = 6.dp, bottom = 4.dp)
            )

            val scheduleGroupCount = if (state.sleepScheduledMode) 2 else 1

            // Scheduled Mode Switch Item
            SegmentedListItem(
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (state.sleepScheduledMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_alarm_sound),
                            contentDescription = null,
                            tint = if (state.sleepScheduledMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                trailingContent = {
                    Switch(
                        checked = state.sleepScheduledMode,
                        onCheckedChange = null,
                        colors = switchColors
                    )
                },
                shapes = segmentedListItemShapes(0, scheduleGroupCount),
                colors = listItemColors,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggleScheduledMode(!state.sleepScheduledMode)
                }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.settings_sleep_scheduled_mode),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.settings_sleep_scheduled_mode_desc),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.5.sp,
                            lineHeight = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            var showBedtimePicker by remember { mutableStateOf(false) }

            if (showBedtimePicker) {
                val timePickerState = rememberTimePickerState(
                    initialHour = state.targetBedtimeHour,
                    initialMinute = state.targetBedtimeMinute,
                    is24Hour = android.text.format.DateFormat.is24HourFormat(context)
                )

                Dialog(
                    onDismissRequest = { showBedtimePicker = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.extraLarge,
                        tonalElevation = 6.dp,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .width(IntrinsicSize.Min)
                            .padding(horizontal = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = stringResource(R.string.settings_target_bedtime),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            TimePicker(
                                state = timePickerState,
                                colors = TimePickerDefaults.colors()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        showBedtimePicker = false
                                    }
                                ) {
                                    Text(stringResource(R.string.cancel))
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                TextButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onSetBedtime(timePickerState.hour, timePickerState.minute)
                                        showBedtimePicker = false
                                    }
                                ) {
                                    Text(
                                        text = stringResource(R.string.confirm),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (state.sleepScheduledMode) {
                // Target Bedtime (Material 3 Time Picker)
                SegmentedListItem(
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_moon),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    trailingContent = {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showBedtimePicker = true
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = formatTime12h(state.targetBedtimeHour, state.targetBedtimeMinute),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Icon(
                                    painter = painterResource(R.drawable.ic_chevron_right),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    shapes = segmentedListItemShapes(1, scheduleGroupCount),
                    colors = listItemColors,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showBedtimePicker = true
                    }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = stringResource(R.string.settings_target_bedtime),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Starts 1h prior at ${formatTime12h((state.targetBedtimeHour - 1 + 24) % 24, state.targetBedtimeMinute)}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 12.5.sp,
                                lineHeight = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // ==========================================
            // 3. SECTION: Tracking & Sync
            // ==========================================
            Text(
                text = stringResource(R.string.settings_section_sleep_tracking),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(start = 6.dp, bottom = 4.dp)
            )

            val syncGroupSize = if (state.healthConnectSyncEnabled) switchItems.size + 1 else switchItems.size

            switchItems.forEachIndexed { index, item ->
                SegmentedListItem(
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (item.checked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(item.icon),
                                contentDescription = null,
                                tint = if (item.checked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    trailingContent = {
                        Switch(
                            checked = item.checked,
                            onCheckedChange = null,
                            thumbContent = {
                                if (item.checked) {
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
                    },
                    shapes = segmentedListItemShapes(index, syncGroupSize),
                    colors = listItemColors,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        item.onClick(!item.checked)
                    }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = stringResource(item.label),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(item.description),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 12.5.sp,
                                lineHeight = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (state.healthConnectSyncEnabled) {
                SegmentedListItem(
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_sparkles),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    trailingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_chevron_right),
                            contentDescription = stringResource(R.string.settings_sync_now),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    shapes = segmentedListItemShapes(switchItems.size, syncGroupSize),
                    colors = listItemColors,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        coroutineScope.launch {
                            kimonApp.sleepRepository.syncFromHealthConnect()
                            kimonApp.sleepRepository.syncUnsyncedToHealthConnect()
                            Toast.makeText(context, context.getString(R.string.settings_sync_success), Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = stringResource(R.string.settings_sync_now),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.settings_sync_now_desc),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 12.5.sp,
                                lineHeight = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // ==========================================
            // 4. SECTION: Special App Access
            // ==========================================
            Text(
                text = stringResource(R.string.settings_section_special_access),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(start = 6.dp, bottom = 4.dp)
            )

            val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
            var refreshKey by remember { androidx.compose.runtime.mutableIntStateOf(0) }

            androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
                val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                    if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                        refreshKey++
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            val canExactAlarm = remember(refreshKey) { com.zenzeros.kimon.service.sleep.SpecialAccessHelper.canScheduleExactAlarms(context) }
            val isBatteryUnrestricted = remember(refreshKey) { com.zenzeros.kimon.service.sleep.SpecialAccessHelper.isIgnoringBatteryOptimizations(context) }
            val hasUsageAccess = remember(refreshKey) { com.zenzeros.kimon.service.sleep.SpecialAccessHelper.hasUsageAccess(context) }

            // 1. Alarms & Reminders
            SegmentedListItem(
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (canExactAlarm) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_alarm_sound),
                            contentDescription = null,
                            tint = if (canExactAlarm) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                trailingContent = {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (canExactAlarm) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            com.zenzeros.kimon.service.sleep.SpecialAccessHelper.openExactAlarmSettings(context)
                        }
                    ) {
                        Text(
                            text = if (canExactAlarm) stringResource(R.string.settings_permission_allowed) else stringResource(R.string.settings_permission_tap_to_grant),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = if (canExactAlarm) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                },
                shapes = segmentedListItemShapes(0, 3),
                colors = listItemColors,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    com.zenzeros.kimon.service.sleep.SpecialAccessHelper.openExactAlarmSettings(context)
                }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.settings_exact_alarms),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.settings_exact_alarms_desc),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.5.sp,
                            lineHeight = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 2. Unrestricted Battery
            SegmentedListItem(
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (isBatteryUnrestricted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_bolt),
                            contentDescription = null,
                            tint = if (isBatteryUnrestricted) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                trailingContent = {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isBatteryUnrestricted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            com.zenzeros.kimon.service.sleep.SpecialAccessHelper.openBatteryOptimizationSettings(context)
                        }
                    ) {
                        Text(
                            text = if (isBatteryUnrestricted) stringResource(R.string.settings_permission_allowed) else stringResource(R.string.settings_permission_tap_to_grant),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = if (isBatteryUnrestricted) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                },
                shapes = segmentedListItemShapes(1, 3),
                colors = listItemColors,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    com.zenzeros.kimon.service.sleep.SpecialAccessHelper.openBatteryOptimizationSettings(context)
                }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.settings_battery_unrestricted),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.settings_battery_unrestricted_desc),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.5.sp,
                            lineHeight = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 3. Usage Access
            SegmentedListItem(
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (hasUsageAccess) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_screen_awake),
                            contentDescription = null,
                            tint = if (hasUsageAccess) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                trailingContent = {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (hasUsageAccess) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            com.zenzeros.kimon.service.sleep.SpecialAccessHelper.openUsageAccessSettings(context)
                        }
                    ) {
                        Text(
                            text = if (hasUsageAccess) stringResource(R.string.settings_permission_allowed) else stringResource(R.string.settings_permission_tap_to_grant),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = if (hasUsageAccess) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                },
                shapes = segmentedListItemShapes(2, 3),
                colors = listItemColors,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    com.zenzeros.kimon.service.sleep.SpecialAccessHelper.openUsageAccessSettings(context)
                }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.settings_app_usage_access),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.settings_app_usage_access_desc),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.5.sp,
                            lineHeight = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // ==========================================
            // 5. SECTION: How it works (Info Card)
            // ==========================================
            Surface(
                shape = cardShape,
                color = cardContainerColor,
                border = cardBorder,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_moon),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = stringResource(R.string.settings_sleep_info_title),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = stringResource(R.string.settings_sleep_info_desc),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}
