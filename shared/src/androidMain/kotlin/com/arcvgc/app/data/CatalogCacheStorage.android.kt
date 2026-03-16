package com.arcvgc.app.data

import android.content.Context

actual class CatalogCacheStorage(context: Context) : CatalogCacheStorageApi {

    private val prefs = context.getSharedPreferences("catalog_cache", Context.MODE_PRIVATE)

    actual override fun getString(key: String, defaultValue: String): String {
        return prefs.getString(key, defaultValue) ?: defaultValue
    }

    actual override fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    actual override fun getLong(key: String, defaultValue: Long): Long {
        return if (prefs.contains(key)) prefs.getLong(key, defaultValue) else defaultValue
    }

    actual override fun putLong(key: String, value: Long) {
        prefs.edit().putLong(key, value).apply()
    }
}
