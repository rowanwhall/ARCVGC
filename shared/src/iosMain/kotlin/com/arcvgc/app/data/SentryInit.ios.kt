package com.arcvgc.app.data

import io.sentry.kotlin.multiplatform.Sentry

actual fun initializeSentry() {
    if (SentryConfig.IOS_DSN.isNotEmpty()) {
        Sentry.init { options ->
            options.dsn = SentryConfig.IOS_DSN
        }
    }
}
