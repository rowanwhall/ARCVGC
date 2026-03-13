package com.arcvgc.app.data

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("""
(dsn) => {
    if (typeof Sentry !== 'undefined' && Sentry.init) {
        Sentry.init({ dsn: dsn });
    }
}
""")
private external fun sentryInitJs(dsn: String)

actual fun initializeSentry() {
    if (SentryConfig.WEB_DSN.isNotEmpty()) {
        sentryInitJs(SentryConfig.WEB_DSN)
    }
}
