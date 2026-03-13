package com.arcvgc.app.data

import android.content.Context

actual class FavoritesStorage(context: Context) {

    private val prefs = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)

    actual fun loadIds(key: String): Set<Int> {
        return prefs.getStringSet(key, emptySet())
            ?.mapNotNull { it.toIntOrNull() }
            ?.toSet()
            ?: emptySet()
    }

    actual fun saveIds(key: String, ids: Set<Int>) {
        prefs.edit().putStringSet(key, ids.map { it.toString() }.toSet()).apply()
    }

    actual fun loadStringSet(key: String): Set<String> {
        return prefs.getStringSet(key, emptySet()) ?: emptySet()
    }

    actual fun saveStringSet(key: String, values: Set<String>) {
        prefs.edit().putStringSet(key, values).apply()
    }
}
