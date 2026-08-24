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
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val LocalIsDarkTheme = staticCompositionLocalOf { false }
val LocalIsAmoledBlack = staticCompositionLocalOf { false }

enum class ColorPalettePreset(val key: String, val displayName: String, val swatchColor: Color) {
    PEACH("PEACH", "Peach", Color(0xFFFCA5A5)),
    ROSE("ROSE", "Rose", Color(0xFFF472B6)),
    LAVENDER("LAVENDER", "Lavender", Color(0xFFC084FC)),
    INDIGO("INDIGO", "Indigo", Color(0xFF818CF8)),
    SKY("SKY", "Sky", Color(0xFF38BDF8))
}

@Composable
fun KimonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    palette: String = "DYNAMIC",
    blackTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isAmoled = blackTheme && darkTheme

    CustomColors.isDark = darkTheme
    CustomColors.black = isAmoled

    val baseScheme: ColorScheme = when {
        dynamicColor && palette == "DYNAMIC" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        palette == "PEACH" -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFFFA07A),
                onPrimary = Color(0xFF4D1C00),
                primaryContainer = Color(0xFF702C00),
                onPrimaryContainer = Color(0xFFFFDBCF),
                secondary = Color(0xFFE7BEAF),
                surface = Color(0xFF1B1210),
                surfaceContainer = Color(0xFF261916),
                surfaceContainerHigh = Color(0xFF32211C)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF984715),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFFFDBCF),
                onPrimaryContainer = Color(0xFF361000),
                secondary = Color(0xFF77574B),
                surface = Color(0xFFFFF8F6),
                surfaceContainer = Color(0xFFF7EBE7),
                surfaceContainerHigh = Color(0xFFF1E5E1)
            )
        }
        palette == "ROSE" -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFFFB0CD),
                onPrimary = Color(0xFF5E1134),
                primaryContainer = Color(0xFF7B294A),
                onPrimaryContainer = Color(0xFFFFD9E2),
                secondary = Color(0xFFE5BDC7),
                surface = Color(0xFF1B1114),
                surfaceContainer = Color(0xFF26171D),
                surfaceContainerHigh = Color(0xFF321E26)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF984061),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFFFD9E2),
                onPrimaryContainer = Color(0xFF3E001F),
                secondary = Color(0xFF74565F),
                surface = Color(0xFFFFF8F8),
                surfaceContainer = Color(0xFFF8ECEF),
                surfaceContainerHigh = Color(0xFFF2E6E9)
            )
        }
        palette == "LAVENDER" -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFDAB9FF),
                onPrimary = Color(0xFF401878),
                primaryContainer = Color(0xFF583290),
                onPrimaryContainer = Color(0xFFEEDCFF),
                secondary = Color(0xFFCCC2DB),
                surface = Color(0xFF16121D),
                surfaceContainer = Color(0xFF211B2A),
                surfaceContainerHigh = Color(0xFF2C2437)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF714CA8),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFEEDCFF),
                onPrimaryContainer = Color(0xFF2B0053),
                secondary = Color(0xFF645B70),
                surface = Color(0xFFFBF8FD),
                surfaceContainer = Color(0xFFF3EDF7),
                surfaceContainerHigh = Color(0xFFEDE7F2)
            )
        }
        palette == "INDIGO" -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFBAC3FF),
                onPrimary = Color(0xFF1E2778),
                primaryContainer = Color(0xFF363E8F),
                onPrimaryContainer = Color(0xFFDFE1FF),
                secondary = Color(0xFFC4C5DD),
                surface = Color(0xFF131318),
                surfaceContainer = Color(0xFF1D1E24),
                surfaceContainerHigh = Color(0xFF27282F)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF4F57A9),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFDFE1FF),
                onPrimaryContainer = Color(0xFF040B5F),
                secondary = Color(0xFF5B5D72),
                surface = Color(0xFFFBF8FD),
                surfaceContainer = Color(0xFFEEEDF4),
                surfaceContainerHigh = Color(0xFFE8E7EE)
            )
        }
        palette == "SKY" -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFF86D2F9),
                onPrimary = Color(0xFF003548),
                primaryContainer = Color(0xFF004D67),
                onPrimaryContainer = Color(0xFFBEE9FF),
                secondary = Color(0xFFB4CAD6),
                surface = Color(0xFF0F1417),
                surfaceContainer = Color(0xFF172025),
                surfaceContainerHigh = Color(0xFF202A30)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF006686),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFBEE9FF),
                onPrimaryContainer = Color(0xFF001F2B),
                secondary = Color(0xFF4E616C),
                surface = Color(0xFFF6FAFD),
                surfaceContainer = Color(0xFFEAF1F6),
                surfaceContainerHigh = Color(0xFFE3EBEF)
            )
        }
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    val colorScheme = if (isAmoled) {
        baseScheme.copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceDim = Color.Black,
            surfaceContainerLowest = Color.Black,
            surfaceContainerLow = Color(0xFF0A0A0A),
            surfaceContainer = Color(0xFF121212),
            surfaceContainerHigh = Color(0xFF1A1A1A),
            surfaceContainerHighest = Color(0xFF242424)
        )
    } else {
        baseScheme
    }

    CompositionLocalProvider(
        LocalAppFonts provides getAppFonts(),
        LocalIsDarkTheme provides darkTheme,
        LocalIsAmoledBlack provides isAmoled
    ) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            typography = typography(),
            motionScheme = MotionScheme.expressive(),
            content = content
        )
    }
}
