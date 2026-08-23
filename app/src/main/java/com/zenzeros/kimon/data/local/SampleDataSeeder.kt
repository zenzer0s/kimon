package com.zenzeros.kimon.data.local

import com.zenzeros.kimon.data.local.entity.FocusSessionEntity
import com.zenzeros.kimon.data.local.entity.TagEntity
import com.zenzeros.kimon.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object SampleDataSeeder {

    suspend fun seedSampleDataIfEmpty(database: KimonDatabase) {
        val existingTags = database.tagDao().getAllActiveTags().first()
        val existingSessions = database.focusSessionDao().getAllSessions().first()

        if (existingTags.isNotEmpty() && existingSessions.isNotEmpty()) {
            return
        }

        // 1. Seed Tags
        val tagCoding = TagEntity(name = "Coding", colorHex = "#6366F1")
        val tagStudy = TagEntity(name = "Study", colorHex = "#8B5CF6")
        val tagDesign = TagEntity(name = "Design", colorHex = "#EC4899")
        val tagWriting = TagEntity(name = "Writing", colorHex = "#10B981")
        val tagExercise = TagEntity(name = "Exercise", colorHex = "#F59E0B")

        val tagCodingId = database.tagDao().insertTag(tagCoding)
        val tagStudyId = database.tagDao().insertTag(tagStudy)
        val tagDesignId = database.tagDao().insertTag(tagDesign)
        val tagWritingId = database.tagDao().insertTag(tagWriting)
        val tagExerciseId = database.tagDao().insertTag(tagExercise)

        val allTagIds = listOf(tagCodingId, tagStudyId, tagDesignId, tagWritingId, tagExerciseId)

        // 2. Seed Tasks
        val tasks = listOf(
            TaskEntity(
                title = "Refactor Compose UI Architecture",
                category = "Coding",
                estimatedPomodoros = 4,
                completedPomodoros = 4,
                isCompleted = true
            ),
            TaskEntity(
                title = "Study Kotlin Coroutines & StateFlow",
                category = "Study",
                estimatedPomodoros = 2,
                completedPomodoros = 2,
                isCompleted = true
            ),
            TaskEntity(
                title = "Design Expressive BottomSheet & Dial",
                category = "Design",
                estimatedPomodoros = 3,
                completedPomodoros = 1,
                isCompleted = false
            ),
            TaskEntity(
                title = "Write Architecture & Setup Guide",
                category = "Writing",
                estimatedPomodoros = 2,
                completedPomodoros = 0,
                isCompleted = false
            ),
            TaskEntity(
                title = "Daily Focus Plan Review",
                category = "Focus",
                estimatedPomodoros = 1,
                completedPomodoros = 0,
                isCompleted = false
            )
        )
        tasks.forEach { database.taskDao().insertTask(it) }

        // 3. Seed Realistic Focus Sessions
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now()
        val sessionsToInsert = mutableListOf<FocusSessionEntity>()

        // A. Today's Sessions (Timeline + Tag Distributions + Today Focus)
        val todaySchedule = listOf(
            Triple(LocalTime.of(9, 0), 25 * 60, tagCodingId),
            Triple(LocalTime.of(9, 30), 5 * 60, null), // break
            Triple(LocalTime.of(9, 40), 25 * 60, tagCodingId),
            Triple(LocalTime.of(10, 30), 25 * 60, tagStudyId),
            Triple(LocalTime.of(11, 15), 25 * 60, tagDesignId),
            Triple(LocalTime.of(14, 0), 25 * 60, tagWritingId),
            Triple(LocalTime.of(15, 30), 25 * 60, tagCodingId)
        )

        todaySchedule.forEach { (time, duration, tagId) ->
            val startEpoch = LocalDateTime.of(today, time).atZone(zoneId).toInstant().toEpochMilli()
            val endEpoch = startEpoch + duration * 1000L
            sessionsToInsert.add(
                FocusSessionEntity(
                    startTimeEpochMs = startEpoch,
                    endTimeEpochMs = endEpoch,
                    targetDurationSeconds = duration,
                    actualDurationSeconds = duration,
                    sessionType = if (tagId == null) "SHORT_BREAK" else "POMODORO",
                    tagId = tagId
                )
            )
        }

        // B. Past 6 Days (Week Tab + Streaks + Overview)
        for (dayOffset in 1..6) {
            val date = today.minusDays(dayOffset.toLong())
            val sessionCount = 3 + (dayOffset % 4) // 3 to 6 sessions
            for (i in 0 until sessionCount) {
                val startHour = 9 + (i * 2)
                val startEpoch = LocalDateTime.of(date, LocalTime.of(startHour, 15)).atZone(zoneId).toInstant().toEpochMilli()
                val duration = 25 * 60
                val tagId = allTagIds[(dayOffset + i) % allTagIds.size]
                sessionsToInsert.add(
                    FocusSessionEntity(
                        startTimeEpochMs = startEpoch,
                        endTimeEpochMs = startEpoch + duration * 1000L,
                        targetDurationSeconds = duration,
                        actualDurationSeconds = duration,
                        sessionType = "POMODORO",
                        tagId = tagId
                    )
                )
            }
        }

        // C. Past 60 Days (Overview Heatmap + Year Tab)
        for (dayOffset in 7..60 step 2) {
            val date = today.minusDays(dayOffset.toLong())
            val sessionCount = 2 + (dayOffset % 3)
            for (i in 0 until sessionCount) {
                val startHour = 10 + (i * 3)
                val startEpoch = LocalDateTime.of(date, LocalTime.of(startHour, 0)).atZone(zoneId).toInstant().toEpochMilli()
                val duration = 25 * 60
                val tagId = allTagIds[(dayOffset + i) % allTagIds.size]
                sessionsToInsert.add(
                    FocusSessionEntity(
                        startTimeEpochMs = startEpoch,
                        endTimeEpochMs = startEpoch + duration * 1000L,
                        targetDurationSeconds = duration,
                        actualDurationSeconds = duration,
                        sessionType = "POMODORO",
                        tagId = tagId
                    )
                )
            }
        }

        sessionsToInsert.forEach { database.focusSessionDao().insertSession(it) }
    }
}
