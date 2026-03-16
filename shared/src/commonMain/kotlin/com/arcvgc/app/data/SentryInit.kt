package com.arcvgc.app.data

/**
 * Initializes Sentry crash reporting. Must be called once at app startup,
 * before any other initialization.
 */
expect fun initializeSentry()

/**
 * Manually captures a non-fatal exception in Sentry.
 * Use for caught errors that should be tracked but don't crash the app.
 */
expect fun captureException(throwable: Throwable)
