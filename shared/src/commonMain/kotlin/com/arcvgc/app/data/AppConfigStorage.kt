package com.arcvgc.app.data

expect class AppConfigStorage : AppConfigStorageApi {
    override fun getString(key: String, defaultValue: String): String
    override fun putString(key: String, value: String)
    override fun getInt(key: String, defaultValue: Int): Int
    override fun putInt(key: String, value: Int)
}
