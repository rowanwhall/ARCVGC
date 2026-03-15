package com.arcvgc.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember

class ViewModelStore {
    private val viewModels = mutableMapOf<String, Any>()

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getOrCreate(key: String, factory: () -> T): T {
        return viewModels.getOrPut(key) { factory() } as T
    }

    fun remove(key: String) {
        viewModels.remove(key)
    }

    fun removeByPrefix(prefix: String) {
        viewModels.keys.filter { it.startsWith(prefix) }.forEach { viewModels.remove(it) }
    }
}

val LocalViewModelStore = compositionLocalOf<ViewModelStore> {
    error("No ViewModelStore provided")
}

@Composable
fun ProvideViewModelStore(content: @Composable () -> Unit) {
    val store = remember { ViewModelStore() }
    CompositionLocalProvider(LocalViewModelStore provides store) {
        content()
    }
}

@Composable
inline fun <reified T : Any> rememberViewModel(key: String, crossinline factory: () -> T): T {
    val store = LocalViewModelStore.current
    return remember(key) { store.getOrCreate(key) { factory() } }
}
