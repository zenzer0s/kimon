package com.zenzeros.kimon.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserSettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val WORK_DURATION_MINUTES = intPreferencesKey("work_duration_minutes")
        val SHORT_BREAK_MINUTES = intPreferencesKey("short_break_minutes")
        val LONG_BREAK_MINUTES = intPreferencesKey("long_break_minutes")
        val SESSIONS_BEFORE_LONG_BREAK = intPreferencesKey("sessions_before_long_break")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val AUTO_START_BREAKS = booleanPreferencesKey("auto_start_breaks")
        val AUTO_START_POMODOROS = booleanPreferencesKey("auto_start_pomodoros")
    }

    val workDurationMinutes: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.WORK_DURATION_MINUTES] ?: 25
    }

    val shortBreakMinutes: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.SHORT_BREAK_MINUTES] ?: 5
    }

    val longBreakMinutes: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.LONG_BREAK_MINUTES] ?: 15
    }

    val soundEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.SOUND_ENABLED] ?: true
    }

    val vibrationEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.VIBRATION_ENABLED] ?: true
    }

    suspend fun setWorkDurationMinutes(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.WORK_DURATION_MINUTES] = minutes
        }
    }

    suspend fun setShortBreakMinutes(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SHORT_BREAK_MINUTES] = minutes
        }
    }

    suspend fun setLongBreakMinutes(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.LONG_BREAK_MINUTES] = minutes
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.VIBRATION_ENABLED] = enabled
        }
    }
}
