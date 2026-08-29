@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
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
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme

val colorSchemes = listOf(
    Color(0xfffeb4a7), Color(0xffffb3c0), Color(0xfffcaaff), Color(0xffb9c3ff),
    Color(0xff62d3ff), Color(0xff44d9f1), Color(0xff52dbc9), Color(0xff78dd77),
    Color(0xff9fd75c), Color(0xffc1d02d), Color(0xfffabd00), Color(0xffffb86e),
    Color.White
)

private val NothingDarkColorScheme = darkColorScheme(
    primary = Color(0xFFD71921),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2B2D2F),
    onPrimaryContainer = Color(0xFFF0F2F2),
    inversePrimary = Color(0xFFD71921),
    secondary = Color(0xFFF0F2F2),
    onSecondary = Color(0xFF06080A),
    secondaryContainer = Color(0xFF2B2D2F),
    onSecondaryContainer = Color(0xFFF0F2F2),
    tertiary = Color(0xFFD71921),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF381316),
    onTertiaryContainer = Color(0xFFFFDAD6),
    background = Color(0xFF06080A),
    onBackground = Color(0xFFF0F2F2),
    surface = Color(0xFF06080A),
    onSurface = Color(0xFFF0F2F2),
    surfaceVariant = Color(0xFF1B1D1F),
    onSurfaceVariant = Color(0xFFA6A8AB),
    surfaceDim = Color(0xFF000000),
    surfaceBright = Color(0xFF2B2D2F),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF06080A),
    surfaceContainer = Color(0xFF121417),
    surfaceContainerHigh = Color(0xFF1B1D1F),
    surfaceContainerHighest = Color(0xFF2B2D2F),
    outline = Color(0xFF33353A),
    outlineVariant = Color(0xFF222428),
    error = Color(0xFFD71921),
    onError = Color.White
)

private val NothingLightColorScheme = lightColorScheme(
    primary = Color(0xFFD71921),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFFFFF),
    onPrimaryContainer = Color(0xFF06080A),
    inversePrimary = Color(0xFFFFB4AB),
    secondary = Color(0xFF06080A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE7E9E9),
    onSecondaryContainer = Color(0xFF06080A),
    tertiary = Color(0xFFD71921),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDAD6),
    onTertiaryContainer = Color(0xFF410002),
    background = Color(0xFFF0F2F2),
    onBackground = Color(0xFF06080A),
    surface = Color(0xFFF0F2F2),
    onSurface = Color(0xFF06080A),
    surfaceVariant = Color(0xFFE7E9E9),
    onSurfaceVariant = Color(0xFF44464D),
    surfaceDim = Color(0xFFD7D8D8),
    surfaceBright = Color.White,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF0F2F2),
    surfaceContainer = Color(0xFFE7E9E9),
    surfaceContainerHigh = Color(0xFFFFFFFF),
    surfaceContainerHighest = Color(0xFFD7D8D8),
    outline = Color(0xFFC8C8C8),
    outlineVariant = Color(0xFFD7D8D8),
    error = Color(0xFFD71921),
    onError = Color.White
)

@Composable
fun KimonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    seedColor: Color = Color.White,
    dynamicColor: Boolean = true,
    blackTheme: Boolean = false,
    nothingOsTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isAmoled = blackTheme && darkTheme

    CustomColors.isDark = darkTheme
    CustomColors.black = isAmoled
    CustomColors.isNothingOs = nothingOsTheme

    val baseScheme: ColorScheme = when {
        nothingOsTheme -> {
            if (darkTheme) NothingDarkColorScheme else NothingLightColorScheme
        }
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    val dynamicColorScheme = rememberDynamicColorScheme(
        seedColor = when (seedColor) {
            Color.White -> baseScheme.primary
            else -> seedColor
        },
        isDark = darkTheme,
        specVersion = if (isAmoled) ColorSpec.SpecVersion.SPEC_2021 else ColorSpec.SpecVersion.SPEC_2025,
        isAmoled = isAmoled
    )

    val baseFinalScheme =
        if (nothingOsTheme) baseScheme
        else if (seedColor == Color.White && !isAmoled) baseScheme
        else dynamicColorScheme

    val scheme = if (isAmoled) {
        baseFinalScheme.copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceDim = Color.Black,
            surfaceBright = Color(0xFF181818),
            surfaceContainerLowest = Color.Black,
            surfaceContainerLow = Color.Black,
            surfaceContainer = Color.Black,
            surfaceContainerHigh = Color(0xFF141414),
            surfaceContainerHighest = Color(0xFF1E1E1E)
        )
    } else {
        baseFinalScheme
    }

    CompositionLocalProvider(
        LocalAppFonts provides getAppFonts(isNothingOs = nothingOsTheme)
    ) {
        MaterialExpressiveTheme(
            colorScheme = scheme,
            typography = typography(isNothingOs = nothingOsTheme),
            motionScheme = MotionScheme.expressive(),
            content = content
        )
    }
}
