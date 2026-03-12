package com.example.showdown26.data

import io.sentry.kotlin.multiplatform.Sentry

actual fun initializeSentry() {
    if (SentryConfig.ANDROID_DSN.isNotEmpty()) {
        Sentry.init { options ->
            options.dsn = SentryConfig.ANDROID_DSN
        }
    }
}
