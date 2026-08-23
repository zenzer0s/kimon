package com.zenzeros.kimon.domain.usecase

import com.zenzeros.kimon.data.local.entity.FocusSessionEntity
import com.zenzeros.kimon.domain.model.StreaksResult
import java.util.Calendar
import java.util.TreeSet

class CalculateStreaksUseCase {

    operator fun invoke(sessions: List<FocusSessionEntity>): StreaksResult {
        if (sessions.isEmpty()) {
            return StreaksResult()
        }

        // Group into distinct calendar days (epoch day number)
        val cal = Calendar.getInstance()
        val distinctDays = TreeSet<Long>()

        for (session in sessions) {
            if (session.sessionType == "POMODORO" && session.actualDurationSeconds > 0) {
                cal.timeInMillis = session.startTimeEpochMs
                val year = cal.get(Calendar.YEAR)
                val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
                // Unique integer day representation: year * 1000 + dayOfYear
                distinctDays.add((year * 1000L) + dayOfYear)
            }
        }

        if (distinctDays.isEmpty()) {
            return StreaksResult()
        }

        // Current day identifier
        cal.timeInMillis = System.currentTimeMillis()
        val todayId = (cal.get(Calendar.YEAR) * 1000L) + cal.get(Calendar.DAY_OF_YEAR)

        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayId = (cal.get(Calendar.YEAR) * 1000L) + cal.get(Calendar.DAY_OF_YEAR)

        val sortedDays = distinctDays.toList()
        var bestStreak = 0
        var currentStreak = 0
        var tempStreak = 0
        var prevDayCal: Calendar? = null

        for (dayId in sortedDays) {
            val year = (dayId / 1000).toInt()
            val dayOfYear = (dayId % 1000).toInt()

            val curDayCal = Calendar.getInstance()
            curDayCal.set(Calendar.YEAR, year)
            curDayCal.set(Calendar.DAY_OF_YEAR, dayOfYear)

            if (prevDayCal != null) {
                val testCal = prevDayCal.clone() as Calendar
                testCal.add(Calendar.DAY_OF_YEAR, 1)

                val isConsecutive = testCal.get(Calendar.YEAR) == year && testCal.get(Calendar.DAY_OF_YEAR) == dayOfYear
                if (isConsecutive) {
                    tempStreak++
                } else {
                    tempStreak = 1
                }
            } else {
                tempStreak = 1
            }

            if (tempStreak > bestStreak) {
                bestStreak = tempStreak
            }

            prevDayCal = curDayCal
        }

        // Check if current streak is active (includes today or yesterday)
        val lastActiveDayId = sortedDays.last()
        if (lastActiveDayId == todayId || lastActiveDayId == yesterdayId) {
            // Count backward from last active day
            var streakCount = 1
            var checkCal = Calendar.getInstance()
            checkCal.set(Calendar.YEAR, (lastActiveDayId / 1000).toInt())
            checkCal.set(Calendar.DAY_OF_YEAR, (lastActiveDayId % 1000).toInt())

            for (i in sortedDays.size - 2 downTo 0) {
                val prevId = sortedDays[i]
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
                val expectedId = (checkCal.get(Calendar.YEAR) * 1000L) + checkCal.get(Calendar.DAY_OF_YEAR)

                if (prevId == expectedId) {
                    streakCount++
                } else {
                    break
                }
            }
            currentStreak = streakCount
        }

        return StreaksResult(
            currentStreakDays = currentStreak,
            bestStreakDays = bestStreak,
            totalFocusDays = distinctDays.size
        )
    }
}
