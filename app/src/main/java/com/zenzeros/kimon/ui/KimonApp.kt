@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.zenzeros.kimon.KimonApplication
import com.zenzeros.kimon.R
import com.zenzeros.kimon.ui.analyze.AnalyzeScreen
import com.zenzeros.kimon.ui.focus.FocusScreen
import com.zenzeros.kimon.ui.navigation.KimonNavKey
import com.zenzeros.kimon.ui.plan.PlanScreen
import com.zenzeros.kimon.ui.pomodoro.PomodoroViewModel
import com.zenzeros.kimon.ui.pomodoro.TimerStatus
import com.zenzeros.kimon.ui.settings.SettingsViewModel
import com.zenzeros.kimon.ui.settings.screens.AboutSettingsScreen
import com.zenzeros.kimon.ui.settings.screens.AlarmSettingsScreen
import com.zenzeros.kimon.ui.settings.screens.AppearanceSettingsScreen
import com.zenzeros.kimon.ui.settings.screens.BackupSettingsScreen
import com.zenzeros.kimon.ui.settings.screens.SettingsMainScreen
import com.zenzeros.kimon.ui.settings.screens.SleepSettingsScreen
import com.zenzeros.kimon.ui.settings.screens.TimerSettingsScreen
import com.zenzeros.kimon.ui.sleep.SleepScreen
import com.zenzeros.kimon.ui.theme.CustomColors
import com.zenzeros.kimon.ui.theme.KimonTheme
import com.zenzeros.kimon.ui.theme.LocalAppFonts
import kotlinx.coroutines.launch

private enum class KimonTab(val titleRes: Int, val iconRes: Int) {
    FOCUS(R.string.tab_focus, R.drawable.ic_focus),
    ANALYZE(R.string.tab_analyze, R.drawable.ic_analyze),
    PLAN(R.string.tab_plan, R.drawable.ic_plan),
    SLEEP(R.string.tab_sleep, R.drawable.ic_moon)
}

