package com.example.showdown26.data

import android.content.Context

actual class SettingsStorage(context: Context) {

    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    actual fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return if (prefs.contains(key)) prefs.getBoolean(key, defaultValue) else defaultValue
    }

    actual fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    actual fun getInt(key: String, defaultValue: Int): Int {
        return if (prefs.contains(key)) prefs.getInt(key, defaultValue) else defaultValue
    }

    actual fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }
}
