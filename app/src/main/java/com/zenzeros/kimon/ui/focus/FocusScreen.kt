@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zenzeros.kimon.R
import com.zenzeros.kimon.ui.pomodoro.ConcentricPomodoroDial

@Composable
fun FocusScreen(
    remainingSeconds: Int,
    isRunning: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isTimerActive = remainingSeconds < 25 * 60
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
        // Concentric Chrono Dial Clock Face
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            ConcentricPomodoroDial(
                remainingSeconds = remainingSeconds
            )
        }

        // 3-State Expressive Button (Start / Pause / Resume) + Animated Circle Restart Button
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
                        contentDescription = "Pause",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pause",
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
                        contentDescription = if (isTimerActive) "Resume" else "Start",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isTimerActive) "Resume" else "Start",
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
                        contentDescription = "Restart",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
