package com.zenzeros.kimon.ui.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenzeros.kimon.R
import com.zenzeros.kimon.ui.theme.CustomColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private data class SessionSegment(
    val type: String, // "FOCUS", "SHORT_BREAK", "LONG_BREAK"
    val durationMinutes: Int,
    val color: Color
)

@Composable
fun SessionPreviewCard(
    pomodoros: Int,
    focusMinutes: Int,
    shortBreakMinutes: Int,
    longBreakMinutes: Int,
    isLongBreakEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val focusColor = Color(0xFF3B82F6) // Material Blue
    val shortBreakColor = Color(0xFF10B981) // Material Emerald Green
    val longBreakColor = Color(0xFF8B5CF6) // Material Purple

    val segments = remember(pomodoros, focusMinutes, shortBreakMinutes, longBreakMinutes, isLongBreakEnabled) {
        val list = mutableListOf<SessionSegment>()
        for (i in 1..pomodoros) {
            list.add(SessionSegment("FOCUS", focusMinutes, focusColor))
            if (i < pomodoros) {
                list.add(SessionSegment("SHORT_BREAK", shortBreakMinutes, shortBreakColor))
            }
        }
        if (isLongBreakEnabled) {
            list.add(SessionSegment("LONG_BREAK", longBreakMinutes, longBreakColor))
        }
        list
    }

    val totalMinutes = remember(segments) {
        segments.sumOf { it.durationMinutes }
    }

    val endTimeString = remember(totalMinutes) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MINUTE, totalMinutes)
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(cal.time)
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = CustomColors.cardContainerColor,
        tonalElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            // --- 1. Segmented Timeline Bar ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                segments.forEach { seg ->
                    Box(
                        modifier = Modifier
                            .weight(seg.durationMinutes.toFloat().coerceAtLeast(1f))
                            .height(6.dp)
                            .background(
                                color = seg.color,
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // --- 2. Summary Metric Row [ Total time (Left) | End time (Right) ] ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Total Time
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.label_total_time_preview),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${totalMinutes}m",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // End Time
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.label_end_time_preview),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = endTimeString,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
