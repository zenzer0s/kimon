@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui.analyze.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenzeros.kimon.R
import com.zenzeros.kimon.ui.analyze.components.AnalyzeCardHeader
import com.zenzeros.kimon.ui.analyze.components.AnalyzeEmptyState
import com.zenzeros.kimon.ui.analyze.components.AnalyzeNavigationHeader
import com.zenzeros.kimon.ui.analyze.components.MetricTileCard
import com.zenzeros.kimon.ui.analyze.components.horizontalSegmentedShape
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DayTab(
    onNavigateToFocus: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var selectedCalendar by remember { mutableStateOf(Calendar.getInstance()) }

    val dayOfWeek = remember(selectedCalendar) {
        SimpleDateFormat("EEE", Locale.getDefault()).format(selectedCalendar.time).uppercase()
    }

    val formattedDate = remember(selectedCalendar) {
        SimpleDateFormat("MM / dd / yyyy", Locale.getDefault()).format(selectedCalendar.time)
    }

    val totalSectionsGroup = 3
    val todayFocusShapesGroup = ListItemDefaults.segmentedShapes(index = 0, count = totalSectionsGroup)
    val focusByTagShapesGroup = ListItemDefaults.segmentedShapes(index = 1, count = totalSectionsGroup)
    val dailyTimelineShapesGroup = ListItemDefaults.segmentedShapes(index = 2, count = totalSectionsGroup)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // 1. Navigation Header: [ Left: Combined Day & Date Pill ] ... [ Right: ButtonGroup with < and > ]
        AnalyzeNavigationHeader(
            onPreviousClick = {
                val newCal = selectedCalendar.clone() as Calendar
                newCal.add(Calendar.DAY_OF_YEAR, -1)
                selectedCalendar = newCal
            },
            onNextClick = {
                val newCal = selectedCalendar.clone() as Calendar
                newCal.add(Calendar.DAY_OF_YEAR, 1)
                selectedCalendar = newCal
            }
        ) {
            Text(
                text = dayOfWeek,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "•",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Text(
                text = formattedDate,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.5.sp,
                    letterSpacing = 0.3.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Icon(
                painter = painterResource(R.drawable.ic_calendar),
                contentDescription = "Calendar",
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Material 3 Expressive Segmented Group (Today's Focus, Focus Time by Tag, Daily Timeline)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(3.5.dp)
        ) {
            // --- Card 1: Today's Focus ---
            Surface(
                shape = todayFocusShapesGroup.shape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(11.dp)
                ) {
                    AnalyzeCardHeader(
                        icon = R.drawable.ic_focus,
                        title = "Today's Focus"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Horizontal Segmented Row: [ Total Focus ] [ Total Sessions ]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        MetricTileCard(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            shape = horizontalSegmentedShape(index = 0, count = 2),
                            icon = R.drawable.ic_focus,
                            iconTint = MaterialTheme.colorScheme.primary,
                            iconBg = MaterialTheme.colorScheme.primaryContainer,
                            cardBg = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                            cardBorder = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            valueColor = MaterialTheme.colorScheme.primary,
                            label = "Total Focus",
                            value = "0m"
                        )

                        MetricTileCard(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            shape = horizontalSegmentedShape(index = 1, count = 2),
                            icon = R.drawable.ic_bar_chart,
                            iconTint = MaterialTheme.colorScheme.secondary,
                            iconBg = MaterialTheme.colorScheme.secondaryContainer,
                            cardBg = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f),
                            cardBorder = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                            valueColor = MaterialTheme.colorScheme.secondary,
                            label = "Total Sessions",
                            value = "0"
                        )
                    }
                }
            }

            // --- Card 2: Focus Time by Tag ---
            Surface(
                shape = focusByTagShapesGroup.shape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(11.dp)
                ) {
                    AnalyzeCardHeader(
                        icon = R.drawable.ic_tag,
                        title = "Focus Time by Tag"
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    AnalyzeEmptyState(
                        icon = R.drawable.ic_tag,
                        message = "No focus sessions for this day.",
                        onActionClick = onNavigateToFocus
                    )
                }
            }

            // --- Card 3: Daily Timeline ---
            Surface(
                shape = dailyTimelineShapesGroup.shape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(11.dp)
                ) {
                    AnalyzeCardHeader(
                        icon = R.drawable.ic_calendar,
                        title = "Daily Timeline",
                        iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                        iconBg = MaterialTheme.colorScheme.tertiaryContainer
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    AnalyzeEmptyState(
                        icon = R.drawable.ic_focus,
                        message = "No focus sessions for this day.",
                        onActionClick = onNavigateToFocus
                    )
                }
            }
        }
    }
}
