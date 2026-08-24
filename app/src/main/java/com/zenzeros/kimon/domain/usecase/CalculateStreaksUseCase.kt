package com.zenzeros.kimon.domain.usecase

import com.zenzeros.kimon.data.local.entity.FocusSessionEntity
import com.zenzeros.kimon.domain.model.StreaksResult
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.TreeSet

class CalculateStreaksUseCase {

    operator fun invoke(sessions: List<FocusSessionEntity>): StreaksResult {
        if (sessions.isEmpty()) {
            return StreaksResult()
        }

        val zone = ZoneId.systemDefault()
        val distinctEpochDays = TreeSet<Long>()

        for (session in sessions) {
            if (session.sessionType == "POMODORO" && session.actualDurationSeconds > 0) {
                val epochDay = Instant.ofEpochMilli(session.startTimeEpochMs).atZone(zone).toLocalDate().toEpochDay()
                distinctEpochDays.add(epochDay)
            }
        }

        if (distinctEpochDays.isEmpty()) {
            return StreaksResult()
        }

        val todayEpochDay = LocalDate.now(zone).toEpochDay()
        val yesterdayEpochDay = todayEpochDay - 1

        val sortedDays = distinctEpochDays.toList()
        var bestStreak = 0
        var tempStreak = 0
        var prevEpochDay: Long? = null

        for (epochDay in sortedDays) {
            if (prevEpochDay != null && epochDay == prevEpochDay + 1) {
                tempStreak++
            } else {
                tempStreak = 1
            }

            if (tempStreak > bestStreak) {
                bestStreak = tempStreak
            }

            prevEpochDay = epochDay
        }

        // Check if current streak is active (includes today or yesterday)
        var currentStreak = 0
        val lastActiveDay = sortedDays.last()
        if (lastActiveDay == todayEpochDay || lastActiveDay == yesterdayEpochDay) {
            var streakCount = 1
            var expectedEpochDay = lastActiveDay - 1

            for (i in sortedDays.size - 2 downTo 0) {
                val prevDay = sortedDays[i]
                if (prevDay == expectedEpochDay) {
                    streakCount++
                    expectedEpochDay--
                } else {
                    break
                }
            }
            currentStreak = streakCount
        }

        return StreaksResult(
            currentStreakDays = currentStreak,
            bestStreakDays = bestStreak,
            totalFocusDays = distinctEpochDays.size
        )
    }
}
