package com.arcvgc.app.data

import com.arcvgc.app.network.ApiService
import com.arcvgc.app.network.loadFullCatalog
import com.arcvgc.app.ui.mapper.ItemUiMapper
import com.arcvgc.app.ui.model.ItemUiModel
import com.arcvgc.app.util.createSafeScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ItemCatalogRepository(
    private val apiService: ApiService,
    private val cacheStorage: CatalogCacheStorageApi
) {
    private val scope = createSafeScope()
    private val _state = MutableStateFlow(CatalogState<ItemUiModel>(isLoading = true))
    val state: StateFlow<CatalogState<ItemUiModel>> = _state.asStateFlow()

    init { load() }

    fun reload() {
        _state.value = CatalogState(isLoading = true)
        load()
    }

    private fun load() {
        scope.launch {
            try {
                val cached = CatalogCache.load(
                    cacheStorage, CACHE_KEY, CatalogCache.TTL_30_DAYS, ItemUiModel.serializer()
                )
                if (cached != null) {
                    _state.value = CatalogState(items = cached)
                    return@launch
                }

                val result = loadFullCatalog(
                    fetch = { limit, page -> apiService.getItems(limit, page) },
                    map = { ItemUiMapper.mapList(it) }
                )
                val items = result.items ?: emptyList()
                _state.value = CatalogState(items = items, error = result.error)
                if (result.error == null && items.isNotEmpty()) {
                    CatalogCache.save(cacheStorage, CACHE_KEY, items, ItemUiModel.serializer())
                }
            } catch (e: Exception) {
                _state.value = CatalogState(error = e.message ?: "Unknown error")
            }
        }
    }

    companion object {
        private const val CACHE_KEY = "item_catalog"
    }
}
