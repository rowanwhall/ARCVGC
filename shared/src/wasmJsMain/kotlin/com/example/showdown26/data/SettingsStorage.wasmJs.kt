package com.example.showdown26.data

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("(key) => { const v = window.localStorage.getItem(key); return v === null ? '' : v; }")
private external fun settingsGetItem(key: String): String

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("(key, value) => { window.localStorage.setItem(key, value); }")
private external fun settingsSetItem(key: String, value: String)

actual class SettingsStorage {

    actual fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val raw = settingsGetItem("settings_$key")
        if (raw.isBlank()) return defaultValue
        return raw == "true"
    }

    actual fun putBoolean(key: String, value: Boolean) {
        settingsSetItem("settings_$key", value.toString())
    }

    actual fun getInt(key: String, defaultValue: Int): Int {
        val raw = settingsGetItem("settings_$key")
        if (raw.isBlank()) return defaultValue
        return raw.toIntOrNull() ?: defaultValue
    }

    actual fun putInt(key: String, value: Int) {
        settingsSetItem("settings_$key", value.toString())
    }
}
