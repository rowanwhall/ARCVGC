package com.arcvgc.app.data

import platform.Foundation.NSUserDefaults

actual class AppConfigStorage : AppConfigStorageApi {

    private val defaults = NSUserDefaults.standardUserDefaults
    private val prefix = "app_config_"

    actual override fun getString(key: String, defaultValue: String): String {
        return (defaults.objectForKey(prefix + key) as? String) ?: defaultValue
    }

    actual override fun putString(key: String, value: String) {
        defaults.setObject(value, forKey = prefix + key)
    }

    actual override fun getInt(key: String, defaultValue: Int): Int {
        return if (defaults.objectForKey(prefix + key) != null) {
            defaults.integerForKey(prefix + key).toInt()
        } else {
            defaultValue
        }
    }

    actual override fun putInt(key: String, value: Int) {
        defaults.setInteger(value.toLong(), forKey = prefix + key)
    }
}
