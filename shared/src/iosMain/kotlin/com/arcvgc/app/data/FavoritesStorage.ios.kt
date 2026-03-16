package com.arcvgc.app.data

import platform.Foundation.NSUserDefaults

actual class FavoritesStorage : FavoritesStorageApi {

    private val defaults = NSUserDefaults.standardUserDefaults

    actual override fun loadIds(key: String): Set<Int> {
        val array = defaults.arrayForKey(key) as? List<*> ?: return emptySet()
        return array.mapNotNull { (it as? Number)?.toInt() }.toSet()
    }

    actual override fun saveIds(key: String, ids: Set<Int>) {
        defaults.setObject(ids.toList(), forKey = key)
    }

    actual override fun loadStringSet(key: String): Set<String> {
        val array = defaults.arrayForKey(key) as? List<*> ?: return emptySet()
        return array.mapNotNull { it as? String }.toSet()
    }

    actual override fun saveStringSet(key: String, values: Set<String>) {
        defaults.setObject(values.toList(), forKey = key)
    }
}
