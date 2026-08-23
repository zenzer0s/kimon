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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenzeros.kimon.R
import com.zenzeros.kimon.ui.components.KimonFloatingToolbar
import com.zenzeros.kimon.ui.components.KimonTab
import com.zenzeros.kimon.ui.focus.FocusScreen
import com.zenzeros.kimon.ui.plan.PlanScreen
import com.zenzeros.kimon.ui.theme.KimonTheme
import com.zenzeros.kimon.ui.theme.LocalAppFonts
import com.zenzeros.kimon.ui.theme.ThemePalette
import kotlinx.coroutines.delay

@Composable
fun KimonApp(
    palette: ThemePalette = ThemePalette.DYNAMIC,
    dynamicColor: Boolean = true
) {
    var selectedTab by remember { mutableStateOf(KimonTab.FOCUS) }
    var remainingSeconds by remember { mutableIntStateOf(25 * 60) }
    var isRunning by remember { mutableStateOf(false) }

    // Countdown timer active when running
    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (isRunning && remainingSeconds > 0) {
                delay(1000L)
                remainingSeconds--
            }
            if (remainingSeconds == 0) {
                isRunning = false
            }
        }
    }

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
                                contentDescription = "Plan",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "Plan",
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
                                contentDescription = "Focus",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "Focus",
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
                                contentDescription = "Analyze",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "Analyze",
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
                                text = "Kimon",
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
                                contentDescription = "Profile",
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
                                        remainingSeconds = remainingSeconds,
                                        isRunning = isRunning,
                                        onStart = { isRunning = true },
                                        onPause = { isRunning = false },
                                        onRestart = {
                                            remainingSeconds = 25 * 60
                                            isRunning = false
                                        }
                                    )
                                }
                                KimonTab.PLAN -> {
                                    PlanScreen()
                                }
                                KimonTab.ANALYZE -> {
                                    // Analyze Tab Canvas Placeholder
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
