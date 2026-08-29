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
    primary = Color(0xFFE52E2E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF381316),
    onPrimaryContainer = Color(0xFFFFB4AB),
    inversePrimary = Color(0xFFD71921),
    secondary = Color(0xFFE2E2E6),
    onSecondary = Color(0xFF1B1C1F),
    secondaryContainer = Color(0xFF26282E),
    onSecondaryContainer = Color(0xFFF0F2F5),
    tertiary = Color(0xFFD71921),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF381316),
    onTertiaryContainer = Color(0xFFFFDAD6),
    background = Color(0xFF06080A),
    onBackground = Color(0xFFF2F2F5),
    surface = Color(0xFF0E1013),
    onSurface = Color(0xFFF2F2F5),
    surfaceVariant = Color(0xFF222429),
    onSurfaceVariant = Color(0xFFA6A9B0),
    surfaceDim = Color(0xFF06080A),
    surfaceBright = Color(0xFF1E2025),
    surfaceContainerLowest = Color(0xFF06080A),
    surfaceContainerLow = Color(0xFF0D0F12),
    surfaceContainer = Color(0xFF14161A),
    surfaceContainerHigh = Color(0xFF1C1E23),
    surfaceContainerHighest = Color(0xFF25272D),
    outline = Color(0xFF383B42),
    outlineVariant = Color(0xFF26282E),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val NothingLightColorScheme = lightColorScheme(
    primary = Color(0xFFD71921),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),
    inversePrimary = Color(0xFFFFB4AB),
    secondary = Color(0xFF35383F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE6E8ED),
    onSecondaryContainer = Color(0xFF181A1D),
    tertiary = Color(0xFFB3261E),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDAD6),
    onTertiaryContainer = Color(0xFF410002),
    background = Color(0xFFF6F7FA),
    onBackground = Color(0xFF121316),
    surface = Color.White,
    onSurface = Color(0xFF121316),
    surfaceVariant = Color(0xFFE2E4E8),
    onSurfaceVariant = Color(0xFF50535B),
    surfaceDim = Color(0xFFE6E8EC),
    surfaceBright = Color.White,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF5F6F8),
    surfaceContainer = Color(0xFFEDEDF1),
    surfaceContainerHigh = Color(0xFFE5E6EA),
    surfaceContainerHighest = Color(0xFFDBDDDF),
    outline = Color(0xFFB4B7BD),
    outlineVariant = Color(0xFFD3D6DC),
    error = Color(0xFFBA1A1A),
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
