package com.zenzeros.kimon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { !contentReady }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KimonApp(onContentReady = { contentReady = true })
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch(Dispatchers.Default) {
            LastNightSleepWidgetProvider.updateAllWidgets(this@MainActivity)
            FocusHeatmapWidgetProvider.updateAllWidgets(this@MainActivity)
        }
    }
}
