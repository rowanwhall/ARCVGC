package com.arcvgc.app.data

expect class CatalogCacheStorage : CatalogCacheStorageApi {
    override fun getString(key: String, defaultValue: String): String
    override fun putString(key: String, value: String)
    override fun getLong(key: String, defaultValue: Long): Long
    override fun putLong(key: String, value: Long)
}
