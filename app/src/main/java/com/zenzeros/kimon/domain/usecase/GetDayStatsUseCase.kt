package com.zenzeros.kimon.domain.usecase

import com.zenzeros.kimon.data.repository.SessionRepository
import com.zenzeros.kimon.domain.model.DayStats
import com.zenzeros.kimon.domain.model.TagDistributionItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

class GetDayStatsUseCase(
    private val sessionRepository: SessionRepository
) {
    operator fun invoke(dayCalendar: Calendar): Flow<DayStats> {
        val startOfDay = dayCalendar.clone() as Calendar
        startOfDay.set(Calendar.HOUR_OF_DAY, 0)
        startOfDay.set(Calendar.MINUTE, 0)
        startOfDay.set(Calendar.SECOND, 0)
        startOfDay.set(Calendar.MILLISECOND, 0)

        val endOfDay = dayCalendar.clone() as Calendar
        endOfDay.set(Calendar.HOUR_OF_DAY, 23)
        endOfDay.set(Calendar.MINUTE, 59)
        endOfDay.set(Calendar.SECOND, 59)
        endOfDay.set(Calendar.MILLISECOND, 999)

        return sessionRepository.getSessionsWithTagBetween(startOfDay.timeInMillis, endOfDay.timeInMillis)
            .map { sessionsWithTag ->
                val focusSessions = sessionsWithTag.filter { it.session.sessionType == "POMODORO" }
                val totalSecs = focusSessions.sumOf { it.session.actualDurationSeconds.toLong() }
                val totalCount = focusSessions.size

                // Group by Tag
                val tagGroups = focusSessions.groupBy { it.tag }
                val distributions = tagGroups.map { (tag, list) ->
                    val tagSecs = list.sumOf { it.session.actualDurationSeconds.toLong() }
                    val percentage = if (totalSecs > 0) (tagSecs.toFloat() / totalSecs) else 0f
                    TagDistributionItem(tag = tag, totalDurationSeconds = tagSecs, percentage = percentage)
                }.sortedByDescending { it.totalDurationSeconds }

                DayStats(
                    totalFocusSeconds = totalSecs,
                    totalSessions = totalCount,
                    tagDistributions = distributions,
                    timelineSessions = sessionsWithTag
                )
            }
    }
}
