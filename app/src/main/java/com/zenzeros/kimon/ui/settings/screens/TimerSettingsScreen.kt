@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui.settings.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenzeros.kimon.R
import com.zenzeros.kimon.ui.settings.SettingsSwitchItem
import com.zenzeros.kimon.ui.settings.SettingsUiState
import com.zenzeros.kimon.ui.settings.components.FocusDurationWheelPicker
import com.zenzeros.kimon.ui.settings.components.SessionPreviewCard
import com.zenzeros.kimon.ui.theme.CustomColors
import com.zenzeros.kimon.ui.theme.CustomColors.listItemColors
import com.zenzeros.kimon.ui.theme.CustomColors.switchColors
import com.zenzeros.kimon.ui.theme.CustomColors.topBarColors
import com.zenzeros.kimon.ui.theme.KimonShapeDefaults.bottomListItemShape
import com.zenzeros.kimon.ui.theme.KimonShapeDefaults.middleListItemShape
import com.zenzeros.kimon.ui.theme.KimonShapeDefaults.segmentedListItemShapes
import com.zenzeros.kimon.ui.theme.KimonShapeDefaults.topListItemShape
import com.zenzeros.kimon.ui.theme.LocalAppFonts

enum class FocusDurationType {
    FOCUS,
    SHORT_BREAK,
    LONG_BREAK
}

