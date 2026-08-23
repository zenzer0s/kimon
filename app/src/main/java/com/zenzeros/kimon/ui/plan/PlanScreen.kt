@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui.plan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenzeros.kimon.R

data class TodoItem(
    val id: String,
    val title: String,
    val category: String,
    val pomodoroCount: Int = 1,
    val isCompleted: Boolean = false
)

@Composable
fun PlanScreen(
    modifier: Modifier = Modifier
) {
    val todos = remember {
        mutableStateListOf(
            TodoItem("1", "Draft architecture documentation", "Deep Work", 3, isCompleted = true),
            TodoItem("2", "Design expressive Pomodoro controls", "UI / UX", 2, isCompleted = false),
            TodoItem("3", "Refactor floating toolbar components", "Code", 1, isCompleted = false),
            TodoItem("4", "Review daily sprint milestones", "Planning", 1, isCompleted = false),
            TodoItem("5", "Optimize dial canvas drawing performance", "Code", 2, isCompleted = false)
        )
    }

    var newTaskTitle by remember { mutableStateOf("") }
    var isAddingTask by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Expandable New Task Input Banner
            AnimatedVisibility(
                visible = isAddingTask,
                enter = fadeIn(animationSpec = tween(200)),
                exit = fadeOut(animationSpec = tween(200))
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newTaskTitle,
                            onValueChange = { newTaskTitle = it },
                            placeholder = { Text("What are you focusing on?", fontSize = 14.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (newTaskTitle.isNotBlank()) {
                                        todos.add(
                                            TodoItem(
                                                id = System.currentTimeMillis().toString(),
                                                title = newTaskTitle.trim(),
                                                category = "Focus",
                                                pomodoroCount = 1
                                            )
                                        )
                                        newTaskTitle = ""
                                        isAddingTask = false
                                    }
                                }
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        FilledIconButton(
                            onClick = {
                                if (newTaskTitle.isNotBlank()) {
                                    todos.add(
                                        TodoItem(
                                            id = System.currentTimeMillis().toString(),
                                            title = newTaskTitle.trim(),
                                            category = "Focus",
                                            pomodoroCount = 1
                                        )
                                    )
                                    newTaskTitle = ""
                                    isAddingTask = false
                                }
                            },
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text("✓", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }

            // 2. Material 3 Expressive Segmented List with Circular Checkboxes
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.5.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
            ) {
                itemsIndexed(todos, key = { _, item -> item.id }) { index, item ->
                    SegmentedListItem(
                        checked = item.isCompleted,
                        onCheckedChange = {
                            val i = todos.indexOfFirst { it.id == item.id }
                            if (i != -1) {
                                todos[i] = item.copy(isCompleted = !item.isCompleted)
                            }
                        },
                        shapes = ListItemDefaults.segmentedShapes(
                            index = index,
                            count = todos.size
                        ),
                        colors = ListItemDefaults.segmentedColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            supportingContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            leadingContentColor = MaterialTheme.colorScheme.primary,
                            selectedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            selectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                            selectedSupportingContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            selectedLeadingContentColor = MaterialTheme.colorScheme.primary
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        leadingContent = {
                            CircularCheckIcon(
                                checked = item.isCompleted
                            )
                        },
                        supportingContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (item.isCompleted) {
                                        MaterialTheme.colorScheme.surfaceContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerHighest
                                    }
                                ) {
                                    Text(
                                        text = item.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (item.isCompleted) {
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_focus),
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp),
                                        tint = if (item.isCompleted) {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        } else {
                                            MaterialTheme.colorScheme.primary
                                        }
                                    )
                                    Text(
                                        text = "${item.pomodoroCount} ${if (item.pomodoroCount == 1) "session" else "sessions"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (item.isCompleted) {
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                        }
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (item.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                                textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 3. Expressive Floating Action Button (FAB)
        FloatingActionButton(
            onClick = { isAddingTask = !isAddingTask },
            shape = RoundedCornerShape(18.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 12.dp, end = 8.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = "Add Task",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun CircularCheckIcon(
    checked: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(200),
        label = "circleCheckBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        animationSpec = tween(200),
        label = "circleCheckBorder"
    )

    Box(
        modifier = modifier
            .size(22.dp)
            .border(width = 2.dp, color = borderColor, shape = CircleShape)
            .background(color = backgroundColor, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = checked,
            enter = fadeIn(animationSpec = tween(150)) + scaleIn(initialScale = 0.6f),
            exit = fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.6f)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = "Completed",
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
