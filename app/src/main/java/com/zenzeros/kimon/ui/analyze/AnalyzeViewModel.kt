package com.zenzeros.kimon.ui.analyze

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zenzeros.kimon.domain.model.DayStats
import com.zenzeros.kimon.domain.model.OverviewStats
import com.zenzeros.kimon.domain.model.WeekStats
import com.zenzeros.kimon.domain.model.YearStats
import com.zenzeros.kimon.domain.usecase.GetDayStatsUseCase
import com.zenzeros.kimon.domain.usecase.GetOverviewStatsUseCase
import com.zenzeros.kimon.domain.usecase.GetWeekStatsUseCase
import com.zenzeros.kimon.domain.usecase.GetYearStatsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyzeViewModel(
    private val getOverviewStatsUseCase: GetOverviewStatsUseCase,
    private val getDayStatsUseCase: GetDayStatsUseCase,
    private val getWeekStatsUseCase: GetWeekStatsUseCase,
    private val getYearStatsUseCase: GetYearStatsUseCase
) : ViewModel() {

    // --- State Calendars ---
    private val _overviewMonthCalendar = MutableStateFlow(Calendar.getInstance())
    val overviewMonthCalendar: StateFlow<Calendar> = _overviewMonthCalendar.asStateFlow()

    private val _selectedDayCalendar = MutableStateFlow(Calendar.getInstance())
    val selectedDayCalendar: StateFlow<Calendar> = _selectedDayCalendar.asStateFlow()

    private val _selectedWeekStartCalendar = MutableStateFlow(
        Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }
    )
    val selectedWeekStartCalendar: StateFlow<Calendar> = _selectedWeekStartCalendar.asStateFlow()

    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    // --- Reactive Data Streams ---
    val overviewStats: StateFlow<OverviewStats> = _overviewMonthCalendar
        .flatMapLatest { cal -> getOverviewStatsUseCase(cal) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OverviewStats())

    val dayStats: StateFlow<DayStats> = _selectedDayCalendar
        .flatMapLatest { cal -> getDayStatsUseCase(cal) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DayStats())

    val weekStats: StateFlow<WeekStats> = _selectedWeekStartCalendar
        .flatMapLatest { cal -> getWeekStatsUseCase(cal) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeekStats())

    val yearStats: StateFlow<YearStats> = _selectedYear
        .flatMapLatest { year -> getYearStatsUseCase(year) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), YearStats())

    // --- Navigation Actions ---
    fun previousOverviewMonth() {
        val newCal = _overviewMonthCalendar.value.clone() as Calendar
        newCal.add(Calendar.MONTH, -1)
        _overviewMonthCalendar.value = newCal
    }

    fun nextOverviewMonth() {
        val newCal = _overviewMonthCalendar.value.clone() as Calendar
        newCal.add(Calendar.MONTH, 1)
        _overviewMonthCalendar.value = newCal
    }

    fun previousDay() {
        val newCal = _selectedDayCalendar.value.clone() as Calendar
        newCal.add(Calendar.DAY_OF_YEAR, -1)
        _selectedDayCalendar.value = newCal
    }

    fun nextDay() {
        val newCal = _selectedDayCalendar.value.clone() as Calendar
        newCal.add(Calendar.DAY_OF_YEAR, 1)
        _selectedDayCalendar.value = newCal
    }

    fun previousWeek() {
        val newCal = _selectedWeekStartCalendar.value.clone() as Calendar
        newCal.add(Calendar.DAY_OF_YEAR, -7)
        _selectedWeekStartCalendar.value = newCal
    }

    fun nextWeek() {
        val newCal = _selectedWeekStartCalendar.value.clone() as Calendar
        newCal.add(Calendar.DAY_OF_YEAR, 7)
        _selectedWeekStartCalendar.value = newCal
    }

    fun previousYear() {
        _selectedYear.value -= 1
    }

    fun nextYear() {
        _selectedYear.value += 1
    }

    companion object {
        fun formatDuration(seconds: Long): String {
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            return when {
                hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
                hours > 0 -> "${hours}h"
                else -> "${minutes}m"
            }
        }

        fun Factory(
            getOverviewStatsUseCase: GetOverviewStatsUseCase,
            getDayStatsUseCase: GetDayStatsUseCase,
            getWeekStatsUseCase: GetWeekStatsUseCase,
            getYearStatsUseCase: GetYearStatsUseCase
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AnalyzeViewModel(
                    getOverviewStatsUseCase,
                    getDayStatsUseCase,
                    getWeekStatsUseCase,
                    getYearStatsUseCase
                ) as T
            }
        }
    }
}
