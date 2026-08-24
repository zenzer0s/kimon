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

@Composable
fun KimonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    seedColor: Color = Color.White,
    dynamicColor: Boolean = true,
    blackTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    CustomColors.isDark = darkTheme
    CustomColors.black = blackTheme && darkTheme

    val baseScheme: ColorScheme = when {
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
        specVersion = if (blackTheme && darkTheme) ColorSpec.SpecVersion.SPEC_2021 else ColorSpec.SpecVersion.SPEC_2025,
        isAmoled = blackTheme && darkTheme
    )

    val scheme =
        if (seedColor == Color.White && !(blackTheme && darkTheme)) baseScheme
        else dynamicColorScheme

    CompositionLocalProvider(
        LocalAppFonts provides getAppFonts()
    ) {
        MaterialExpressiveTheme(
            colorScheme = scheme,
            typography = typography(),
            motionScheme = MotionScheme.expressive(),
            content = content
        )
    }
}
