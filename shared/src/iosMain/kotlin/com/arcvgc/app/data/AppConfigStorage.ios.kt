package com.arcvgc.app.data

import platform.Foundation.NSUserDefaults

actual class AppConfigStorage {

    private val defaults = NSUserDefaults.standardUserDefaults
    private val prefix = "app_config_"

    actual fun getString(key: String, defaultValue: String): String {
        return (defaults.objectForKey(prefix + key) as? String) ?: defaultValue
    }

    actual fun putString(key: String, value: String) {
        defaults.setObject(value, forKey = prefix + key)
    }

    actual fun getInt(key: String, defaultValue: Int): Int {
        return if (defaults.objectForKey(prefix + key) != null) {
            defaults.integerForKey(prefix + key).toInt()
        } else {
            defaultValue
        }
    }

    actual fun putInt(key: String, value: Int) {
        defaults.setInteger(value.toLong(), forKey = prefix + key)
    }
}
