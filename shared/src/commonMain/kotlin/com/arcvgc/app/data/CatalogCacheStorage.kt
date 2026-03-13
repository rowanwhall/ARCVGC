package com.arcvgc.app.data

expect class CatalogCacheStorage {
    fun getString(key: String, defaultValue: String): String
    fun putString(key: String, value: String)
    fun getLong(key: String, defaultValue: Long): Long
    fun putLong(key: String, value: Long)
}
