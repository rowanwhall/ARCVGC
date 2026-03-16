package com.arcvgc.app.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

object CatalogCache {

    const val TTL_7_DAYS: Long = 7L * 24 * 60 * 60 * 1000
    const val TTL_30_DAYS: Long = 30L * 24 * 60 * 60 * 1000

    private val json = Json { ignoreUnknownKeys = true }

    fun <T> load(
        storage: CatalogCacheStorageApi,
        key: String,
        ttlMillis: Long,
        serializer: KSerializer<T>
    ): List<T>? {
        val timestamp = storage.getLong("${key}_timestamp", 0L)
        if (timestamp == 0L) return null

        val age = currentTimeMillis() - timestamp
        if (age > ttlMillis) return null

        val raw = storage.getString(key, "")
        if (raw.isBlank()) return null

        return try {
            json.decodeFromString(ListSerializer(serializer), raw)
        } catch (_: Exception) {
            null
        }
    }

    fun <T> save(
        storage: CatalogCacheStorageApi,
        key: String,
        items: List<T>,
        serializer: KSerializer<T>
    ) {
        try {
            val raw = json.encodeToString(ListSerializer(serializer), items)
            storage.putString(key, raw)
            storage.putLong("${key}_timestamp", currentTimeMillis())
        } catch (_: Exception) {
            // Silent fail — cache is best-effort
        }
    }

    fun clearAll(storage: CatalogCacheStorageApi) {
        listOf("pokemon_catalog", "item_catalog", "tera_type_catalog", "format_catalog").forEach { key ->
            storage.putString(key, "")
            storage.putLong("${key}_timestamp", 0L)
        }
    }
}
