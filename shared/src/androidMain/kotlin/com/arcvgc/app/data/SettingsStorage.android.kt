package com.arcvgc.app.data

import android.content.Context

actual class SettingsStorage(context: Context) : SettingsStorageApi {

    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    actual override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return if (prefs.contains(key)) prefs.getBoolean(key, defaultValue) else defaultValue
    }

    actual override fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    actual override fun getInt(key: String, defaultValue: Int): Int {
        return if (prefs.contains(key)) prefs.getInt(key, defaultValue) else defaultValue
    }

    actual override fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }
}
