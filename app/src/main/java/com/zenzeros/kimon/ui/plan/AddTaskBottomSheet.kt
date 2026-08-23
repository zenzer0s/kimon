@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.zenzeros.kimon.ui.plan

import androidx.compose.foundation.background
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
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilledTonalToggleButton
import androidx.compose.material3.FilledTonalToggleButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

@Composable
fun AddTaskBottomSheet(
    sheetState: SheetState,
    tags: List<TagEntity>,
    onAddTask: (title: String, category: String, estimatedPomodoros: Int) -> Unit,
    onDismissRequest: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf<TagEntity?>(tags.firstOrNull()) }
    var estimatedPomodoros by remember { mutableIntStateOf(1) }

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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header with Icon & Title
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        painter = painterResource(R.drawable.ic_plan),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.task_add_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // 2. Task Title Input Field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = {
                    Text(
                        text = stringResource(R.string.plan_placeholder_new_task),
                        fontSize = 14.sp
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (title.isNotBlank()) {
                            onAddTask(title.trim(), selectedTag?.name ?: "Focus", estimatedPomodoros)
                            onDismissRequest()
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            )

            // 3. Exact Same Tags Grid (Max 3 per row using ButtonGroup)
            if (tags.isNotEmpty()) {
                val tagChunks = remember(tags) { tags.chunked(3) }

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tagChunks.forEach { rowTags ->
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
                                                selectedTag = tag
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
                                                .height(42.dp)
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
                }
            }

            // 4. Estimated Sessions Stepper Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_focus),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.task_estimated_sessions),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${estimatedPomodoros * 25} minutes",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Stepper Controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilledTonalIconButton(
                            onClick = { if (estimatedPomodoros > 1) estimatedPomodoros-- },
                            enabled = estimatedPomodoros > 1,
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text("-", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        Text(
                            text = estimatedPomodoros.toString(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        FilledTonalIconButton(
                            onClick = { if (estimatedPomodoros < 12) estimatedPomodoros++ },
                            enabled = estimatedPomodoros < 12,
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text("+", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // 5. Submit Button
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAddTask(title.trim(), selectedTag?.name ?: "Focus", estimatedPomodoros)
                        onDismissRequest()
                    }
                },
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.plan_action_add_task),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
