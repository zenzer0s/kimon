package com.zenzeros.kimon.domain.usecase

import com.zenzeros.kimon.data.local.entity.FocusSessionEntity
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class CalculateStreaksUseCaseTest {

    private lateinit var calculateStreaksUseCase: CalculateStreaksUseCase

    @Before
    fun setup() {
        calculateStreaksUseCase = CalculateStreaksUseCase()
    }

    @Test
    fun `empty sessions returns all zeros`() {
        val result = calculateStreaksUseCase(emptyList())
        assertEquals(0, result.currentStreakDays)
        assertEquals(0, result.bestStreakDays)
        assertEquals(0, result.totalFocusDays)
    }

    @Test
    fun `breaks only does not count towards streaks`() {
        val now = System.currentTimeMillis()
        val sessions = listOf(
            FocusSessionEntity(
                sessionType = "SHORT_BREAK",
                startTimeEpochMs = now,
                endTimeEpochMs = now + 300000,
                targetDurationSeconds = 300,
                actualDurationSeconds = 300
            ),
            FocusSessionEntity(
                sessionType = "LONG_BREAK",
                startTimeEpochMs = now,
                endTimeEpochMs = now + 900000,
                targetDurationSeconds = 900,
                actualDurationSeconds = 900
            )
        )
        val result = calculateStreaksUseCase(sessions)
        assertEquals(0, result.currentStreakDays)
        assertEquals(0, result.bestStreakDays)
        assertEquals(0, result.totalFocusDays)
    }

    @Test
    fun `single session today gives 1 day streak`() {
        val now = System.currentTimeMillis()
        val sessions = listOf(
            FocusSessionEntity(
                sessionType = "POMODORO",
                startTimeEpochMs = now,
                endTimeEpochMs = now + 1500000,
                targetDurationSeconds = 1500,
                actualDurationSeconds = 1500
            )
        )
        val result = calculateStreaksUseCase(sessions)
        assertEquals(1, result.currentStreakDays)
        assertEquals(1, result.bestStreakDays)
        assertEquals(1, result.totalFocusDays)
    }

    @Test
    fun `single session yesterday maintains 1 day current streak`() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = cal.timeInMillis

        val sessions = listOf(
            FocusSessionEntity(
                sessionType = "POMODORO",
                startTimeEpochMs = yesterday,
                endTimeEpochMs = yesterday + 1500000,
                targetDurationSeconds = 1500,
                actualDurationSeconds = 1500
            )
        )
        val result = calculateStreaksUseCase(sessions)
        assertEquals(1, result.currentStreakDays)
        assertEquals(1, result.bestStreakDays)
        assertEquals(1, result.totalFocusDays)
    }

    @Test
    fun `consecutive 3 days gives 3 day streak`() {
        val sessions = mutableListOf<FocusSessionEntity>()
        for (i in 0..2) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val time = cal.timeInMillis

            sessions.add(
                FocusSessionEntity(
                    sessionType = "POMODORO",
                    startTimeEpochMs = time,
                    endTimeEpochMs = time + 1500000,
                    targetDurationSeconds = 1500,
                    actualDurationSeconds = 1500
                )
            )
        }

        val result = calculateStreaksUseCase(sessions)
        assertEquals(3, result.currentStreakDays)
        assertEquals(3, result.bestStreakDays)
        assertEquals(3, result.totalFocusDays)
    }

    @Test
    fun `multiple sessions on same day counts as 1 distinct day`() {
        val now = System.currentTimeMillis()
        val sessions = listOf(
            FocusSessionEntity(
                sessionType = "POMODORO",
                startTimeEpochMs = now,
                endTimeEpochMs = now + 1500000,
                targetDurationSeconds = 1500,
                actualDurationSeconds = 1500
            ),
            FocusSessionEntity(
                sessionType = "POMODORO",
                startTimeEpochMs = now + 2000000,
                endTimeEpochMs = now + 3500000,
                targetDurationSeconds = 1500,
                actualDurationSeconds = 1500
            )
        )

        val result = calculateStreaksUseCase(sessions)
        assertEquals(1, result.currentStreakDays)
        assertEquals(1, result.bestStreakDays)
        assertEquals(1, result.totalFocusDays)
    }

    @Test
    fun `broken streak calculates best streak and current streak correctly`() {
        val sessions = mutableListOf<FocusSessionEntity>()

        // 4 consecutive days in the past (10 to 7 days ago)
        for (i in 7..10) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            sessions.add(
                FocusSessionEntity(
                    sessionType = "POMODORO",
                    startTimeEpochMs = cal.timeInMillis,
                    endTimeEpochMs = cal.timeInMillis + 1500000,
                    targetDurationSeconds = 1500,
                    actualDurationSeconds = 1500
                )
            )
        }

        // 2 consecutive days currently (1 day ago to today)
        for (i in 0..1) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            sessions.add(
                FocusSessionEntity(
                    sessionType = "POMODORO",
                    startTimeEpochMs = cal.timeInMillis,
                    endTimeEpochMs = cal.timeInMillis + 1500000,
                    targetDurationSeconds = 1500,
                    actualDurationSeconds = 1500
                )
            )
        }

        val result = calculateStreaksUseCase(sessions)
        assertEquals(2, result.currentStreakDays)
        assertEquals(4, result.bestStreakDays)
        assertEquals(6, result.totalFocusDays)
    }
}
