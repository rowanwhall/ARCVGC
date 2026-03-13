package com.arcvgc.app.network

actual fun getPlatformBaseUrl(): String = "https://arcvgc.com"

actual fun normalizeImageUrl(url: String?): String? = url
