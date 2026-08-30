package com.zenzeros.kimon.service.sleep

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.ServiceCompat
import com.zenzeros.kimon.KimonApplication
import com.zenzeros.kimon.data.local.entity.SleepSessionEntity
import com.zenzeros.kimon.service.sleep.native.NativeEpochData
import com.zenzeros.kimon.service.sleep.native.NativeSleepEngine
import com.zenzeros.kimon.service.sleep.usage.AppUsageHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs
import kotlin.math.sqrt

enum class SleepTrackingState {
    MONITORING_REST,   // Actively monitoring overnight, waiting for consolidated sleep
    IN_SLEEP,          // Consolidated sleep in progress
    CANDIDATE_WAKE     // Potential wake activity detected, 20-minute observation window running
}

class CustomSleepService : Service(), SensorEventListener {

    companion object {
        const val TAG = "KimonSleepService"
        const val ACTION_START = "com.zenzeros.kimon.service.sleep.ACTION_START"
        const val ACTION_STOP = "com.zenzeros.kimon.service.sleep.ACTION_STOP"
        const val ACTION_FORCE_EVALUATE = "com.zenzeros.kimon.service.sleep.ACTION_FORCE_EVALUATE"
        private const val MAX_OVERNIGHT_EPOCHS = 720 // 12 hours max rolling window (1 epoch / min)
        private const val CANDIDATE_WAKE_TIMEOUT_MS = 20 * 60 * 1000L // 20 minutes confirmation window
        private const val STEP_THRESHOLD_CONFIRMED_WAKE = 15 // 15 physical steps confirms out-of-bed

        var isRunning: Boolean = false
            private set

        fun start(context: Context) {
            val intent = Intent(context, CustomSleepService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, CustomSleepService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var lightSensor: Sensor? = null
    private var stepDetector: Sensor? = null

    // State machine tracking
    private var currentState = SleepTrackingState.MONITORING_REST
    private var candidateWakeStartMs = 0L
    private var candidateWakeSteps = 0
    private var consecutiveStillEpochsInCandidate = 0

    // Zero-allocation scalar accumulators for current 60-second epoch
    private var accSampleCount = 0
    private var accEnmoSum = 0.0
    private var accEnmoSqSum = 0.0
    private var lightSampleCount = 0
    private var lightSum = 0.0
    private var epochStepCount = 0

    // Rolling circular buffer of 60-second epochs (max 12 hours)
    private val overnightEpochs = CopyOnWriteArrayList<NativeEpochData>()

    private var isScreenOn: Boolean = false
    private var isCharging: Boolean = false
    private var currentEpochStartMs: Long = 0L

    private val json = Json { ignoreUnknownKeys = true }

    private val stateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON -> {
                    isScreenOn = true
                    Log.d(TAG, "[Screen] Screen turned ON")
                    handlePossibleWakeEvent()
                }
                Intent.ACTION_SCREEN_OFF -> {
                    isScreenOn = false
                    Log.d(TAG, "[Screen] Screen turned OFF")
                }
                Intent.ACTION_USER_PRESENT -> {
                    isScreenOn = true
                    Log.i(TAG, "[UserPresent] User unlocked phone")
                    handlePossibleWakeEvent()
                }
                Intent.ACTION_POWER_CONNECTED -> {
                    isCharging = true
                    Log.i(TAG, "[Power] Bedside charging connected")
                }
                Intent.ACTION_POWER_DISCONNECTED -> {
                    isCharging = false
                    Log.i(TAG, "[Power] Charging disconnected")
                    if (isScreenOn && currentState != SleepTrackingState.MONITORING_REST) {
                        candidateWakeSteps += 5
                        handlePossibleWakeEvent()
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "[SleepService] Custom Native Sleep Service onCreate. Native Engine: ${NativeSleepEngine.getVersion()}")

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        // Initialize state
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        isScreenOn = powerManager.isInteractive
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            registerReceiver(null, filter)
        }
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        // Register system broadcast filters
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        registerReceiver(stateReceiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                Log.i(TAG, "[SleepService] Received STOP command. Finalizing session...")
                finalizeSessionAndSave()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_FORCE_EVALUATE -> {
                Log.i(TAG, "[SleepService] Force evaluation triggered")
                evaluateCurrentData()
                return START_STICKY
            }
            else -> {
                startMonitoring()
            }
        }
        return START_STICKY
    }

    private fun startMonitoring() {
        if (isRunning) {
            Log.d(TAG, "[SleepService] Service already running.")
            return
        }

        isRunning = true
        currentState = SleepTrackingState.MONITORING_REST
        candidateWakeStartMs = 0L
        candidateWakeSteps = 0
        consecutiveStillEpochsInCandidate = 0

        val notification = SleepNotificationHelper.createMonitoringNotification(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                SleepNotificationHelper.MONITORING_NOTIFICATION_ID,
                notification,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                } else {
                    0
                }
            )
        } else {
            startForeground(SleepNotificationHelper.MONITORING_NOTIFICATION_ID, notification)
        }

        Log.i(TAG, "[SleepService] Foreground notification attached. Registering hardware sensor listeners...")

        // Register accelerometer with 10-second hardware batching
        accelerometer?.let {
            val registered = sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL,
                10_000_000
            )
            Log.i(TAG, "[Accelerometer] Registered with hardware batching: $registered")
        } ?: Log.w(TAG, "[Accelerometer] No accelerometer sensor found on device")

