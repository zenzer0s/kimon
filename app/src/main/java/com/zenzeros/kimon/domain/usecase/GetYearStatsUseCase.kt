package com.zenzeros.kimon.domain.usecase

import com.zenzeros.kimon.data.repository.SessionRepository
import com.zenzeros.kimon.domain.model.YearStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoField

class GetYearStatsUseCase(
    private val sessionRepository: SessionRepository,
    private val calculateStreaksUseCase: CalculateStreaksUseCase = CalculateStreaksUseCase()
) {
    operator fun invoke(year: Int): Flow<YearStats> {
        val zone = ZoneId.systemDefault()
        val startOfYear = LocalDate.of(year, 1, 1).atStartOfDay(zone).toInstant().toEpochMilli()
        val endOfYear = LocalDate.of(year, 12, 31).atTime(23, 59, 59, 999_000_000).atZone(zone).toInstant().toEpochMilli()

        return combine(
            sessionRepository.getAllSessions(),
            sessionRepository.getFocusSessionsBetween(startOfYear, endOfYear)
        ) { allSessions, yearSessions ->
            val streaks = calculateStreaksUseCase(allSessions)

            val totalSecs = yearSessions.sumOf { it.actualDurationSeconds.toLong() }
            val totalCount = yearSessions.size
            val avgSessionSecs = if (totalCount > 0) totalSecs / totalCount else 0L

            val activeDays = mutableSetOf<String>()
            val dayTotals = LongArray(367)
            val weekTotals = LongArray(54)
            val monthTotals = LongArray(12)

            for (session in yearSessions) {
                if (session.actualDurationSeconds > 0) {
                    val localDate = Instant.ofEpochMilli(session.startTimeEpochMs).atZone(zone).toLocalDate()
                    val y = localDate.year
                    val m = localDate.monthValue
                    val d = localDate.dayOfMonth
                    val dayKey = "$y-${if (m < 10) "0$m" else "$m"}-${if (d < 10) "0$d" else "$d"}"
                    activeDays.add(dayKey)

                    val dayOfYear = localDate.get(ChronoField.DAY_OF_YEAR).coerceIn(1, 366)
                    val weekOfYear = localDate.get(ChronoField.ALIGNED_WEEK_OF_YEAR).coerceIn(1, 53)
                    val month = (m - 1).coerceIn(0, 11)

                    val dur = session.actualDurationSeconds.toLong()
                    dayTotals[dayOfYear] += dur
                    weekTotals[weekOfYear] += dur
                    monthTotals[month] += dur
                }
            }

            val bestDaySecs = dayTotals.maxOrNull() ?: 0L
            val bestWeekSecs = weekTotals.maxOrNull() ?: 0L
            val bestMonthSecs = monthTotals.maxOrNull() ?: 0L

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
