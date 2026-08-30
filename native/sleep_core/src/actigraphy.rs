use crate::models::{EpochData, SleepAnalysisResult, SleepStage, SleepState};

pub struct ActigraphyEngine;

impl ActigraphyEngine {
    /// Cole-Kripke Algorithm for 1-minute (or scaled 30s) epochs:
    /// D = P * (0.001 * (106*A_t-4 + 54*A_t-3 + 58*A_t-2 + 76*A_t-1 + 230*A_t + 74*A_t+1 + 67*A_t+2))
    /// D < 1.0 => Sleep (0), D >= 1.0 => Wake (1)
    pub fn score_cole_kripke(epochs: &[EpochData]) -> Vec<SleepState> {
        let n = epochs.len();
        if n == 0 {
            return Vec::new();
        }

        let mut states = vec![SleepState::Sleep; n];
        let p_scale: f32 = 0.0033; // Calibration constant for scaled mobile accelerometer ENMO

        for i in 0..n {
            // Screen ON is an unequivocal ground-truth signal of wakefulness
            if epochs[i].screen_on {
                states[i] = SleepState::Wake;
                continue;
            }

            let a_m4 = if i >= 4 { epochs[i - 4].activity_count } else { epochs[i].activity_count };
            let a_m3 = if i >= 3 { epochs[i - 3].activity_count } else { epochs[i].activity_count };
            let a_m2 = if i >= 2 { epochs[i - 2].activity_count } else { epochs[i].activity_count };
            let a_m1 = if i >= 1 { epochs[i - 1].activity_count } else { epochs[i].activity_count };
            let a_0  = epochs[i].activity_count;
            let a_p1 = if i + 1 < n { epochs[i + 1].activity_count } else { epochs[i].activity_count };
            let a_p2 = if i + 2 < n { epochs[i + 2].activity_count } else { epochs[i].activity_count };

            let d = p_scale * (
                106.0 * a_m4 +
                54.0  * a_m3 +
                58.0  * a_m2 +
                76.0  * a_m1 +
                230.0 * a_0  +
                74.0  * a_p1 +
                67.0  * a_p2
            );

            // Bias with ambient light if there is accompanying physical movement
            let light_penalty = if epochs[i].mean_light_lux > 60.0 && epochs[i].activity_count > 2.0 { 0.4 } else { 0.0 };

            if (d + light_penalty) >= 1.0 {
                states[i] = SleepState::Wake;
            } else {
                states[i] = SleepState::Sleep;
            }
        }

        // Apply Webster Rescoring Rules to eliminate solitary flickers
        Self::apply_webster_rules(&mut states);

        states
    }

    /// Webster rescoring rules to smooth out single-minute false transitions
    pub fn apply_webster_rules(states: &mut [SleepState]) {
        let n = states.len();
        if n < 5 {
            return;
        }

        // Rule 1: Single minute of sleep surrounded by wake -> wake
        for i in 1..(n - 1) {
            if states[i] == SleepState::Sleep && states[i - 1] == SleepState::Wake && states[i + 1] == SleepState::Wake {
                states[i] = SleepState::Wake;
            }
        }

        // Rule 2: Single minute of wake surrounded by sleep -> sleep (if not screen-on)
        for i in 1..(n - 1) {
            if states[i] == SleepState::Wake && states[i - 1] == SleepState::Sleep && states[i + 1] == SleepState::Sleep {
                states[i] = SleepState::Sleep;
            }
        }
    }

    /// Find the primary consolidated sleep window in a sequence of scored epoch states.
    /// Filters out early pre-bed "table stillness" that is followed by active phone usage / prolonged wakefulness.
    pub fn find_consolidated_sleep_window(states: &[SleepState], epochs: &[EpochData]) -> Option<(usize, usize)> {
        let n = states.len();
        if n < 10 {
            return None;
        }

        // 1. Identify all candidate sleep segments (contiguous blocks of Sleep, allowing brief WASO gaps <= 5 mins)
        let mut segments: Vec<(usize, usize, usize)> = Vec::new(); // (start_idx, end_idx, sleep_epoch_count)
        let mut in_segment = false;
        let mut seg_start = 0;
        let mut seg_sleep_count = 0;
        let mut consecutive_wake = 0;

        for i in 0..n {
            let is_sleep = states[i] == SleepState::Sleep && !epochs[i].screen_on;
            if is_sleep {
                if !in_segment {
                    in_segment = true;
                    seg_start = i;
                    seg_sleep_count = 1;
                    consecutive_wake = 0;
                } else {
                    seg_sleep_count += 1;
                    consecutive_wake = 0;
                }
            } else {
                if in_segment {
                    consecutive_wake += 1;
                    // If wake continues for > 15 consecutive minutes (or explicit screen-on > 3 mins), the sleep segment closes
                    let is_active_wake = consecutive_wake >= 15 || 
                        (consecutive_wake >= 3 && (0..consecutive_wake).all(|w| epochs[i - w].screen_on));
                    
                    if is_active_wake {
                        let seg_end = i.saturating_sub(consecutive_wake);
                        if seg_sleep_count >= 8 {
                            segments.push((seg_start, seg_end, seg_sleep_count));
                        }
                        in_segment = false;
                        seg_sleep_count = 0;
                        consecutive_wake = 0;
                    }
                }
            }
        }

        if in_segment && seg_sleep_count >= 8 {
            let seg_end = (n - 1).saturating_sub(consecutive_wake);
            segments.push((seg_start, seg_end, seg_sleep_count));
        }

        if segments.is_empty() {
            return None;
        }

        // 2. Select the dominant / main sleep segment (highest sleep volume)
        let max_seg = segments.iter().max_by_key(|s| s.2)?;
        
        let mut merged_start = max_seg.0;
        let mut merged_end = max_seg.1;

        for seg in &segments {
            // If another segment is after max_seg and separated by < 20 mins, merge it
            if seg.0 > merged_end && (seg.0 - merged_end) <= 20 {
                merged_end = seg.1;
            }
            // If another segment is before max_seg and separated by < 20 mins without active screen usage, merge it
            if seg.1 < merged_start && (merged_start - seg.1) <= 20 {
                let has_screen_in_gap = (seg.1..merged_start).any(|idx| epochs[idx].screen_on);
                if !has_screen_in_gap {
                    merged_start = seg.0;
                }
            }
        }

        Some((merged_start, merged_end))
    }

