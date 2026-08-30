package com.zenzeros.kimon.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test

class StartupBaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(packageName = "com.zenzeros.kimon") {
        pressHome()
        startActivityAndWait()

        device.wait(Until.hasObject(By.text("Focus")), 10_000)

        for (tab in listOf("Analyze", "Plan", "Sleep", "Focus")) {
            device.findObject(By.text(tab))?.click()
            device.waitForIdle()
        }
    }
}
