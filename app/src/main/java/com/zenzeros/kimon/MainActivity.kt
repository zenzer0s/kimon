package com.zenzeros.kimon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.zenzeros.kimon.ui.KimonApp
import com.zenzeros.kimon.widget.FocusHeatmapWidgetProvider
import com.zenzeros.kimon.widget.LastNightSleepWidgetProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        updateWidgets()
        setContent {
            KimonApp()
        }
    }

    override fun onResume() {
        super.onResume()
        updateWidgets()
    }

    private fun updateWidgets() {
        LastNightSleepWidgetProvider.updateAllWidgets(this)
        FocusHeatmapWidgetProvider.updateAllWidgets(this)
    }
}