@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.zenzeros.kimon.ui.focus

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenzeros.kimon.R
import com.zenzeros.kimon.data.local.entity.TagEntity
import com.zenzeros.kimon.ui.pomodoro.ConcentricPomodoroDial

@Composable
fun FocusScreen(
    remainingSeconds: Int,
    isRunning: Boolean,
    selectedTag: TagEntity? = null,
    tags: List<TagEntity> = emptyList(),
    onSelectTag: (TagEntity?) -> Unit = {},
    onCreateTag: (name: String, colorHex: String) -> Unit = { _, _ -> },
    onDeleteTag: (TagEntity) -> Unit = {},
    onStart: () -> Unit,
    onPause: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isTimerActive = remainingSeconds < 25 * 60
    var isTagSheetOpen by remember { mutableStateOf(false) }
    val tagSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val buttonCornerRadius by animateDpAsState(
        targetValue = when {
            isRunning -> 16.dp
            isTimerActive -> 24.dp
            else -> 28.dp
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "actionButtonShape"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // 1. Concentric Chrono Dial Clock Face with integrated Center Tag Pill
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            ConcentricPomodoroDial(
                remainingSeconds = remainingSeconds
            )

            // Integrated Center Tag Complication (Below big minutes text)
            Surface(
                onClick = { isTagSheetOpen = true },
                shape = CircleShape,
                color = if (selectedTag != null) {
                    MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.88f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.35f)
                },
                border = BorderStroke(
                    width = 1.dp,
                    color = if (selectedTag != null) {
                        val tagColor = try {
                            Color(android.graphics.Color.parseColor(selectedTag.colorHex))
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.primary
                        }
                        tagColor.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                    }
                ),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 96.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 4.5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (selectedTag != null) {
                        val tagColor = remember(selectedTag.colorHex) {
                            try {
                                Color(android.graphics.Color.parseColor(selectedTag.colorHex))
                            } catch (e: Exception) {
                                Color(0xFF6366F1)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(tagColor)
                        )
                        Text(
                            text = selectedTag.name,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_tag),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = stringResource(R.string.tag_choose_button),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // 3. 3-State Expressive Button (Start / Pause / Resume) + Animated Circle Restart Button
        Row(
            modifier = Modifier.padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Primary Action Button (Outlined when Running, Filled when Start/Resume)
            if (isRunning) {
                OutlinedButton(
                    onClick = onPause,
                    shape = RoundedCornerShape(buttonCornerRadius),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp),
                    modifier = Modifier.height(52.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_pause),
                        contentDescription = stringResource(R.string.timer_pause),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.timer_pause),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            } else {
                Button(
                    onClick = onStart,
                    shape = RoundedCornerShape(buttonCornerRadius),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp),
                    modifier = Modifier.height(52.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_start),
                        contentDescription = if (isTimerActive) stringResource(R.string.timer_resume) else stringResource(R.string.timer_start),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isTimerActive) stringResource(R.string.timer_resume) else stringResource(R.string.timer_start),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            // Animated Circular Restart Button (Visible when Running or Paused)
            AnimatedVisibility(
                visible = isRunning || isTimerActive,
                enter = fadeIn(animationSpec = tween(200)) + expandHorizontally(),
                exit = fadeOut(animationSpec = tween(200)) + shrinkHorizontally()
            ) {
                FilledTonalIconButton(
                    onClick = onRestart,
                    shape = CircleShape,
                    modifier = Modifier.size(52.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_restart),
                        contentDescription = stringResource(R.string.timer_restart),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }

    // 4. Tag Selection BottomSheet
    if (isTagSheetOpen) {
        TagSelectionBottomSheet(
            sheetState = tagSheetState,
            tags = tags,
            selectedTag = selectedTag,
            onSelectTag = onSelectTag,
            onCreateTag = onCreateTag,
            onDeleteTag = onDeleteTag,
            onDismissRequest = { isTagSheetOpen = false }
        )
    }
}
