package com.arcvgc.app.data

import android.content.Context

actual class AppConfigStorage(context: Context) : AppConfigStorageApi {

    private val prefs = context.getSharedPreferences("app_config", Context.MODE_PRIVATE)

    actual override fun getString(key: String, defaultValue: String): String {
        return prefs.getString(key, defaultValue) ?: defaultValue
    }

    actual override fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    actual override fun getInt(key: String, defaultValue: Int): Int {
        return if (prefs.contains(key)) prefs.getInt(key, defaultValue) else defaultValue
    }

    actual override fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }
}
