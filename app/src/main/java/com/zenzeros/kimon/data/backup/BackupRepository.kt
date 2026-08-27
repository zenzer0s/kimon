package com.zenzeros.kimon.data.backup

import android.content.Context
import android.net.Uri
import com.zenzeros.kimon.data.local.KimonDatabase
import com.zenzeros.kimon.data.local.entity.FocusSessionEntity
import com.zenzeros.kimon.data.local.entity.SleepSessionEntity
import com.zenzeros.kimon.data.local.entity.TagEntity
import com.zenzeros.kimon.data.local.entity.TaskEntity
import com.zenzeros.kimon.data.repository.UserSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class BackupRepository(
    private val context: Context,
    private val database: KimonDatabase,
    private val userSettingsRepository: UserSettingsRepository
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun createBackup(): KimonBackup = withContext(Dispatchers.IO) {
        val tags = database.tagDao().getAllTagsList().map { entity ->
            TagBackup(
                id = entity.id,
                name = entity.name,
                colorHex = entity.colorHex,
                iconName = entity.iconName,
                targetDailyMinutes = entity.targetDailyMinutes,
                isArchived = entity.isArchived,
                createdAtEpochMs = entity.createdAtEpochMs
            )
        }

        val focusSessions = database.focusSessionDao().getAllSessionsList().map { entity ->
            FocusSessionBackup(
                id = entity.id,
                tagId = entity.tagId,
                sessionType = entity.sessionType,
                startTimeEpochMs = entity.startTimeEpochMs,
                endTimeEpochMs = entity.endTimeEpochMs,
                targetDurationSeconds = entity.targetDurationSeconds,
                actualDurationSeconds = entity.actualDurationSeconds,
                isCompleted = entity.isCompleted,
                notes = entity.notes
            )
        }

        val tasks = database.taskDao().getAllTasksList().map { entity ->
            TaskBackup(
                id = entity.id,
                title = entity.title,
                category = entity.category,
                estimatedPomodoros = entity.estimatedPomodoros,
                completedPomodoros = entity.completedPomodoros,
                isCompleted = entity.isCompleted,
                createdAtEpochMs = entity.createdAtEpochMs
            )
        }

        val sleepSessions = database.sleepSessionDao().getAllSessionsList().map { entity ->
            SleepSessionBackup(
                id = entity.id,
                startTimeEpochMs = entity.startTimeEpochMs,
                endTimeEpochMs = entity.endTimeEpochMs,
                durationMinutes = entity.durationMinutes,
                qualityScore = entity.qualityScore,
                status = entity.status,
                source = entity.source,
                dateString = entity.dateString,
                syncedToHealthConnect = entity.syncedToHealthConnect,
                notes = entity.notes
            )
        }

        val settings = KimonSettingsBackup(
            workDurationMinutes = userSettingsRepository.workDurationMinutes.first(),
            shortBreakMinutes = userSettingsRepository.shortBreakMinutes.first(),
            longBreakMinutes = userSettingsRepository.longBreakMinutes.first(),
            sessionsBeforeLongBreak = userSettingsRepository.sessionsBeforeLongBreak.first(),
            dailyGoalMinutes = userSettingsRepository.dailyGoalMinutes.first(),
            autoStartBreaks = userSettingsRepository.autoStartBreaks.first(),
            autoStartPomodoros = userSettingsRepository.autoStartPomodoros.first(),
            keepScreenOn = userSettingsRepository.keepScreenOn.first(),
            dndEnabled = userSettingsRepository.dndEnabled.first(),
            soundEnabled = userSettingsRepository.soundEnabled.first(),
            vibrationEnabled = userSettingsRepository.vibrationEnabled.first(),
            headphoneMode = userSettingsRepository.headphoneMode.first(),
            alarmSoundUri = userSettingsRepository.alarmSoundUri.first(),
            alarmSoundTitle = userSettingsRepository.alarmSoundTitle.first(),
            themeMode = userSettingsRepository.themeMode.first(),
            themePalette = userSettingsRepository.themePalette.first(),
            themeColor = userSettingsRepository.themeColor.first(),
            amoledBlack = userSettingsRepository.amoledBlack.first(),
            clockStyle = userSettingsRepository.clockStyle.first(),
            sleepMonitoringEnabled = userSettingsRepository.sleepMonitoringEnabled.first(),
            healthConnectSyncEnabled = userSettingsRepository.healthConnectSyncEnabled.first(),
            sleepGoalMinutes = userSettingsRepository.sleepGoalMinutes.first()
        )

        KimonBackup(
            version = 1,
            appVersion = "1.0",
            backupDateEpochMs = System.currentTimeMillis(),
            settings = settings,
            tags = tags,
            focusSessions = focusSessions,
            tasks = tasks,
            sleepSessions = sleepSessions
        )
    }

    suspend fun exportBackupToUri(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val backup = createBackup()
            val jsonString = json.encodeToString(backup)
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(jsonString)
                }
            } ?: return@withContext Result.failure(Exception("Failed to open output stream"))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreBackupFromJson(
        jsonString: String,
        replaceExisting: Boolean = true
    ): Result<RestoreSummary> = withContext(Dispatchers.IO) {
        try {
            val backup = json.decodeFromString<KimonBackup>(jsonString)

            if (replaceExisting) {
                database.focusSessionDao().deleteAllSessions()
                database.taskDao().deleteAllTasks()
                database.sleepSessionDao().deleteAllSessions()
                database.tagDao().deleteAllTags()
            }

            // Restore tags
            val tagIdMap = mutableMapOf<Long, Long>()
            val tagsToInsert = backup.tags.map { tagBackup ->
                TagEntity(
                    id = if (replaceExisting) tagBackup.id else 0,
                    name = tagBackup.name,
                    colorHex = tagBackup.colorHex,
                    iconName = tagBackup.iconName,
                    targetDailyMinutes = tagBackup.targetDailyMinutes,
                    isArchived = tagBackup.isArchived,
                    createdAtEpochMs = if (tagBackup.createdAtEpochMs > 0) tagBackup.createdAtEpochMs else System.currentTimeMillis()
                )
            }
            if (replaceExisting) {
                database.tagDao().insertAll(tagsToInsert)
            } else {
                tagsToInsert.forEach { tagEntity ->
                    val originalId = tagEntity.id
                    val newId = database.tagDao().insertTag(tagEntity)
                    if (originalId > 0) tagIdMap[originalId] = newId
                }
            }

            // Restore focus sessions
            val focusSessionsToInsert = backup.focusSessions.map { sessionBackup ->
                val mappedTagId = if (replaceExisting) sessionBackup.tagId else sessionBackup.tagId?.let { tagIdMap[it] ?: it }
                FocusSessionEntity(
                    id = if (replaceExisting) sessionBackup.id else 0,
                    tagId = mappedTagId,
                    sessionType = sessionBackup.sessionType,
                    startTimeEpochMs = sessionBackup.startTimeEpochMs,
                    endTimeEpochMs = sessionBackup.endTimeEpochMs,
                    targetDurationSeconds = sessionBackup.targetDurationSeconds,
                    actualDurationSeconds = sessionBackup.actualDurationSeconds,
                    isCompleted = sessionBackup.isCompleted,
                    notes = sessionBackup.notes
                )
            }
            database.focusSessionDao().insertAll(focusSessionsToInsert)

            // Restore tasks
            val tasksToInsert = backup.tasks.map { taskBackup ->
                TaskEntity(
                    id = if (replaceExisting) taskBackup.id else 0,
                    title = taskBackup.title,
                    category = taskBackup.category,
                    estimatedPomodoros = taskBackup.estimatedPomodoros,
                    completedPomodoros = taskBackup.completedPomodoros,
                    isCompleted = taskBackup.isCompleted,
                    createdAtEpochMs = if (taskBackup.createdAtEpochMs > 0) taskBackup.createdAtEpochMs else System.currentTimeMillis()
                )
            }
            database.taskDao().insertAll(tasksToInsert)

            // Restore sleep sessions
            val sleepSessionsToInsert = backup.sleepSessions.map { sleepBackup ->
                SleepSessionEntity(
                    id = if (replaceExisting) sleepBackup.id else 0,
                    startTimeEpochMs = sleepBackup.startTimeEpochMs,
                    endTimeEpochMs = sleepBackup.endTimeEpochMs,
                    durationMinutes = sleepBackup.durationMinutes,
                    qualityScore = sleepBackup.qualityScore,
                    status = sleepBackup.status,
                    source = sleepBackup.source,
                    dateString = sleepBackup.dateString,
                    syncedToHealthConnect = sleepBackup.syncedToHealthConnect,
                    notes = sleepBackup.notes
                )
            }
            database.sleepSessionDao().insertAll(sleepSessionsToInsert)

            // Restore user settings if present
            backup.settings?.let { s ->
                userSettingsRepository.setWorkDurationMinutes(s.workDurationMinutes)
                userSettingsRepository.setShortBreakMinutes(s.shortBreakMinutes)
                userSettingsRepository.setLongBreakMinutes(s.longBreakMinutes)
                userSettingsRepository.setSessionsBeforeLongBreak(s.sessionsBeforeLongBreak)
                userSettingsRepository.setDailyGoalMinutes(s.dailyGoalMinutes)
                userSettingsRepository.setAutoStartBreaks(s.autoStartBreaks)
                userSettingsRepository.setAutoStartPomodoros(s.autoStartPomodoros)
                userSettingsRepository.setKeepScreenOn(s.keepScreenOn)
                userSettingsRepository.setDndEnabled(s.dndEnabled)
                userSettingsRepository.setSoundEnabled(s.soundEnabled)
                userSettingsRepository.setVibrationEnabled(s.vibrationEnabled)
                userSettingsRepository.setHeadphoneMode(s.headphoneMode)
                userSettingsRepository.setAlarmSound(s.alarmSoundUri, s.alarmSoundTitle)
                userSettingsRepository.setThemeMode(s.themeMode)
                userSettingsRepository.setThemePalette(s.themePalette)
                userSettingsRepository.setThemeColor(s.themeColor)
                userSettingsRepository.setAmoledBlack(s.amoledBlack)
                userSettingsRepository.setClockStyle(s.clockStyle)
                userSettingsRepository.setSleepMonitoringEnabled(s.sleepMonitoringEnabled)
                userSettingsRepository.setHealthConnectSyncEnabled(s.healthConnectSyncEnabled)
                userSettingsRepository.setSleepGoalMinutes(s.sleepGoalMinutes)
            }

            Result.success(
                RestoreSummary(
                    focusSessionsRestored = focusSessionsToInsert.size,
                    tagsRestored = tagsToInsert.size,
                    tasksRestored = tasksToInsert.size,
                    sleepSessionsRestored = sleepSessionsToInsert.size,
                    settingsRestored = backup.settings != null
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importBackupFromUri(
        uri: Uri,
        replaceExisting: Boolean = true
    ): Result<RestoreSummary> = withContext(Dispatchers.IO) {
        try {
            val stringBuilder = java.lang.StringBuilder()
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String? = reader.readLine()
                    while (line != null) {
                        stringBuilder.append(line).append('\n')
                        line = reader.readLine()
                    }
                }
            } ?: return@withContext Result.failure(Exception("Failed to open input stream"))

            restoreBackupFromJson(stringBuilder.toString(), replaceExisting)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearAllData(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            database.focusSessionDao().deleteAllSessions()
            database.taskDao().deleteAllTasks()
            database.sleepSessionDao().deleteAllSessions()
            database.tagDao().deleteAllTags()
            userSettingsRepository.clearAllSettings()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
