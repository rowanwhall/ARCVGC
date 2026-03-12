package com.example.showdown26.network

actual fun getPlatformBaseUrl(): String = ""

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("() => window.location.origin")
private external fun getWindowOrigin(): String

actual fun normalizeImageUrl(url: String?): String? {
    if (url == null) return null
    if (url.startsWith(API_HOST)) {
        return getWindowOrigin() + url.removePrefix(API_HOST)
    }
    return url
}
