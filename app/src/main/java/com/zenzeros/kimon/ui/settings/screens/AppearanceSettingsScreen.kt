@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui.settings.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenzeros.kimon.R
import com.zenzeros.kimon.ui.settings.SettingsUiState
import com.zenzeros.kimon.ui.theme.ColorPalettePreset
import com.zenzeros.kimon.ui.theme.CustomColors.listItemColors
import com.zenzeros.kimon.ui.theme.CustomColors.switchColors
import com.zenzeros.kimon.ui.theme.CustomColors.topBarColors
import com.zenzeros.kimon.ui.theme.KimonShapeDefaults.cardShape
import com.zenzeros.kimon.ui.theme.LocalAppFonts

@Composable
fun AppearanceSettingsScreen(
    state: SettingsUiState,
    onSetThemeMode: (String) -> Unit,
    onSetThemePalette: (String) -> Unit,
    onToggleAmoledBlack: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_section_appearance),
                        fontFamily = LocalAppFonts.current.topBarTitle,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = onBack,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = listItemColors.containerColor
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_chevron_left),
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = topBarColors
            )
        },
        containerColor = topBarColors.containerColor,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // ==========================================
            // Card 1: Theme (System | Light | Dark)
            // ==========================================
            Surface(
                shape = cardShape,
                color = listItemColors.containerColor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_theme_mode),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = stringResource(R.string.settings_theme_mode),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // 3-Way Segmented Pill Control
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val modes = listOf(
                            "SYSTEM" to stringResource(R.string.theme_system),
                            "LIGHT" to stringResource(R.string.theme_light),
                            "DARK" to stringResource(R.string.theme_dark)
                        )

                        modes.forEach { (modeKey, label) ->
                            val isSelected = state.themeMode == modeKey
                            val animatedBg by animateColorAsState(
                                targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                                animationSpec = tween(durationMillis = 200),
                                label = "theme_pill_bg"
                            )
                            val animatedTextColor by animateColorAsState(
                                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                animationSpec = tween(durationMillis = 200),
                                label = "theme_pill_text"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(animatedBg)
                                    .clickable {
                                        if (!isSelected) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onSetThemeMode(modeKey)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = animatedTextColor
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // Card 2: Dynamic Color
            // ==========================================
            val isDynamic = state.themePalette == "DYNAMIC"
            Surface(
                shape = cardShape,
                color = listItemColors.containerColor,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSetThemePalette(if (isDynamic) "PEACH" else "DYNAMIC")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_dynamic_color),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.settings_dynamic_color),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.settings_dynamic_color_desc),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 12.5.sp,
                                lineHeight = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = isDynamic,
                        onCheckedChange = { checked ->
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSetThemePalette(if (checked) "DYNAMIC" else "PEACH")
                        },
                        colors = switchColors
                    )
                }
            }

            // ==========================================
            // Card 3: Color Scheme Palette Selection
            // ==========================================
            Surface(
                shape = cardShape,
                color = listItemColors.containerColor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_palette),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = stringResource(R.string.settings_color_scheme),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (state.themePalette == "DYNAMIC") "Dynamic" else (ColorPalettePreset.entries.find { it.key == state.themePalette }?.displayName ?: state.themePalette),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 12.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 5 Color Swatches Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ColorPalettePreset.entries.forEach { preset ->
                            val isSelected = state.themePalette == preset.key
                            val swatchBorderColor by animateColorAsState(
                                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                animationSpec = tween(durationMillis = 200),
                                label = "swatch_border"
                            )

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(preset.swatchColor)
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = swatchBorderColor,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onSetThemePalette(preset.key)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // Card 4: Black Theme (AMOLED)
            // ==========================================
            Surface(
                shape = cardShape,
                color = listItemColors.containerColor,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggleAmoledBlack(!state.amoledBlack)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_contrast),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.settings_amoled_black),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.settings_amoled_black_desc),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 12.5.sp,
                                lineHeight = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = state.amoledBlack,
                        onCheckedChange = { checked ->
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleAmoledBlack(checked)
                        },
                        colors = switchColors
                    )
                }
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}
