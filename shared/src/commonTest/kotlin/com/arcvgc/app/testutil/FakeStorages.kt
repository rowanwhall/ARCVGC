package com.arcvgc.app.testutil

import com.arcvgc.app.data.AppConfigStorageApi
import com.arcvgc.app.data.CatalogCacheStorageApi
import com.arcvgc.app.data.FavoritesStorageApi
import com.arcvgc.app.data.SettingsStorageApi

class FakeFavoritesStorage : FavoritesStorageApi {
    private val intSets = mutableMapOf<String, Set<Int>>()
    private val stringSets = mutableMapOf<String, Set<String>>()

    override fun loadIds(key: String): Set<Int> = intSets[key] ?: emptySet()
    override fun saveIds(key: String, ids: Set<Int>) { intSets[key] = ids }
    override fun loadStringSet(key: String): Set<String> = stringSets[key] ?: emptySet()
    override fun saveStringSet(key: String, values: Set<String>) { stringSets[key] = values }
}

class FakeSettingsStorage : SettingsStorageApi {
    private val booleans = mutableMapOf<String, Boolean>()
    private val ints = mutableMapOf<String, Int>()

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = booleans[key] ?: defaultValue
    override fun putBoolean(key: String, value: Boolean) { booleans[key] = value }
    override fun getInt(key: String, defaultValue: Int): Int = ints[key] ?: defaultValue
    override fun putInt(key: String, value: Int) { ints[key] = value }
}

class FakeAppConfigStorage : AppConfigStorageApi {
    private val strings = mutableMapOf<String, String>()
    private val ints = mutableMapOf<String, Int>()

    override fun getString(key: String, defaultValue: String): String = strings[key] ?: defaultValue
    override fun putString(key: String, value: String) { strings[key] = value }
    override fun getInt(key: String, defaultValue: Int): Int = ints[key] ?: defaultValue
    override fun putInt(key: String, value: Int) { ints[key] = value }
}

class FakeCatalogCacheStorage : CatalogCacheStorageApi {
    private val strings = mutableMapOf<String, String>()
    private val longs = mutableMapOf<String, Long>()

    override fun getString(key: String, defaultValue: String): String = strings[key] ?: defaultValue
    override fun putString(key: String, value: String) { strings[key] = value }
    override fun getLong(key: String, defaultValue: Long): Long = longs[key] ?: defaultValue
    override fun putLong(key: String, value: Long) { longs[key] = value }
}
