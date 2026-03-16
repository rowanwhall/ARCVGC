package com.arcvgc.app.data

import platform.Foundation.NSUserDefaults

actual class SettingsStorage : SettingsStorageApi {

    private val defaults = NSUserDefaults.standardUserDefaults

    actual override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return if (defaults.objectForKey(key) != null) defaults.boolForKey(key) else defaultValue
    }

    actual override fun putBoolean(key: String, value: Boolean) {
        defaults.setBool(value, forKey = key)
    }

    actual override fun getInt(key: String, defaultValue: Int): Int {
        return if (defaults.objectForKey(key) != null) defaults.integerForKey(key).toInt() else defaultValue
    }

    actual override fun putInt(key: String, value: Int) {
        defaults.setInteger(value.toLong(), forKey = key)
    }
}
