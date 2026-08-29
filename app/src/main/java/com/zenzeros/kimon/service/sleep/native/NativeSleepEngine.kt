package com.zenzeros.kimon.service.sleep.native

import android.util.Log
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class NativeEpochData(
    @SerialName("timestamp_ms") val timestampMs: Long,
    @SerialName("duration_seconds") val durationSeconds: Int,
    @SerialName("activity_count") val activityCount: Float,
    @SerialName("variance") val variance: Float = 0f,
    @SerialName("mean_light_lux") val meanLightLux: Float = 0f,
    @SerialName("screen_on") val screenOn: Boolean = false,
    @SerialName("charging") val charging: Boolean = false
)

@Serializable
data class NativeSleepAnalysisResult(
    @SerialName("sleep_onset_time_ms") val sleepOnsetEpochMs: Long,
    @SerialName("wake_time_ms") val wakeTimeEpochMs: Long,
    @SerialName("total_duration_minutes") val totalDurationMinutes: Long,
    @SerialName("sleep_duration_minutes") val sleepDurationMinutes: Long,
    @SerialName("wake_duration_minutes") val wakeDurationMinutes: Long,
    @SerialName("sleep_onset_latency_minutes") val sleepOnsetLatencyMinutes: Long,
    @SerialName("sleep_efficiency") val sleepEfficiency: Float,
    @SerialName("quality_score") val qualityScore: Int,
    @SerialName("deep_sleep_minutes") val deepSleepMinutes: Long,
    @SerialName("light_sleep_minutes") val lightSleepMinutes: Long,
    @SerialName("rem_sleep_minutes") val remSleepMinutes: Long,
    @SerialName("wake_count") val wakeCount: Int,
    @SerialName("epoch_states") val epochStates: List<Int> = emptyList(),
    @SerialName("epoch_stages") val epochStages: List<Int> = emptyList()
)

object NativeSleepEngine {
    private const val TAG = "NativeSleepEngine"
    private var isLoaded = false

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    init {
        try {
            System.loadLibrary("sleep_core")
            isLoaded = true
            Log.i(TAG, "Loaded native sleep_core library: ${nativeGetVersion()}")
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "Failed to load libsleep_core.so native library", e)
            isLoaded = false
        }
    }

    fun isAvailable(): Boolean = isLoaded

    fun getVersion(): String {
        return if (isLoaded) nativeGetVersion() else "Native library not loaded"
    }

    fun analyzeEpochs(epochs: List<NativeEpochData>): NativeSleepAnalysisResult? {
        if (!isLoaded || epochs.isEmpty()) return null
        return try {
            val jsonPayload = json.encodeToString(epochs)
            val resultJson = nativeAnalyzeEpochsJson(jsonPayload)
            if (resultJson.isBlank() || resultJson == "{}") null
            else json.decodeFromString<NativeSleepAnalysisResult>(resultJson)
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing epochs with native engine", e)
            null
        }
    }

    fun processRawBuffers(
        x: FloatArray,
        y: FloatArray,
        z: FloatArray,
        light: FloatArray,
        timestamps: LongArray,
        epochDurationSec: Int = 60,
        screenOn: Boolean = false,
        charging: Boolean = false
    ): NativeSleepAnalysisResult? {
        if (!isLoaded || timestamps.isEmpty()) return null
        return try {
            val resultJson = nativeProcessRawBuffers(
                x = x,
                y = y,
                z = z,
                light = light,
                timeArr = timestamps,
                epochDurationSec = epochDurationSec,
                screenOn = screenOn,
                charging = charging
            )
            if (resultJson.isBlank() || resultJson == "{}") null
            else json.decodeFromString<NativeSleepAnalysisResult>(resultJson)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing raw buffers with native engine", e)
            null
        }
    }

    // --- Native JNI External Methods ---
    private external fun nativeGetVersion(): String

    private external fun nativeAnalyzeEpochsJson(epochsJson: String): String

    private external fun nativeProcessRawBuffers(
        x: FloatArray,
        y: FloatArray,
        z: FloatArray,
        light: FloatArray,
        timeArr: LongArray,
        epochDurationSec: Int,
        screenOn: Boolean,
        charging: Boolean
    ): String
}
