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

class KimonApplication : Application() {

    val database by lazy { KimonDatabase.getInstance(this) }
    val sessionRepository by lazy { SessionRepository(database.focusSessionDao()) }
    val tagRepository by lazy { TagRepository(database.tagDao()) }
    val taskRepository by lazy { TaskRepository(database.taskDao()) }
    val userSettingsRepository by lazy { UserSettingsRepository(this) }

    val sleepMonitorManager by lazy { com.zenzeros.kimon.service.sleep.SleepMonitorManager(this) }
    val healthConnectManager by lazy { com.zenzeros.kimon.service.health.HealthConnectManager(this) }
    val sleepRepository by lazy {
        com.zenzeros.kimon.data.repository.SleepRepository(
            context = this,
            sleepSessionDao = database.sleepSessionDao(),
            sleepMonitorManager = sleepMonitorManager,
            healthConnectManager = healthConnectManager
        )
    }

    val backupRepository by lazy {
        com.zenzeros.kimon.data.backup.BackupRepository(
            context = this,
            database = database,
            userSettingsRepository = userSettingsRepository
        )
    }

    val getOverviewStatsUseCase by lazy { GetOverviewStatsUseCase(sessionRepository) }
    val getDayStatsUseCase by lazy { GetDayStatsUseCase(sessionRepository) }
    val getWeekStatsUseCase by lazy { GetWeekStatsUseCase(sessionRepository) }
    val getYearStatsUseCase by lazy { GetYearStatsUseCase(sessionRepository) }
}
