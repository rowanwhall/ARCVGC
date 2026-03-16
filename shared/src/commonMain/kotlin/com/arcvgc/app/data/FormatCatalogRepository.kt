package com.arcvgc.app.data

import com.arcvgc.app.network.ApiService
import com.arcvgc.app.network.loadFullCatalog
import com.arcvgc.app.ui.mapper.FormatUiMapper
import com.arcvgc.app.ui.model.FormatUiModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FormatCatalogRepository(
    private val apiService: ApiService,
    private val cacheStorage: CatalogCacheStorageApi
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(CatalogState<FormatUiModel>(isLoading = true))
    val state: StateFlow<CatalogState<FormatUiModel>> = _state.asStateFlow()

    init { load() }

    fun reload() {
        _state.value = CatalogState(isLoading = true)
        load()
    }

    private fun load() {
        scope.launch {
            try {
                val cached = CatalogCache.load(
                    cacheStorage, CACHE_KEY, CatalogCache.TTL_7_DAYS, FormatUiModel.serializer()
                )
                if (cached != null) {
                    _state.value = CatalogState(items = cached)
                    return@launch
                }

                val result = loadFullCatalog(
                    fetch = { limit, page -> apiService.getFormats(limit, page) },
                    map = { FormatUiMapper.mapList(it) }
                )
                val items = result.items ?: emptyList()
                _state.value = CatalogState(items = items, error = result.error)
                if (result.error == null && items.isNotEmpty()) {
                    CatalogCache.save(cacheStorage, CACHE_KEY, items, FormatUiModel.serializer())
                }
            } catch (e: Exception) {
                _state.value = CatalogState(error = e.message ?: "Unknown error")
            }
        }
    }

    companion object {
        private const val CACHE_KEY = "format_catalog"
    }
}
