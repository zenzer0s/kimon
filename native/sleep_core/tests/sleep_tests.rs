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
    assert!(result.total_duration_minutes >= 420);
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

#[test]
fn test_table_stillness_then_midnight_usage_then_actual_sleep() {
    // Scenario:
    // 0..60 mins (11:30pm - 12:30am): Phone untouched on table (stillness, 0 lux)
    // 60..90 mins (12:30am - 1:00am): User picks up phone, checks ChatGPT & messages (screen_on, active)
    // 90..150 mins (1:00am - 2:00am): User works on laptop, phone on table
    // 150..160 mins (2:00am - 2:10am): User sets alarm, locks phone (screen_on)
    // 160..520 mins (2:10am - 8:10am): Actual consolidated sleep (360 mins)
    // 520..550 mins (8:10am - 8:40am): Morning wake-up, walk, screen on
    let mut epochs = Vec::with_capacity(550);
    let start_time_ms = 1700000000000i64;

    for i in 0..550 {
        let timestamp_ms = start_time_ms + (i as i64 * 60_000);
        let is_early_table_idle = i < 60;
        let is_midnight_phone_use = (60..90).contains(&i);
        let is_laptop_table_idle = (90..150).contains(&i);
        let is_bedtime_phone_use = (150..160).contains(&i);
        let is_main_sleep = (160..520).contains(&i);
        let is_morning_wake = i >= 520;

        let (activity, screen, light) = if is_midnight_phone_use || is_bedtime_phone_use || is_morning_wake {
            (25.0, true, 35.0)
        } else if is_early_table_idle || is_laptop_table_idle {
            (0.0, false, 0.0)
        } else if is_main_sleep {
            (0.3, false, 0.0)
        } else {
            (0.0, false, 0.0)
        };

        epochs.push(EpochData {
            timestamp_ms,
            duration_seconds: 60,
            activity_count: activity,
            variance: 0.001,
            mean_light_lux: light,
            screen_on: screen,
            charging: true,
        });
    }

    let result = ActigraphyEngine::analyze_session(&epochs).expect("Should analyze session");

    // The main consolidated sleep should be recognized from ~min 160 (2:10am) to ~min 520 (8:10am)
    let onset_diff_mins = (result.sleep_onset_time_ms - (start_time_ms + 160 * 60_000)).abs() / 60_000;
    let wake_diff_mins = (result.wake_time_ms - (start_time_ms + 520 * 60_000)).abs() / 60_000;

    assert!(onset_diff_mins <= 5, "Onset should be close to 2:10am (min 160), got diff {} mins", onset_diff_mins);
    assert!(wake_diff_mins <= 5, "Wake should be close to 8:10am (min 520), got diff {} mins", wake_diff_mins);
    assert!(result.sleep_duration_minutes >= 350, "Sleep duration should be ~360 mins, got {}", result.sleep_duration_minutes);
}

#[test]
fn test_long_duration_10hr_sleep() {
    // 10 hours (600 minutes) weekend recovery sleep
    // 0..10m: bedtime reading
    // 10..580m: 570m sleep with 2 brief awakenings (min 200, min 400)
    // 580..600m: morning wake
    let mut epochs = Vec::with_capacity(600);
    let start_time_ms = 1700000000000i64;

    for i in 0..600 {
        let timestamp_ms = start_time_ms + (i as i64 * 60_000);
        let is_bedtime = i < 10;
        let is_morning = i >= 580;
        let is_awakening = i == 200 || i == 400;

        let (activity, screen, light) = if is_bedtime || is_morning {
            (30.0, true, 40.0)
        } else if is_awakening {
            (18.0, false, 0.5)
        } else {
            (0.1, false, 0.0)
        };

        epochs.push(EpochData {
            timestamp_ms,
            duration_seconds: 60,
            activity_count: activity,
            variance: 0.001,
            mean_light_lux: light,
            screen_on: screen,
            charging: true,
        });
    }

    let result = ActigraphyEngine::analyze_session(&epochs).expect("Should analyze long session");
    assert!(result.sleep_duration_minutes >= 550, "Expected >= 550m sleep, got {}", result.sleep_duration_minutes);
    assert!(result.sleep_efficiency >= 90.0, "Expected high efficiency, got {}", result.sleep_efficiency);
    assert_eq!(result.wake_count, 2, "Expected 2 awakenings");
    assert!(result.quality_score >= 85, "Expected high quality score for 10h rest, got {}", result.quality_score);
}

