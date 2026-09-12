@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.zenzeros.kimon.ui.settings.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
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
import com.zenzeros.kimon.update.UpdateChecker
import com.zenzeros.kimon.ui.components.bouncyScroll
import kotlinx.coroutines.launch
import com.zenzeros.kimon.ui.theme.CustomColors
import com.zenzeros.kimon.ui.theme.CustomColors.listItemColors
import com.zenzeros.kimon.ui.theme.CustomColors.topBarColors
import com.zenzeros.kimon.ui.theme.KimonShapeDefaults.bottomListItemShape
import com.zenzeros.kimon.ui.theme.KimonShapeDefaults.middleListItemShape
import com.zenzeros.kimon.ui.theme.KimonShapeDefaults.singleListItemShape
import com.zenzeros.kimon.ui.theme.KimonShapeDefaults.topListItemShape
import com.zenzeros.kimon.ui.theme.LocalAppFonts
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val GITHUB_PROFILE_URL = "https://github.com/zenzer0s"
private const val GITHUB_REPO_URL = "https://github.com/zenzer0s/kimon"
private const val GITHUB_ISSUES_URL = "https://github.com/zenzer0s/kimon/issues"
private const val GITHUB_AVATAR_URL = "https://avatars.githubusercontent.com/u/151006009?v=4"
private const val TELEGRAM_SUPPORT_URL = "https://t.me/zenzer0s_support"
private const val BUYMEACOFFEE_URL = "https://buymeacoffee.com/zenzer0s"
private const val UPI_ID = "zenzero@slc"

