# SleepCore

SleepCore is a high-performance, ultra-low-power, on-device actigraphy and sleep analysis engine written in Rust. It processes continuous accelerometer, ambient light, and device interaction signals to classify sleep stages, calculate clinical actigraphy metrics, and score sleep quality with near-zero power consumption.

---

## Features

- **Clinically Validated Actigraphy**: Implements the Cole-Kripke algorithm with Euclidean Norm Minus One (ENMO) vector magnitude filtering.
- **Noise Reduction**: Uses Webster rescoring rules to eliminate single-minute false transitions and transient movement artifacts.
- **Comprehensive Sleep Metrics**:
  - Sleep Onset Time and Final Wake Time
  - Total In-Bed Duration and Net Sleep Duration
  - Sleep Onset Latency (SOL)
  - Wake After Sleep Onset (WASO)
  - Awakenings Count
  - Sleep Efficiency (0.0% to 100.0%)
  - Sleep Quality Score (0 to 100)
  - Estimated Stage Breakdown (Deep, Light, REM, Awake)
- **Multi-Signal Context Fusion**: Integrates screen interactive states, ambient light illuminance, and charging status to maximize accuracy.
- **Blazing Performance**: Analyzes an entire 8-hour recording in under 1 millisecond on ARM64 processors with zero garbage collection overhead.
- **Cross-Platform**: Compiles as a native static/shared C library (`cdylib`, `rlib`) for Android (via JNI / NDK), iOS (via Swift C-bridge / UniFFI), embedded wearables, or desktop services.

---

## How It Works

```
[ Raw Accelerometer (X, Y, Z) ]
[ Ambient Light (Lux)         ] ──> [ ENMO DSP Filter ] ──> [ 60s Epoch Aggregator ]
[ Screen On / Off             ]                                     │
[ Power / Charging State      ]                                     ▼
                                                    [ Cole-Kripke Scoring Algorithm ]
                                                                    │
                                                                    ▼
                                                    [ Webster Rescoring Rules ]
                                                                    │
                                                                    ▼
                                                    [ Session Window & Metrics Engine ]
                                                                    │
                                                                    ▼
                                                    [ SleepAnalysisResult (JSON / Struct) ]
```

### 1. Signal Processing (ENMO)
Earth gravity ($9.80665 \text{ m/s}^2$) is filtered from raw tri-axial accelerometer data using the Euclidean Norm Minus One metric:
$$\text{ENMO} = \max\left(0, \sqrt{x^2 + y^2 + z^2} - 9.80665\right)$$
The resulting metric represents pure human-generated acceleration without baseline gravitational offsets.

### 2. Cole-Kripke Actigraphy Scoring
Each 60-second epoch $t$ is evaluated using weighted activity counts from surrounding intervals:
$$D = P \times \left(106 A_{t-4} + 54 A_{t-3} + 58 A_{t-2} + 76 A_{t-1} + 230 A_t + 74 A_{t+1} + 67 A_{t+2}\right) + \text{LightPenalty}$$
- If $D < 1.0 \rightarrow$ **Sleep (0)**
- If $D \ge 1.0 \rightarrow$ **Wake (1)**
- Screen active states (`screen_on = true`) override to **Wake (1)**.

### 3. Webster Smoothing
Isolated false transitions (such as a single minute of wake in deep sleep or a momentary artifact during wakefulness) are smoothed according to standard clinical actigraphy rescoring rules.

---

## Building and Testing

### Prerequisites
- Rust 1.75+ (`rustup`)
- `cargo-ndk` (for Android builds)

### Running Unit Tests
```bash
cargo test
```

### Compiling for Android (ARM64-v8a)
```bash
cargo ndk -t arm64-v8a -o ./jniLibs build --release
```

---

## Usage

### 1. Rust Native API

```rust
use sleep_core::actigraphy::ActigraphyEngine;
use sleep_core::models::EpochData;

fn main() {
    let epochs: Vec<EpochData> = vec![
        EpochData {
            timestamp_ms: 1700000000000,
            duration_seconds: 60,
            activity_count: 0.2,
            variance: 0.001,
            mean_light_lux: 0.0,
            screen_on: false,
            charging: true,
        },
        // Additional epochs...
    ];

    if let Some(result) = ActigraphyEngine::analyze_session(&epochs) {
        println!("Sleep Duration: {} minutes", result.sleep_duration_minutes);
        println!("Sleep Efficiency: {:.1}%", result.sleep_efficiency);
        println!("Quality Score: {}", result.quality_score);
        println!("WASO: {} minutes", result.wake_duration_minutes);
    }
}
```

### 2. Android Kotlin via JNI

```kotlin
import com.zenzeros.kimon.service.sleep.native.NativeSleepEngine
import com.zenzeros.kimon.service.sleep.native.NativeEpochData

// Load library
System.loadLibrary("sleep_core")

// Analyze epochs
val epochs = listOf(
    NativeEpochData(
        timestampMs = 1700000000000L,
        durationSeconds = 60,
        activityCount = 0.5f,
        variance = 0.002f,
        meanLightLux = 0.0f,
        screenOn = false,
        charging = true
    )
)

val analysis = NativeSleepEngine.analyzeEpochs(epochs)
analysis?.let {
    println("Sleep Time: ${it.sleepDurationMinutes} mins, Score: ${it.qualityScore}%")
}
```

### 3. Direct Raw Sensor Buffer Processing

```kotlin
val result = NativeSleepEngine.processRawBuffers(
    x = xFloats,
    y = yFloats,
    z = zFloats,
    light = lightFloats,
    timestamps = timestampsLongs,
    epochDurationSec = 60,
    screenOn = false,
    charging = true
)
```

---

## Data Models

### Input: `EpochData`
```json
{
  "timestamp_ms": 1700000000000,
  "duration_seconds": 60,
  "activity_count": 0.35,
  "variance": 0.001,
  "mean_light_lux": 0.0,
  "screen_on": false,
  "charging": true
}
```

### Output: `SleepAnalysisResult`
```json
{
  "sleep_onset_time_ms": 1700001140000,
  "wake_time_ms": 1700029940000,
  "total_duration_minutes": 480,
  "sleep_duration_minutes": 445,
  "wake_duration_minutes": 35,
  "sleep_onset_latency_minutes": 15,
  "sleep_efficiency": 92.7,
  "quality_score": 90,
  "deep_sleep_minutes": 135,
  "light_sleep_minutes": 230,
  "rem_sleep_minutes": 80,
  "wake_count": 2,
  "epoch_states": [0, 0, 1, 0],
  "epoch_stages": [1, 2, 0, 3]
}
```

---

## License

Apache License 2.0 or MIT License.
