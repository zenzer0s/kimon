@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zenzeros.kimon.ui.theme.KimonTheme
import com.zenzeros.kimon.ui.theme.ThemePalette

@Composable
fun KimonApp(
    palette: ThemePalette = ThemePalette.DYNAMIC,
    dynamicColor: Boolean = true
) {
    KimonTheme(palette = palette, dynamicColor = dynamicColor) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            // App content / Navigation host goes here
        }
    }
}
