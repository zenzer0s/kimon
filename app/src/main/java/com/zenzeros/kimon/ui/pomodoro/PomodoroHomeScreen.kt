@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui.pomodoro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun PomodoroHomeScreen(
    modifier: Modifier = Modifier,
    initialState: PomodoroUiState = PomodoroUiState()
) {
    var state by remember { mutableStateOf(initialState) }

    // Live countdown ticker effect when timer is RUNNING
    LaunchedEffect(state.timerStatus, state.remainingSeconds) {
        if (state.timerStatus == TimerStatus.RUNNING && state.remainingSeconds > 0) {
            delay(1000L)
            state = state.copy(remainingSeconds = state.remainingSeconds - 1)
        } else if (state.timerStatus == TimerStatus.RUNNING && state.remainingSeconds <= 0) {
            val nextMode = if (state.currentMode == PomodoroMode.FOCUS) PomodoroMode.SHORT_BREAK else PomodoroMode.FOCUS
            val newCompleted = if (state.currentMode == PomodoroMode.FOCUS) state.currentSessionIndex + 1 else state.currentSessionIndex
            state = state.copy(
                currentMode = nextMode,
                remainingSeconds = nextMode.durationMinutes * 60,
                totalSeconds = nextMode.durationMinutes * 60,
                timerStatus = TimerStatus.IDLE,
                currentSessionIndex = newCompleted
            )
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        val sidePadding = 20.dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = sidePadding, vertical = sidePadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ~90% Upper Screen Area for Clock
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.89f),
                contentAlignment = Alignment.Center
            ) {
                when (state.clockStyle) {
                    ClockStyle.FLIP_CARD -> {
                        FlipCardPomodoroClock(
                            remainingSeconds = state.remainingSeconds,
                            isRunning = state.timerStatus == TimerStatus.RUNNING,
                            spacing = sidePadding
                        )
                    }
                    ClockStyle.CONCENTRIC -> {
                        ConcentricPomodoroDial(
                            remainingSeconds = state.remainingSeconds,
                            currentMode = state.currentMode
                        )
                    }
                }
            }

            // ~10% Lower Screen Area for Control Buttons (Reset, Start/Pause, Next)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.11f),
                contentAlignment = Alignment.Center
            ) {
                TimerControls(
                    status = state.timerStatus,
                    onStart = { state = state.copy(timerStatus = TimerStatus.RUNNING) },
                    onPause = { state = state.copy(timerStatus = TimerStatus.PAUSED) },
                    onReset = {
                        state = state.copy(
                            timerStatus = TimerStatus.IDLE,
                            remainingSeconds = state.currentMode.durationMinutes * 60
                        )
                    },
                    onNext = {
                        val nextMode = when (state.currentMode) {
                            PomodoroMode.FOCUS -> PomodoroMode.SHORT_BREAK
                            PomodoroMode.SHORT_BREAK -> PomodoroMode.FOCUS
                            PomodoroMode.LONG_BREAK -> PomodoroMode.FOCUS
                        }
                        state = state.copy(
                            currentMode = nextMode,
                            remainingSeconds = nextMode.durationMinutes * 60,
                            totalSeconds = nextMode.durationMinutes * 60,
                            timerStatus = TimerStatus.IDLE
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun TimerControls(
    status: TimerStatus,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Reset Button
        FilledTonalIconButton(
            onClick = onReset,
            modifier = Modifier.size(56.dp),
            shape = CircleShape
        ) {
            Text(
                text = "↺",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        // Start / Pause Primary Button
        val isRunning = status == TimerStatus.RUNNING
        Button(
            onClick = { if (isRunning) onPause() else onStart() },
            modifier = Modifier
                .height(64.dp)
                .width(160.dp)
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(32.dp)),
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (isRunning) "❚❚" else "▶",
                    fontSize = 18.sp
                )
                Text(
                    text = if (isRunning) "PAUSE" else "START",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.width(20.dp))

        // Next Button
        FilledTonalIconButton(
            onClick = onNext,
            modifier = Modifier.size(56.dp),
            shape = CircleShape
        ) {
            Text(
                text = "⏭",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
