@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui.settings.screens

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenzeros.kimon.R
import com.zenzeros.kimon.ui.settings.SettingsSwitchItem
import com.zenzeros.kimon.ui.settings.SettingsUiState
import com.zenzeros.kimon.ui.settings.components.ColorSchemePickerCard
import com.zenzeros.kimon.ui.settings.components.ThemePickerListItem
import com.zenzeros.kimon.ui.theme.CustomColors
import com.zenzeros.kimon.ui.theme.CustomColors.cardBorder
import com.zenzeros.kimon.ui.theme.CustomColors.listItemColors
import com.zenzeros.kimon.ui.theme.CustomColors.switchColors
import com.zenzeros.kimon.ui.theme.CustomColors.topBarColors
import com.zenzeros.kimon.ui.theme.KimonShapeDefaults.bottomListItemShape
import com.zenzeros.kimon.ui.theme.KimonShapeDefaults.middleListItemShape
import com.zenzeros.kimon.ui.theme.KimonShapeDefaults.topListItemShape
import com.zenzeros.kimon.ui.theme.LocalAppFonts
import com.zenzeros.kimon.ui.theme.colorSchemes

@Composable
fun AppearanceSettingsScreen(
    state: SettingsUiState,
    onSetThemeMode: (String) -> Unit,
    onSetThemeColor: (Color) -> Unit,
    onToggleAmoledBlack: (Boolean) -> Unit,
    onToggleNothingOsTheme: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()
    val hasDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val totalItems = if (hasDynamicColor) 5 else 4

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
                        ),
                        modifier = Modifier.padding(start = 12.dp)
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
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Spacer(Modifier.height(6.dp))

            // Item 1: Theme (System | Light | Dark) -> topListItemShape
            ThemePickerListItem(
                theme = state.themeMode,
                items = totalItems,
                index = 0,
                onThemeChange = onSetThemeMode
            )

            // Item 2: Dynamic Color (Android 12+) -> middleListItemShape
            if (hasDynamicColor) {
                val isDynamic = state.themeColor == colorSchemes.last()
                AppearanceSwitchRow(
                    item = SettingsSwitchItem(
                        checked = isDynamic,
                        icon = R.drawable.colors,
                        label = R.string.settings_dynamic_color,
                        description = R.string.settings_dynamic_color_desc,
                        onClick = { enabled ->
                            if (enabled) onSetThemeColor(colorSchemes.last())
                            else onSetThemeColor(colorSchemes.first())
                        }
                    ),
                    shape = middleListItemShape,
                    onToggle = { enabled ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (enabled) onSetThemeColor(colorSchemes.last())
                        else onSetThemeColor(colorSchemes.first())
                    }
                )
            }

            // Item 3: Color Scheme (Clean Palette) -> middleListItemShape
            ColorSchemePickerCard(
                color = state.themeColor,
                shape = middleListItemShape,
                onColorChange = { color ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSetThemeColor(color)
                }
            )

            // Item 4: Nothing OS Theme (NDot & Red Accent) -> middleListItemShape
            AppearanceSwitchRow(
                item = SettingsSwitchItem(
                    checked = state.nothingOsTheme,
                    icon = R.drawable.ic_sparkles,
                    label = R.string.settings_nothing_os_theme,
                    description = R.string.settings_nothing_os_theme_desc,
                    onClick = onToggleNothingOsTheme
                ),
                shape = middleListItemShape,
                onToggle = { enabled ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggleNothingOsTheme(enabled)
                }
            )

            // Item 5: Black Theme (Pure AMOLED) -> bottomListItemShape
            AppearanceSwitchRow(
                item = SettingsSwitchItem(
                    checked = state.amoledBlack,
                    icon = R.drawable.contrast,
                    label = R.string.settings_amoled_black,
                    description = R.string.settings_amoled_black_desc,
                    onClick = onToggleAmoledBlack
                ),
                shape = bottomListItemShape,
                onToggle = { enabled ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggleAmoledBlack(enabled)
                }
            )

            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun AppearanceSwitchRow(
    item: SettingsSwitchItem,
    shape: Shape,
    onToggle: (Boolean) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Surface(
        shape = shape,
        color = listItemColors.containerColor,
        border = CustomColors.cardBorder,
        onClick = {
            if (item.enabled) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onToggle(!item.checked)
            }
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
                painter = painterResource(item.icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(item.label),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(item.description),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.5.sp,
                        lineHeight = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = item.checked,
                enabled = item.enabled,
                onCheckedChange = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggle(it)
                },
                thumbContent = {
                    if (item.checked) {
                        Icon(
                            painter = painterResource(R.drawable.check),
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.clear),
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                },
                colors = switchColors
            )
        }
    }
}
