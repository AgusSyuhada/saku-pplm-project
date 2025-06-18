package com.pplm.saku.utils

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "saku_preferences"
        private const val KEY_DAILY_REMINDER = "daily_reminder"
        private const val KEY_REMINDER_TIME = "reminder_time"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_LANGUAGE = "language"

        private const val DEFAULT_REMINDER_TIME = "20:00"
        private const val DEFAULT_LANGUAGE = "Indonesia"
    }

    fun isDailyReminderEnabled(): Boolean {
        return preferences.getBoolean(KEY_DAILY_REMINDER, false)
    }

    fun setDailyReminder(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_DAILY_REMINDER, enabled).apply()
    }

    fun getReminderTime(): String {
        return preferences.getString(KEY_REMINDER_TIME, DEFAULT_REMINDER_TIME) ?: DEFAULT_REMINDER_TIME
    }

    fun setReminderTime(time: String) {
        preferences.edit().putString(KEY_REMINDER_TIME, time).apply()
    }

    fun isDarkModeEnabled(): Boolean {
        return preferences.getBoolean(KEY_DARK_MODE, false)
    }

    fun setDarkMode(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    fun getLanguage(): String {
        return preferences.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }

    fun setLanguage(language: String) {
        preferences.edit().putString(KEY_LANGUAGE, language).apply()
    }
}