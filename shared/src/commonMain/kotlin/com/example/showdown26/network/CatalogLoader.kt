package com.example.showdown26.network

import com.example.showdown26.data.CatalogCache
import com.example.showdown26.data.CatalogCacheStorage
import com.example.showdown26.domain.model.NetworkResult
import com.example.showdown26.domain.model.Pagination
import com.example.showdown26.ui.mapper.FormatUiMapper
import com.example.showdown26.ui.mapper.ItemUiMapper
import com.example.showdown26.ui.mapper.PokemonPickerUiMapper
import com.example.showdown26.ui.mapper.TeraTypeUiMapper
import com.example.showdown26.ui.model.FormatUiModel
import com.example.showdown26.ui.model.ItemUiModel
import com.example.showdown26.ui.model.PokemonPickerUiModel
import com.example.showdown26.ui.model.TeraTypeUiModel

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
    var totalPages = 1

    while (page <= totalPages) {
        when (val result = fetch(50, page)) {
            is NetworkResult.Success -> {
                val (items, pagination) = result.data
                allItems.addAll(map(items))
                totalPages = pagination.totalPages
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
): CatalogResult<PokemonPickerUiModel> {
    val cached = CatalogCache.load(
        cacheStorage, "pokemon_catalog", CatalogCache.TTL_30_DAYS, PokemonPickerUiModel.serializer()
    )
    if (cached != null) return CatalogResult(items = cached)

    val result = loadFullCatalog(
        fetch = { limit, page -> apiService.getPokemonList(limit, page) },
        map = { PokemonPickerUiMapper.mapList(it) }
    )
    if (result.error == null && !result.items.isNullOrEmpty()) {
        CatalogCache.save(cacheStorage, "pokemon_catalog", result.items, PokemonPickerUiModel.serializer())
    }
    return result
}

suspend fun loadItemCatalog(
    apiService: ApiService,
    cacheStorage: CatalogCacheStorage
): CatalogResult<ItemUiModel> {
    val cached = CatalogCache.load(
        cacheStorage, "item_catalog", CatalogCache.TTL_30_DAYS, ItemUiModel.serializer()
    )
    if (cached != null) return CatalogResult(items = cached)

    val result = loadFullCatalog(
        fetch = { limit, page -> apiService.getItems(limit, page) },
        map = { ItemUiMapper.mapList(it) }
    )
    if (result.error == null && !result.items.isNullOrEmpty()) {
        CatalogCache.save(cacheStorage, "item_catalog", result.items, ItemUiModel.serializer())
    }
    return result
}

suspend fun loadTeraTypeCatalog(
    apiService: ApiService,
    cacheStorage: CatalogCacheStorage
): CatalogResult<TeraTypeUiModel> {
    val cached = CatalogCache.load(
        cacheStorage, "tera_type_catalog", CatalogCache.TTL_7_DAYS, TeraTypeUiModel.serializer()
    )
    if (cached != null) return CatalogResult(items = cached)

    val result = loadFullCatalog(
        fetch = { limit, page -> apiService.getTeraTypes(limit, page) },
        map = { TeraTypeUiMapper.mapList(it) }
    )
    if (result.error == null && !result.items.isNullOrEmpty()) {
        CatalogCache.save(cacheStorage, "tera_type_catalog", result.items, TeraTypeUiModel.serializer())
    }
    return result
}

suspend fun loadFormatCatalog(
    apiService: ApiService,
    cacheStorage: CatalogCacheStorage
): CatalogResult<FormatUiModel> {
    val cached = CatalogCache.load(
        cacheStorage, "format_catalog", CatalogCache.TTL_7_DAYS, FormatUiModel.serializer()
    )
    if (cached != null) return CatalogResult(items = cached)

    val result = loadFullCatalog(
        fetch = { limit, page -> apiService.getFormats(limit, page) },
        map = { FormatUiMapper.mapList(it) }
    )
    if (result.error == null && !result.items.isNullOrEmpty()) {
        CatalogCache.save(cacheStorage, "format_catalog", result.items, FormatUiModel.serializer())
    }
    return result
}