#[test]
fn test_midnight_bathroom_trip_screen_on_brief_then_sleep_resumption() {
    // 0..15m: bedtime
    // 15..200m: first sleep block (185m)
    // 200..204m: 3:00am bathroom break, screen on for 3 mins, motion
    // 204..480m: second sleep block (276m)
    // 480..500m: morning wake up
    let mut epochs = Vec::with_capacity(500);
    let start_time_ms = 1700000000000i64;

    for i in 0..500 {
        let timestamp_ms = start_time_ms + (i as i64 * 60_000);
        let is_bedtime = i < 15;
        let is_morning = i >= 480;
        let is_bathroom_break = (200..204).contains(&i);

        let (activity, screen, light) = if is_bedtime || is_morning {
            (25.0, true, 40.0)
        } else if is_bathroom_break {
            (20.0, true, 10.0)
        } else {
            (0.2, false, 0.0)
        };

        epochs.push(EpochData {
            timestamp_ms,
            duration_seconds: 60,
            activity_count: activity,
            variance: 0.001,
            mean_light_lux: light,
            screen_on: screen,
            charging: true,
        });
    }

    let result = ActigraphyEngine::analyze_session(&epochs).expect("Should merge overnight blocks");
    // Should be a single consolidated session from min 15 to min 480 (465 mins total in bed)
    assert!(result.total_duration_minutes >= 450, "Total duration should be ~465 mins, got {}", result.total_duration_minutes);
    assert!(result.sleep_duration_minutes >= 430, "Sleep duration should be ~450 mins, got {}", result.sleep_duration_minutes);
    assert!(result.wake_duration_minutes <= 30, "WASO should be small, got {}", result.wake_duration_minutes);
}

#[test]
fn test_power_nap_45min() {
    // Afternoon nap: 45 minutes total (35 minutes sleep)
    let mut epochs = Vec::with_capacity(45);
    let start_time_ms = 1700000000000i64;

    for i in 0..45 {
        let timestamp_ms = start_time_ms + (i as i64 * 60_000);
        let is_onset = i < 5;
        let is_wake = i >= 40;

        let (activity, screen, light) = if is_onset || is_wake {
            (20.0, true, 50.0)
        } else {
            (0.2, false, 15.0)
        };

        epochs.push(EpochData {
            timestamp_ms,
            duration_seconds: 60,
            activity_count: activity,
            variance: 0.001,
            mean_light_lux: light,
            screen_on: screen,
            charging: false,
        });
    }

    let result = ActigraphyEngine::analyze_session(&epochs).expect("Should analyze power nap");
    assert!(result.sleep_duration_minutes >= 28, "Nap sleep should be >= 28 mins, got {}", result.sleep_duration_minutes);
    assert!(result.total_duration_minutes <= 45, "Total duration should be <= 45 mins, got {}", result.total_duration_minutes);
}

