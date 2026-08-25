@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui.analyze.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalToggleButton
import androidx.compose.material3.FilledTonalToggleButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenzeros.kimon.R
import com.zenzeros.kimon.ui.theme.CustomColors
import com.zenzeros.kimon.ui.theme.LocalAppFonts

// Pre-allocated static shapes to eliminate runtime object allocation during recomposition
private val Shape2Leading = RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp, topEnd = 4.dp, bottomEnd = 4.dp)
private val Shape2Trailing = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 14.dp, bottomEnd = 14.dp)

private val Shape3Leading = RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp, topEnd = 4.dp, bottomEnd = 4.dp)
private val Shape3Middle = RoundedCornerShape(4.dp)
private val Shape3Trailing = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 14.dp, bottomEnd = 14.dp)

private val Shape3Leading10 = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp, topEnd = 4.dp, bottomEnd = 4.dp)
private val Shape3Trailing10 = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 10.dp, bottomEnd = 10.dp)

/**
 * Creates horizontal segmented shapes with zero allocations for common configs.
 */
fun horizontalSegmentedShape(
    index: Int,
    count: Int,
    outerCornerRadius: Dp = 14.dp,
    innerCornerRadius: Dp = 4.dp
): Shape {
    if (innerCornerRadius == 4.dp) {
        if (outerCornerRadius == 14.dp) {
            when (count) {
                2 -> when (index) {
                    0 -> return Shape2Leading
                    1 -> return Shape2Trailing
                }
                3 -> when (index) {
                    0 -> return Shape3Leading
                    1 -> return Shape3Middle
                    2 -> return Shape3Trailing
                }
            }
        } else if (outerCornerRadius == 10.dp && count == 3) {
            when (index) {
                0 -> return Shape3Leading10
                1 -> return Shape3Middle
                2 -> return Shape3Trailing10
            }
        }
    }

    return when {
        count <= 1 -> RoundedCornerShape(outerCornerRadius)
        index == 0 -> RoundedCornerShape(
            topStart = outerCornerRadius,
            bottomStart = outerCornerRadius,
            topEnd = innerCornerRadius,
            bottomEnd = innerCornerRadius
        )
        index == count - 1 -> RoundedCornerShape(
            topStart = innerCornerRadius,
            bottomStart = innerCornerRadius,
            topEnd = outerCornerRadius,
            bottomEnd = outerCornerRadius
        )
        else -> RoundedCornerShape(innerCornerRadius)
    }
}

/**
 * Standardized Header Row with Icon Badge, Title, and optional trailing content.
 */
@Composable
fun AnalyzeCardHeader(
    icon: Int,
    title: String,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    iconBg: Color = MaterialTheme.colorScheme.primaryContainer,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = iconTint
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        trailingContent?.invoke()
    }
}

/**
 * Standardized Navigation Header with Left Pill and Right < > ButtonGroup.
 */
@Composable
fun AnalyzeNavigationHeader(
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
    pillContent: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Pill
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = CustomColors.innerCardContainerColor,
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = CustomColors.innerCardBorderColor
            ),
            modifier = Modifier
                .height(36.dp)
                .weight(1f, fill = false)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                pillContent()
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right < > ButtonGroup
        ButtonGroup(
            overflowIndicator = { menuState ->
                ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
            },
            modifier = Modifier.wrapContentWidth(),
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
        ) {
            // Previous
            customItem(
                buttonGroupContent = {
                    FilledTonalToggleButton(
                        checked = false,
                        onCheckedChange = { onPreviousClick() },
                        shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
                        colors = FilledTonalToggleButtonDefaults.filledTonalToggleButtonColors(
                            containerColor = CustomColors.innerCardContainerColor,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = CustomColors.innerCardBorderColor
                        ),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.size(width = 42.dp, height = 36.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_chevron_left),
                            contentDescription = stringResource(R.string.action_previous),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                menuContent = {}
            )

            // Next
            customItem(
                buttonGroupContent = {
                    FilledTonalToggleButton(
                        checked = false,
                        onCheckedChange = { onNextClick() },
                        shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                        colors = FilledTonalToggleButtonDefaults.filledTonalToggleButtonColors(
                            containerColor = CustomColors.innerCardContainerColor,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = CustomColors.innerCardBorderColor
                        ),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.size(width = 42.dp, height = 36.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_chevron_right),
                            contentDescription = stringResource(R.string.action_next),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                menuContent = {}
            )
        }
    }
}

/**
 * Metric Tile Card with horizontal segmented shapes and baseline locking.
 */
@Composable
fun MetricTileCard(
    icon: Int,
    iconTint: Color,
    iconBg: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    cardBg: Color = CustomColors.innerCardContainerColor,
    cardBorder: Color = CustomColors.innerCardBorderColor,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    shape: Shape = RoundedCornerShape(12.dp),
    minLabelLines: Int = 1
) {
    Surface(
        shape = shape,
        color = cardBg,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = cardBorder
        ),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp)
        ) {
            // Centered Circular Icon Badge
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = iconTint
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Label Text
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    lineHeight = 12.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                minLines = minLabelLines,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Metric Value
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    fontFamily = LocalAppFonts.current.topBarTitle
                ),
                color = valueColor,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

/**
 * Compact Summary Tile for monthly metrics.
 */
@Composable
fun CompactSummaryTile(
    label: String,
    value: String,
    valueColor: Color,
    cardBg: Color,
    cardBorder: Color,
    shape: Shape,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = shape,
        color = cardBg,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = cardBorder
        ),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 9.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    fontFamily = LocalAppFonts.current.topBarTitle
                ),
                color = valueColor
            )
        }
    }
}

/**
 * Empty State view with centered icon badge and action link.
 */
@Composable
fun AnalyzeEmptyState(
    icon: Int,
    message: String,
    actionText: String = "Go here to start one",
    onActionClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(26.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Normal
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable { onActionClick() }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
