package com.arcvgc.app.data

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("(key) => { const v = window.localStorage.getItem(key); return v === null ? '' : v; }")
private external fun configGetItem(key: String): String

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("(key, value) => { window.localStorage.setItem(key, value); }")
private external fun configSetItem(key: String, value: String)

actual class AppConfigStorage : AppConfigStorageApi {

    private val prefix = "app_config_"

    actual override fun getString(key: String, defaultValue: String): String {
        val raw = configGetItem(prefix + key)
        return if (raw.isBlank()) defaultValue else raw
    }

    actual override fun putString(key: String, value: String) {
        configSetItem(prefix + key, value)
    }

    actual override fun getInt(key: String, defaultValue: Int): Int {
        val raw = configGetItem(prefix + key)
        if (raw.isBlank()) return defaultValue
        return raw.toIntOrNull() ?: defaultValue
    }

    actual override fun putInt(key: String, value: Int) {
        configSetItem(prefix + key, value.toString())
    }
}
