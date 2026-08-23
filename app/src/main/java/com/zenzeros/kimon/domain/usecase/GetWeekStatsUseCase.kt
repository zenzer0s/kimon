package com.zenzeros.kimon.domain.usecase

import com.zenzeros.kimon.data.repository.SessionRepository
import com.zenzeros.kimon.domain.model.TagDistributionItem
import com.zenzeros.kimon.domain.model.WeekStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

class GetWeekStatsUseCase(
    private val sessionRepository: SessionRepository
) {
    operator fun invoke(weekStartCalendar: Calendar): Flow<WeekStats> {
        val startOfWeek = weekStartCalendar.clone() as Calendar
        startOfWeek.set(Calendar.HOUR_OF_DAY, 0)
        startOfWeek.set(Calendar.MINUTE, 0)
        startOfWeek.set(Calendar.SECOND, 0)
        startOfWeek.set(Calendar.MILLISECOND, 0)

        val endOfWeek = startOfWeek.clone() as Calendar
        endOfWeek.add(Calendar.DAY_OF_YEAR, 6)
        endOfWeek.set(Calendar.HOUR_OF_DAY, 23)
        endOfWeek.set(Calendar.MINUTE, 59)
        endOfWeek.set(Calendar.SECOND, 59)
        endOfWeek.set(Calendar.MILLISECOND, 999)

        return sessionRepository.getSessionsWithTagBetween(startOfWeek.timeInMillis, endOfWeek.timeInMillis)
            .map { sessionsWithTag ->
                val focusSessions = sessionsWithTag.filter { it.session.sessionType == "POMODORO" }
                val totalSecs = focusSessions.sumOf { it.session.actualDurationSeconds.toLong() }
                val totalCount = focusSessions.size

                // Tag distributions
                val tagGroups = focusSessions.groupBy { it.tag }
                val distributions = tagGroups.map { (tag, list) ->
                    val tagSecs = list.sumOf { it.session.actualDurationSeconds.toLong() }
                    val percentage = if (totalSecs > 0) (tagSecs.toFloat() / totalSecs) else 0f
                    TagDistributionItem(tag = tag, totalDurationSeconds = tagSecs, percentage = percentage)
                }.sortedByDescending { it.totalDurationSeconds }

                // 7 days daily minutes (Monday=0 ... Sunday=6)
                val dailyMins = IntArray(7)
                val cal = Calendar.getInstance()
                for (item in focusSessions) {
                    cal.timeInMillis = item.session.startTimeEpochMs
                    val dayIndex = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
                    dailyMins[dayIndex] += (item.session.actualDurationSeconds / 60)
                }

                WeekStats(
                    totalFocusSeconds = totalSecs,
                    totalSessions = totalCount,
                    tagDistributions = distributions,
                    dailyMinutes = dailyMins.toList()
                )
            }
    }
}
