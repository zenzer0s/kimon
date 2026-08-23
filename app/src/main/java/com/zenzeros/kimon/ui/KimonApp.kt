package com.zenzeros.kimon.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.zenzeros.kimon.ui.theme.KimonTheme
import com.zenzeros.kimon.ui.theme.ThemePalette

@Composable
fun KimonApp(
    palette: ThemePalette = ThemePalette.DEFAULT
) {
    KimonTheme(palette = palette) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // App content / Navigation host goes here
        }
    }
}
