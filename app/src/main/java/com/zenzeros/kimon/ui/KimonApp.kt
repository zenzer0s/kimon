@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zenzeros.kimon.KimonApplication
import com.zenzeros.kimon.R
import com.zenzeros.kimon.ui.analyze.AnalyzeScreen
import com.zenzeros.kimon.ui.components.KimonFloatingToolbar
import com.zenzeros.kimon.ui.components.KimonTab
import com.zenzeros.kimon.ui.focus.FocusScreen
import com.zenzeros.kimon.ui.plan.PlanScreen
import com.zenzeros.kimon.ui.pomodoro.PomodoroViewModel
import com.zenzeros.kimon.ui.pomodoro.TimerStatus
import com.zenzeros.kimon.ui.theme.KimonTheme
import com.zenzeros.kimon.ui.theme.LocalAppFonts
import com.zenzeros.kimon.ui.theme.ThemePalette

@Composable
fun KimonApp(
    palette: ThemePalette = ThemePalette.DYNAMIC,
    dynamicColor: Boolean = true
) {
    val context = LocalContext.current
    val kimonApp = context.applicationContext as KimonApplication
    val pomodoroViewModel: PomodoroViewModel = viewModel(
        factory = PomodoroViewModel.Factory(
            sessionRepository = kimonApp.sessionRepository,
            tagRepository = kimonApp.tagRepository,
            userSettingsRepository = kimonApp.userSettingsRepository
        )
    )
    val pomodoroUiState by pomodoroViewModel.uiState.collectAsStateWithLifecycle()
    val allTags by pomodoroViewModel.allTags.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(KimonTab.FOCUS) }

    KimonTheme(palette = palette, dynamicColor = dynamicColor) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 3.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == KimonTab.PLAN,
                        onClick = { selectedTab = KimonTab.PLAN },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_plan),
                                contentDescription = stringResource(R.string.tab_plan),
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(R.string.tab_plan),
                                fontWeight = if (selectedTab == KimonTab.PLAN) FontWeight.SemiBold else FontWeight.Normal
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

                    NavigationBarItem(
                        selected = selectedTab == KimonTab.FOCUS,
                        onClick = { selectedTab = KimonTab.FOCUS },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_focus),
                                contentDescription = stringResource(R.string.tab_focus),
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(R.string.tab_focus),
                                fontWeight = if (selectedTab == KimonTab.FOCUS) FontWeight.SemiBold else FontWeight.Normal
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

                    NavigationBarItem(
                        selected = selectedTab == KimonTab.ANALYZE,
                        onClick = { selectedTab = KimonTab.ANALYZE },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_analyze),
                                contentDescription = stringResource(R.string.tab_analyze),
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(R.string.tab_analyze),
                                fontWeight = if (selectedTab == KimonTab.ANALYZE) FontWeight.SemiBold else FontWeight.Normal
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
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1. Balanced Top App Header (Kimon Brand + Profile Icon)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Expressive App Brand Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.5.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = LocalAppFonts.current.topBarTitle,
                                    fontSize = 24.sp,
                                    letterSpacing = (-0.3).sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Expressive Brand Accent Dot
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .align(Alignment.Bottom)
                                    .padding(bottom = 4.5.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape
                                    )
                            )
                        }

                        // Profile Action Button with Expressive Circle Shape
                        FilledTonalIconButton(
                            onClick = { /* Profile */ },
                            modifier = Modifier.size(38.dp),
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_profile),
                                contentDescription = stringResource(R.string.profile),
                                modifier = Modifier.size(19.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 2. 2nd Surface Canvas (Top Rounded Corners Only, Full Bottom)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // Content Canvas Area
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            when (selectedTab) {
                                KimonTab.FOCUS -> {
                                    FocusScreen(
                                        remainingSeconds = pomodoroUiState.remainingSeconds,
                                        isRunning = pomodoroUiState.timerStatus == TimerStatus.RUNNING,
                                        selectedTag = pomodoroUiState.selectedTag,
                                        tags = allTags,
                                        onSelectTag = { tag -> pomodoroViewModel.selectTag(tag) },
                                        onCreateTag = { name, colorHex -> pomodoroViewModel.createTag(name, colorHex) },
                                        onStart = { pomodoroViewModel.startTimer(context) },
                                        onPause = { pomodoroViewModel.pauseTimer(context) },
                                        onRestart = { pomodoroViewModel.stopTimer(context) }
                                    )
                                }
                                KimonTab.PLAN -> {
                                    PlanScreen()
                                }
                                KimonTab.ANALYZE -> {
                                    AnalyzeScreen(
                                        onNavigateToFocus = { selectedTab = KimonTab.FOCUS }
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
