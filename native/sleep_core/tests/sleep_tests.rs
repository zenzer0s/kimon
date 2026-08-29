use sleep_core::actigraphy::ActigraphyEngine;
use sleep_core::dsp::DspProcessor;
use sleep_core::models::{EpochData, RawSensorSample};

#[test]
fn test_enmo_calculation() {
    // Pure stationary sensor resting flat on a table (x=0, y=0, z=9.80665)
    let enmo_stationary = DspProcessor::compute_enmo(0.0, 0.0, 9.80665);
    assert!((enmo_stationary - 0.0).abs() < 0.001);

    // Active movement (x=3.0, y=4.0, z=12.0) -> norm = 13.0 -> ENMO = 13.0 - 9.80665 = 3.19335
    let enmo_active = DspProcessor::compute_enmo(3.0, 4.0, 12.0);
    assert!((enmo_active - 3.19335).abs() < 0.001);
}

#[test]
fn test_synthetic_overnight_session_analysis() {
    // Simulate 8 hours (480 minutes) of 60-second epochs
    // 0..15 mins: awake reading phone (screen_on = true, activity > 10)
    // 15..450 mins: sound sleep (stationary, screen_on = false, low activity < 1.0)
    // with 2 brief awakenings around min 180 and min 300
    // 450..480 mins: morning wake up and movement
    let mut epochs = Vec::with_capacity(480);
    let start_time_ms = 1700000000000i64;

    for i in 0..480 {
        let timestamp_ms = start_time_ms + (i as i64 * 60_000);
        let is_bedtime_awake = i < 15;
        let is_morning_awake = i >= 450;
        let is_night_awakening = i == 180 || i == 300;

        let (activity_count, screen_on, light) = if is_bedtime_awake {
            (25.0, true, 30.0)
        } else if is_morning_awake {
            (45.0, true, 120.0)
        } else if is_night_awakening {
            (20.0, false, 0.5)
        } else {
            (0.2, false, 0.0)
        };

        epochs.push(EpochData {
            timestamp_ms,
            duration_seconds: 60,
            activity_count,
            variance: 0.001,
            mean_light_lux: light,
            screen_on,
            charging: true,
        });
    }

    let result = ActigraphyEngine::analyze_session(&epochs).expect("Should analyze session");

    println!("Analysis Result: {:?}", result);
    assert!(result.total_duration_minutes >= 430);
    assert!(result.sleep_duration_minutes >= 400);
    assert!(result.sleep_efficiency >= 85.0);
    assert!(result.quality_score >= 80);
    assert_eq!(result.wake_count, 2); // 2 night awakenings detected
}

#[test]
fn test_sample_aggregation() {
    let mut samples = Vec::new();
    let base_time = 1000000i64;

    // 100 samples at 10Hz (10 seconds)
    for i in 0..100 {
        samples.push(RawSensorSample {
            timestamp_ms: base_time + (i * 100),
            x: 0.0,
            y: 0.0,
            z: 9.80665,
            light_lux: 10.0,
        });
    }

    let epochs = DspProcessor::aggregate_samples_into_epochs(&samples, 10, false, true);
    assert_eq!(epochs.len(), 1);
    assert!((epochs[0].activity_count - 0.0).abs() < 0.01);
}
