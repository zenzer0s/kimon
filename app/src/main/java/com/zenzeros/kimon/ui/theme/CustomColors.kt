@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable

object CustomColors {
    var isDark = false
    var black = false

    val topBarColors: TopAppBarColors
        @Composable get() =
            TopAppBarDefaults.topAppBarColors(
                containerColor = if (!black) colorScheme.surfaceContainer else colorScheme.surface,
                scrolledContainerColor = if (!black) colorScheme.surfaceContainer else colorScheme.surface
            )

    val detailPaneTopBarColors: TopAppBarColors
        @Composable get() =
            TopAppBarDefaults.topAppBarColors(
                containerColor = if (!black) colorScheme.surfaceContainerLow else colorScheme.surface,
                scrolledContainerColor = if (!black) colorScheme.surfaceContainerLow else colorScheme.surface
            )

    val cardContainerColor: androidx.compose.ui.graphics.Color
        @Composable get() = if (!black) colorScheme.surfaceBright else colorScheme.surfaceContainerHigh

    val innerCardContainerColor: androidx.compose.ui.graphics.Color
        @Composable get() = if (!black) colorScheme.surfaceContainerLow else colorScheme.surfaceContainer

    val innerCardBorderColor: androidx.compose.ui.graphics.Color
        @Composable get() = if (!black) colorScheme.outlineVariant.copy(alpha = 0.35f) else colorScheme.outlineVariant.copy(alpha = 0.2f)

    val listItemColors: ListItemColors
        @Composable get() =
            ListItemDefaults.segmentedColors(
                containerColor = if (!black) colorScheme.surfaceBright else colorScheme.surfaceContainerHigh,
                disabledContainerColor = if (!black) colorScheme.surfaceBright else colorScheme.surfaceContainerHigh,
            )

    val switchColors: SwitchColors
        @Composable get() = SwitchDefaults.colors(
            checkedIconColor = colorScheme.primary,
        )
}
