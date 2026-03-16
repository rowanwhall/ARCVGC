package com.arcvgc.app.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

/**
 * Factory for creating and cancelling CoroutineScopes from platforms where
 * top-level functions and extension functions don't bridge cleanly (iOS/Swift).
 *
 * Android/Web pass viewModelScope directly — they don't need this.
 */
object CoroutineScopeFactory {
    fun createMainScope(): CoroutineScope = MainScope()
    fun cancel(scope: CoroutineScope) { scope.cancel() }
}
