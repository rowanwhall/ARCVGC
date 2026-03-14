package com.arcvgc.app.data

import android.content.Context

actual class AppConfigStorage(context: Context) {

    private val prefs = context.getSharedPreferences("app_config", Context.MODE_PRIVATE)

    actual fun getString(key: String, defaultValue: String): String {
        return prefs.getString(key, defaultValue) ?: defaultValue
    }

    actual fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    actual fun getInt(key: String, defaultValue: Int): Int {
        return if (prefs.contains(key)) prefs.getInt(key, defaultValue) else defaultValue
    }

    actual fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }
}
