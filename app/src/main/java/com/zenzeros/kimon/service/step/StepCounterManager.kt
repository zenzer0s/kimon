package com.zenzeros.kimon.service.step

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StepCounterManager(val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val stepCounterSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val stepDetectorSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    private val activeSensor: Sensor? = stepCounterSensor ?: stepDetectorSensor

    private val prefs = context.getSharedPreferences("kimon_steps_prefs", Context.MODE_PRIVATE)

    private val _todaySteps = MutableStateFlow(0)
    val todaySteps: StateFlow<Int> = _todaySteps.asStateFlow()

    private var isListening = false

    init {
        loadSavedTodaySteps()
    }

    fun isSensorAvailable(): Boolean = activeSensor != null

    fun getSensorName(): String = activeSensor?.name ?: "Hardware Pedometer"

    fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun startListening() {
        loadSavedTodaySteps()
        pruneOldHistory()
        if (isListening || activeSensor == null || !hasPermission()) return
        val registered = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            sensorManager?.registerListener(
                this,
                activeSensor,
                SensorManager.SENSOR_DELAY_NORMAL,
                60_000_000 // 60s batching latency for maximum hardware efficiency
            ) ?: false
        } else {
            sensorManager?.registerListener(this, activeSensor, SensorManager.SENSOR_DELAY_NORMAL) ?: false
        }
        isListening = registered
    }

    fun stopListening() {
        if (!isListening) return
        sensorManager?.unregisterListener(this)
        isListening = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val rawSteps = event.values.firstOrNull()?.toLong() ?: return
                calculateDailySteps(rawSteps)
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                if (stepCounterSensor == null) {
                    handleStepDetectorEvent()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun loadSavedTodaySteps() {
        val today = getTodayDateString()
        val savedDate = prefs.getString(KEY_LAST_STEP_DATE, null)
        if (savedDate == today) {
            _todaySteps.value = prefs.getInt(KEY_TODAY_STEPS, 0)
        } else {
            _todaySteps.value = 0
        }
    }

    private fun calculateDailySteps(rawSteps: Long) {
        val today = getTodayDateString()
        val savedDate = prefs.getString(KEY_LAST_STEP_DATE, null)
        val lastRaw = prefs.getLong(KEY_LAST_RAW_STEPS, -1L)
        var todayStepsCount = prefs.getInt(KEY_TODAY_STEPS, 0)

        // Reset if new day arrives. On the first reading of a new day we also rebase the
        // raw baseline to `rawSteps` (by skipping the delta below), otherwise steps taken
        // late yesterday / overnight get wrongly credited to today.
        val isNewDay = savedDate != today
        if (isNewDay) {
            todayStepsCount = 0
        }

        if (lastRaw != -1L && !isNewDay) {
            val delta = if (rawSteps >= lastRaw) {
                rawSteps - lastRaw
            } else {
                // Device rebooted: sensor reset to 0
                rawSteps
            }

            if (delta in 1..100_000) {
                todayStepsCount += delta.toInt()
            }
        }

        prefs.edit()
            .putLong(KEY_LAST_RAW_STEPS, rawSteps)
            .putInt(KEY_TODAY_STEPS, todayStepsCount)
            .putInt(historyKey(today), todayStepsCount)
            .putString(KEY_LAST_STEP_DATE, today)
            .apply()

        _todaySteps.value = todayStepsCount
    }

    private fun handleStepDetectorEvent() {
        val today = getTodayDateString()
        val savedDate = prefs.getString(KEY_LAST_STEP_DATE, null)
        var todayStepsCount = if (savedDate == today) prefs.getInt(KEY_TODAY_STEPS, 0) else 0

        todayStepsCount += 1

        prefs.edit()
            .putInt(KEY_TODAY_STEPS, todayStepsCount)
            .putInt(historyKey(today), todayStepsCount)
            .putString(KEY_LAST_STEP_DATE, today)
            .apply()

        _todaySteps.value = todayStepsCount
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    /** Step total for a given "yyyy-MM-dd" date. Today comes from the live counter; past
     *  days are read from the persisted daily history (0 if that day was never tracked). */
    fun getStepsForDate(dateString: String): Int {
        return if (dateString == getTodayDateString()) {
            _todaySteps.value
        } else {
            prefs.getInt(historyKey(dateString), 0)
        }
    }

    private fun historyKey(dateString: String): String = "$KEY_HISTORY_PREFIX$dateString"

    private fun pruneOldHistory() {
        try {
            val cutoff = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -HISTORY_RETENTION_DAYS)
            }.time
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val editor = prefs.edit()
            var changed = false
            for (key in prefs.all.keys.toList()) {
                if (!key.startsWith(KEY_HISTORY_PREFIX)) continue
                val parsed = try {
                    fmt.parse(key.removePrefix(KEY_HISTORY_PREFIX))
                } catch (_: Exception) {
                    null
                }
                if (parsed == null || parsed.before(cutoff)) {
                    editor.remove(key)
                    changed = true
                }
            }
            if (changed) editor.apply()
        } catch (_: Exception) {
            // Non-critical housekeeping
        }
    }

    companion object {
        private const val KEY_LAST_STEP_DATE = "step_last_date"
        private const val KEY_LAST_RAW_STEPS = "step_last_raw"
        private const val KEY_TODAY_STEPS = "step_today_steps"
        private const val KEY_HISTORY_PREFIX = "step_hist_"
        private const val HISTORY_RETENTION_DAYS = 35

        fun calculateDistanceKm(steps: Int): Float = steps * 0.00076f
        fun calculateCaloriesKcal(steps: Int): Int = (steps * 0.04f).toInt()
    }
}
