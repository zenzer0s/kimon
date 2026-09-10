package com.zenzeros.kimon

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.zenzeros.kimon.ui.KimonApp
import com.zenzeros.kimon.update.UpdateChecker
import com.zenzeros.kimon.widget.FocusHeatmapWidgetProvider
import com.zenzeros.kimon.widget.LastNightSleepWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    @Volatile
    private var contentReady = false

    // Deep-link target from a notification tap (e.g. "sleep"); consumed by KimonApp.
    private var pendingNavTarget by mutableStateOf<String?>(null)

    private var notificationPermissionChecked = false

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Notifications degrade gracefully if denied; nothing to do here. */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { !contentReady }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        maybeRequestNotificationPermission()

        // Throttled check for a newer stable release on GitHub; notifies if found.
        lifecycleScope.launch(Dispatchers.IO) {
            runCatching { UpdateChecker.checkForUpdate(applicationContext) }
        }

        pendingNavTarget = intent?.getStringExtra(EXTRA_NAVIGATE_TO)
        setContent {
            KimonApp(
                onContentReady = { contentReady = true },
                navTarget = pendingNavTarget,
                onNavTargetHandled = { pendingNavTarget = null }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingNavTarget = intent.getStringExtra(EXTRA_NAVIGATE_TO)
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch(Dispatchers.Default) {
            LastNightSleepWidgetProvider.updateAllWidgets(this@MainActivity)
            FocusHeatmapWidgetProvider.updateAllWidgets(this@MainActivity)
        }
    }

    /**
     * Ask for POST_NOTIFICATIONS once on first launch (Android 13+). The pomodoro
     * timer, sleep summaries and step counter all rely on notifications, so without
     * this a fresh install silently shows nothing.
     */
    private fun maybeRequestNotificationPermission() {
        if (notificationPermissionChecked) return
        notificationPermissionChecked = true
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val granted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) return

        val prefs = getSharedPreferences(PERM_PREFS, MODE_PRIVATE)
        val alreadyAsked = prefs.getBoolean(KEY_NOTIF_PERMISSION_ASKED, false)
        // Ask on the first launch, or again while the system is still willing to
        // show the dialog (i.e. the user hasn't permanently dismissed it).
        if (!alreadyAsked || shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
            prefs.edit().putBoolean(KEY_NOTIF_PERMISSION_ASKED, true).apply()
            try {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } catch (_: Exception) {
                // Ignore - permission just stays ungranted
            }
        }
    }

    companion object {
        const val EXTRA_NAVIGATE_TO = "navigate_to"
        private const val PERM_PREFS = "kimon_permission_prefs"
        private const val KEY_NOTIF_PERMISSION_ASKED = "post_notifications_asked"
    }
}
