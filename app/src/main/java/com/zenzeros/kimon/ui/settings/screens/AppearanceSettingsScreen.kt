@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui.settings.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenzeros.kimon.R
import com.zenzeros.kimon.ui.settings.SettingsUiState
import com.zenzeros.kimon.ui.settings.components.ColorSchemePickerListItem
import com.zenzeros.kimon.ui.settings.components.ThemePickerListItem
import com.zenzeros.kimon.ui.theme.CustomColors.listItemColors
import com.zenzeros.kimon.ui.theme.CustomColors.switchColors
import com.zenzeros.kimon.ui.theme.CustomColors.topBarColors
import com.zenzeros.kimon.ui.theme.KimonShapeDefaults.PANE_MAX_WIDTH
import com.zenzeros.kimon.ui.theme.KimonShapeDefaults.bottomListItemShape
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_section_appearance),
                        fontFamily = LocalAppFonts.current.topBarTitle,
                        fontSize = 22.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
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
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            contentPadding = innerPadding,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(Modifier.height(4.dp))
            }

            // 1. Theme Picker (System, Light, Dark)
            item {
                ThemePickerListItem(
                    theme = state.themeMode,
                    items = 2,
                    index = 0,
                    onThemeChange = onSetThemeMode
                )
            }

            // 2. Color Palette & Dynamic Color Picker
            item {
                ColorSchemePickerListItem(
                    paletteName = state.themePalette,
                    items = 2,
                    index = 1,
                    onPaletteChange = onSetThemePalette
                )
            }

            item { Spacer(Modifier.height(8.dp)) }

            // 3. AMOLED Black Theme Switch
            item {
                ListItem(
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.ic_profile),
                            contentDescription = null,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    },
                    headlineContent = { Text(stringResource(R.string.settings_amoled_black)) },
                    supportingContent = { Text(stringResource(R.string.settings_amoled_black_desc)) },
                    trailingContent = {
                        Switch(
                            checked = state.amoledBlack,
                            onCheckedChange = onToggleAmoledBlack,
                            colors = switchColors
                        )
                    },
                    colors = listItemColors,
                    modifier = Modifier.clip(bottomListItemShape)
                )
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
