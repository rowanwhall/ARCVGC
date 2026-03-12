package com.example.showdown26.network

const val API_HOST = "https://arcvgc.com"

object ApiConstants {
    val BASE_URL: String = getPlatformBaseUrl()

    const val MATCHES_ENDPOINT = "/api/v0/matches/"
    const val POKEMON_ENDPOINT = "/api/v0/pokemon/"
    const val ITEMS_ENDPOINT = "/api/v0/items/"
    const val TERA_TYPES_ENDPOINT = "/api/v0/types/tera"
    const val SEARCH_ENDPOINT = "/api/v0/matches/search"
    const val FORMATS_ENDPOINT = "/api/v0/formats/"
    const val PLAYERS_ENDPOINT = "/api/v0/players/"
}

expect fun getPlatformBaseUrl(): String

/** Rewrites image URLs for the current platform. No-op on Android/iOS; replaces API host with browser origin on wasmJs. */
expect fun normalizeImageUrl(url: String?): String?
