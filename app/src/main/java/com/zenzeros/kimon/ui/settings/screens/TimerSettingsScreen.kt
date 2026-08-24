@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui.settings.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenzeros.kimon.R
import com.zenzeros.kimon.ui.settings.SettingsSwitchItem
import com.zenzeros.kimon.ui.settings.SettingsUiState
import com.zenzeros.kimon.ui.settings.components.MinuteInputField
import com.zenzeros.kimon.ui.settings.components.MinutesInputTransformation3Digits
import com.zenzeros.kimon.ui.settings.components.SliderListItem
import com.zenzeros.kimon.ui.theme.CustomColors.listItemColors
import com.zenzeros.kimon.ui.theme.CustomColors.switchColors
import com.zenzeros.kimon.ui.theme.CustomColors.topBarColors
import com.zenzeros.kimon.ui.theme.KimonShapeDefaults.PANE_MAX_WIDTH
import com.zenzeros.kimon.ui.theme.KimonShapeDefaults.bottomListItemShape
import com.zenzeros.kimon.ui.theme.KimonShapeDefaults.cardShape
import com.zenzeros.kimon.ui.theme.KimonShapeDefaults.middleListItemShape
import com.zenzeros.kimon.ui.theme.KimonShapeDefaults.topListItemShape
import com.zenzeros.kimon.ui.theme.LocalAppFonts

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
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val focusTimeState = rememberTextFieldState(state.workDurationMinutes.toString())
    val shortBreakState = rememberTextFieldState(state.shortBreakMinutes.toString())
    val longBreakState = rememberTextFieldState(state.longBreakMinutes.toString())

    LaunchedEffect(focusTimeState.text) {
        val num = focusTimeState.text.toString().toIntOrNull()
        if (num != null && num != state.workDurationMinutes) {
            onSetWorkDuration(num)
        }
    }

    LaunchedEffect(shortBreakState.text) {
        val num = shortBreakState.text.toString().toIntOrNull()
        if (num != null && num != state.shortBreakMinutes) {
            onSetShortBreak(num)
        }
    }

    LaunchedEffect(longBreakState.text) {
        val num = longBreakState.text.toString().toIntOrNull()
        if (num != null && num != state.longBreakMinutes) {
            onSetLongBreak(num)
        }
    }

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
                        text = stringResource(R.string.settings_section_timer),
                        fontFamily = LocalAppFonts.current.topBarTitle,
                        fontSize = 22.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                },
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = onBack,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = listItemColors.containerColor
                        )
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
            item {
                Spacer(Modifier.height(4.dp))
            }

                // 1. Triple Minute Input Field (Focus, Short Break, Long Break)
                item {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                stringResource(R.string.mode_focus),
                                style = typography.titleSmall
                            )
                            MinuteInputField(
                                state = focusTimeState,
                                enabled = true,
                                shape = RoundedCornerShape(
                                    topStart = topListItemShape.topStart,
                                    bottomStart = topListItemShape.topStart,
                                    topEnd = topListItemShape.bottomStart,
                                    bottomEnd = topListItemShape.bottomStart
                                ),
                                inputTransformation = MinutesInputTransformation3Digits,
                                imeAction = ImeAction.Next
                            )
                        }

                        Spacer(Modifier.width(4.dp))

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                stringResource(R.string.mode_short_break),
                                style = typography.titleSmall
                            )
                            MinuteInputField(
                                state = shortBreakState,
                                enabled = true,
                                shape = RoundedCornerShape(middleListItemShape.topStart),
                                imeAction = ImeAction.Next
                            )
                        }

                        Spacer(Modifier.width(4.dp))

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                stringResource(R.string.mode_long_break),
                                style = typography.titleSmall
                            )
                            MinuteInputField(
                                state = longBreakState,
                                enabled = true,
                                shape = RoundedCornerShape(
                                    topStart = bottomListItemShape.topStart,
                                    bottomStart = bottomListItemShape.topStart,
                                    topEnd = bottomListItemShape.bottomStart,
                                    bottomEnd = bottomListItemShape.bottomStart
                                ),
                                imeAction = ImeAction.Done
                            )
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(14.dp))
                }

                // 2. Session Length Slider
                item {
                    Column(Modifier.background(listItemColors.containerColor, topListItemShape)) {
                        ListItem(
                            leadingContent = {
                                Icon(painterResource(R.drawable.ic_bar_chart), null)
                            },
                            headlineContent = {
                                Text(stringResource(R.string.settings_long_break_interval))
                            },
                            supportingContent = {
                                Text("Every ${state.sessionsBeforeLongBreak} sessions")
                            },
                            colors = listItemColors,
                            modifier = Modifier.clip(cardShape)
                        )
                        Slider(
                            value = state.sessionsBeforeLongBreak.toFloat(),
                            valueRange = 1f..8f,
                            steps = 6,
                            onValueChange = { onSetSessionsBeforeLongBreak(it.toInt()) },
                            modifier = Modifier.padding(start = 56.dp, end = 16.dp, bottom = 12.dp)
                        )
                    }
                }

                // 3. Daily Focus Goal Slider
                item {
                    SliderListItem(
                        value = state.dailyGoalMinutes.toFloat(),
                        valueRange = 0f..480f,
                        enabled = true,
                        label = stringResource(R.string.settings_daily_goal),
                        trailingLabel = { mins ->
                            val m = mins.toInt()
                            "${m / 60}h ${m % 60}m"
                        },
                        icon = { Icon(painterResource(R.drawable.ic_target), null) },
                        shape = bottomListItemShape,
                        onValueChangeFinished = { onSetDailyGoal(it.toInt()) }
                    )
                }

                item { Spacer(Modifier.height(14.dp)) }

                // 4. Automation Switch Items
                itemsIndexed(switchItems) { index, item ->
                    ListItem(
                        leadingContent = {
                            Icon(
                                painterResource(item.icon),
                                contentDescription = null,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        },
                        headlineContent = { Text(stringResource(item.label)) },
                        supportingContent = { Text(stringResource(item.description)) },
                        trailingContent = {
                            Switch(
                                checked = item.checked,
                                enabled = item.enabled,
                                onCheckedChange = { item.onClick(it) },
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
                    )
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }
}
