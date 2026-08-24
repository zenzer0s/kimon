@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui.settings.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zenzeros.kimon.R
import com.zenzeros.kimon.ui.theme.CustomColors.listItemColors
import com.zenzeros.kimon.ui.theme.KimonShapeDefaults.segmentedListItemShapes

@Composable
fun ThemePickerListItem(
    theme: String,
    items: Int,
    index: Int,
    onThemeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val themeMap: Map<String, Pair<Int, Int>> = remember {
        mapOf(
            "SYSTEM" to Pair(R.drawable.ic_profile, R.string.theme_system),
            "LIGHT" to Pair(R.drawable.ic_sparkles, R.string.theme_light),
            "DARK" to Pair(R.drawable.ic_focus, R.string.theme_dark)
        )
    }

    SegmentedListItem(
        onClick = {},
        leadingContent = {
            AnimatedContent(themeMap[theme]?.first ?: R.drawable.ic_profile) { iconRes ->
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        content = { Text(stringResource(R.string.settings_theme_mode)) },
        supportingContent = {
            val options = themeMap.toList()
            val selectedIndex = options.indexOfFirst { it.first == theme }.coerceAtLeast(0)

            Row(
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                options.forEachIndexed { optIndex, (modeKey, pair) ->
                    val isSelected = selectedIndex == optIndex
                    ToggleButton(
                        checked = isSelected,
                        onCheckedChange = { onThemeChange(modeKey) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            stringResource(pair.second),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        },
        shapes = segmentedListItemShapes(index, items),
        colors = listItemColors,
        modifier = modifier
    )
}
