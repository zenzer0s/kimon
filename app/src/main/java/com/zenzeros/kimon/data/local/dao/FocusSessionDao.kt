package com.zenzeros.kimon.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.zenzeros.kimon.data.local.entity.FocusSessionEntity
import com.zenzeros.kimon.data.local.entity.FocusSessionWithTag
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<FocusSessionEntity>)

    @Delete
    suspend fun deleteSession(session: FocusSessionEntity)

    @Query("SELECT * FROM focus_sessions ORDER BY startTimeEpochMs DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions ORDER BY startTimeEpochMs ASC")
    suspend fun getAllSessionsList(): List<FocusSessionEntity>

    @Transaction
    @Query("SELECT * FROM focus_sessions WHERE startTimeEpochMs >= :startTimeMs AND startTimeEpochMs <= :endTimeMs ORDER BY startTimeEpochMs ASC")
    fun getSessionsWithTagBetween(startTimeMs: Long, endTimeMs: Long): Flow<List<FocusSessionWithTag>>

    @Query("SELECT * FROM focus_sessions WHERE startTimeEpochMs >= :startTimeMs AND startTimeEpochMs <= :endTimeMs AND sessionType = 'POMODORO'")
    fun getFocusSessionsBetween(startTimeMs: Long, endTimeMs: Long): Flow<List<FocusSessionEntity>>

    @Query("SELECT SUM(actualDurationSeconds) FROM focus_sessions WHERE sessionType = 'POMODORO'")
    fun getTotalFocusTimeSeconds(): Flow<Long?>

    @Query("SELECT COUNT(*) FROM focus_sessions WHERE sessionType = 'POMODORO'")
    fun getTotalSessionsCount(): Flow<Int>

    @Query("SELECT COUNT(DISTINCT (startTimeEpochMs / 86400000)) FROM focus_sessions WHERE sessionType = 'POMODORO'")
    fun getDistinctFocusDaysCount(): Flow<Int>

    @Query("DELETE FROM focus_sessions")
    suspend fun deleteAllSessions()
}
