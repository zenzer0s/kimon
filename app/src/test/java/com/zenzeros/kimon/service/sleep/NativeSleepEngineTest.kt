package com.zenzeros.kimon.service.sleep

import com.zenzeros.kimon.service.sleep.native.NativeEpochData
import com.zenzeros.kimon.service.sleep.native.NativeSleepAnalysisResult
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class NativeSleepEngineTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun testEpochDataSerialization() {
        val epoch = NativeEpochData(
            timestampMs = 1700000000000L,
            durationSeconds = 60,
            activityCount = 12.5f,
            variance = 0.04f,
            meanLightLux = 1.2f,
            screenOn = false,
            charging = true
        )

        val encoded = json.encodeToString(epoch)
        val decoded = json.decodeFromString<NativeEpochData>(encoded)

        assertEquals(epoch.timestampMs, decoded.timestampMs)
        assertEquals(epoch.activityCount, decoded.activityCount)
        assertEquals(epoch.screenOn, decoded.screenOn)
        assertEquals(epoch.charging, decoded.charging)
    }

    @Test
    fun testAnalysisResultDeserialization() {
        val mockJson = """
            {
                "sleep_onset_time_ms": 1700000900000,
                "wake_time_ms": 1700029700000,
                "total_duration_minutes": 480,
                "sleep_duration_minutes": 440,
                "wake_duration_minutes": 40,
                "sleep_onset_latency_minutes": 15,
                "sleep_efficiency": 91.6,
                "quality_score": 88,
                "deep_sleep_minutes": 120,
                "light_sleep_minutes": 220,
                "rem_sleep_minutes": 100,
                "wake_count": 2,
                "epoch_states": [0, 0, 1, 0],
                "epoch_stages": [1, 2, 0, 3]
            }
        """.trimIndent()

        val result = json.decodeFromString<NativeSleepAnalysisResult>(mockJson)
        assertNotNull(result)
        assertEquals(480L, result.totalDurationMinutes)
        assertEquals(440L, result.sleepDurationMinutes)
        assertEquals(88, result.qualityScore)
        assertEquals(2, result.wakeCount)
        assertEquals(4, result.epochStates.size)
        assertEquals(4, result.epochStages.size)
    }

    @Test
    fun testSleepAppUsageSerialization() {
        val event = com.zenzeros.kimon.service.sleep.usage.SleepAppUsageEvent(
            packageName = "com.google.android.youtube",
            appName = "YouTube",
            startTimeEpochMs = 1700010000000L,
            endTimeEpochMs = 1700010720000L,
            durationSeconds = 720
        )

        val list = listOf(event)
        val encoded = json.encodeToString(list)
        val decoded = json.decodeFromString<List<com.zenzeros.kimon.service.sleep.usage.SleepAppUsageEvent>>(encoded)

        assertEquals(1, decoded.size)
        assertEquals("YouTube", decoded[0].appName)
        assertEquals("com.google.android.youtube", decoded[0].packageName)
        assertEquals(720L, decoded[0].durationSeconds)
    }
}

