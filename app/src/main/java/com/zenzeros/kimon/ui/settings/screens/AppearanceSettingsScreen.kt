@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui.settings.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zenzeros.kimon.R
import com.zenzeros.kimon.ui.settings.SettingsSwitchItem
import com.zenzeros.kimon.ui.settings.SettingsUiState
import com.zenzeros.kimon.ui.settings.components.ColorSchemePickerListItem
import com.zenzeros.kimon.ui.settings.components.ThemePickerListItem
import com.zenzeros.kimon.ui.theme.CustomColors.listItemColors
import com.zenzeros.kimon.ui.theme.CustomColors.switchColors
import com.zenzeros.kimon.ui.theme.CustomColors.topBarColors
import com.zenzeros.kimon.ui.theme.KimonShapeDefaults.segmentedListItemShapes
import com.zenzeros.kimon.ui.theme.LocalAppFonts

@Composable
fun AppearanceSettingsScreen(
    state: SettingsUiState,
    onSetThemeMode: (String) -> Unit,
    onSetThemeColor: (Color) -> Unit,
    onToggleAmoledBlack: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.settings_section_appearance),
                        fontFamily = LocalAppFonts.current.topBarTitle
                    )
                },
                subtitle = {
                    Text(stringResource(R.string.title_settings))
                },
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = onBack,
                        shapes = IconButtonDefaults.shapes(),
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
                colors = topBarColors,
                scrollBehavior = scrollBehavior
            )
        },
        containerColor = topBarColors.containerColor,
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            contentPadding = innerPadding,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(Modifier.height(14.dp))
            }
            item {
                ThemePickerListItem(
                    theme = state.themeMode,
                    items = 3,
                    index = 0,
                    onThemeChange = onSetThemeMode
                )
            }
            item {
                ColorSchemePickerListItem(
                    color = state.themeColor,
                    items = 3,
                    index = 1,
                    onColorChange = onSetThemeColor
                )
            }
            item {
                val item = SettingsSwitchItem(
                    checked = state.amoledBlack,
                    icon = R.drawable.contrast,
                    label = R.string.settings_amoled_black,
                    description = R.string.settings_amoled_black_desc,
                    onClick = onToggleAmoledBlack
                )
                SegmentedListItem(
                    onClick = { item.onClick(!item.checked) },
                    leadingContent = {
                        Icon(painterResource(item.icon), contentDescription = null)
                    },
                    content = { Text(stringResource(item.label)) },
                    supportingContent = { Text(stringResource(item.description)) },
                    trailingContent = {
                        Switch(
                            checked = item.checked,
                            onCheckedChange = { item.onClick(it) },
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
                    },
                    colors = listItemColors,
                    shapes = segmentedListItemShapes(2, 3)
                )
            }
            item {
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}
