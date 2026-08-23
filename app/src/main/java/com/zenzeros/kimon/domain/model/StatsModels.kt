package com.zenzeros.kimon.domain.model

import com.zenzeros.kimon.data.local.entity.FocusSessionWithTag
import com.zenzeros.kimon.data.local.entity.TagEntity

data class StreaksResult(
    val currentStreakDays: Int = 0,
    val bestStreakDays: Int = 0,
    val totalFocusDays: Int = 0
)

data class TagDistributionItem(
    val tag: TagEntity?,
    val totalDurationSeconds: Long,
    val percentage: Float
)

data class DayStats(
    val totalFocusSeconds: Long = 0,
    val totalSessions: Int = 0,
    val tagDistributions: List<TagDistributionItem> = emptyList(),
    val timelineSessions: List<FocusSessionWithTag> = emptyList()
)

data class WeekStats(
    val totalFocusSeconds: Long = 0,
    val totalSessions: Int = 0,
    val tagDistributions: List<TagDistributionItem> = emptyList(),
    val dailyMinutes: List<Int> = List(7) { 0 } // Mon to Sun minutes
)

data class YearStats(
    val totalFocusSeconds: Long = 0,
    val totalSessions: Int = 0,
    val avgSessionSeconds: Long = 0,
    val focusDaysCount: Int = 0,
    val bestStreakDays: Int = 0,
    val bestDaySeconds: Long = 0,
    val bestWeekSeconds: Long = 0,
    val bestMonthSeconds: Long = 0,
    val activeDaysSet: Set<String> = emptySet() // Set of "yyyy-MM-dd"
)

data class OverviewStats(
    val todayFocusSeconds: Long = 0,
    val todaySessionsCount: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val monthDaysFocused: Int = 0,
    val monthTotalDays: Int = 31,
    val monthAvgDailySeconds: Long = 0,
    val monthTotalFocusSeconds: Long = 0,
    val lifetimeFocusSeconds: Long = 0,
    val lifetimeSessionsCount: Int = 0,
    val lifetimeFocusDays: Int = 0,
    val monthActiveDays: Set<Int> = emptySet()
)
