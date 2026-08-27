package com.zenzeros.kimon.data.repository

import com.zenzeros.kimon.data.local.dao.FocusSessionDao
import com.zenzeros.kimon.data.local.entity.FocusSessionEntity
import com.zenzeros.kimon.data.local.entity.FocusSessionWithTag
import kotlinx.coroutines.flow.Flow

class SessionRepository(
    private val sessionDao: FocusSessionDao,
    private val context: android.content.Context? = null
) {
    suspend fun recordSession(session: FocusSessionEntity): Long {
        val id = sessionDao.insertSession(session)
        context?.let { com.zenzeros.kimon.widget.FocusHeatmapWidgetProvider.updateAllWidgets(it) }
        return id
    }

    suspend fun deleteSession(session: FocusSessionEntity) {
        sessionDao.deleteSession(session)
        context?.let { com.zenzeros.kimon.widget.FocusHeatmapWidgetProvider.updateAllWidgets(it) }
    }

    fun getAllSessions(): Flow<List<FocusSessionEntity>> = sessionDao.getAllSessions()

    fun getSessionsWithTagBetween(startTimeMs: Long, endTimeMs: Long): Flow<List<FocusSessionWithTag>> =
        sessionDao.getSessionsWithTagBetween(startTimeMs, endTimeMs)

    fun getFocusSessionsBetween(startTimeMs: Long, endTimeMs: Long): Flow<List<FocusSessionEntity>> =
        sessionDao.getFocusSessionsBetween(startTimeMs, endTimeMs)

    fun getTotalFocusTimeSeconds(): Flow<Long?> = sessionDao.getTotalFocusTimeSeconds()

    fun getTotalSessionsCount(): Flow<Int> = sessionDao.getTotalSessionsCount()

    fun getDistinctFocusDaysCount(): Flow<Int> = sessionDao.getDistinctFocusDaysCount()

    suspend fun clearAllSessions() {
        sessionDao.deleteAllSessions()
        context?.let { com.zenzeros.kimon.widget.FocusHeatmapWidgetProvider.updateAllWidgets(it) }
    }
}
