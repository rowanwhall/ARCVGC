package com.arcvgc.app.util

import com.arcvgc.app.data.captureException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

private val sentryExceptionHandler = CoroutineExceptionHandler { _, throwable ->
    captureException(throwable)
}

/**
 * Creates a CoroutineScope with SupervisorJob + Dispatchers.Default + Sentry exception handler.
 * Use for background scopes in repositories and other shared components.
 */
fun createSafeScope(): CoroutineScope =
    CoroutineScope(SupervisorJob() + Dispatchers.Default + sentryExceptionHandler)

/**
 * Factory for creating and cancelling CoroutineScopes from platforms where
 * top-level functions and extension functions don't bridge cleanly (iOS/Swift).
 *
 * Android/Web pass viewModelScope directly — they don't need this.
 */
object CoroutineScopeFactory {
    fun createMainScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main + sentryExceptionHandler)

    fun cancel(scope: CoroutineScope) { scope.cancel() }
}
