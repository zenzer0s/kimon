use jni::objects::{JClass, JFloatArray, JLongArray, JString};
use jni::sys::{jboolean, jint, jstring};
use jni::JNIEnv;

use crate::actigraphy::ActigraphyEngine;
use crate::dsp::DspProcessor;
use crate::models::{EpochData, RawSensorSample};

#[no_mangle]
pub extern "C" fn Java_com_zenzeros_kimon_service_sleep_native_NativeSleepEngine_nativeGetVersion(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let version = "Kimon SleepCore v1.0.0 (Rust NEON/SIMD)";
    env.new_string(version).unwrap().into_raw()
}

#[no_mangle]
pub extern "C" fn Java_com_zenzeros_kimon_service_sleep_native_NativeSleepEngine_nativeAnalyzeEpochsJson(
    mut env: JNIEnv,
    _class: JClass,
    epochs_json: JString,
) -> jstring {
    let json_str: String = match env.get_string(&epochs_json) {
        Ok(s) => s.into(),
        Err(_) => return env.new_string("{}").unwrap().into_raw(),
    };

    let epochs: Vec<EpochData> = match serde_json::from_str(&json_str) {
        Ok(e) => e,
        Err(_) => return env.new_string("{}").unwrap().into_raw(),
    };

    let result = match ActigraphyEngine::analyze_session(&epochs) {
        Some(res) => res,
        None => return env.new_string("{}").unwrap().into_raw(),
    };

    let res_json = serde_json::to_string(&result).unwrap_or_else(|_| "{}".to_string());
    env.new_string(res_json).unwrap().into_raw()
}

#[no_mangle]
pub extern "C" fn Java_com_zenzeros_kimon_service_sleep_native_NativeSleepEngine_nativeProcessRawBuffers(
    env: JNIEnv,
    _class: JClass,
    x_arr: JFloatArray,
    y_arr: JFloatArray,
    z_arr: JFloatArray,
    light_arr: JFloatArray,
    time_arr: JLongArray,
    epoch_duration_sec: jint,
    screen_on: jboolean,
    charging: jboolean,
) -> jstring {
    let len = match env.get_array_length(&time_arr) {
        Ok(l) => l as usize,
        Err(_) => return env.new_string("{}").unwrap().into_raw(),
    };

    if len == 0 {
        return env.new_string("{}").unwrap().into_raw();
    }

    let mut x_buf = vec![0.0f32; len];
    let mut y_buf = vec![0.0f32; len];
    let mut z_buf = vec![0.0f32; len];
    let mut light_buf = vec![0.0f32; len];
    let mut time_buf = vec![0i64; len];

    if env.get_float_array_region(&x_arr, 0, &mut x_buf).is_err()
        || env.get_float_array_region(&y_arr, 0, &mut y_buf).is_err()
        || env.get_float_array_region(&z_arr, 0, &mut z_buf).is_err()
        || env.get_float_array_region(&light_arr, 0, &mut light_buf).is_err()
        || env.get_long_array_region(&time_arr, 0, &mut time_buf).is_err()
    {
        return env.new_string("{}").unwrap().into_raw();
    }

    let samples: Vec<RawSensorSample> = (0..len)
        .map(|i| RawSensorSample {
            timestamp_ms: time_buf[i],
            x: x_buf[i],
            y: y_buf[i],
            z: z_buf[i],
            light_lux: light_buf[i],
        })
        .collect();

    let epochs = DspProcessor::aggregate_samples_into_epochs(
        &samples,
        epoch_duration_sec as u32,
        screen_on != 0,
        charging != 0,
    );

    let result = match ActigraphyEngine::analyze_session(&epochs) {
        Some(res) => res,
        None => return env.new_string("{}").unwrap().into_raw(),
    };

    let res_json = serde_json::to_string(&result).unwrap_or_else(|_| "{}".to_string());
    env.new_string(res_json).unwrap().into_raw()
}
