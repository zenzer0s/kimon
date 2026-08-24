package com.zenzeros.kimon.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserSettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        // Timer Durations
        val WORK_DURATION_MINUTES = intPreferencesKey("work_duration_minutes")
        val SHORT_BREAK_MINUTES = intPreferencesKey("short_break_minutes")
        val LONG_BREAK_MINUTES = intPreferencesKey("long_break_minutes")
        val SESSIONS_BEFORE_LONG_BREAK = intPreferencesKey("sessions_before_long_break")
        val DAILY_GOAL_MINUTES = intPreferencesKey("daily_goal_minutes")

        // Automation & Focus Mode
        val AUTO_START_BREAKS = booleanPreferencesKey("auto_start_breaks")
        val AUTO_START_POMODOROS = booleanPreferencesKey("auto_start_pomodoros")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val DND_ENABLED = booleanPreferencesKey("dnd_enabled")

        // Sound & Alarm
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val MEDIA_VOLUME_FOR_ALARM = booleanPreferencesKey("media_volume_for_alarm")
        val HEADPHONE_MODE = booleanPreferencesKey("headphone_mode")
        val ALARM_SOUND_URI = stringPreferencesKey("alarm_sound_uri")
        val ALARM_SOUND_TITLE = stringPreferencesKey("alarm_sound_title")

        // Appearance
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val THEME_PALETTE = stringPreferencesKey("theme_palette")
        val THEME_COLOR = stringPreferencesKey("theme_color")
        val AMOLED_BLACK = booleanPreferencesKey("amoled_black")
        val CLOCK_STYLE = stringPreferencesKey("clock_style")
    }

    // --- Flows ---
    val workDurationMinutes: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.WORK_DURATION_MINUTES] ?: 25
    }

    val shortBreakMinutes: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.SHORT_BREAK_MINUTES] ?: 5
    }

    val longBreakMinutes: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.LONG_BREAK_MINUTES] ?: 15
    }

    val sessionsBeforeLongBreak: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.SESSIONS_BEFORE_LONG_BREAK] ?: 4
    }

    val dailyGoalMinutes: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.DAILY_GOAL_MINUTES] ?: 120
    }

    val autoStartBreaks: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.AUTO_START_BREAKS] ?: false
    }

    val autoStartPomodoros: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.AUTO_START_POMODOROS] ?: false
    }

    val keepScreenOn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.KEEP_SCREEN_ON] ?: false
    }

    val dndEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.DND_ENABLED] ?: false
    }

    val clockStyle: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.CLOCK_STYLE] ?: "DIAL"
    }

    val soundEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.SOUND_ENABLED] ?: true
    }

    val vibrationEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.VIBRATION_ENABLED] ?: true
    }

    val mediaVolumeForAlarm: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.HEADPHONE_MODE] ?: prefs[PreferencesKeys.MEDIA_VOLUME_FOR_ALARM] ?: false
    }

    val headphoneMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.HEADPHONE_MODE] ?: prefs[PreferencesKeys.MEDIA_VOLUME_FOR_ALARM] ?: false
    }

    val alarmSoundUri: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.ALARM_SOUND_URI] ?: ""
    }

    val alarmSoundTitle: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.ALARM_SOUND_TITLE] ?: "Default"
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.THEME_MODE] ?: "SYSTEM"
    }

    val themePalette: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.THEME_PALETTE] ?: "DYNAMIC"
    }

    val themeColor: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.THEME_COLOR] ?: "Color.White"
    }

    val amoledBlack: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.AMOLED_BLACK] ?: false
    }

    // --- Setters ---
    suspend fun setWorkDurationMinutes(minutes: Int) {
        context.dataStore.edit { prefs -> prefs[PreferencesKeys.WORK_DURATION_MINUTES] = minutes }
    }

    suspend fun setShortBreakMinutes(minutes: Int) {
        context.dataStore.edit { prefs -> prefs[PreferencesKeys.SHORT_BREAK_MINUTES] = minutes }
    }

    suspend fun setLongBreakMinutes(minutes: Int) {
        context.dataStore.edit { prefs -> prefs[PreferencesKeys.LONG_BREAK_MINUTES] = minutes }
    }

    suspend fun setSessionsBeforeLongBreak(count: Int) {
        context.dataStore.edit { prefs -> prefs[PreferencesKeys.SESSIONS_BEFORE_LONG_BREAK] = count }
    }

    suspend fun setDailyGoalMinutes(minutes: Int) {
        context.dataStore.edit { prefs -> prefs[PreferencesKeys.DAILY_GOAL_MINUTES] = minutes }
    }

    suspend fun setAutoStartBreaks(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[PreferencesKeys.AUTO_START_BREAKS] = enabled }
    }

    suspend fun setAutoStartPomodoros(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[PreferencesKeys.AUTO_START_POMODOROS] = enabled }
    }

    suspend fun setKeepScreenOn(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[PreferencesKeys.KEEP_SCREEN_ON] = enabled }
    }

    suspend fun setDndEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[PreferencesKeys.DND_ENABLED] = enabled }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[PreferencesKeys.SOUND_ENABLED] = enabled }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[PreferencesKeys.VIBRATION_ENABLED] = enabled }
    }

    suspend fun setMediaVolumeForAlarm(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.MEDIA_VOLUME_FOR_ALARM] = enabled
            prefs[PreferencesKeys.HEADPHONE_MODE] = enabled
        }
    }

    suspend fun setHeadphoneMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.HEADPHONE_MODE] = enabled
            prefs[PreferencesKeys.MEDIA_VOLUME_FOR_ALARM] = enabled
        }
    }

    suspend fun setAlarmSound(uri: String, title: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.ALARM_SOUND_URI] = uri
            prefs[PreferencesKeys.ALARM_SOUND_TITLE] = title
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs -> prefs[PreferencesKeys.THEME_MODE] = mode }
    }

    suspend fun setThemePalette(palette: String) {
        context.dataStore.edit { prefs -> prefs[PreferencesKeys.THEME_PALETTE] = palette }
    }

    suspend fun setThemeColor(colorString: String) {
        context.dataStore.edit { prefs -> prefs[PreferencesKeys.THEME_COLOR] = colorString }
    }

    suspend fun setAmoledBlack(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[PreferencesKeys.AMOLED_BLACK] = enabled }
    }

    suspend fun setClockStyle(style: String) {
        context.dataStore.edit { prefs -> prefs[PreferencesKeys.CLOCK_STYLE] = style }
    }

    suspend fun clearAllSettings() {
        context.dataStore.edit { prefs -> prefs.clear() }
    }
}
