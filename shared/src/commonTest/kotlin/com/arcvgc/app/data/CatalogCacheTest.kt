package com.arcvgc.app.data

import com.arcvgc.app.testutil.FakeCatalogCacheStorage
import kotlinx.serialization.builtins.serializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CatalogCacheTest {

    private val stringSerializer = String.serializer()

    @Test
    fun load_returnsNull_whenStorageEmpty() {
        val storage = FakeCatalogCacheStorage()
        val result = CatalogCache.load(storage, "pokemon_catalog", CatalogCache.TTL_7_DAYS, stringSerializer)
        assertNull(result)
    }

    @Test
    fun saveThenLoad_roundTripsCorrectly() {
        val storage = FakeCatalogCacheStorage()
        val items = listOf("Pikachu", "Charizard", "Mewtwo")

        CatalogCache.save(storage, "pokemon_catalog", items, stringSerializer)
        val loaded = CatalogCache.load(storage, "pokemon_catalog", CatalogCache.TTL_7_DAYS, stringSerializer)

        assertEquals(items, loaded)
    }

    @Test
    fun load_returnsNull_whenNoTimestamp() {
        val storage = FakeCatalogCacheStorage()
        // Put data but no timestamp (timestamp defaults to 0L)
        storage.putString("pokemon_catalog", "[\"Pikachu\"]")

        val result = CatalogCache.load(storage, "pokemon_catalog", CatalogCache.TTL_7_DAYS, stringSerializer)
        assertNull(result)
    }

    @Test
    fun load_returnsNull_whenDataIsBlank() {
        val storage = FakeCatalogCacheStorage()
        storage.putString("pokemon_catalog", "")
        storage.putLong("pokemon_catalog_timestamp", Long.MAX_VALUE) // Far-future timestamp so TTL passes

        val result = CatalogCache.load(storage, "pokemon_catalog", CatalogCache.TTL_7_DAYS, stringSerializer)
        assertNull(result)
    }

    @Test
    fun load_returnsNull_whenJsonCorrupt() {
        val storage = FakeCatalogCacheStorage()
        storage.putString("pokemon_catalog", "not valid json {{{")
        storage.putLong("pokemon_catalog_timestamp", Long.MAX_VALUE)

        val result = CatalogCache.load(storage, "pokemon_catalog", CatalogCache.TTL_7_DAYS, stringSerializer)
        assertNull(result)
    }

    @Test
    fun clearAll_resetsAllFourCatalogKeys() {
        val storage = FakeCatalogCacheStorage()
        val keys = listOf("pokemon_catalog", "item_catalog", "tera_type_catalog", "format_catalog")

        // Populate all catalog keys
        for (key in keys) {
            CatalogCache.save(storage, key, listOf("test"), stringSerializer)
        }

        CatalogCache.clearAll(storage)

        for (key in keys) {
            assertEquals("", storage.getString(key, ""))
            assertEquals(0L, storage.getLong("${key}_timestamp", 0L))
        }
    }

    @Test
    fun clearAll_afterSave_loadReturnsNull() {
        val storage = FakeCatalogCacheStorage()
        CatalogCache.save(storage, "pokemon_catalog", listOf("Pikachu"), stringSerializer)

        CatalogCache.clearAll(storage)

        val result = CatalogCache.load(storage, "pokemon_catalog", CatalogCache.TTL_7_DAYS, stringSerializer)
        assertNull(result)
    }
}
