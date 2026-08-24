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
import androidx.compose.ui.graphics.Color

object CustomColors {
    var isDark = false
    var black = false

    val topBarColors: TopAppBarColors
        @Composable get() =
            TopAppBarDefaults.topAppBarColors(
                containerColor = if (black) colorScheme.surface else colorScheme.surfaceContainer,
                scrolledContainerColor = if (black) colorScheme.surface else colorScheme.surfaceContainer
            )

    val detailPaneTopBarColors: TopAppBarColors
        @Composable get() =
            TopAppBarDefaults.topAppBarColors(
                containerColor = if (black) colorScheme.surface else colorScheme.surfaceContainer,
                scrolledContainerColor = if (black) colorScheme.surface else colorScheme.surfaceContainer
            )

    val cardContainerColor: Color
        @Composable get() =
            if (isDark) colorScheme.surfaceContainerHigh else colorScheme.surfaceContainerLowest

    val listItemColors: ListItemColors
        @Composable get() =
            ListItemDefaults.segmentedColors(
                containerColor = cardContainerColor,
                disabledContainerColor = cardContainerColor,
                selectedContainerColor = colorScheme.surfaceContainerLow,
                contentColor = colorScheme.onSurface,
                leadingContentColor = colorScheme.primary,
                trailingContentColor = colorScheme.onSurfaceVariant,
                supportingContentColor = colorScheme.onSurfaceVariant
            )

    val switchColors: SwitchColors
        @Composable get() = SwitchDefaults.colors(
            checkedIconColor = colorScheme.primary,
        )
}
