package com.arcvgc.app.data

expect class SettingsStorage : SettingsStorageApi {
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean
    override fun putBoolean(key: String, value: Boolean)
    override fun getInt(key: String, defaultValue: Int): Int
    override fun putInt(key: String, value: Int)
}
