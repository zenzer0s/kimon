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
    var black = false

    val topBarColors: TopAppBarColors
        @Composable get() = TopAppBarDefaults.topAppBarColors()

    val detailPaneTopBarColors: TopAppBarColors
        @Composable get() = TopAppBarDefaults.topAppBarColors()

    val listItemColors: ListItemColors
        @Composable get() = ListItemDefaults.segmentedColors()

    val switchColors: SwitchColors
        @Composable get() = SwitchDefaults.colors(
            checkedIconColor = colorScheme.primary,
        )
}
