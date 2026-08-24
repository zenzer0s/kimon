@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
fun KimonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    blackTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    CustomColors.black = blackTheme && darkTheme

    // 1. Official Google Material 3 Dynamic / Baseline Color Scheme
    val baseScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    // 2. Official AMOLED Pure Black adaptation if enabled in dark mode
    val colorScheme = if (blackTheme && darkTheme) {
        baseScheme.copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceDim = Color.Black,
            surfaceContainerLowest = Color.Black,
            surfaceContainerLow = Color(0xFF0C0C0C),
            surfaceContainer = Color(0xFF141414),
            surfaceContainerHigh = Color(0xFF1E1E1E),
            surfaceContainerHighest = Color(0xFF282828)
        )
    } else {
        baseScheme
    }

    CompositionLocalProvider(
        LocalAppFonts provides getAppFonts()
    ) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            typography = typography(),
            motionScheme = MotionScheme.expressive(),
            content = content
        )
    }
}