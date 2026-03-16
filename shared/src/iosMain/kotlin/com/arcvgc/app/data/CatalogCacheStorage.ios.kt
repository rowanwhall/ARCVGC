package com.arcvgc.app.data

import platform.Foundation.NSUserDefaults

actual class CatalogCacheStorage : CatalogCacheStorageApi {

    private val defaults = NSUserDefaults.standardUserDefaults

    actual override fun getString(key: String, defaultValue: String): String {
        return (defaults.objectForKey(key) as? String) ?: defaultValue
    }

    actual override fun putString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }

    actual override fun getLong(key: String, defaultValue: Long): Long {
        return if (defaults.objectForKey(key) != null) {
            defaults.integerForKey(key)
        } else {
            defaultValue
        }
    }

    actual override fun putLong(key: String, value: Long) {
        defaults.setInteger(value, forKey = key)
    }
}
