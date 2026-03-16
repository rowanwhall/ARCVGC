package com.arcvgc.app.data

expect class FavoritesStorage : FavoritesStorageApi {
    override fun loadIds(key: String): Set<Int>
    override fun saveIds(key: String, ids: Set<Int>)
    override fun loadStringSet(key: String): Set<String>
    override fun saveStringSet(key: String, values: Set<String>)
}
