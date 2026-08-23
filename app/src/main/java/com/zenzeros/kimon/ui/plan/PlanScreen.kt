@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zenzeros.kimon.KimonApplication
import com.zenzeros.kimon.R
import com.zenzeros.kimon.ui.analyze.components.AnalyzeEmptyState

@Composable
fun PlanScreen(
    viewModel: PlanViewModel = viewModel(
        factory = PlanViewModel.Factory(
            taskRepository = (LocalContext.current.applicationContext as KimonApplication).taskRepository,
            tagRepository = (LocalContext.current.applicationContext as KimonApplication).tagRepository
        )
    ),
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val tags by viewModel.tags.collectAsStateWithLifecycle()
    var isAddingTask by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    AnalyzeEmptyState(
                        icon = R.drawable.ic_plan,
                        message = stringResource(R.string.plan_empty_tasks),
                        actionText = stringResource(R.string.plan_action_add_task),
                        onActionClick = { isAddingTask = true }
                    )
                }
            } else {
                // 1. Material 3 Expressive Segmented List with Circular Checkboxes
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.5.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
                ) {
                    itemsIndexed(tasks, key = { _, item -> item.id }) { index, item ->
                        SegmentedListItem(
                            checked = item.isCompleted,
                            onCheckedChange = {
                                viewModel.toggleTaskCompletion(item)
                            },
                            shapes = ListItemDefaults.segmentedShapes(
                                index = index,
                                count = tasks.size
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
                                            text = "${item.estimatedPomodoros} ${if (item.estimatedPomodoros == 1) "session" else "sessions"}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (item.isCompleted) {
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    }
                                }
                            },
                            content = {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.5.sp,
                                        textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                    ),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }
            }
        }

        // 2. Material 3 Expressive Floating Action Button
        FloatingActionButton(
            onClick = {
                isAddingTask = true
            },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 3.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 4.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = stringResource(R.string.plan_action_add_task),
                modifier = Modifier.size(24.dp)
            )
        }
    }

    // 3. Add Task BottomSheet
    if (isAddingTask) {
        AddTaskBottomSheet(
            sheetState = sheetState,
            tags = tags,
            onAddTask = { title, category, pomodoros ->
                viewModel.addTask(title = title, category = category, pomodoros = pomodoros)
            },
            onDismissRequest = { isAddingTask = false }
        )
    }
}

@Composable
private fun CircularCheckIcon(
    checked: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(durationMillis = 150),
        label = "checkCircleBackground"
    )

    val borderColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(durationMillis = 150),
        label = "checkCircleBorder"
    )

    Box(
        modifier = modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = checked,
            enter = fadeIn(animationSpec = tween(150)) + scaleIn(initialScale = 0.5f),
            exit = fadeOut(animationSpec = tween(100)) + scaleOut(targetScale = 0.5f)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = stringResource(R.string.plan_content_desc_completed),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}
