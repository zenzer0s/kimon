use serde::{Deserialize, Serialize};

/// Raw 3-axis accelerometer and ambient light sample
#[derive(Debug, Clone, Copy, Serialize, Deserialize)]
pub struct RawSensorSample {
    pub timestamp_ms: i64,
    pub x: f32, // m/s^2
    pub y: f32, // m/s^2
    pub z: f32, // m/s^2
    pub light_lux: f32,
}

/// A fixed time window (typically 30 or 60 seconds) aggregating sensor metrics
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct EpochData {
    pub timestamp_ms: i64,
    pub duration_seconds: u32,
    pub activity_count: f32,
    pub variance: f32,
    pub mean_light_lux: f32,
    pub screen_on: bool,
    pub charging: bool,
}

/// Binary sleep state for an epoch
#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
#[repr(u8)]
pub enum SleepState {
    Sleep = 0,
    Wake = 1,
    Unknown = 2,
}

/// Heuristic sleep stages
#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
#[repr(u8)]
pub enum SleepStage {
    Awake = 0,
    Light = 1,
    Deep = 2,
    Rem = 3,
}

/// High-level session analysis result returned to Kotlin
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct SleepAnalysisResult {
    pub sleep_onset_time_ms: i64,
    pub wake_time_ms: i64,
    pub total_duration_minutes: i64,
    pub sleep_duration_minutes: i64,
    pub wake_duration_minutes: i64,      // WASO (Wake After Sleep Onset)
    pub sleep_onset_latency_minutes: i64,// SOL (Time to fall asleep)
    pub sleep_efficiency: f32,           // Percentage 0.0 - 100.0%
    pub quality_score: i32,              // 0 - 100
    pub deep_sleep_minutes: i64,
    pub light_sleep_minutes: i64,
    pub rem_sleep_minutes: i64,
    pub wake_count: i32,                 // Number of awakenings
    pub epoch_states: Vec<u8>,           // 0 = Sleep, 1 = Wake for each epoch
    pub epoch_stages: Vec<u8>,           // 0 = Awake, 1 = Light, 2 = Deep, 3 = Rem
}
