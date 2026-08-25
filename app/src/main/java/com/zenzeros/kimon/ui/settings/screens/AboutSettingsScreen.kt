@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui.settings.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.zenzeros.kimon.R
import com.zenzeros.kimon.ui.theme.CustomColors
import com.zenzeros.kimon.ui.theme.CustomColors.listItemColors
import com.zenzeros.kimon.ui.theme.CustomColors.topBarColors
import com.zenzeros.kimon.ui.theme.KimonShapeDefaults.bottomListItemShape
import com.zenzeros.kimon.ui.theme.KimonShapeDefaults.middleListItemShape
import com.zenzeros.kimon.ui.theme.KimonShapeDefaults.topListItemShape
import com.zenzeros.kimon.ui.theme.LocalAppFonts

private const val GITHUB_PROFILE_URL = "https://github.com/zenzer0s"
private const val GITHUB_AVATAR_URL = "https://avatars.githubusercontent.com/u/151006009?v=4"
private const val BUYMEACOFFEE_URL = "https://buymeacoffee.com/zenzer0s"
private const val PAYPAL_URL = "https://www.paypal.me/zenzer0s"
private const val PAYPAL_USERNAME = "zenzer0s"
private const val UPI_ID = "zenzer0s@upi"

@Composable
fun AboutSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "About",
                        fontFamily = LocalAppFonts.current.topBarTitle,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = onBack,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = listItemColors.containerColor
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_chevron_left),
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = topBarColors
            )
        },
        containerColor = topBarColors.containerColor,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(2.5.dp),
            contentPadding = innerPadding,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // --- 1. Expressive Developer Hero Profile Card ---
            item {
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = CustomColors.cardContainerColor,
                    border = CustomColors.cardBorder,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp, horizontal = 16.dp)
                    ) {
                        // Developer Avatar with Outer Glowing Border Ring
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(96.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                                .padding(4.dp)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(GITHUB_AVATAR_URL)
                                    .crossfade(true)
                                    .build(),
                                placeholder = painterResource(R.drawable.ic_profile),
                                error = painterResource(R.drawable.ic_profile),
                                contentDescription = "zenzer0s developer avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        // Developer Name & Handle
                        Text(
                            text = "zenzeros",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = LocalAppFonts.current.topBarTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(Modifier.height(2.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "@zenzer0s • Developer & Creator",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.5.sp
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Crafting minimal, expressive, and distraction-free productivity experiences.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        Spacer(Modifier.height(18.dp))

                        // Quick Social Action Buttons Row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // GitHub Quick Icon Button
                            FilledTonalIconButton(
                                onClick = { openUrl(context, GITHUB_PROFILE_URL) },
                                modifier = Modifier.size(46.dp),
                                shape = CircleShape,
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_github),
                                    contentDescription = "GitHub",
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // PayPal Quick Icon Button
                            FilledTonalIconButton(
                                onClick = { openPayPal(context, PAYPAL_USERNAME) },
                                modifier = Modifier.size(46.dp),
                                shape = CircleShape,
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = Color(0xFF0079C1).copy(alpha = 0.15f),
                                    contentColor = Color(0xFF0079C1)
                                )
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_paypal),
                                    contentDescription = "PayPal",
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // BuyMeCoffee Quick Icon Button
                            FilledTonalIconButton(
                                onClick = { openUrl(context, BUYMEACOFFEE_URL) },
                                modifier = Modifier.size(46.dp),
                                shape = CircleShape,
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = Color(0xFFFFDD00).copy(alpha = 0.22f),
                                    contentColor = Color(0xFFC58300)
                                )
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_buymeacoffee),
                                    contentDescription = "Buy Me a Coffee",
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // UPI Quick Icon Button
                            FilledTonalIconButton(
                                onClick = { openUpiPayment(context, UPI_ID, "zenzer0s") },
                                modifier = Modifier.size(46.dp),
                                shape = CircleShape,
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = Color(0xFF00897B).copy(alpha = 0.18f),
                                    contentColor = Color(0xFF00897B)
                                )
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_upi),
                                    contentDescription = "UPI",
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
            }

            // --- Section Header: Support & Donations ---
            item {
                Text(
                    text = "Support Development",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 6.dp, top = 2.dp, bottom = 4.dp)
                )
            }

            // --- 2. Buy Me a Coffee ListItem ---
            item {
                ListItem(
                    leadingContent = {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = Color(0xFFFFDD00).copy(alpha = 0.22f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_buymeacoffee),
                                contentDescription = null,
                                tint = Color(0xFFD68A00),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    headlineContent = {
                        Text(
                            text = "Buy Me a Coffee",
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    supportingContent = {
                        Text(
                            text = "buymeacoffee.com/zenzer0s",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_open_in_browser),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = listItemColors,
                    modifier = Modifier
                        .clip(topListItemShape)
                        .clickable { openUrl(context, BUYMEACOFFEE_URL) }
                )
            }

            // --- 3. PayPal ListItem ---
            item {
                ListItem(
                    leadingContent = {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = Color(0xFF0079C1).copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_paypal),
                                contentDescription = null,
                                tint = Color(0xFF0079C1),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    headlineContent = {
                        Text(
                            text = "PayPal",
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    supportingContent = {
                        Text(
                            text = "@zenzer0s • paypal.me/zenzer0s",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_open_in_browser),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = listItemColors,
                    modifier = Modifier
                        .clip(middleListItemShape)
                        .clickable { openPayPal(context, PAYPAL_USERNAME) }
                )
            }

            // --- 4. UPI Payment ListItem ---
            item {
                ListItem(
                    leadingContent = {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = Color(0xFF00897B).copy(alpha = 0.18f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_upi),
                                contentDescription = null,
                                tint = Color(0xFF00897B),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    headlineContent = {
                        Text(
                            text = "Pay with UPI (India)",
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    supportingContent = {
                        Text(
                            text = "Instant via GPay, PhonePe, Paytm, BHIM",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_bolt),
                            contentDescription = null,
                            tint = Color(0xFF00897B),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    colors = listItemColors,
                    modifier = Modifier
                        .clip(bottomListItemShape)
                        .clickable { openUpiPayment(context, UPI_ID, "zenzer0s") }
                )
            }

            // --- Section Header: Application Info ---
            item {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Project Info",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 6.dp, top = 2.dp, bottom = 4.dp)
                )
            }

            // --- 5. GitHub Repository ListItem ---
            item {
                ListItem(
                    leadingContent = {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_github),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    headlineContent = {
                        Text(
                            text = "GitHub Profile & Projects",
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    supportingContent = {
                        Text(
                            text = "github.com/zenzer0s",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_open_in_browser),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = listItemColors,
                    modifier = Modifier
                        .clip(topListItemShape)
                        .clickable { openUrl(context, GITHUB_PROFILE_URL) }
                )
            }

            // --- 6. Version ---
            item {
                ListItem(
                    leadingContent = {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_sparkles),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    headlineContent = {
                        Text(
                            text = "Version",
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    supportingContent = {
                        Text(
                            text = "1.0.0 (Material 3 Expressive & Android 16+)",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = listItemColors,
                    modifier = Modifier.clip(middleListItemShape)
                )
            }

            // --- 7. License ---
            item {
                ListItem(
                    leadingContent = {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_trophy),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    headlineContent = {
                        Text(
                            text = "License",
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    supportingContent = {
                        Text(
                            text = "Free & Open Source • GPL v3.0",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = listItemColors,
                    modifier = Modifier.clip(bottomListItemShape)
                )
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open link: $url", Toast.LENGTH_SHORT).show()
    }
}

private fun openPayPal(context: Context, username: String) {
    val webUrl = "https://www.paypal.me/$username"
    try {
        // Try direct paypal intent or browser
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open PayPal: $webUrl", Toast.LENGTH_SHORT).show()
    }
}

private fun openUpiPayment(context: Context, upiId: String, name: String) {
    try {
        val upiUri = Uri.parse("upi://pay?pa=$upiId&pn=$name&cu=INR")
        val upiIntent = Intent(Intent.ACTION_VIEW, upiUri)
        val chooser = Intent.createChooser(upiIntent, "Select UPI App to Pay")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (e: Exception) {
        // Fallback: Copy UPI ID to clipboard & notify user
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("UPI ID", upiId)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, "No UPI app found. Copied UPI ID: $upiId", Toast.LENGTH_LONG).show()
    }
}
