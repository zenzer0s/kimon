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

    suspend fun recordSession(session: SleepSessionEntity): Long {
        val id = sleepSessionDao.insertSession(session)
        // Automatically attempt Health Connect sync if available and permitted
        if (healthConnectManager.isAvailable() && healthConnectManager.hasPermissions()) {
            val synced = healthConnectManager.writeSleepSession(session.copy(id = id))
            if (synced) {
                sleepSessionDao.updateSession(session.copy(id = id, syncedToHealthConnect = true))
            }
        }
        return id
    }

    suspend fun deleteSession(session: SleepSessionEntity) = sleepSessionDao.deleteSession(session)

    suspend fun clearAllSessions() = sleepSessionDao.deleteAllSessions()

    suspend fun syncFromHealthConnect(): Int {
        if (!healthConnectManager.isAvailable() || !healthConnectManager.hasPermissions()) return 0

        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -30)
        val startTime = cal.timeInMillis
        val endTime = System.currentTimeMillis()

        val healthConnectSessions = healthConnectManager.readSleepSessions(startTime, endTime)
        if (healthConnectSessions.isNotEmpty()) {
            sleepSessionDao.insertAll(healthConnectSessions)
        }
        return healthConnectSessions.size
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
