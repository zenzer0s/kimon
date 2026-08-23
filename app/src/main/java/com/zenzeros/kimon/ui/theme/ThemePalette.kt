@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

enum class ThemePalette(val displayName: String) {
    DYNAMIC("Dynamic / System"),
    DEFAULT("Default"),
    OCEAN("Ocean Blue"),
    EMERALD("Emerald Green"),
    SUNSET("Sunset Amber"),
    LAVENDER("Lavender Purple")
}

val OceanLightColorScheme = lightColorScheme(
    primary = OceanPrimaryLight,
    onPrimary = OceanOnPrimaryLight,
    primaryContainer = OceanPrimaryContainerLight,
    onPrimaryContainer = OceanOnPrimaryContainerLight,
    secondary = OceanSecondaryLight,
    onSecondary = OceanOnSecondaryLight,
    tertiary = OceanTertiaryLight,
    onTertiary = OceanOnTertiaryLight
)

val OceanDarkColorScheme = darkColorScheme(
    primary = OceanPrimaryDark,
    onPrimary = OceanOnPrimaryDark,
    primaryContainer = OceanPrimaryContainerDark,
    onPrimaryContainer = OceanOnPrimaryContainerDark,
    secondary = OceanSecondaryDark,
    onSecondary = OceanOnSecondaryDark,
    tertiary = OceanTertiaryDark,
    onTertiary = OceanOnTertiaryDark
)

val EmeraldLightColorScheme = lightColorScheme(
    primary = EmeraldPrimaryLight,
    onPrimary = EmeraldOnPrimaryLight,
    primaryContainer = EmeraldPrimaryContainerLight,
    onPrimaryContainer = EmeraldOnPrimaryContainerLight,
    secondary = EmeraldSecondaryLight,
    onSecondary = EmeraldOnSecondaryLight,
    tertiary = EmeraldTertiaryLight,
    onTertiary = EmeraldOnTertiaryLight
)

val EmeraldDarkColorScheme = darkColorScheme(
    primary = EmeraldPrimaryDark,
    onPrimary = EmeraldOnPrimaryDark,
    primaryContainer = EmeraldPrimaryContainerDark,
    onPrimaryContainer = EmeraldOnPrimaryContainerDark,
    secondary = EmeraldSecondaryDark,
    onSecondary = EmeraldOnSecondaryDark,
    tertiary = EmeraldTertiaryDark,
    onTertiary = EmeraldOnTertiaryDark
)

val SunsetLightColorScheme = lightColorScheme(
    primary = SunsetPrimaryLight,
    onPrimary = SunsetOnPrimaryLight,
    primaryContainer = SunsetPrimaryContainerLight,
    onPrimaryContainer = SunsetOnPrimaryContainerLight,
    secondary = SunsetSecondaryLight,
    onSecondary = SunsetOnSecondaryLight,
    tertiary = SunsetTertiaryLight,
    onTertiary = SunsetOnTertiaryLight
)

val SunsetDarkColorScheme = darkColorScheme(
    primary = SunsetPrimaryDark,
    onPrimary = SunsetOnPrimaryDark,
    primaryContainer = SunsetPrimaryContainerDark,
    onPrimaryContainer = SunsetOnPrimaryContainerDark,
    secondary = SunsetSecondaryDark,
    onSecondary = SunsetOnSecondaryDark,
    tertiary = SunsetTertiaryDark,
    onTertiary = SunsetOnTertiaryDark
)

val LavenderLightColorScheme = lightColorScheme(
    primary = LavenderPrimaryLight,
    onPrimary = LavenderOnPrimaryLight,
    primaryContainer = LavenderPrimaryContainerLight,
    onPrimaryContainer = LavenderOnPrimaryContainerLight,
    secondary = LavenderSecondaryLight,
    onSecondary = LavenderOnSecondaryLight,
    tertiary = LavenderTertiaryLight,
    onTertiary = LavenderOnTertiaryLight
)

val LavenderDarkColorScheme = darkColorScheme(
    primary = LavenderPrimaryDark,
    onPrimary = LavenderOnPrimaryDark,
    primaryContainer = LavenderPrimaryContainerDark,
    onPrimaryContainer = LavenderOnPrimaryContainerDark,
    secondary = LavenderSecondaryDark,
    onSecondary = LavenderOnSecondaryDark,
    tertiary = LavenderTertiaryDark,
    onTertiary = LavenderOnTertiaryDark
)

fun getPaletteColorScheme(palette: ThemePalette, darkTheme: Boolean): ColorScheme {
    return when (palette) {
        ThemePalette.DYNAMIC,
        ThemePalette.DEFAULT -> if (darkTheme) darkColorScheme() else lightColorScheme()
        ThemePalette.OCEAN -> if (darkTheme) OceanDarkColorScheme else OceanLightColorScheme
        ThemePalette.EMERALD -> if (darkTheme) EmeraldDarkColorScheme else EmeraldLightColorScheme
        ThemePalette.SUNSET -> if (darkTheme) SunsetDarkColorScheme else SunsetLightColorScheme
        ThemePalette.LAVENDER -> if (darkTheme) LavenderDarkColorScheme else LavenderLightColorScheme
    }
}