#[test]
fn test_restless_insomnia_sleep() {
    // 7 hours in bed (420 mins) with frequent tossing & turning and 5 awakenings
    let mut epochs = Vec::with_capacity(420);
    let start_time_ms = 1700000000000i64;

    for i in 0..420 {
        let timestamp_ms = start_time_ms + (i as i64 * 60_000);
        let is_bedtime = i < 15;
        let is_morning = i >= 400;
        let is_awakening = i == 80 || i == 150 || i == 220 || i == 290 || i == 350;
        let is_restless = i % 15 == 0;

        let (activity, screen, light) = if is_bedtime || is_morning {
            (25.0, true, 30.0)
        } else if is_awakening {
            (25.0, true, 20.0)
        } else if is_restless {
            (8.0, false, 0.0)
        } else {
            (0.3, false, 0.0)
        };

        epochs.push(EpochData {
            timestamp_ms,
            duration_seconds: 60,
            activity_count: activity,
            variance: 0.05,
            mean_light_lux: light,
            screen_on: screen,
            charging: true,
        });
    }

    let result = ActigraphyEngine::analyze_session(&epochs).expect("Should analyze restless sleep");
    assert!(result.wake_count >= 4, "Should record multiple awakenings, got {}", result.wake_count);
    assert!(result.sleep_efficiency < 90.0, "Efficiency should reflect restlessness, got {}", result.sleep_efficiency);
    assert!(result.quality_score <= 80, "Quality score should have penalties for WASO and awakenings, got {}", result.quality_score);
}

#[test]
fn test_daytime_shift_worker_sleep() {
    // Shift worker: Sleeps 8:00am to 4:00pm (480 mins) in ambient daytime light (80 lux)
    let mut epochs = Vec::with_capacity(480);
    let start_time_ms = 1700000000000i64;

    for i in 0..480 {
        let timestamp_ms = start_time_ms + (i as i64 * 60_000);
        let is_bedtime = i < 20;
        let is_morning = i >= 460;

        let (activity, screen, light) = if is_bedtime || is_morning {
            (25.0, true, 120.0)
        } else {
            // Still body, screen off, but bedroom has daylight leakage (75 lux)
            (0.2, false, 75.0)
        };

        epochs.push(EpochData {
            timestamp_ms,
            duration_seconds: 60,
            activity_count: activity,
            variance: 0.001,
            mean_light_lux: light,
            screen_on: screen,
            charging: false,
        });
    }

    let result = ActigraphyEngine::analyze_session(&epochs).expect("Should analyze daytime sleep");
    assert!(result.sleep_duration_minutes >= 420, "Daytime sleep duration should be ~440 mins, got {}", result.sleep_duration_minutes);
    assert!(result.sleep_efficiency >= 85.0, "Daytime sleep should have solid efficiency if body is still");
}

#[test]
fn test_maximum_12hr_rolling_window() {
    // 12 hours max rolling window (720 minutes)
    let mut epochs = Vec::with_capacity(720);
    let start_time_ms = 1700000000000i64;

    for i in 0..720 {
        let timestamp_ms = start_time_ms + (i as i64 * 60_000);
        let is_prebed_idle = i < 120; // 2 hours idle on table
        let is_intermittent_active = (120..140).contains(&i); // 20m phone use
        let is_sleep = (140..680).contains(&i); // 9 hours sleep (540m)
        let is_morning = i >= 680;

        let (activity, screen, light) = if is_intermittent_active || is_morning {
            (25.0, true, 40.0)
        } else if is_prebed_idle {
            (0.0, false, 0.0)
        } else if is_sleep {
            (0.2, false, 0.0)
        } else {
            (0.0, false, 0.0)
        };

        epochs.push(EpochData {
            timestamp_ms,
            duration_seconds: 60,
            activity_count: activity,
            variance: 0.001,
            mean_light_lux: light,
            screen_on: screen,
            charging: true,
        });
    }

    let result = ActigraphyEngine::analyze_session(&epochs).expect("Should handle 12-hour max buffer");
    assert!(result.sleep_duration_minutes >= 500, "Should isolate 9hr sleep from 12hr buffer, got {}", result.sleep_duration_minutes);
    assert!(result.total_duration_minutes <= 560, "Should ignore initial 2hr table idle, got {}", result.total_duration_minutes);
}
