package com.arcvgc.app.data

data class CatalogState<T>(
    val isLoading: Boolean = false,
    val items: List<T> = emptyList(),
    val error: String? = null
)
