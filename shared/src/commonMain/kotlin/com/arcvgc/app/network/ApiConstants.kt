package com.arcvgc.app.network

const val API_HOST = "https://arcvgc.com"

object ApiConstants {
    val BASE_URL: String = getPlatformBaseUrl()

    const val MATCHES_ENDPOINT = "/api/v1/matches/"
    const val POKEMON_ENDPOINT = "/api/v1/pokemon/"
    const val ITEMS_ENDPOINT = "/api/v1/items/"
    const val TERA_TYPES_ENDPOINT = "/api/v1/types/tera"
    const val SEARCH_ENDPOINT = "/api/v1/matches/search"
    const val FORMATS_ENDPOINT = "/api/v1/formats/"
    const val PLAYERS_ENDPOINT = "/api/v1/players/"
    const val CONFIG_ENDPOINT = "/api/v1/config/"
    const val ABILITIES_ENDPOINT = "/api/v1/abilities/"
    const val SETS_ENDPOINT = "/api/v1/sets/"
}

expect fun getPlatformBaseUrl(): String

/** Rewrites image URLs for the current platform. No-op on Android/iOS; replaces API host with browser origin on wasmJs. */
expect fun normalizeImageUrl(url: String?): String?
