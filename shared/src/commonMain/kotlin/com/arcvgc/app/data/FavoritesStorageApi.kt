package com.arcvgc.app.data

interface FavoritesStorageApi {
    fun loadIds(key: String): Set<Int>
    fun saveIds(key: String, ids: Set<Int>)
    fun loadStringSet(key: String): Set<String>
    fun saveStringSet(key: String, values: Set<String>)
}
