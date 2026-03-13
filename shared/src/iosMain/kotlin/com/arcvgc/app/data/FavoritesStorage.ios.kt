package com.arcvgc.app.data

import platform.Foundation.NSUserDefaults

actual class FavoritesStorage {

    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun loadIds(key: String): Set<Int> {
        val array = defaults.arrayForKey(key) as? List<*> ?: return emptySet()
        return array.mapNotNull { (it as? Number)?.toInt() }.toSet()
    }

    actual fun saveIds(key: String, ids: Set<Int>) {
        defaults.setObject(ids.toList(), forKey = key)
    }

    actual fun loadStringSet(key: String): Set<String> {
        val array = defaults.arrayForKey(key) as? List<*> ?: return emptySet()
        return array.mapNotNull { it as? String }.toSet()
    }

    actual fun saveStringSet(key: String, values: Set<String>) {
        defaults.setObject(values.toList(), forKey = key)
    }
}
