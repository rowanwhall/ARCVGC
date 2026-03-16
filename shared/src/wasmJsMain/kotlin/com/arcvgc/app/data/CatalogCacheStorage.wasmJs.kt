package com.arcvgc.app.data

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("(key) => { const v = window.localStorage.getItem(key); return v === null ? '' : v; }")
private external fun cacheGetItem(key: String): String

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("(key, value) => { window.localStorage.setItem(key, value); }")
private external fun cacheSetItem(key: String, value: String)

actual class CatalogCacheStorage : CatalogCacheStorageApi {

    actual override fun getString(key: String, defaultValue: String): String {
        val raw = cacheGetItem("cache_$key")
        return if (raw.isBlank()) defaultValue else raw
    }

    actual override fun putString(key: String, value: String) {
        cacheSetItem("cache_$key", value)
    }

    actual override fun getLong(key: String, defaultValue: Long): Long {
        val raw = cacheGetItem("cache_$key")
        if (raw.isBlank()) return defaultValue
        return raw.toLongOrNull() ?: defaultValue
    }

    actual override fun putLong(key: String, value: Long) {
        cacheSetItem("cache_$key", value.toString())
    }
}
