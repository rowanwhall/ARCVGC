package com.arcvgc.app.data

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("(key) => { const v = window.localStorage.getItem(key); return v === null ? '' : v; }")
private external fun localStorageGetItem(key: String): String

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("(key, value) => { window.localStorage.setItem(key, value); }")
private external fun localStorageSetItem(key: String, value: String)

actual class FavoritesStorage : FavoritesStorageApi {

    actual override fun loadIds(key: String): Set<Int> {
        val raw = localStorageGetItem(key)
        if (raw.isBlank()) return emptySet()
        return raw.split(",").mapNotNull { it.toIntOrNull() }.toSet()
    }

    actual override fun saveIds(key: String, ids: Set<Int>) {
        localStorageSetItem(key, ids.joinToString(","))
    }

    actual override fun loadStringSet(key: String): Set<String> {
        val raw = localStorageGetItem(key)
        if (raw.isBlank()) return emptySet()
        return kotlinx.serialization.json.Json.decodeFromString<List<String>>(raw).toSet()
    }

    actual override fun saveStringSet(key: String, values: Set<String>) {
        localStorageSetItem(key, kotlinx.serialization.json.Json.encodeToString(values.toList()))
    }
}
