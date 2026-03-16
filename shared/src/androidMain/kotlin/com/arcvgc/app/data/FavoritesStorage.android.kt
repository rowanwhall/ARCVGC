package com.arcvgc.app.data

import android.content.Context

actual class FavoritesStorage(context: Context) : FavoritesStorageApi {

    private val prefs = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)

    actual override fun loadIds(key: String): Set<Int> {
        return prefs.getStringSet(key, emptySet())
            ?.mapNotNull { it.toIntOrNull() }
            ?.toSet()
            ?: emptySet()
    }

    actual override fun saveIds(key: String, ids: Set<Int>) {
        prefs.edit().putStringSet(key, ids.map { it.toString() }.toSet()).apply()
    }

    actual override fun loadStringSet(key: String): Set<String> {
        return prefs.getStringSet(key, emptySet()) ?: emptySet()
    }

    actual override fun saveStringSet(key: String, values: Set<String>) {
        prefs.edit().putStringSet(key, values).apply()
    }
}
