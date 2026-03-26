package com.arcvgc.app.ui.contentlist

import com.arcvgc.app.ui.model.ContentListItem

data class ContentListUiState(
    val isLoading: Boolean = true,
    val items: List<ContentListItem> = emptyList(),
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val isPaginating: Boolean = false,
    val currentPage: Int = 1,
    val canPaginate: Boolean = false,
    val loadingSections: Set<String> = emptySet()
)