@Composable
fun TimerSettingsScreen(
    state: SettingsUiState,
    onSetWorkDuration: (Int) -> Unit,
    onSetShortBreak: (Int) -> Unit,
    onSetLongBreak: (Int) -> Unit,
    onSetSessionsBeforeLongBreak: (Int) -> Unit,
    onSetDailyGoal: (Int) -> Unit,
    onToggleAutoStartBreaks: (Boolean) -> Unit,
    onToggleAutoStartPomodoros: (Boolean) -> Unit,
    onToggleKeepScreenOn: (Boolean) -> Unit,
    onToggleDnd: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var activeDurationType by remember { mutableStateOf<FocusDurationType?>(FocusDurationType.FOCUS) }
    var isLongBreakEnabled by remember { mutableStateOf(true) }

    val switchItems = remember(
        state.autoStartBreaks,
        state.autoStartPomodoros,
        state.keepScreenOn,
        state.dndEnabled
    ) {
        listOf(
            SettingsSwitchItem(
                checked = state.autoStartBreaks,
                icon = R.drawable.ic_start,
                label = R.string.settings_auto_start_breaks,
                description = R.string.settings_auto_start_breaks_desc,
                onClick = onToggleAutoStartBreaks
            ),
            SettingsSwitchItem(
                checked = state.autoStartPomodoros,
                icon = R.drawable.ic_focus,
                label = R.string.settings_auto_start_pomodoros,
                description = R.string.settings_auto_start_pomodoros_desc,
                onClick = onToggleAutoStartPomodoros
            ),
            SettingsSwitchItem(
                checked = state.keepScreenOn,
                icon = R.drawable.ic_calendar,
                label = R.string.settings_keep_screen_on,
                description = R.string.settings_keep_screen_on_desc,
                onClick = onToggleKeepScreenOn
            ),
            SettingsSwitchItem(
                checked = state.dndEnabled,
                icon = R.drawable.ic_pie_chart,
                label = R.string.settings_dnd,
                description = R.string.settings_dnd_desc,
                onClick = onToggleDnd
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.title_focus_settings),
                        fontFamily = LocalAppFonts.current.topBarTitle,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    FilledTonalIconButton(
                        onClick = onBack,
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(38.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = stringResource(R.string.back),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = topBarColors
            )
        },
        containerColor = topBarColors.containerColor,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            contentPadding = innerPadding,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item { Spacer(Modifier.height(6.dp)) }

            // ==========================================
            // 1. SECTION: Adjust durations
            // ==========================================
            item {
                Text(
                    text = stringResource(R.string.section_adjust_durations),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.padding(start = 6.dp, bottom = 6.dp, top = 2.dp)
                )
            }

            // 1.1 Focus Row
            item {
                val isSelected = activeDurationType == FocusDurationType.FOCUS
                SegmentedListItem(
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3B82F6))
                        )
                    },
                    trailingContent = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${state.workDurationMinutes}m",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                ),
                                color = if (isSelected) Color(0xFF3B82F6) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                painter = painterResource(R.drawable.ic_chevron_right),
                                contentDescription = null,
                                modifier = Modifier
                                    .rotate(if (isSelected) -90f else 90f)
                                    .size(16.dp),
                                tint = if (isSelected) Color(0xFF3B82F6) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    shapes = segmentedListItemShapes(0, 3),
                    colors = listItemColors,
                    onClick = {
                        activeDurationType = if (isSelected) null else FocusDurationType.FOCUS
                    }
                ) {
                    Text(
                        text = stringResource(R.string.mode_focus),
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            }

            // 1.2 Short Break Row
            item {
                val isSelected = activeDurationType == FocusDurationType.SHORT_BREAK
                SegmentedListItem(
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                    },
                    trailingContent = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${state.shortBreakMinutes}m",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                ),
                                color = if (isSelected) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                painter = painterResource(R.drawable.ic_chevron_right),
                                contentDescription = null,
                                modifier = Modifier
                                    .rotate(if (isSelected) -90f else 90f)
                                    .size(16.dp),
                                tint = if (isSelected) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    shapes = segmentedListItemShapes(1, 3),
                    colors = listItemColors,
                    onClick = {
                        activeDurationType = if (isSelected) null else FocusDurationType.SHORT_BREAK
                    }
                ) {
                    Text(
                        text = stringResource(R.string.mode_short_break),
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            }

            // 1.3 Long Break Row
            item {
                val isSelected = activeDurationType == FocusDurationType.LONG_BREAK
                SegmentedListItem(
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8B5CF6))
                        )
                    },
                    trailingContent = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${state.longBreakMinutes}m",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                ),
                                color = if (isSelected) Color(0xFF8B5CF6) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                painter = painterResource(R.drawable.ic_chevron_right),
                                contentDescription = null,
                                modifier = Modifier
                                    .rotate(if (isSelected) -90f else 90f)
                                    .size(16.dp),
                                tint = if (isSelected) Color(0xFF8B5CF6) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    shapes = segmentedListItemShapes(2, 3),
                    colors = listItemColors,
                    onClick = {
                        activeDurationType = if (isSelected) null else FocusDurationType.LONG_BREAK
                    }
                ) {
                    Text(
                        text = stringResource(R.string.mode_long_break),
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            }

            // 1.4 Active Duration Interactive Wheel Picker
            item {
                AnimatedVisibility(
                    visible = activeDurationType != null,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        when (activeDurationType) {
                            FocusDurationType.FOCUS -> {
                                FocusDurationWheelPicker(
                                    totalMinutes = state.workDurationMinutes,
                                    onMinutesChanged = onSetWorkDuration,
                                    maxMinutes = 180,
                                    minMinutes = 1
                                )
                            }
                            FocusDurationType.SHORT_BREAK -> {
                                FocusDurationWheelPicker(
                                    totalMinutes = state.shortBreakMinutes,
                                    onMinutesChanged = onSetShortBreak,
                                    maxMinutes = 60,
                                    minMinutes = 1
                                )
                            }
                            FocusDurationType.LONG_BREAK -> {
                                FocusDurationWheelPicker(
                                    totalMinutes = state.longBreakMinutes,
                                    onMinutesChanged = onSetLongBreak,
                                    maxMinutes = 90,
                                    minMinutes = 1
                                )
                            }
                            null -> Unit
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(14.dp)) }

            // ==========================================
            // 2. SECTION: Configuration (Pomodoros stepper & Long break switch)
            // ==========================================
            item {
                Text(
                    text = stringResource(R.string.section_configuration),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.padding(start = 6.dp, bottom = 6.dp)
                )
            }

            // 2.1 Pomodoros Stepper Item
            item {
                SegmentedListItem(
                    trailingContent = {
                        // Stepper Container: [ -   4   + ]
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
                                    if (state.sessionsBeforeLongBreak > 1) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onSetSessionsBeforeLongBreak(state.sessionsBeforeLongBreak - 1)
                                    }
                                },
                                enabled = state.sessionsBeforeLongBreak > 1,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_remove),
                                    contentDescription = "Decrease",
                                    modifier = Modifier.size(16.dp),
                                    tint = if (state.sessionsBeforeLongBreak > 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant
                                )
                            }

                            Text(
                                text = state.sessionsBeforeLongBreak.toString(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            IconButton(
                                onClick = {
                                    if (state.sessionsBeforeLongBreak < 12) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onSetSessionsBeforeLongBreak(state.sessionsBeforeLongBreak + 1)
                                    }
                                },
                                enabled = state.sessionsBeforeLongBreak < 12,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_add),
                                    contentDescription = "Increase",
                                    modifier = Modifier.size(16.dp),
                                    tint = if (state.sessionsBeforeLongBreak < 12) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    },
                    shapes = segmentedListItemShapes(0, 2),
                    colors = listItemColors,
                    onClick = {}
                ) {
                    Text(
                        text = stringResource(R.string.label_pomodoros),
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                }
            }

            // 2.2 Long Break Switch Item
            item {
                SegmentedListItem(
                    trailingContent = {
                        Switch(
                            checked = isLongBreakEnabled,
                            onCheckedChange = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                isLongBreakEnabled = it
                            },
                            colors = switchColors
                        )
                    },
                    shapes = segmentedListItemShapes(1, 2),
                    colors = listItemColors,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        isLongBreakEnabled = !isLongBreakEnabled
                    }
                ) {
                    Text(
                        text = stringResource(R.string.label_long_break),
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                }
            }

            item { Spacer(Modifier.height(14.dp)) }

            // ==========================================
            // 3. SECTION: Session preview
            // ==========================================
            item {
                Text(
                    text = stringResource(R.string.section_session_preview),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.padding(start = 6.dp, bottom = 6.dp)
                )
            }

            item {
                SessionPreviewCard(
                    pomodoros = state.sessionsBeforeLongBreak,
                    focusMinutes = state.workDurationMinutes,
                    shortBreakMinutes = state.shortBreakMinutes,
                    longBreakMinutes = state.longBreakMinutes,
                    isLongBreakEnabled = isLongBreakEnabled
                )
            }

            item { Spacer(Modifier.height(18.dp)) }

            // ==========================================
            // 4. SECTION: Automation & Behavior
            // ==========================================
            item {
                Text(
                    text = stringResource(R.string.settings_section_automation),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.padding(start = 6.dp, bottom = 6.dp)
                )
            }

            itemsIndexed(switchItems) { index, item ->
                ListItem(
                    leadingContent = {
                        Icon(
                            painterResource(item.icon),
                            contentDescription = null,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    },
                    supportingContent = { Text(stringResource(item.description)) },
                    trailingContent = {
                        Switch(
                            checked = item.checked,
                            enabled = item.enabled,
                            onCheckedChange = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                item.onClick(it)
                            },
                            colors = switchColors
                        )
                    },
                    colors = listItemColors,
                    modifier = Modifier.clip(
                        when (index) {
                            0 -> topListItemShape
                            switchItems.size - 1 -> bottomListItemShape
                            else -> middleListItemShape
                        }
                    )
                ) {
                    Text(stringResource(item.label))
                }
            }

            item { Spacer(Modifier.height(28.dp)) }
        }
    }
}
