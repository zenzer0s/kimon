package com.zenzeros.kimon.data.repository

import com.zenzeros.kimon.data.local.dao.FocusSessionDao
import com.zenzeros.kimon.data.local.entity.FocusSessionEntity
import com.zenzeros.kimon.data.local.entity.FocusSessionWithTag
import kotlinx.coroutines.flow.Flow

class SessionRepository(private val sessionDao: FocusSessionDao) {
    suspend fun recordSession(session: FocusSessionEntity): Long = sessionDao.insertSession(session)

    suspend fun deleteSession(session: FocusSessionEntity) = sessionDao.deleteSession(session)

    fun getAllSessions(): Flow<List<FocusSessionEntity>> = sessionDao.getAllSessions()

    fun getSessionsWithTagBetween(startTimeMs: Long, endTimeMs: Long): Flow<List<FocusSessionWithTag>> =
        sessionDao.getSessionsWithTagBetween(startTimeMs, endTimeMs)

    fun getFocusSessionsBetween(startTimeMs: Long, endTimeMs: Long): Flow<List<FocusSessionEntity>> =
        sessionDao.getFocusSessionsBetween(startTimeMs, endTimeMs)

    fun getTotalFocusTimeSeconds(): Flow<Long?> = sessionDao.getTotalFocusTimeSeconds()

    fun getTotalSessionsCount(): Flow<Int> = sessionDao.getTotalSessionsCount()

    fun getDistinctFocusDaysCount(): Flow<Int> = sessionDao.getDistinctFocusDaysCount()
}
