@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zenzeros.kimon.R
import com.zenzeros.kimon.ui.theme.CustomColors.listItemColors
import com.zenzeros.kimon.ui.theme.CustomColors.switchColors
import com.zenzeros.kimon.ui.theme.KimonShapeDefaults.segmentedListItemShapes
import com.zenzeros.kimon.ui.theme.ThemePalette

@Composable
fun ColorSchemePickerListItem(
    paletteName: String,
    items: Int,
    index: Int,
    onPaletteChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDynamic = paletteName == "DYNAMIC"

    val palettes = listOf(
        ThemePalette.DEFAULT to Color(0xFF00668B),
        ThemePalette.OCEAN to Color(0xFF00639B),
        ThemePalette.EMERALD to Color(0xFF006C4C),
        ThemePalette.SUNSET to Color(0xFF904A42),
        ThemePalette.LAVENDER to Color(0xFF7551A0)
    )

    Column {
        // 1. Dynamic Color Switch Item
        SegmentedListItem(
            onClick = {
                if (!isDynamic) onPaletteChange("DYNAMIC")
                else onPaletteChange("DEFAULT")
            },
            leadingContent = { Icon(painterResource(R.drawable.ic_sparkles), null) },
            content = { Text(stringResource(R.string.dynamic_color)) },
            supportingContent = { Text(stringResource(R.string.dynamic_color_desc)) },
            trailingContent = {
                Switch(
                    checked = isDynamic,
                    onCheckedChange = {
                        if (it) onPaletteChange("DYNAMIC")
                        else onPaletteChange("DEFAULT")
                    },
                    colors = switchColors
                )
            },
            shapes = segmentedListItemShapes(index, items),
            colors = listItemColors,
            modifier = modifier
        )

        // 2. Custom Palettes Swatches Row (when not dynamic)
        if (!isDynamic) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                palettes.forEach { (pal, col) ->
                    val isSelected = paletteName == pal.name
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(col)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { onPaletteChange(pal.name) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }
                }
            }
        }
    }
}