@Composable
fun AboutSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val cookieShape = MaterialShapes.Cookie7Sided.toShape()
    val cloverShape = MaterialShapes.Clover4Leaf.toShape()
    val squircleShape = MaterialShapes.Cookie12Sided.toShape()

    val packageInfo = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (_: Exception) {
            null
        }
    }
    val versionName = packageInfo?.versionName ?: "?"
    val versionCode = packageInfo?.longVersionCode ?: 0L
    val installedDate = remember(packageInfo) {
        packageInfo?.firstInstallTime?.let {
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))
        } ?: "Installed"
    }

    var isCheckingUpdate by remember { mutableStateOf(false) }

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
                        ),
                        modifier = Modifier.padding(start = 12.dp)
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
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            contentPadding = innerPadding,
            modifier = Modifier
                .fillMaxSize()
                .bouncyScroll()
                .padding(horizontal = 16.dp)
        ) {
            // --- 1. Vivi-Music Styled Big Header & Subtitle Pill ---
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(R.mipmap.ic_launcher)
                            .build(),
                        contentDescription = "Kimon app icon",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(96.dp)
                            .clip(squircleShape)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "KIMON",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 44.sp,
                            letterSpacing = 2.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Version Pill Badge with Border
                    Row(
                        modifier = Modifier
                            .border(
                                width = 1.5.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_sparkles),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "v$versionName • Material 3 Expressive",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // --- 2. Action Badges Row (UPI, Coffee) ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // UPI Badge
                    Surface(
                        onClick = { openUpiPayment(context, UPI_ID, "zenzeros") },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.currency_rupee_upi),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "UPI",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Coffee Badge
                    Surface(
                        onClick = { openUrl(context, BUYMEACOFFEE_URL) },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_buymeacoffee),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Coffee",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // --- 3. DEVELOPER SECTION ---
            item {
                Text(
                    text = "Developer",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 6.dp, top = 6.dp, bottom = 4.dp)
                )
            }

            item {
                AboutItem(
                    icon = null,
                    customIconContent = {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(GITHUB_AVATAR_URL)
                                .crossfade(true)
                                .build(),
                            placeholder = painterResource(R.drawable.ic_profile),
                            error = painterResource(R.drawable.ic_profile),
                            contentDescription = "zenzer0s avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(cookieShape)
                        )
                    },
                    title = "zenzeros",
                    subtitle = "Lead Developer",
                    subtitleColor = MaterialTheme.colorScheme.primary,
                    shape = singleListItemShape,
                    isExternal = true,
                    onClick = { openUrl(context, GITHUB_PROFILE_URL) }
                )
            }

            // --- 4. COMMUNITY SECTION ---
            item {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Community",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 6.dp, top = 6.dp, bottom = 4.dp)
                )
            }

            item {
                AboutItem(
                    icon = R.drawable.ic_telegram,
                    title = "Support Group",
                    subtitle = "t.me/zenzer0s_support",
                    shape = topListItemShape,
                    isExternal = true,
                    onClick = { openUrl(context, TELEGRAM_SUPPORT_URL) }
                )
            }

            item {
                AboutItem(
                    icon = R.drawable.ic_github,
                    title = "GitHub Repository",
                    subtitle = "github.com/zenzer0s/kimon",
                    shape = middleListItemShape,
                    isExternal = true,
                    onClick = { openUrl(context, GITHUB_REPO_URL) }
                )
            }

            item {
                AboutItem(
                    icon = R.drawable.ic_sparkles,
                    title = "Contribute & Feedback",
                    subtitle = "github.com/zenzer0s/kimon/issues",
                    shape = bottomListItemShape,
                    isExternal = true,
                    onClick = { openUrl(context, GITHUB_ISSUES_URL) }
                )
            }

            // --- 5. APP INFO SECTION ---
            item {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "App Info",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 6.dp, top = 6.dp, bottom = 4.dp)
                )
            }

            item {
                AboutItem(
                    icon = R.drawable.deployed_app_update,
                    title = stringResource(R.string.update_check_title),
                    subtitle = if (isCheckingUpdate) {
                        stringResource(R.string.update_checking)
                    } else {
                        "Latest stable release from GitHub"
                    },
                    shape = topListItemShape,
                    isExternal = false,
                    onClick = {
                        if (!isCheckingUpdate) {
                            isCheckingUpdate = true
                            scope.launch {
                                val result = UpdateChecker.checkForUpdate(context, force = true)
                                isCheckingUpdate = false
                                when (result) {
                                    is UpdateChecker.Result.UpdateAvailable ->
                                        openUrl(context, result.url)
                                    is UpdateChecker.Result.UpToDate ->
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.update_up_to_date, result.version),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    UpdateChecker.Result.Failed ->
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.update_check_failed),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                }
                            }
                        }
                    }
                )
            }

            item {
                AboutItem(
                    icon = R.drawable.ic_sparkles,
                    title = "Installed Date",
                    subtitle = installedDate,
                    shape = middleListItemShape,
                    isExternal = false,
                    onClick = null
                )
            }

            item {
                AboutItem(
                    icon = R.drawable.ic_list,
                    title = "Version",
                    subtitle = "$versionName ($versionCode)",
                    shape = middleListItemShape,
                    isExternal = false,
                    onClick = null
                )
            }

            item {
                AboutItem(
                    icon = R.drawable.ic_trophy,
                    title = "License",
                    subtitle = "PolyForm Noncommercial 1.0.0",
                    shape = bottomListItemShape,
                    isExternal = true,
                    onClick = { openUrl(context, GITHUB_REPO_URL + "/blob/master/LICENSE.md") }
                )
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun AboutItem(
    icon: Int?,
    title: String,
    subtitle: String,
    shape: Shape,
    modifier: Modifier = Modifier,
    customIconContent: (@Composable () -> Unit)? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    iconContainerColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
    subtitleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    isExternal: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Surface(
        shape = shape,
        color = listItemColors.containerColor,
        border = CustomColors.cardBorder,
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (customIconContent != null) {
                customIconContent()
            } else if (icon != null) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = iconContainerColor,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.5.sp,
                        lineHeight = 16.sp
                    ),
                    color = subtitleColor
                )
            }

            if (isExternal && onClick != null) {
                Icon(
                    painter = painterResource(R.drawable.open_in_new_icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
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
        Toast.makeText(context, "Copied UPI ID: $upiId", Toast.LENGTH_LONG).show()
    }
}
