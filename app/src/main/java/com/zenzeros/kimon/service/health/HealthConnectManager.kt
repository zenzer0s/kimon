package com.zenzeros.kimon.service.health

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.zenzeros.kimon.data.local.entity.SleepSessionEntity
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneOffset
import java.util.Date
import java.util.Locale

class HealthConnectManager(private val context: Context) {

    companion object {
        private const val TAG = "HealthConnectManager"

        val PERMISSIONS = setOf(
            HealthPermission.getReadPermission(SleepSessionRecord::class),
            HealthPermission.getWritePermission(SleepSessionRecord::class)
        )
    }

    private val healthConnectClient by lazy {
        if (isAvailable()) {
            HealthConnectClient.getOrCreate(context)
        } else {
            null
        }
    }

    fun isAvailable(): Boolean {
        val status = HealthConnectClient.getSdkStatus(context)
        return status == HealthConnectClient.SDK_AVAILABLE
    }

    suspend fun hasPermissions(): Boolean {
        val client = healthConnectClient ?: return false
        val granted = client.permissionController.getGrantedPermissions()
        return granted.containsAll(PERMISSIONS)
    }

    suspend fun readSleepSessions(startTimeMs: Long, endTimeMs: Long): List<SleepSessionEntity> {
        val client = healthConnectClient ?: return emptyList()
        if (!hasPermissions()) return emptyList()

        return try {
            val startInstant = Instant.ofEpochMilli(startTimeMs)
            val endInstant = Instant.ofEpochMilli(endTimeMs)
            val request = ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startInstant, endInstant)
            )
            val response = client.readRecords(request)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            response.records.map { record ->
                val start = record.startTime.toEpochMilli()
                val end = record.endTime.toEpochMilli()
                val duration = (end - start) / (1000 * 60)
                SleepSessionEntity(
                    startTimeEpochMs = start,
                    endTimeEpochMs = end,
                    durationMinutes = duration,
                    qualityScore = 85,
                    status = 0,
                    source = "HEALTH_CONNECT",
                    dateString = dateFormat.format(Date(start)),
                    syncedToHealthConnect = true,
                    notes = record.notes
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading sleep sessions from Health Connect", e)
            emptyList()
        }
    }

    suspend fun writeSleepSession(session: SleepSessionEntity): Boolean {
        val client = healthConnectClient ?: return false
        if (!hasPermissions()) return false

        return try {
            val startInstant = Instant.ofEpochMilli(session.startTimeEpochMs)
            val endInstant = Instant.ofEpochMilli(session.endTimeEpochMs)
            val zoneOffset = ZoneOffset.systemDefault().rules.getOffset(Instant.now())

            val record = SleepSessionRecord(
                startTime = startInstant,
                startZoneOffset = zoneOffset,
                endTime = endInstant,
                endZoneOffset = zoneOffset,
                title = "Kimon Focus & Sleep",
                notes = session.notes
            )

            client.insertRecords(listOf(record))
            Log.d(TAG, "Successfully synced sleep session to Health Connect")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error writing sleep session to Health Connect", e)
            false
        }
    }
}
