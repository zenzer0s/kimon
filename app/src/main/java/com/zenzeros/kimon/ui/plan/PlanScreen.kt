@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.zenzeros.kimon.ui.plan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zenzeros.kimon.KimonApplication
import com.zenzeros.kimon.R
import com.zenzeros.kimon.ui.analyze.components.AnalyzeEmptyState
import com.zenzeros.kimon.ui.theme.CustomColors
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    val lazyListState = rememberLazyListState()
    var currentTasks by remember { mutableStateOf(tasks) }
    var draggingItemId by remember { mutableStateOf<Long?>(null) }
    var draggingItemOffset by remember { mutableFloatStateOf(0f) }
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val edgeThresholdPx = with(density) { 56.dp.toPx() }

    val activeTasks = remember(currentTasks) { currentTasks.filter { !it.isCompleted } }
    val completedTasks = remember(currentTasks) { currentTasks.filter { it.isCompleted } }
    var isCompletedExpanded by remember { mutableStateOf(true) }

    LaunchedEffect(tasks) {
        if (draggingItemId == null) {
            currentTasks = tasks
        }
    }

    LaunchedEffect(draggingItemId, draggingItemOffset) {
        if (draggingItemId == null) return@LaunchedEffect
        while (true) {
            val layoutInfo = lazyListState.layoutInfo
            val currentInfo = layoutInfo.visibleItemsInfo.firstOrNull { it.key == draggingItemId }
                ?: break
            val itemTop = currentInfo.offset + draggingItemOffset
            val itemBottom = itemTop + currentInfo.size
            val scrollAmount = when {
                itemTop < layoutInfo.viewportStartOffset + edgeThresholdPx -> {
                    val dist = (layoutInfo.viewportStartOffset + edgeThresholdPx - itemTop).coerceAtLeast(0f)
                    -(dist / edgeThresholdPx).coerceIn(0f, 1f) * 15f
                }
                itemBottom > layoutInfo.viewportEndOffset - edgeThresholdPx -> {
                    val dist = (itemBottom - (layoutInfo.viewportEndOffset - edgeThresholdPx)).coerceAtLeast(0f)
                    (dist / edgeThresholdPx).coerceIn(0f, 1f) * 15f
                }
                else -> 0f
            }
            if (scrollAmount != 0f) {
                lazyListState.scrollBy(scrollAmount)
                delay(16)
            } else {
                break
            }
        }
    }

    val onDragStart: (Long) -> Unit = { id ->
        draggingItemId = id
        draggingItemOffset = 0f
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    val onDrag: (Float) -> Unit = onDragLambda@{ delta ->
        draggingItemOffset += delta
        val currentId = draggingItemId ?: return@onDragLambda
        val layoutInfo = lazyListState.layoutInfo
        val currentItemInfo = layoutInfo.visibleItemsInfo.firstOrNull { it.key == currentId } ?: return@onDragLambda
        val draggedCenterY = currentItemInfo.offset + (currentItemInfo.size / 2f) + draggingItemOffset

        val targetItem = layoutInfo.visibleItemsInfo.firstOrNull { item ->
            item.key != currentId &&
                activeTasks.any { it.id == item.key } &&
                draggedCenterY in (item.offset.toFloat()..(item.offset + item.size).toFloat())
        }

        if (targetItem != null) {
            val currentIndex = currentTasks.indexOfFirst { it.id == currentId }
            val targetIndex = currentTasks.indexOfFirst { it.id == targetItem.key }
            if (currentIndex != -1 && targetIndex != -1 && currentIndex != targetIndex) {
                val mutable = currentTasks.toMutableList()
                val moved = mutable.removeAt(currentIndex)
                mutable.add(targetIndex, moved)
                currentTasks = mutable
                draggingItemOffset += (currentItemInfo.offset - targetItem.offset)
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
    }

    val onDragEnd: () -> Unit = {
        if (draggingItemId != null) {
            if (currentTasks != tasks) {
                viewModel.updateTaskOrder(currentTasks)
            }
            draggingItemId = null
            draggingItemOffset = 0f
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    val onDragCancel: () -> Unit = {
        draggingItemId = null
        draggingItemOffset = 0f
        currentTasks = tasks
    }

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
                // 1. Material 3 Expressive Segmented List with Separate Active & Completed Sections
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
                ) {
                    // Active Tasks Section
                    if (activeTasks.isNotEmpty()) {
                        itemsIndexed(activeTasks, key = { _, item -> item.id }) { index, item ->
                            val isDragging = item.id == draggingItemId
                            val itemShapes = ListItemDefaults.segmentedShapes(
                                index = index,
                                count = activeTasks.size
                            )

                            val elevation by animateDpAsState(
                                targetValue = if (isDragging) 12.dp else 0.dp,
                                animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
                                label = "dragElevation"
                            )
                            val scale by animateFloatAsState(
                                targetValue = if (isDragging) 1.03f else 1f,
                                animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
                                label = "dragScale"
                            )

                            SwipeToRevealTaskItem(
                                task = item,
                                shapes = itemShapes,
                                isDragging = isDragging,
                                showDragHandle = true,
                                onToggleCompletion = {
                                    viewModel.toggleTaskCompletion(item)
                                },
                                onDelete = {
                                    viewModel.deleteTask(item)
                                },
                                onDragStart = { onDragStart(item.id) },
                                onDrag = onDrag,
                                onDragEnd = onDragEnd,
                                onDragCancel = onDragCancel,
                                modifier = Modifier
                                    .animateItem(
                                        fadeInSpec = null,
                                        fadeOutSpec = null,
                                        placementSpec = if (isDragging) null else MaterialTheme.motionScheme.defaultSpatialSpec()
                                    )
                                    .zIndex(if (isDragging) 10f else 0f)
                                    .graphicsLayer {
                                        if (isDragging) {
                                            translationY = draggingItemOffset
                                        }
                                        scaleX = scale
                                        scaleY = scale
                                        shadowElevation = elevation.toPx()
                                    }
                            )
                        }
                    }

                    // Completed Tasks Section
                    if (completedTasks.isNotEmpty()) {
                        item(key = "completed_header") {
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { isCompletedExpanded = !isCompletedExpanded }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val chevronRotation by animateFloatAsState(
                                        targetValue = if (isCompletedExpanded) 90f else 0f,
                                        label = "completedChevronRotation"
                                    )
                                    Icon(
                                        painter = painterResource(R.drawable.ic_chevron_right),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .graphicsLayer { rotationZ = chevronRotation },
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = stringResource(R.string.plan_section_completed, completedTasks.size),
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.5.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                FilledTonalButton(
                                    onClick = { viewModel.clearCompletedTasks() },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.plan_action_clear_completed),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        if (isCompletedExpanded) {
                            itemsIndexed(completedTasks, key = { _, item -> item.id }) { index, item ->
                                val itemShapes = ListItemDefaults.segmentedShapes(
                                    index = index,
                                    count = completedTasks.size
                                )

                                SwipeToRevealTaskItem(
                                    task = item,
                                    shapes = itemShapes,
                                    isDragging = false,
                                    showDragHandle = false,
                                    onToggleCompletion = {
                                        viewModel.toggleTaskCompletion(item)
                                    },
                                    onDelete = {
                                        viewModel.deleteTask(item)
                                    },
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }
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

@Composable
private fun SwipeToRevealTaskItem(
    task: com.zenzeros.kimon.data.local.entity.TaskEntity,
    shapes: ListItemShapes,
    isDragging: Boolean,
    showDragHandle: Boolean = true,
    onToggleCompletion: () -> Unit,
    onDelete: () -> Unit,
    onDragStart: () -> Unit = {},
    onDrag: (Float) -> Unit = {},
    onDragEnd: () -> Unit = {},
    onDragCancel: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val maxOffsetPx = with(density) { 68.dp.toPx() }
    val offsetX = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    val isRevealed by remember { derivedStateOf { offsetX.value <= -maxOffsetPx * 0.7f } }

    LaunchedEffect(isDragging) {
        if (isDragging && offsetX.value != 0f) {
            offsetX.snapTo(0f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shapes.shape)
    ) {
        // Background Action Drawer: only rendered when item is actively swiped
        if (offsetX.value < -0.5f && !isDragging) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(shapes.shape)
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f))
                    .padding(end = 14.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            offsetX.animateTo(0f)
                            onDelete()
                        }
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = stringResource(R.string.tag_delete_title),
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Foreground: The Segmented Task List Item with Smooth Swipe Gestures & Drag and Drop
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(task.id, isDragging) {
                    if (!isDragging) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                coroutineScope.launch {
                                    val newOffset = (offsetX.value + dragAmount).coerceIn(-maxOffsetPx, 0f)
                                    offsetX.snapTo(newOffset)
                                }
                            },
                            onDragEnd = {
                                coroutineScope.launch {
                                    val target = if (offsetX.value < -maxOffsetPx / 2.5f) -maxOffsetPx else 0f
                                    offsetX.animateTo(
                                        targetValue = target,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    )
                                }
                            },
                            onDragCancel = {
                                coroutineScope.launch {
                                    offsetX.animateTo(0f)
                                }
                            }
                        )
                    }
                }
        ) {
            SegmentedListItem(
                checked = task.isCompleted,
                onCheckedChange = {
                    if (isRevealed) {
                        coroutineScope.launch {
                            offsetX.animateTo(
                                0f,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            )
                        }
                    } else {
                        onToggleCompletion()
                    }
                },
                shapes = shapes,
                colors = CustomColors.listItemColors,
                verticalAlignment = Alignment.CenterVertically,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                leadingContent = {
                    CircularCheckIcon(
                        checked = task.isCompleted
                    )
                },
                trailingContent = if (showDragHandle) {
                    {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .pointerInput(task.id) {
                                    detectVerticalDragGestures(
                                        onDragStart = { onDragStart() },
                                        onDragEnd = { onDragEnd() },
                                        onDragCancel = { onDragCancel() },
                                        onVerticalDrag = { change, dragAmount ->
                                            change.consume()
                                            onDrag(dragAmount)
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_drag_handle),
                                contentDescription = stringResource(R.string.plan_content_desc_reorder),
                                tint = if (isDragging) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                } else null,
                supportingContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (task.isCompleted) {
                                MaterialTheme.colorScheme.surfaceContainer
                            } else {
                                MaterialTheme.colorScheme.secondaryContainer
                            }
                        ) {
                            Text(
                                text = task.category,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = if (task.isCompleted) {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                } else {
                                    MaterialTheme.colorScheme.onSecondaryContainer
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
                                tint = if (task.isCompleted) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            )
                            Text(
                                text = "${task.estimatedPomodoros} ${if (task.estimatedPomodoros == 1) "session" else "sessions"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (task.isCompleted) {
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
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.5.sp,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
}
