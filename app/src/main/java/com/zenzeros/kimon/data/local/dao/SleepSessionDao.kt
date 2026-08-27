package com.zenzeros.kimon.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zenzeros.kimon.data.local.entity.SleepSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SleepSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<SleepSessionEntity>)

    @Update
    suspend fun updateSession(session: SleepSessionEntity)

    @Delete
    suspend fun deleteSession(session: SleepSessionEntity)

    @Query("SELECT * FROM sleep_sessions ORDER BY startTimeEpochMs DESC")
    fun getAllSessions(): Flow<List<SleepSessionEntity>>

    @Query("SELECT * FROM sleep_sessions ORDER BY startTimeEpochMs ASC")
    suspend fun getAllSessionsList(): List<SleepSessionEntity>

    @Query("SELECT * FROM sleep_sessions ORDER BY startTimeEpochMs DESC LIMIT 1")
    fun getLatestSession(): Flow<SleepSessionEntity?>

    @Query("SELECT * FROM sleep_sessions WHERE startTimeEpochMs >= :startTimeMs AND startTimeEpochMs <= :endTimeMs ORDER BY startTimeEpochMs ASC")
    fun getSessionsBetween(startTimeMs: Long, endTimeMs: Long): Flow<List<SleepSessionEntity>>

    @Query("SELECT * FROM sleep_sessions WHERE syncedToHealthConnect = 0")
    suspend fun getUnsyncedSessions(): List<SleepSessionEntity>

    @Query("SELECT AVG(durationMinutes) FROM sleep_sessions WHERE startTimeEpochMs >= :sinceEpochMs")
    fun getAverageDurationMinutesSince(sinceEpochMs: Long): Flow<Double?>

    @Query("SELECT COUNT(*) FROM sleep_sessions")
    fun getTotalSessionsCount(): Flow<Int>

    @Query("DELETE FROM sleep_sessions")
    suspend fun deleteAllSessions()
}
