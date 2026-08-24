@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui.settings.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenzeros.kimon.R
import com.zenzeros.kimon.ui.navigation.KimonNavKey
import com.zenzeros.kimon.ui.settings.SettingsUiState
import com.zenzeros.kimon.ui.settings.components.ResetDataDialog
import com.zenzeros.kimon.ui.theme.CustomColors.listItemColors
import com.zenzeros.kimon.ui.theme.CustomColors.topBarColors
import com.zenzeros.kimon.ui.theme.KimonShapeDefaults.segmentedListItemShapes
import com.zenzeros.kimon.ui.theme.LocalAppFonts

data class SettingsNavCategory(
    val key: KimonNavKey,
    val icon: Int,
    val title: Int,
    val subtitle: String
)

@Composable
fun SettingsMainScreen(
    state: SettingsUiState,
    onNavigate: (KimonNavKey) -> Unit,
    onBack: () -> Unit,
    onResetData: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        ResetDataDialog(
            onReset = onResetData,
            onDismiss = { showResetDialog = false }
        )
    }

    val categories = remember {
        listOf(
            SettingsNavCategory(
                key = KimonNavKey.TimerSettings,
                icon = R.drawable.ic_focus,
                title = R.string.settings_section_timer,
                subtitle = "Durations, DND, Always on display, Goals"
            ),
            SettingsNavCategory(
                key = KimonNavKey.AlarmSettings,
                icon = R.drawable.ic_sparkles,
                title = R.string.settings_section_sound,
                subtitle = "Alarm sound, Sound, Vibrate, Media volume"
            ),
            SettingsNavCategory(
                key = KimonNavKey.AppearanceSettings,
                icon = R.drawable.ic_profile,
                title = R.string.settings_section_appearance,
                subtitle = "Theme mode, Palette, AMOLED Black"
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.title_settings),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = LocalAppFonts.current.topBarTitle,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
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
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            contentPadding = innerPadding,
            modifier = Modifier
                .background(topBarColors.containerColor)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            // 1. Settings Main Categories (Timer, Alarm, Appearance)
            items(categories.size) { index ->
                val cat = categories[index]
                SegmentedListItem(
                    leadingContent = {
                        Icon(painterResource(cat.icon), contentDescription = null)
                    },
                    supportingContent = {
                        Text(
                            cat.subtitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    trailingContent = {
                        Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null)
                    },
                    shapes = segmentedListItemShapes(index, categories.size),
                    colors = listItemColors,
                    onClick = { onNavigate(cat.key) }
                ) {
                    Text(stringResource(cat.title))
                }
            }

            item { Spacer(Modifier.height(12.dp)) }

            // 2. Data & Reset Group
            item {
                SegmentedListItem(
                    leadingContent = {
                        Icon(painterResource(R.drawable.ic_close), contentDescription = null)
                    },
                    supportingContent = {
                        Text(stringResource(R.string.reset_data_desc))
                    },
                    trailingContent = {
                        Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null)
                    },
                    shapes = segmentedListItemShapes(0, 2),
                    colors = listItemColors,
                    onClick = { showResetDialog = true }
                ) {
                    Text(stringResource(R.string.reset_data_title))
                }
            }

            // 3. About App Group
            item {
                SegmentedListItem(
                    leadingContent = {
                        Icon(painterResource(R.drawable.ic_profile), contentDescription = null)
                    },
                    supportingContent = {
                        Text(stringResource(R.string.app_name) + " 1.0")
                    },
                    trailingContent = {
                        Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null)
                    },
                    shapes = segmentedListItemShapes(1, 2),
                    colors = listItemColors,
                    onClick = { onNavigate(KimonNavKey.AboutSettings) }
                ) {
                    Text("About")
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
