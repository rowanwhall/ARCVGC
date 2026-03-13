package com.arcvgc.app.data

import platform.Foundation.NSUserDefaults

actual class SettingsStorage {

    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return if (defaults.objectForKey(key) != null) defaults.boolForKey(key) else defaultValue
    }

    actual fun putBoolean(key: String, value: Boolean) {
        defaults.setBool(value, forKey = key)
    }

    actual fun getInt(key: String, defaultValue: Int): Int {
        return if (defaults.objectForKey(key) != null) defaults.integerForKey(key).toInt() else defaultValue
    }

    actual fun putInt(key: String, value: Int) {
        defaults.setInteger(value.toLong(), forKey = key)
    }
}
