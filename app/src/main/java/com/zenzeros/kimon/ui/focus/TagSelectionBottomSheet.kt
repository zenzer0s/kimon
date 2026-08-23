@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.zenzeros.kimon.ui.focus

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalToggleButton
import androidx.compose.material3.FilledTonalToggleButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenzeros.kimon.R
import com.zenzeros.kimon.data.local.entity.TagEntity

private val PRESET_COLORS = listOf(
    "#6366F1", "#8B5CF6", "#EC4899", "#EF4444",
    "#F59E0B", "#10B981", "#06B6D4", "#3B82F6"
)

@Composable
fun TagSelectionBottomSheet(
    sheetState: SheetState,
    tags: List<TagEntity>,
    selectedTag: TagEntity?,
    onSelectTag: (TagEntity?) -> Unit,
    onCreateTag: (name: String, colorHex: String) -> Unit,
    onDismissRequest: () -> Unit
) {
    var isCreatingTag by remember { mutableStateOf(false) }
    var newTagName by remember { mutableStateOf("") }
    var selectedColorHex by remember { mutableStateOf(PRESET_COLORS[0]) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Header with Icon, Title & "+ Create New Tag" Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_tag),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.tag_select_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                FilledTonalButton(
                    onClick = { isCreatingTag = !isCreatingTag },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.tag_create_new),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Expandable Inline Tag Creation Card (Clean with no redundant labels)
            AnimatedVisibility(
                visible = isCreatingTag,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = newTagName,
                            onValueChange = { newTagName = it },
                            placeholder = { Text(stringResource(R.string.tag_name_placeholder), fontSize = 13.5.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (newTagName.isNotBlank()) {
                                        onCreateTag(newTagName.trim(), selectedColorHex)
                                        newTagName = ""
                                        isCreatingTag = false
                                    }
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        )

                        // Color Presets Row
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(PRESET_COLORS) { colorHex ->
                                val isColorSelected = colorHex == selectedColorHex
                                val parsedColor = remember(colorHex) {
                                    try {
                                        Color(android.graphics.Color.parseColor(colorHex))
                                    } catch (e: Exception) {
                                        Color(0xFF6366F1)
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(parsedColor)
                                        .border(
                                            width = if (isColorSelected) 2.5.dp else 1.dp,
                                            color = if (isColorSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedColorHex = colorHex },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isColorSelected) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_check),
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (newTagName.isNotBlank()) {
                                    onCreateTag(newTagName.trim(), selectedColorHex)
                                    newTagName = ""
                                    isCreatingTag = false
                                }
                            },
                            enabled = newTagName.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.action_create),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // 3. Special "No Tag" Option
            val isNoTagSelected = selectedTag == null
            Surface(
                onClick = {
                    onSelectTag(null)
                    onDismissRequest()
                },
                shape = RoundedCornerShape(12.dp),
                color = if (isNoTagSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (isNoTagSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isNoTagSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                    )
                    Text(
                        text = stringResource(R.string.tag_no_tag),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isNoTagSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        ),
                        color = if (isNoTagSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Tag Grid: Max 3 Tags per Row using ButtonGroup
            val tagChunks = remember(tags) { tags.chunked(3) }

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                tagChunks.forEach { rowTags ->
                    TagButtonGroupRow(
                        rowTags = rowTags,
                        selectedTag = selectedTag,
                        onSelectTag = onSelectTag,
                        onDismissRequest = onDismissRequest
                    )
                }
            }
        }
    }
}

@Composable
private fun TagButtonGroupRow(
    rowTags: List<TagEntity>,
    selectedTag: TagEntity?,
    onSelectTag: (TagEntity?) -> Unit,
    onDismissRequest: () -> Unit
) {
    ButtonGroup(
        overflowIndicator = { menuState ->
            ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
        },
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
    ) {
        rowTags.forEachIndexed { indexInRow, tag ->
            val isSelected = selectedTag?.id == tag.id
            val tagColor = try {
                Color(android.graphics.Color.parseColor(tag.colorHex))
            } catch (e: Exception) {
                Color(0xFF6366F1)
            }

            customItem(
                buttonGroupContent = {
                    val buttonShapes = when {
                        rowTags.size == 1 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        indexInRow == 0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        indexInRow == rowTags.size - 1 -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    }

                    FilledTonalToggleButton(
                        checked = isSelected,
                        onCheckedChange = {
                            onSelectTag(tag)
                            onDismissRequest()
                        },
                        shapes = buttonShapes,
                        colors = FilledTonalToggleButtonDefaults.filledTonalToggleButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(tagColor)
                            )
                            Text(
                                text = tag.name,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.5.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                menuContent = {}
            )
        }
    }
}
