@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui.settings.components

import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zenzeros.kimon.R
import com.zenzeros.kimon.ui.theme.CustomColors.listItemColors
import com.zenzeros.kimon.ui.theme.CustomColors.switchColors
import com.zenzeros.kimon.ui.theme.KimonShapeDefaults.segmentedListItemShapes
import com.zenzeros.kimon.ui.theme.colorSchemes

@Composable
fun ColorSchemePickerListItem(
    color: Color,
    items: Int,
    index: Int,
    onColorChange: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val checked = color == colorSchemes.last()
        SegmentedListItem(
            onClick = {
                if (!checked) onColorChange(colorSchemes.last())
                else onColorChange(colorSchemes.first())
            },
            leadingContent = { Icon(painterResource(R.drawable.colors), null) },
            content = { Text(stringResource(R.string.settings_dynamic_color)) },
            supportingContent = { Text(stringResource(R.string.settings_dynamic_color_desc)) },
            trailingContent = {
                Switch(
                    checked = checked,
                    onCheckedChange = {
                        if (it) onColorChange(colorSchemes.last())
                        else onColorChange(colorSchemes.first())
                    },
                    thumbContent = {
                        if (checked) {
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
            shapes = segmentedListItemShapes(index, items),
            modifier = modifier
        )
        Spacer(Modifier.height(2.dp))
    }

    Box {
        SegmentedListItem(
            onClick = {},
            leadingContent = {
                Icon(
                    painter = painterResource(R.drawable.palette),
                    contentDescription = null
                )
            },
            content = { Text(stringResource(R.string.settings_color_scheme)) },
            supportingContent = {
                Text(
                    if (color == Color.White) stringResource(R.string.theme_system)
                    else stringResource(R.string.settings_color_scheme)
                )
            },
            colors = listItemColors,
            shapes = ListItemDefaults.segmentedShapes(
                1,
                3,
                ListItemDefaults.shapes(
                    shape = shapes.extraSmall.copy(
                        bottomStart = CornerSize(0),
                        bottomEnd = CornerSize(0)
                    )
                )
            ),
            modifier = modifier
        )

        Box(
            Modifier
                .matchParentSize()
                .clickable(false) {}
        )
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 48.dp),
        modifier = modifier
            .background(
                animateColorAsState(listItemColors.containerColor).value,
                shape = shapes.extraSmall.copy(topStart = CornerSize(0), topEnd = CornerSize(0))
            )
            .padding(bottom = 8.dp)
    ) {
        items(colorSchemes.dropLast(1)) { swatchColor ->
            ColorPickerButton(
                color = swatchColor,
                isSelected = swatchColor == color,
                modifier = Modifier.padding(4.dp)
            ) {
                onColorChange(swatchColor)
            }
        }
    }
}

@Composable
fun ColorPickerButton(
    color: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    IconButton(
        shapes = IconButtonDefaults.shapes(),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = color,
            disabledContainerColor = color.copy(0.3f)
        ),
        modifier = modifier.size(48.dp),
        onClick = onClick
    ) {
        AnimatedContent(isSelected) { selected ->
            when (selected) {
                true -> Icon(
                    painterResource(R.drawable.check),
                    tint = Color.Black,
                    contentDescription = null
                )
                else ->
                    if (color == Color.White) Icon(
                        painterResource(R.drawable.colors),
                        tint = Color.Black,
                        contentDescription = null
                    )
            }
        }
    }
}