    /// Analyze overnight epochs and produce a comprehensive SleepAnalysisResult
    pub fn analyze_session(epochs: &[EpochData]) -> Option<SleepAnalysisResult> {
        if epochs.is_empty() {
            return None;
        }

        let states = Self::score_cole_kripke(epochs);
        let n = states.len();

        let (onset_idx, wake_idx) = Self::find_consolidated_sleep_window(&states, epochs)?;

        let onset_time_ms = epochs[onset_idx].timestamp_ms;
        let wake_time_ms = epochs[wake_idx].timestamp_ms + ((epochs[wake_idx].duration_seconds as i64) * 1000);
        let total_in_bed_minutes = ((wake_time_ms - onset_time_ms) / (1000 * 60)).max(1);

        let mut sleep_epochs_count = 0i64;
        let mut wake_epochs_count = 0i64;
        let mut awakenings_count = 0i32;
        let mut in_wake_block = false;

        let mut epoch_stages = Vec::with_capacity(n);

        for (i, state) in states.iter().enumerate() {
            if i < onset_idx || i > wake_idx {
                epoch_stages.push(SleepStage::Awake as u8);
                continue;
            }

            match state {
                SleepState::Sleep => {
                    sleep_epochs_count += 1;
                    in_wake_block = false;
                    
                    let var = epochs[i].variance;
                    let count = epochs[i].activity_count;
                    if count < 1.0 && var < 0.01 {
                        epoch_stages.push(SleepStage::Deep as u8);
                    } else if count < 4.0 {
                        epoch_stages.push(SleepStage::Light as u8);
                    } else {
                        epoch_stages.push(SleepStage::Rem as u8);
                    }
                }
                SleepState::Wake => {
                    wake_epochs_count += 1;
                    epoch_stages.push(SleepStage::Awake as u8);
                    if !in_wake_block {
                        awakenings_count += 1;
                        in_wake_block = true;
                    }
                }
                SleepState::Unknown => {
                    epoch_stages.push(SleepStage::Light as u8);
                }
            }
        }

        let epoch_mins = (epochs[0].duration_seconds as f32) / 60.0;
        let sleep_duration_minutes = ((sleep_epochs_count as f32) * epoch_mins).round() as i64;
        let waso_minutes = ((wake_epochs_count as f32) * epoch_mins).round() as i64;
        let sol_minutes = ((onset_idx as f32) * epoch_mins).round() as i64;

        let sleep_efficiency = ((sleep_duration_minutes as f32) / (total_in_bed_minutes as f32) * 100.0).clamp(0.0, 100.0);

        // Compute Sleep Quality Score (0 - 100)
        // Optimal sleep: 7-9 hours (420 - 540m), efficiency > 85%, WASO < 30m
        let mut score: f32 = 80.0;

        // Efficiency contribution (±15)
        score += (sleep_efficiency - 85.0) * 0.5;

        // Duration contribution
        if (420..=540).contains(&sleep_duration_minutes) {
            score += 10.0;
        } else if sleep_duration_minutes < 360 {
            score -= (360 - sleep_duration_minutes) as f32 * 0.08;
        }

        // WASO penalty
        if waso_minutes > 45 {
            score -= (waso_minutes - 45) as f32 * 0.2;
        }

        let quality_score = (score.round() as i32).clamp(30, 99);

        let deep_sleep_mins = (epoch_stages.iter().filter(|&&s| s == SleepStage::Deep as u8).count() as f32 * epoch_mins) as i64;
        let light_sleep_mins = (epoch_stages.iter().filter(|&&s| s == SleepStage::Light as u8).count() as f32 * epoch_mins) as i64;
        let rem_sleep_mins = (epoch_stages.iter().filter(|&&s| s == SleepStage::Rem as u8).count() as f32 * epoch_mins) as i64;

        Some(SleepAnalysisResult {
            sleep_onset_time_ms: onset_time_ms,
            wake_time_ms,
            total_duration_minutes: total_in_bed_minutes,
            sleep_duration_minutes,
            wake_duration_minutes: waso_minutes,
            sleep_onset_latency_minutes: sol_minutes,
            sleep_efficiency,
            quality_score,
            deep_sleep_minutes: deep_sleep_mins,
            light_sleep_minutes: light_sleep_mins,
            rem_sleep_minutes: rem_sleep_mins,
            wake_count: awakenings_count,
            epoch_states: states.into_iter().map(|s| s as u8).collect(),
            epoch_stages,
        })
    }
}

