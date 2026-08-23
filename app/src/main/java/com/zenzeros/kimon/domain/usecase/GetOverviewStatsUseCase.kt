package com.zenzeros.kimon.domain.usecase

import com.zenzeros.kimon.data.repository.SessionRepository
import com.zenzeros.kimon.domain.model.OverviewStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar

class GetOverviewStatsUseCase(
    private val sessionRepository: SessionRepository,
    private val calculateStreaksUseCase: CalculateStreaksUseCase = CalculateStreaksUseCase()
) {
    operator fun invoke(targetMonthCalendar: Calendar): Flow<OverviewStats> {
        val startOfMonth = targetMonthCalendar.clone() as Calendar
        startOfMonth.set(Calendar.DAY_OF_MONTH, 1)
        startOfMonth.set(Calendar.HOUR_OF_DAY, 0)
        startOfMonth.set(Calendar.MINUTE, 0)
        startOfMonth.set(Calendar.SECOND, 0)
        startOfMonth.set(Calendar.MILLISECOND, 0)

        val endOfMonth = startOfMonth.clone() as Calendar
        endOfMonth.set(Calendar.DAY_OF_MONTH, startOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH))
        endOfMonth.set(Calendar.HOUR_OF_DAY, 23)
        endOfMonth.set(Calendar.MINUTE, 59)
        endOfMonth.set(Calendar.SECOND, 59)
        endOfMonth.set(Calendar.MILLISECOND, 999)

        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val todayEnd = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        return combine(
            sessionRepository.getAllSessions(),
            sessionRepository.getFocusSessionsBetween(startOfMonth.timeInMillis, endOfMonth.timeInMillis),
            sessionRepository.getFocusSessionsBetween(todayStart, todayEnd)
        ) { allSessions, monthSessions, todaySessions ->
            val streaks = calculateStreaksUseCase(allSessions)

            val todayFocusSecs = todaySessions.sumOf { it.actualDurationSeconds.toLong() }
            val todaySessionsCount = todaySessions.size

            val monthActiveDays = mutableSetOf<Int>()
            val cal = Calendar.getInstance()
            var monthTotalFocusSecs = 0L

            for (session in monthSessions) {
                if (session.actualDurationSeconds > 0) {
                    monthTotalFocusSecs += session.actualDurationSeconds
                    cal.timeInMillis = session.startTimeEpochMs
                    monthActiveDays.add(cal.get(Calendar.DAY_OF_MONTH))
                }
            }

            val totalDaysInMonth = startOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
            val monthDaysFocused = monthActiveDays.size
            val monthAvgDailySecs = if (monthDaysFocused > 0) monthTotalFocusSecs / monthDaysFocused else 0L

            val lifetimeFocusSecs = allSessions
                .filter { it.sessionType == "POMODORO" }
                .sumOf { it.actualDurationSeconds.toLong() }

            val lifetimeSessionsCount = allSessions.count { it.sessionType == "POMODORO" }

            OverviewStats(
                todayFocusSeconds = todayFocusSecs,
                todaySessionsCount = todaySessionsCount,
                currentStreak = streaks.currentStreakDays,
                bestStreak = streaks.bestStreakDays,
                monthDaysFocused = monthDaysFocused,
                monthTotalDays = totalDaysInMonth,
                monthAvgDailySeconds = monthAvgDailySecs,
                monthTotalFocusSeconds = monthTotalFocusSecs,
                lifetimeFocusSeconds = lifetimeFocusSecs,
                lifetimeSessionsCount = lifetimeSessionsCount,
                lifetimeFocusDays = streaks.totalFocusDays,
                monthActiveDays = monthActiveDays
            )
        }
    }
}
