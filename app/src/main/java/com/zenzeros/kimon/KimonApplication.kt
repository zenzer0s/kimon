package com.zenzeros.kimon

import android.app.Application
import com.zenzeros.kimon.data.local.KimonDatabase
import com.zenzeros.kimon.data.repository.SessionRepository
import com.zenzeros.kimon.data.repository.TagRepository
import com.zenzeros.kimon.data.repository.TaskRepository
import com.zenzeros.kimon.data.repository.UserSettingsRepository
import com.zenzeros.kimon.domain.usecase.GetDayStatsUseCase
import com.zenzeros.kimon.domain.usecase.GetOverviewStatsUseCase
import com.zenzeros.kimon.domain.usecase.GetWeekStatsUseCase
import com.zenzeros.kimon.domain.usecase.GetYearStatsUseCase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.zenzeros.kimon.data.local.SampleDataSeeder

class KimonApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy { KimonDatabase.getInstance(this) }
    val sessionRepository by lazy { SessionRepository(database.focusSessionDao()) }
    val tagRepository by lazy { TagRepository(database.tagDao()) }
    val taskRepository by lazy { TaskRepository(database.taskDao()) }
    val userSettingsRepository by lazy { UserSettingsRepository(this) }

    val getOverviewStatsUseCase by lazy { GetOverviewStatsUseCase(sessionRepository) }
    val getDayStatsUseCase by lazy { GetDayStatsUseCase(sessionRepository) }
    val getWeekStatsUseCase by lazy { GetWeekStatsUseCase(sessionRepository) }
    val getYearStatsUseCase by lazy { GetYearStatsUseCase(sessionRepository) }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            SampleDataSeeder.seedSampleDataIfEmpty(database)
        }
    }
}