@Composable
fun KimonApp() {
    val context = LocalContext.current
    val kimonApp = context.applicationContext as KimonApplication
    val coroutineScope = rememberCoroutineScope()

    val pomodoroViewModel: PomodoroViewModel = viewModel(
        factory = PomodoroViewModel.Factory(
            appContext = kimonApp.applicationContext,
            sessionRepository = kimonApp.sessionRepository,
            tagRepository = kimonApp.tagRepository,
            userSettingsRepository = kimonApp.userSettingsRepository
        )
    )
    val pomodoroUiState by pomodoroViewModel.uiState.collectAsStateWithLifecycle()
    val allTags by pomodoroViewModel.allTags.collectAsStateWithLifecycle()

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(
            application = kimonApp,
            userSettingsRepository = kimonApp.userSettingsRepository,
            sessionRepository = kimonApp.sessionRepository,
            tagRepository = kimonApp.tagRepository,
            taskRepository = kimonApp.taskRepository
        )
    )
    val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    val isDark = when (settingsState.themeMode) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemInDarkTheme()
    }

    CustomColors.black = isDark && settingsState.amoledBlack

    val activity = context as? Activity
    DisposableEffect(settingsState.keepScreenOn, pomodoroUiState.timerStatus) {
        if (settingsState.keepScreenOn && pomodoroUiState.timerStatus == TimerStatus.RUNNING) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Live sync timer with wall-clock when screen turns on / app returns to foreground
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME || event == Lifecycle.Event.ON_START) {
                pomodoroViewModel.syncWithWallClock(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Dynamic Root Tabs: Focus > Analyze > Plan > Sleep (when enabled)
    val enabledTabs = remember(settingsState.sleepMonitoringEnabled) {
        if (settingsState.sleepMonitoringEnabled) {
            listOf(KimonTab.FOCUS, KimonTab.ANALYZE, KimonTab.PLAN, KimonTab.SLEEP)
        } else {
            listOf(KimonTab.FOCUS, KimonTab.ANALYZE, KimonTab.PLAN)
        }
    }

    val mainTabPagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { enabledTabs.size }
    )

    // Settings Navigation Stack
    val settingsBackStack = rememberNavBackStack()
    val isSettingsOpen = settingsBackStack.isNotEmpty()

    val navigateBack: () -> Unit = {
        if (settingsBackStack.size > 1) {
            settingsBackStack.removeAt(settingsBackStack.lastIndex)
        } else {
            settingsBackStack.clear()
        }
    }

    // Handle system back press when Settings is open
    BackHandler(enabled = isSettingsOpen) {
        navigateBack()
    }

    // Handle back press from other tabs to return to Focus
    BackHandler(enabled = !isSettingsOpen && mainTabPagerState.currentPage != 0) {
        coroutineScope.launch {
            mainTabPagerState.scrollToPage(0)
        }
    }

    // Dedicated Settings Entry Provider
    val settingsEntryProvider = remember(settingsState) {
        entryProvider<NavKey> {
            entry<KimonNavKey.Focus> { Box(Modifier.fillMaxSize()) }
            entry<KimonNavKey.Plan> { Box(Modifier.fillMaxSize()) }
            entry<KimonNavKey.Analyze> { Box(Modifier.fillMaxSize()) }
            entry<KimonNavKey.SettingsMain> {
                SettingsMainScreen(
                    state = settingsState,
                    onNavigate = { key -> settingsBackStack.add(key) },
                    onBack = navigateBack
                )
            }
            entry<KimonNavKey.TimerSettings> {
                TimerSettingsScreen(
                    state = settingsState,
                    onSetWorkDuration = { settingsViewModel.setWorkDuration(it) },
                    onSetShortBreak = { settingsViewModel.setShortBreak(it) },
                    onSetLongBreak = { settingsViewModel.setLongBreak(it) },
                    onSetSessionsBeforeLongBreak = { settingsViewModel.setSessionsBeforeLongBreak(it) },
                    onSetDailyGoal = { settingsViewModel.setDailyGoal(it) },
                    onSetClockStyle = { settingsViewModel.setClockStyle(it) },
                    onToggleAutoStartBreaks = { settingsViewModel.toggleAutoStartBreaks(it) },
                    onToggleAutoStartPomodoros = { settingsViewModel.toggleAutoStartPomodoros(it) },
                    onToggleKeepScreenOn = { settingsViewModel.toggleKeepScreenOn(it) },
                    onToggleDnd = { settingsViewModel.toggleDnd(it) },
                    onBack = navigateBack
                )
            }
            entry<KimonNavKey.AlarmSettings> {
                AlarmSettingsScreen(
                    state = settingsState,
                    onToggleSound = { settingsViewModel.toggleSound(it) },
                    onToggleVibration = { settingsViewModel.toggleVibration(it) },
                    onToggleMediaVolume = { settingsViewModel.toggleMediaVolume(it) },
                    onSetAlarmSound = { uri, title -> settingsViewModel.setAlarmSound(uri, title) },
                    onBack = navigateBack
                )
            }
            entry<KimonNavKey.AppearanceSettings> {
                AppearanceSettingsScreen(
                    state = settingsState,
                    onSetThemeMode = { settingsViewModel.setThemeMode(it) },
                    onSetThemeColor = { settingsViewModel.setThemeColor(it) },
                    onToggleAmoledBlack = { settingsViewModel.toggleAmoledBlack(it) },
                    onToggleNothingOsTheme = { settingsViewModel.toggleNothingOsTheme(it) },
                    onBack = navigateBack
                )
            }
            entry<KimonNavKey.SleepSettings> {
                SleepSettingsScreen(
                    state = settingsState,
                    onToggleSleepMonitoring = { settingsViewModel.toggleSleepMonitoring(it) },
                    onToggleHealthConnectSync = { settingsViewModel.toggleHealthConnectSync(it) },
                    onSetSleepGoal = { settingsViewModel.setSleepGoal(it) },
                    onToggleScheduledMode = { settingsViewModel.toggleSleepScheduledMode(it) },
                    onSetBedtime = { h, m -> settingsViewModel.setTargetBedtime(h, m) },
                    onSetWakeTime = { h, m -> settingsViewModel.setTargetWakeTime(h, m) },
                    onToggleAppUsageAccess = { settingsViewModel.toggleAppUsageAccess(it) },
                    onBack = navigateBack
                )
            }
            entry<KimonNavKey.BackupSettings> {
                BackupSettingsScreen(
                    onBack = navigateBack
                )
            }
            entry<KimonNavKey.AboutSettings> {
                AboutSettingsScreen(
                    onBack = navigateBack
                )
            }
        }
    }

    KimonTheme(
        darkTheme = isDark,
        seedColor = settingsState.themeColor,
        dynamicColor = settingsState.themeColor == androidx.compose.ui.graphics.Color.White,
        blackTheme = settingsState.amoledBlack,
        nothingOsTheme = settingsState.nothingOsTheme
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                AnimatedVisibility(
                    visible = !isSettingsOpen,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    NavigationBar {
                        enabledTabs.forEachIndexed { index, tab ->
                            NavigationBarItem(
                                selected = mainTabPagerState.currentPage == index,
                                onClick = {
                                    if (mainTabPagerState.currentPage != index) {
                                        coroutineScope.launch {
                                            mainTabPagerState.scrollToPage(index)
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        painter = painterResource(tab.iconRes),
                                        contentDescription = stringResource(tab.titleRes),
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = stringResource(tab.titleRes),
                                        fontWeight = if (mainTabPagerState.currentPage == index) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                // 1. Persistent Main Content (Zero allocation / destruction when opening settings)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.statusBars)
                ) {
                    // 1. Balanced Top App Header (Kimon Brand + Settings/Profile Icon)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Expressive App Brand Header
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = LocalAppFonts.current.topBarTitle,
                                fontSize = 24.sp,
                                letterSpacing = (-0.3).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Settings Action Button with Expressive Circle Shape
                        FilledTonalIconButton(
                            onClick = {
                                settingsBackStack.add(KimonNavKey.SettingsMain)
                            },
                            modifier = Modifier.size(38.dp),
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_settings),
                                contentDescription = stringResource(R.string.title_settings),
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 2. Pre-warmed Main Root Content Tabs (Dynamic: Plan, Focus, Analyze + optional Sleep)
                    HorizontalPager(
                        state = mainTabPagerState,
                        beyondViewportPageCount = 3,
                        userScrollEnabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) { page ->
                        when (enabledTabs.getOrNull(page)) {
                            KimonTab.SLEEP -> SleepScreen(
                                onNavigateToSettings = {
                                    settingsBackStack.add(KimonNavKey.SleepSettings)
                                }
                            )
                            KimonTab.PLAN -> PlanScreen()
                            KimonTab.FOCUS -> FocusScreen(
                                remainingSeconds = pomodoroUiState.remainingSeconds,
                                timerStatus = pomodoroUiState.timerStatus,
                                clockStyle = settingsState.clockStyle,
                                selectedTag = pomodoroUiState.selectedTag,
                                tags = allTags,
                                onSelectTag = { tag -> pomodoroViewModel.selectTag(tag) },
                                onCreateTag = { name, colorHex -> pomodoroViewModel.createTag(name, colorHex) },
                                onDeleteTag = { tag -> pomodoroViewModel.deleteTag(tag) },
                                onStart = { pomodoroViewModel.startTimer(context) },
                                onPause = { pomodoroViewModel.pauseTimer(context) },
                                onRestart = { pomodoroViewModel.stopTimer(context) }
                            )
                            KimonTab.ANALYZE -> AnalyzeScreen(
                                onNavigateToFocus = {
                                    val focusIndex = enabledTabs.indexOf(KimonTab.FOCUS).coerceAtLeast(0)
                                    coroutineScope.launch {
                                        mainTabPagerState.scrollToPage(focusIndex)
                                    }
                                }
                            )
                            null -> {}
                        }
                    }
                }

                // 2. Instant Settings Overlay (Slides in without tearing down main tabs)
                AnimatedVisibility(
                    visible = isSettingsOpen,
                    enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                    exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {}
                            )
                    ) {
                        if (settingsBackStack.isNotEmpty()) {
                            NavDisplay(
                                backStack = settingsBackStack,
                                entryProvider = settingsEntryProvider,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}