        // Register light sensor
        lightSensor?.let {
            val registered = sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL,
                10_000_000
            )
            Log.i(TAG, "[LightSensor] Registered: $registered")
        }

        // Register hardware step detector
        stepDetector?.let {
            val registered = sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL,
                10_000_000
            )
            Log.i(TAG, "[StepDetector] Registered: $registered")
        }

        currentEpochStartMs = System.currentTimeMillis()

        // Start background epoch aggregator loop
        serviceScope.launch {
            Log.i(TAG, "[Loop] Epoch aggregation loop started (evaluating every 60s)")
            while (isActive) {
                delay(60_000L)
                try {
                    sensorManager.flush(this@CustomSleepService)
                } catch (e: Exception) {
                    // Ignore flush errors on legacy devices
                }
                flushCurrentEpoch()
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        synchronized(this) {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    val magnitude = sqrt((x * x + y * y + z * z).toDouble())
                    val enmo = (magnitude - 9.80665).coerceAtLeast(0.0)

                    accEnmoSum += enmo
                    accEnmoSqSum += (enmo * enmo)
                    accSampleCount++
                }
                Sensor.TYPE_LIGHT -> {
                    val lux = event.values[0].toDouble()
                    lightSum += lux
                    lightSampleCount++
                }
                Sensor.TYPE_STEP_DETECTOR -> {
                    epochStepCount++
                    if (currentState == SleepTrackingState.IN_SLEEP || currentState == SleepTrackingState.CANDIDATE_WAKE) {
                        candidateWakeSteps++
                        Log.d(TAG, "[StepDetector] Step detected (Total candidate steps: $candidateWakeSteps)")
                        if (candidateWakeSteps >= STEP_THRESHOLD_CONFIRMED_WAKE) {
                            Log.i(TAG, "[StepDetector] $candidateWakeSteps steps detected. Confirming morning wake-up.")
                            confirmWakeUpAndFinalize()
                        }
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun flushCurrentEpoch() {
        val (meanEnmo, variance, meanLight, samples, steps) = synchronized(this) {
            val meanE = if (accSampleCount > 0) (accEnmoSum / accSampleCount).toFloat() else 0f
            val v = if (accSampleCount > 1) {
                val meanSq = accEnmoSqSum / accSampleCount
                val sqMean = (accEnmoSum / accSampleCount) * (accEnmoSum / accSampleCount)
                (meanSq - sqMean).coerceAtLeast(0.0).toFloat()
            } else 0f
            val meanL = if (lightSampleCount > 0) (lightSum / lightSampleCount).toFloat() else 0f
            val count = accSampleCount
            val stepCount = epochStepCount

            // Reset scalar accumulators for next epoch
            accSampleCount = 0
            accEnmoSum = 0.0
            accEnmoSqSum = 0.0
            lightSampleCount = 0
            lightSum = 0.0
            epochStepCount = 0

            Tuple5(meanE, v, meanL, count, stepCount)
        }

        val now = System.currentTimeMillis()
        val epochStart = if (currentEpochStartMs > 0) currentEpochStartMs else now - 60_000L
        currentEpochStartMs = now

        val activityCount = meanEnmo * 100f

        val epoch = NativeEpochData(
            timestampMs = epochStart,
            durationSeconds = 60,
            activityCount = activityCount,
            variance = variance,
            meanLightLux = meanLight,
            screenOn = isScreenOn,
            charging = isCharging
        )

        // Rolling circular buffer enforcement
        if (overnightEpochs.size >= MAX_OVERNIGHT_EPOCHS) {
            overnightEpochs.removeAt(0)
        }
        overnightEpochs.add(epoch)

        Log.i(
            TAG,
            "[Epoch #${overnightEpochs.size}] State: $currentState, ENMO: ${"%.2f".format(activityCount)}, " +
                    "Lux: ${"%.1f".format(meanLight)}, Screen: $isScreenOn, Steps: $steps"
        )

        processStateMachine(epoch, activityCount)
    }

    private fun processStateMachine(epoch: NativeEpochData, activityCount: Float) {
        when (currentState) {
            SleepTrackingState.MONITORING_REST -> {
                // Check if actigraphy detects a consolidated sleep core
                if (overnightEpochs.size >= 20) {
                    val analysis = NativeSleepEngine.analyzeEpochs(overnightEpochs.toList())
                    if (analysis != null && analysis.sleepDurationMinutes >= 20) {
                        currentState = SleepTrackingState.IN_SLEEP
                        candidateWakeStartMs = 0L
                        candidateWakeSteps = 0
                        Log.i(TAG, "[StateMachine] Consolidated sleep detected (${analysis.sleepDurationMinutes}m). Transitioned to IN_SLEEP.")
                    }
                }
            }

            SleepTrackingState.IN_SLEEP -> {
                // If high movement or screen usage occurs, enter candidate wake observation buffer
                if (epoch.screenOn || activityCount > 15f) {
                    currentState = SleepTrackingState.CANDIDATE_WAKE
                    candidateWakeStartMs = epoch.timestampMs
                    candidateWakeSteps = 0
                    consecutiveStillEpochsInCandidate = 0
                    Log.i(TAG, "[StateMachine] Wake activity detected. Entered CANDIDATE_WAKE observation buffer.")
                }
            }

            SleepTrackingState.CANDIDATE_WAKE -> {
                val isStillAndScreenOff = !epoch.screenOn && activityCount < 1.5f

                if (isStillAndScreenOff) {
                    consecutiveStillEpochsInCandidate++
                    Log.d(TAG, "[CandidateWake] Stillness count: $consecutiveStillEpochsInCandidate / 5")
                    // If stillness resumes for 5 consecutive minutes, it was a brief awakening (WASO)
                    if (consecutiveStillEpochsInCandidate >= 5) {
                        currentState = SleepTrackingState.IN_SLEEP
                        candidateWakeStartMs = 0L
                        candidateWakeSteps = 0
                        consecutiveStillEpochsInCandidate = 0
                        Log.i(TAG, "[StateMachine] Stillness resumed for 5m. Reverted from CANDIDATE_WAKE to IN_SLEEP (WASO recorded).")
                    }
                } else {
                    consecutiveStillEpochsInCandidate = 0

                    // Instant wake if step threshold reached
                    if (candidateWakeSteps >= STEP_THRESHOLD_CONFIRMED_WAKE) {
                        Log.i(TAG, "[StateMachine] Step threshold reached in candidate buffer. Confirming wake-up.")
                        confirmWakeUpAndFinalize()
                        return
                    }

                    // Wake confirmed if sustained activity persists for timeout duration (20 mins)
                    val elapsedMs = System.currentTimeMillis() - candidateWakeStartMs
                    if (candidateWakeStartMs > 0 && elapsedMs >= CANDIDATE_WAKE_TIMEOUT_MS) {
                        Log.i(TAG, "[StateMachine] 20m candidate wake timeout elapsed with sustained activity. Confirming wake-up.")
                        confirmWakeUpAndFinalize()
                    }
                }
            }
        }
    }

    private fun handlePossibleWakeEvent() {
        if (currentState == SleepTrackingState.IN_SLEEP) {
            currentState = SleepTrackingState.CANDIDATE_WAKE
            candidateWakeStartMs = System.currentTimeMillis()
            candidateWakeSteps = 0
            consecutiveStillEpochsInCandidate = 0
            Log.i(TAG, "[Interaction] User activity detected. Transitioned to CANDIDATE_WAKE.")
        }
    }

    private fun confirmWakeUpAndFinalize() {
        serviceScope.launch {
            finalizeSessionAndSave()

            val app = applicationContext as? KimonApplication
            val isScheduled = app?.userSettingsRepository?.sleepScheduledMode?.first() ?: true
            if (isScheduled) {
                Log.i(TAG, "[SleepService] Scheduled mode active. Stopping service and scheduling next night.")
                SleepAlarmScheduler.scheduleNextBedtimeAlarm(this@CustomSleepService)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            } else {
                // In manual continuous mode, reset to monitoring
                currentState = SleepTrackingState.MONITORING_REST
                candidateWakeStartMs = 0L
                candidateWakeSteps = 0
                consecutiveStillEpochsInCandidate = 0
            }
        }
    }

    private fun evaluateCurrentData() {
        serviceScope.launch {
            flushCurrentEpoch()
            val currentCal = Calendar.getInstance()
            val currentHour = currentCal.get(Calendar.HOUR_OF_DAY)
            val currentMinute = currentCal.get(Calendar.MINUTE)
            val isMorningWindow = currentHour in 5..12 || (currentHour == 4 && currentMinute >= 30)

            val epochs = overnightEpochs.toList()
            val analysis = NativeSleepEngine.analyzeEpochs(epochs)
            if (analysis != null && isMorningWindow && analysis.sleepDurationMinutes >= 45) {
                Log.i(TAG, "[CurrentEvaluation] Morning sleep detected (${analysis.sleepDurationMinutes}m). Finalizing session.")
                finalizeSessionAndSave()
            } else {
                Log.d(TAG, "[CurrentEvaluation] Still monitoring overnight (${epochs.size} epochs).")
            }
        }
    }

    private fun finalizeSessionAndSave() {
        val epochsList = overnightEpochs.toList()
        if (epochsList.size < 15) {
            Log.w(TAG, "[Finalize] Session too short (< 15 mins). Discarding.")
            overnightEpochs.clear()
            return
        }

        val analysis = NativeSleepEngine.analyzeEpochs(epochsList)
        if (analysis == null || analysis.sleepDurationMinutes < 15) {
            Log.w(TAG, "[Finalize] No valid consolidated sleep detected by Native Actigraphy Engine.")
            overnightEpochs.clear()
            return
        }

        // Query App Usage during the sleep interval
        val app = applicationContext as? KimonApplication ?: return
        serviceScope.launch {
            val isAppUsageEnabled = app.userSettingsRepository.appUsageAccessEnabled.first()
            val appUsageEvents = if (isAppUsageEnabled && AppUsageHelper.hasUsageStatsPermission(this@CustomSleepService)) {
                val events = AppUsageHelper.getAppUsageDuringInterval(
                    this@CustomSleepService,
                    analysis.sleepOnsetEpochMs,
                    analysis.wakeTimeEpochMs
                )
                Log.i(TAG, "[AppUsage] Extracted ${events.size} app usage events during sleep window")
                events
            } else {
                emptyList()
            }

            val appUsageJsonString = if (appUsageEvents.isNotEmpty()) {
                json.encodeToString(appUsageEvents)
            } else null

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val session = SleepSessionEntity(
                startTimeEpochMs = analysis.sleepOnsetEpochMs,
                endTimeEpochMs = analysis.wakeTimeEpochMs,
                durationMinutes = analysis.sleepDurationMinutes,
                qualityScore = analysis.qualityScore,
                status = 0,
                source = "NATIVE_RUST_ENGINE",
                dateString = dateFormat.format(Date(analysis.sleepOnsetEpochMs)),
                notes = "Deep: ${analysis.deepSleepMinutes}m • Light: ${analysis.lightSleepMinutes}m • REM: ${analysis.remSleepMinutes}m • WASO: ${analysis.wakeDurationMinutes}m",
                appUsageJson = appUsageJsonString
            )

            val id = app.sleepRepository.recordSession(session, sendNotification = true)
            Log.i(TAG, "[Database] Successfully saved SleepSession (ID=$id, Duration=${session.durationMinutes}m, Quality=${session.qualityScore}%, Mid-sleep Apps: ${appUsageEvents.size})")
            overnightEpochs.clear()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        Log.i(TAG, "[SleepService] Custom Native Sleep Service onDestroy.")
        try {
            unregisterReceiver(stateReceiver)
            sensorManager.unregisterListener(this)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering sensors/receivers", e)
        }
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
}

