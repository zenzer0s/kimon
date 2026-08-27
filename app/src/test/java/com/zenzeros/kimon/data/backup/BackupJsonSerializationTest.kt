package com.zenzeros.kimon.data.backup

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class BackupJsonSerializationTest {

    private lateinit var json: Json

    @Before
    fun setup() {
        json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }

    @Test
    fun `full backup serializes and deserializes accurately`() {
        val originalBackup = KimonBackup(
            version = 1,
            appVersion = "1.0",
            backupDateEpochMs = 1787814877328L,
            settings = KimonSettingsBackup(
                workDurationMinutes = 30,
                shortBreakMinutes = 7,
                longBreakMinutes = 20,
                sessionsBeforeLongBreak = 5,
                dailyGoalMinutes = 180,
                autoStartBreaks = true,
                autoStartPomodoros = true,
                keepScreenOn = true,
                dndEnabled = true,
                soundEnabled = true,
                vibrationEnabled = false,
                headphoneMode = true,
                alarmSoundUri = "content://media/internal/audio/media/42",
                alarmSoundTitle = "Digital Alarm",
                themeMode = "DARK",
                themePalette = "EXPRESSIVE",
                themeColor = "#7C4DFF",
                amoledBlack = true,
                clockStyle = "FLIP",
                sleepMonitoringEnabled = true,
                healthConnectSyncEnabled = true,
                sleepGoalMinutes = 510
            ),
            tags = listOf(
                TagBackup(id = 1, name = "Deep Work", colorHex = "#FF5722", iconName = "ic_sparkles", targetDailyMinutes = 120),
                TagBackup(id = 2, name = "Study", colorHex = "#2196F3", iconName = "ic_book", targetDailyMinutes = 60)
            ),
            focusSessions = listOf(
                FocusSessionBackup(
                    id = 101,
                    tagId = 1,
                    sessionType = "POMODORO",
                    startTimeEpochMs = 1787800000000L,
                    endTimeEpochMs = 1787801800000L,
                    targetDurationSeconds = 1800,
                    actualDurationSeconds = 1800,
                    isCompleted = true,
                    notes = "Finished database refactoring"
                )
            ),
            tasks = listOf(
                TaskBackup(
                    id = 201,
                    title = "Complete Unit Tests",
                    category = "Development",
                    estimatedPomodoros = 3,
                    completedPomodoros = 2,
                    isCompleted = false
                )
            ),
            sleepSessions = listOf(
                SleepSessionBackup(
                    id = 301,
                    startTimeEpochMs = 1787750000000L,
                    endTimeEpochMs = 1787780000000L,
                    durationMinutes = 500,
                    qualityScore = 92,
                    status = 0,
                    source = "GOOGLE_SLEEP_API",
                    dateString = "2026-08-27",
                    syncedToHealthConnect = true,
                    notes = "Restful sleep"
                )
            )
        )

        // 1. Serialize to JSON string
        val jsonString = json.encodeToString(originalBackup)
        assertTrue(jsonString.contains("\"version\": 1"))
        assertTrue(jsonString.contains("\"Deep Work\""))
        assertTrue(jsonString.contains("\"Complete Unit Tests\""))
        assertTrue(jsonString.contains("\"GOOGLE_SLEEP_API\""))

        // 2. Deserialize back to KimonBackup
        val restoredBackup = json.decodeFromString<KimonBackup>(jsonString)

        assertEquals(originalBackup.version, restoredBackup.version)
        assertEquals(originalBackup.appVersion, restoredBackup.appVersion)
        assertEquals(originalBackup.backupDateEpochMs, restoredBackup.backupDateEpochMs)

        // Verify Settings
        assertNotNull(restoredBackup.settings)
        assertEquals(30, restoredBackup.settings?.workDurationMinutes)
        assertEquals(7, restoredBackup.settings?.shortBreakMinutes)
        assertEquals(510, restoredBackup.settings?.sleepGoalMinutes)
        assertEquals("FLIP", restoredBackup.settings?.clockStyle)
        assertEquals(true, restoredBackup.settings?.sleepMonitoringEnabled)

        // Verify Tags
        assertEquals(2, restoredBackup.tags.size)
        assertEquals("Deep Work", restoredBackup.tags[0].name)
        assertEquals("#FF5722", restoredBackup.tags[0].colorHex)
        assertEquals("Study", restoredBackup.tags[1].name)

        // Verify Focus Sessions
        assertEquals(1, restoredBackup.focusSessions.size)
        assertEquals(101L, restoredBackup.focusSessions[0].id)
        assertEquals(1L, restoredBackup.focusSessions[0].tagId)
        assertEquals("POMODORO", restoredBackup.focusSessions[0].sessionType)
        assertEquals("Finished database refactoring", restoredBackup.focusSessions[0].notes)

        // Verify Tasks
        assertEquals(1, restoredBackup.tasks.size)
        assertEquals("Complete Unit Tests", restoredBackup.tasks[0].title)
        assertEquals(3, restoredBackup.tasks[0].estimatedPomodoros)
        assertEquals(false, restoredBackup.tasks[0].isCompleted)

        // Verify Sleep Sessions
        assertEquals(1, restoredBackup.sleepSessions.size)
        assertEquals(500L, restoredBackup.sleepSessions[0].durationMinutes)
        assertEquals(92, restoredBackup.sleepSessions[0].qualityScore)
        assertEquals("2026-08-27", restoredBackup.sleepSessions[0].dateString)
        assertEquals(true, restoredBackup.sleepSessions[0].syncedToHealthConnect)
    }

    @Test
    fun `deserializing minimal backup with defaults succeeds`() {
        val minimalJson = """
            {
                "version": 1,
                "appVersion": "1.0",
                "backupDateEpochMs": 1700000000000
            }
        """.trimIndent()

        val restored = json.decodeFromString<KimonBackup>(minimalJson)
        assertEquals(1, restored.version)
        assertEquals("1.0", restored.appVersion)
        assertEquals(1700000000000L, restored.backupDateEpochMs)
        assertTrue(restored.tags.isEmpty())
        assertTrue(restored.focusSessions.isEmpty())
        assertTrue(restored.tasks.isEmpty())
        assertTrue(restored.sleepSessions.isEmpty())
    }

    @Test
    fun `deserializing JSON with unknown future fields succeeds without crash`() {
        val futureJson = """
            {
                "version": 2,
                "appVersion": "2.0",
                "backupDateEpochMs": 1787814877328,
                "newFeatureFlag": true,
                "futureMetadata": { "cloudSyncId": "xyz123" },
                "tags": [
                    {
                        "id": 1,
                        "name": "Research",
                        "colorHex": "#00E676",
                        "iconName": "ic_terminal",
                        "targetDailyMinutes": 60,
                        "newTagAttribute": 999
                    }
                ]
            }
        """.trimIndent()

        val restored = json.decodeFromString<KimonBackup>(futureJson)
        assertEquals(2, restored.version)
        assertEquals(1, restored.tags.size)
        assertEquals("Research", restored.tags[0].name)
    }

    @Test
    fun `corrupted or non-JSON string fails to decode`() {
        val invalidJson = "This is not a JSON object"
        try {
            json.decodeFromString<KimonBackup>(invalidJson)
            fail("Expected serialization exception for invalid JSON")
        } catch (e: Exception) {
            assertTrue(e is kotlinx.serialization.SerializationException || e is IllegalArgumentException)
        }
    }
}
