package com.zenzeros.kimon.domain.usecase

import com.zenzeros.kimon.data.repository.SessionRepository
import com.zenzeros.kimon.domain.model.YearStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class GetYearStatsUseCase(
    private val sessionRepository: SessionRepository,
    private val calculateStreaksUseCase: CalculateStreaksUseCase = CalculateStreaksUseCase()
) {
    operator fun invoke(year: Int): Flow<YearStats> {
        val startOfYear = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfYear = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, Calendar.DECEMBER)
            set(Calendar.DAY_OF_MONTH, 31)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        return combine(
            sessionRepository.getAllSessions(),
            sessionRepository.getFocusSessionsBetween(startOfYear, endOfYear)
        ) { allSessions, yearSessions ->
            val streaks = calculateStreaksUseCase(allSessions)

            val totalSecs = yearSessions.sumOf { it.actualDurationSeconds.toLong() }
            val totalCount = yearSessions.size
            val avgSessionSecs = if (totalCount > 0) totalSecs / totalCount else 0L

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val activeDays = mutableSetOf<String>()

            val cal = Calendar.getInstance()
            val dayTotals = mutableMapOf<Int, Long>() // dayOfYear -> seconds
            val weekTotals = mutableMapOf<Int, Long>() // weekOfYear -> seconds
            val monthTotals = mutableMapOf<Int, Long>() // month -> seconds

            for (session in yearSessions) {
                if (session.actualDurationSeconds > 0) {
                    cal.timeInMillis = session.startTimeEpochMs
                    activeDays.add(dateFormat.format(cal.time))

                    val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
                    val weekOfYear = cal.get(Calendar.WEEK_OF_YEAR)
                    val month = cal.get(Calendar.MONTH)

                    dayTotals[dayOfYear] = (dayTotals[dayOfYear] ?: 0L) + session.actualDurationSeconds
                    weekTotals[weekOfYear] = (weekTotals[weekOfYear] ?: 0L) + session.actualDurationSeconds
                    monthTotals[month] = (monthTotals[month] ?: 0L) + session.actualDurationSeconds
                }
            }

            val bestDaySecs = dayTotals.values.maxOrNull() ?: 0L
            val bestWeekSecs = weekTotals.values.maxOrNull() ?: 0L
            val bestMonthSecs = monthTotals.values.maxOrNull() ?: 0L

            YearStats(
                totalFocusSeconds = totalSecs,
                totalSessions = totalCount,
                avgSessionSeconds = avgSessionSecs,
                focusDaysCount = activeDays.size,
                bestStreakDays = streaks.bestStreakDays,
                bestDaySeconds = bestDaySecs,
                bestWeekSeconds = bestWeekSecs,
                bestMonthSeconds = bestMonthSecs,
                activeDaysSet = activeDays
            )
        }
    }
}
