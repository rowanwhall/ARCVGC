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

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("""
(message) => {
    if (typeof Sentry !== 'undefined' && Sentry.captureException) {
        Sentry.captureException(new Error(message));
    }
}
""")
private external fun sentryCaptureExceptionJs(message: String)

actual fun initializeSentry() {
    if (SentryConfig.WEB_DSN.isNotEmpty()) {
        sentryInitJs(SentryConfig.WEB_DSN)
    }
}

actual fun captureException(throwable: Throwable) {
    sentryCaptureExceptionJs(throwable.message ?: throwable.toString())
}
