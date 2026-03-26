package com.arcvgc.app.network

import com.arcvgc.app.data.CatalogCache
import com.arcvgc.app.data.CatalogCacheStorage
import com.arcvgc.app.data.captureException
import com.arcvgc.app.domain.model.NetworkResult
import com.arcvgc.app.domain.model.Pagination
import com.arcvgc.app.ui.mapper.FormatUiMapper
import com.arcvgc.app.ui.mapper.ItemUiMapper
import com.arcvgc.app.ui.mapper.PokemonPickerUiMapper
import com.arcvgc.app.ui.mapper.TeraTypeUiMapper
import com.arcvgc.app.ui.model.FormatUiModel
import com.arcvgc.app.ui.model.ItemUiModel
import com.arcvgc.app.ui.model.PokemonPickerUiModel
import com.arcvgc.app.ui.model.TeraTypeUiModel

data class CatalogResult<T>(
    val items: List<T>? = null,
    val error: String? = null
)

suspend fun <TDomain, TUi> loadFullCatalog(
    fetch: suspend (limit: Int, page: Int) -> NetworkResult<Pair<List<TDomain>, Pagination>>,
    map: (List<TDomain>) -> List<TUi>
): CatalogResult<TUi> {
    val allItems = mutableListOf<TUi>()
    var page = 1
    var hasMore = true

    while (hasMore) {
        when (val result = fetch(50, page)) {
            is NetworkResult.Success -> {
                val (items, pagination) = result.data
                allItems.addAll(map(items))
                hasMore = pagination.hasNext
                page++
            }
            is NetworkResult.Error -> {
                return CatalogResult(error = result.message)
            }
        }
    }

    return CatalogResult(items = allItems)
}

/** Typed loaders for iOS interop — avoids generic lambda bridging issues. */

suspend fun loadPokemonCatalog(
    apiService: ApiService,
    cacheStorage: CatalogCacheStorage
): CatalogResult<PokemonPickerUiModel> = safeCatalogLoad {
    val cached = CatalogCache.load(
        cacheStorage, "pokemon_catalog", CatalogCache.TTL_30_DAYS, PokemonPickerUiModel.serializer()
    )
    if (cached != null) return@safeCatalogLoad CatalogResult(items = cached)

    val result = loadFullCatalog(
        fetch = { limit, page -> apiService.getPokemonList(limit, page) },
        map = { PokemonPickerUiMapper.mapList(it) }
    )
    if (result.error == null && !result.items.isNullOrEmpty()) {
        CatalogCache.save(cacheStorage, "pokemon_catalog", result.items, PokemonPickerUiModel.serializer())
    }
    result
}

suspend fun loadItemCatalog(
    apiService: ApiService,
    cacheStorage: CatalogCacheStorage
): CatalogResult<ItemUiModel> = safeCatalogLoad {
    val cached = CatalogCache.load(
        cacheStorage, "item_catalog", CatalogCache.TTL_30_DAYS, ItemUiModel.serializer()
    )
    if (cached != null) return@safeCatalogLoad CatalogResult(items = cached)

    val result = loadFullCatalog(
        fetch = { limit, page -> apiService.getItems(limit, page) },
        map = { ItemUiMapper.mapList(it) }
    )
    if (result.error == null && !result.items.isNullOrEmpty()) {
        CatalogCache.save(cacheStorage, "item_catalog", result.items, ItemUiModel.serializer())
    }
    result
}

suspend fun loadTeraTypeCatalog(
    apiService: ApiService,
    cacheStorage: CatalogCacheStorage
): CatalogResult<TeraTypeUiModel> = safeCatalogLoad {
    val cached = CatalogCache.load(
        cacheStorage, "tera_type_catalog", CatalogCache.TTL_7_DAYS, TeraTypeUiModel.serializer()
    )
    if (cached != null) return@safeCatalogLoad CatalogResult(items = cached)

    val result = loadFullCatalog(
        fetch = { limit, page -> apiService.getTeraTypes(limit, page) },
        map = { TeraTypeUiMapper.mapList(it) }
    )
    if (result.error == null && !result.items.isNullOrEmpty()) {
        CatalogCache.save(cacheStorage, "tera_type_catalog", result.items, TeraTypeUiModel.serializer())
    }
    result
}

suspend fun loadFormatCatalog(
    apiService: ApiService,
    cacheStorage: CatalogCacheStorage
): CatalogResult<FormatUiModel> = safeCatalogLoad {
    val cached = CatalogCache.load(
        cacheStorage, "format_catalog", CatalogCache.TTL_7_DAYS, FormatUiModel.serializer()
    )
    if (cached != null) return@safeCatalogLoad CatalogResult(items = cached)

    val result = loadFullCatalog(
        fetch = { limit, page -> apiService.getFormats(limit, page) },
        map = { FormatUiMapper.mapList(it) }
    )
    if (result.error == null && !result.items.isNullOrEmpty()) {
        CatalogCache.save(cacheStorage, "format_catalog", result.items, FormatUiModel.serializer())
    }
    result
}

private suspend inline fun <T> safeCatalogLoad(
    block: () -> CatalogResult<T>
): CatalogResult<T> {
    return try {
        block()
    } catch (e: Exception) {
        captureException(e)
        CatalogResult(error = e.message ?: "Unknown error")
    }
}
