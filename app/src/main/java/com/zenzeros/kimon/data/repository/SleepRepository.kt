package com.zenzeros.kimon.data.repository

import com.zenzeros.kimon.data.local.dao.SleepSessionDao
import com.zenzeros.kimon.data.local.entity.SleepSessionEntity
import com.zenzeros.kimon.service.health.HealthConnectManager
import com.zenzeros.kimon.service.sleep.SleepMonitorManager
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SleepRepository(
    private val context: android.content.Context,
    private val sleepSessionDao: SleepSessionDao,
    val sleepMonitorManager: SleepMonitorManager,
    val healthConnectManager: HealthConnectManager
) {

    fun getAllSessions(): Flow<List<SleepSessionEntity>> = sleepSessionDao.getAllSessions()

    fun getLatestSession(): Flow<SleepSessionEntity?> = sleepSessionDao.getLatestSession()

    fun getSessionsBetween(startTimeMs: Long, endTimeMs: Long): Flow<List<SleepSessionEntity>> =
        sleepSessionDao.getSessionsBetween(startTimeMs, endTimeMs)

    fun getAverageDurationMinutesSince(sinceEpochMs: Long): Flow<Double?> =
        sleepSessionDao.getAverageDurationMinutesSince(sinceEpochMs)

    fun getTotalSessionsCount(): Flow<Int> = sleepSessionDao.getTotalSessionsCount()

    suspend fun cleanDuplicates() {
        sleepSessionDao.removeDuplicateSessions()
    }

    suspend fun recordSession(session: SleepSessionEntity, sendNotification: Boolean = true): Long {
        // Prevent duplicate insertions
        val duplicate = sleepSessionDao.findDuplicateOrOverlappingSession(session.startTimeEpochMs, session.endTimeEpochMs)
        if (duplicate != null) {
            android.util.Log.w("SleepRepository", "[SleepRepository] Duplicate sleep session detected (ID=${duplicate.id}), skipping insertion.")
            return duplicate.id
        }

        val id = sleepSessionDao.insertSession(session)
        if (id == -1L) {
            // Already existed due to unique constraint
            val existing = sleepSessionDao.findDuplicateOrOverlappingSession(session.startTimeEpochMs, session.endTimeEpochMs)
            return existing?.id ?: -1L
        }

        // Automatically attempt Health Connect sync if available and permitted
        if (healthConnectManager.isAvailable() && healthConnectManager.hasPermissions()) {
            val synced = healthConnectManager.writeSleepSession(session.copy(id = id))
            if (synced) {
                sleepSessionDao.updateSession(session.copy(id = id, syncedToHealthConnect = true))
            }
        }
        com.zenzeros.kimon.widget.LastNightSleepWidgetProvider.updateAllWidgets(context)

        // Only send push notification if session actually ended recently (within the last 2 hours)
        val isRecent = (System.currentTimeMillis() - session.endTimeEpochMs) in 0..(2 * 3600 * 1000L)
        if (sendNotification && isRecent) {
            com.zenzeros.kimon.service.sleep.SleepNotificationHelper.sendSleepSummaryNotification(context, session.copy(id = id))
        }
        return id
    }

    suspend fun deleteSession(session: SleepSessionEntity) {
        sleepSessionDao.deleteSession(session)
        com.zenzeros.kimon.widget.LastNightSleepWidgetProvider.updateAllWidgets(context)
    }

    suspend fun clearAllSessions() {
        sleepSessionDao.deleteAllSessions()
        com.zenzeros.kimon.widget.LastNightSleepWidgetProvider.updateAllWidgets(context)
    }

    suspend fun syncFromHealthConnect(): Int {
        if (!healthConnectManager.isAvailable() || !healthConnectManager.hasPermissions()) return 0

        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -30)
        val startTime = cal.timeInMillis
        val endTime = System.currentTimeMillis()

        val healthConnectSessions = healthConnectManager.readSleepSessions(startTime, endTime)
        var inserted = 0
        for (session in healthConnectSessions) {
            // Skip anything that overlaps an existing session (e.g. the same night already
            // captured by the Google Sleep API, or a session this app itself wrote to HC).
            val existing = sleepSessionDao.findDuplicateOrOverlappingSession(
                session.startTimeEpochMs,
                session.endTimeEpochMs
            )
            if (existing != null) continue
            if (sleepSessionDao.insertSession(session) != -1L) inserted++
        }
        if (inserted > 0) {
            com.zenzeros.kimon.widget.LastNightSleepWidgetProvider.updateAllWidgets(context)
        }
        return inserted
    }

    suspend fun syncUnsyncedToHealthConnect() {
        if (!healthConnectManager.isAvailable() || !healthConnectManager.hasPermissions()) return

        val unsynced = sleepSessionDao.getUnsyncedSessions()
        for (session in unsynced) {
            val success = healthConnectManager.writeSleepSession(session)
            if (success) {
                sleepSessionDao.updateSession(session.copy(syncedToHealthConnect = true))
            }
        }
    }

    suspend fun generateSampleData() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sampleList = mutableListOf<SleepSessionEntity>()
        val durations = listOf(440L, 480L, 420L, 465L, 510L, 430L, 495L) // 7 days of realistic sleep in mins (7.3h - 8.5h)
        val qualityScores = listOf(82, 90, 78, 88, 94, 80, 92)

        for (i in 0 until 7) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            // Bedtime around 11:30 PM previous day
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, (15..45).random())
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val startTime = cal.timeInMillis - (86400000L) // prev night

            val duration = durations[i]
            val endTime = startTime + (duration * 60 * 1000)

            sampleList.add(
                SleepSessionEntity(
                    startTimeEpochMs = startTime,
                    endTimeEpochMs = endTime,
                    durationMinutes = duration,
                    qualityScore = qualityScores[i],
                    status = 0,
                    source = if (i % 2 == 0) "GOOGLE_SLEEP_API" else "HEALTH_CONNECT",
                    dateString = dateFormat.format(Date(endTime)),
                    syncedToHealthConnect = true
                )
            )
        }

        sleepSessionDao.insertAll(sampleList)
    }
}
