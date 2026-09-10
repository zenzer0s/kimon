package com.zenzeros.kimon

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.zenzeros.kimon.ui.KimonApp
import com.zenzeros.kimon.widget.FocusHeatmapWidgetProvider
import com.zenzeros.kimon.widget.LastNightSleepWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    @Volatile
    private var contentReady = false

    // Deep-link target from a notification tap (e.g. "sleep"); consumed by KimonApp.
    private var pendingNavTarget by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { !contentReady }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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

    companion object {
        const val EXTRA_NAVIGATE_TO = "navigate_to"
    }
}
