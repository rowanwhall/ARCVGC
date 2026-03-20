package com.arcvgc.app.data

import com.arcvgc.app.domain.model.DeepLink
import com.arcvgc.app.domain.model.DeepLinkTarget
import com.arcvgc.app.domain.model.NetworkResult
import com.arcvgc.app.domain.model.PlayerListItem
import com.arcvgc.app.domain.model.PokemonListItem
import com.arcvgc.app.domain.model.SearchFilterSlot
import com.arcvgc.app.domain.model.SearchParams
import com.arcvgc.app.domain.model.SearchQueryParams
import com.arcvgc.app.network.ApiService
import com.arcvgc.app.ui.model.FavoriteContentType
import com.arcvgc.app.ui.model.FormatUiModel
import com.arcvgc.app.ui.model.ItemUiModel
import com.arcvgc.app.ui.model.TeraTypeUiModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class DeepLinkResolver(
    private val apiService: ApiService,
    private val itemCatalogProvider: (() -> List<ItemUiModel>)? = null,
    private val teraTypeCatalogProvider: (() -> List<TeraTypeUiModel>)? = null,
    private val formatCatalogProvider: (() -> List<FormatUiModel>)? = null
) {

    sealed class ResolvedLink {
        data object Home : ResolvedLink()
        data class Pokemon(val item: PokemonListItem) : ResolvedLink()
        data class Player(val item: PlayerListItem) : ResolvedLink()
        data class Favorites(val contentType: FavoriteContentType) : ResolvedLink()
        data class Search(val params: SearchParams) : ResolvedLink()
        data object SearchTab : ResolvedLink()
        data object SettingsTab : ResolvedLink()
    }

    suspend fun resolve(deepLink: DeepLink): ResolvedLink? = resolve(deepLink.target)

    suspend fun resolve(target: DeepLinkTarget): ResolvedLink? = when (target) {
        is DeepLinkTarget.Home -> ResolvedLink.Home
        is DeepLinkTarget.Pokemon -> {
            when (val result = apiService.getPokemonById(target.id)) {
                is NetworkResult.Success -> ResolvedLink.Pokemon(result.data.toPokemonListItem())
                is NetworkResult.Error -> null
            }
        }
        is DeepLinkTarget.Player -> {
            when (val result = apiService.getPlayersByName(target.name)) {
                is NetworkResult.Success -> result.data.firstOrNull()?.let { ResolvedLink.Player(it) }
                is NetworkResult.Error -> null
            }
        }
        is DeepLinkTarget.Favorites -> {
            val contentType = when (target.contentType) {
                "battles" -> FavoriteContentType.Battles
                "pokemon" -> FavoriteContentType.Pokemon
                "players" -> FavoriteContentType.Players
                else -> return@resolve null
            }
            ResolvedLink.Favorites(contentType)
        }
        is DeepLinkTarget.Search -> resolveSearch(target.params)
        is DeepLinkTarget.SearchTab -> ResolvedLink.SearchTab
        is DeepLinkTarget.SettingsTab -> ResolvedLink.SettingsTab
    }

    private suspend fun resolveSearch(query: SearchQueryParams): ResolvedLink.Search? {
        val items = itemCatalogProvider?.invoke() ?: emptyList()
        val teraTypes = teraTypeCatalogProvider?.invoke() ?: emptyList()
        val formats = formatCatalogProvider?.invoke() ?: emptyList()

        // Resolve Pokemon display data from API (parallel)
        val pokemonItems = coroutineScope {
            query.pokemonIds.map { id ->
                async {
                    when (val result = apiService.getPokemonById(id)) {
                        is NetworkResult.Success -> result.data.toPokemonListItem()
                        is NetworkResult.Error -> null
                    }
                }
            }.awaitAll()
        }

        // Build SearchFilterSlots
        val filters = query.pokemonIds.mapIndexedNotNull { index, _ ->
            val pokemon = pokemonItems[index] ?: return@mapIndexedNotNull null
            val itemId = query.itemIds.getOrNull(index)
            val teraTypeId = query.teraTypeIds.getOrNull(index)
            val item = itemId?.let { id -> items.find { it.id == id } }
            val teraType = teraTypeId?.let { id -> teraTypes.find { it.id == id } }

            SearchFilterSlot(
                pokemonId = pokemon.id,
                itemId = itemId,
                teraTypeId = teraTypeId,
                pokemonName = pokemon.name,
                pokemonImageUrl = pokemon.imageUrl,
                itemName = item?.name,
                teraTypeImageUrl = teraType?.imageUrl
            )
        }

        if (filters.isEmpty()) return null

        val formatName = formats.find { it.id == query.formatId }?.displayName

        return ResolvedLink.Search(
            SearchParams(
                filters = filters,
                formatId = query.formatId,
                minimumRating = query.minimumRating,
                maximumRating = query.maximumRating,
                unratedOnly = query.unratedOnly,
                orderBy = query.orderBy,
                timeRangeStart = query.timeRangeStart,
                timeRangeEnd = query.timeRangeEnd,
                playerName = query.playerName,
                formatName = formatName
            )
        )
    }
}
