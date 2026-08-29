use crate::models::{EpochData, RawSensorSample};

/// Standard Earth gravity constant in m/s^2
pub const STANDARD_GRAVITY: f32 = 9.80665;

/// DSP Engine for sensor signals
pub struct DspProcessor;

impl DspProcessor {
    /// Compute Euclidean Norm Minus One (ENMO) metric from tri-axial acceleration.
    /// Clamped at 0.0 so negative values (free-fall / offset) do not bias movement upwards.
    #[inline(always)]
    pub fn compute_enmo(x: f32, y: f32, z: f32) -> f32 {
        let magnitude = (x * x + y * y + z * z).sqrt();
        (magnitude - STANDARD_GRAVITY).max(0.0)
    }

    /// Aggregate raw samples into fixed duration epochs (e.g. 30s or 60s).
    pub fn aggregate_samples_into_epochs(
        samples: &[RawSensorSample],
        epoch_duration_seconds: u32,
        screen_on: bool,
        charging: bool,
    ) -> Vec<EpochData> {
        if samples.is_empty() {
            return Vec::new();
        }

        let epoch_duration_ms = (epoch_duration_seconds as i64) * 1000;
        let mut epochs = Vec::new();

        let mut current_epoch_start = samples[0].timestamp_ms;
        let mut epoch_enmo_sum = 0.0f32;
        let mut epoch_light_sum = 0.0f32;
        let mut epoch_samples_count = 0usize;
        let mut enmo_values = Vec::with_capacity(300);

        for sample in samples {
            if sample.timestamp_ms >= current_epoch_start + epoch_duration_ms {
                // Finalize previous epoch
                if epoch_samples_count > 0 {
                    let mean_enmo = epoch_enmo_sum / (epoch_samples_count as f32);
                    let mean_light = epoch_light_sum / (epoch_samples_count as f32);
                    
                    // Compute variance
                    let variance = if epoch_samples_count > 1 {
                        let sum_sq_diff: f32 = enmo_values
                            .iter()
                            .map(|&v| {
                                let diff = v - mean_enmo;
                                diff * diff
                            })
                            .sum();
                        sum_sq_diff / (epoch_samples_count as f32)
                    } else {
                        0.0
                    };

                    // Scale activity count into standard ActiGraph-like units
                    let activity_count = mean_enmo * 100.0;

                    epochs.push(EpochData {
                        timestamp_ms: current_epoch_start,
                        duration_seconds: epoch_duration_seconds,
                        activity_count,
                        variance,
                        mean_light_lux: mean_light,
                        screen_on,
                        charging,
                    });
                }

                // Reset for next epoch
                current_epoch_start = sample.timestamp_ms;
                epoch_enmo_sum = 0.0;
                epoch_light_sum = 0.0;
                epoch_samples_count = 0;
                enmo_values.clear();
            }

            let enmo = Self::compute_enmo(sample.x, sample.y, sample.z);
            epoch_enmo_sum += enmo;
            epoch_light_sum += sample.light_lux;
            enmo_values.push(enmo);
            epoch_samples_count += 1;
        }

        // Process leftover samples in the last epoch
        if epoch_samples_count > 0 {
            let mean_enmo = epoch_enmo_sum / (epoch_samples_count as f32);
            let mean_light = epoch_light_sum / (epoch_samples_count as f32);
            let activity_count = mean_enmo * 100.0;

            epochs.push(EpochData {
                timestamp_ms: current_epoch_start,
                duration_seconds: epoch_duration_seconds,
                activity_count,
                variance: 0.0,
                mean_light_lux: mean_light,
                screen_on,
                charging,
            });
        }

        epochs
    }
}
