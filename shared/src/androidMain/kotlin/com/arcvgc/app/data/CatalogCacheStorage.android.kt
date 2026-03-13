package com.arcvgc.app.data

import android.content.Context

actual class CatalogCacheStorage(context: Context) {

    private val prefs = context.getSharedPreferences("catalog_cache", Context.MODE_PRIVATE)

    actual fun getString(key: String, defaultValue: String): String {
        return prefs.getString(key, defaultValue) ?: defaultValue
    }

    actual fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    actual fun getLong(key: String, defaultValue: Long): Long {
        return if (prefs.contains(key)) prefs.getLong(key, defaultValue) else defaultValue
    }

    actual fun putLong(key: String, value: Long) {
        prefs.edit().putLong(key, value).apply()
    }
}
