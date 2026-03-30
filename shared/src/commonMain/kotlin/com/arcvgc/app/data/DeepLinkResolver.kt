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
import com.arcvgc.app.ui.model.AbilityUiModel
import com.arcvgc.app.ui.model.FormatUiModel
import com.arcvgc.app.ui.model.ItemUiModel
import com.arcvgc.app.ui.model.TeraTypeUiModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

class DeepLinkResolver(
    private val apiService: ApiService,
    private val itemCatalogProvider: (() -> List<ItemUiModel>)? = null,
    private val teraTypeCatalogProvider: (() -> List<TeraTypeUiModel>)? = null,
    private val formatCatalogProvider: (() -> List<FormatUiModel>)? = null,
    private val abilityCatalogProvider: (() -> List<AbilityUiModel>)? = null,
    private val catalogStateFlows: List<StateFlow<CatalogState<*>>> = emptyList()
) {

    sealed class ResolvedLink {
        data object Home : ResolvedLink()
        data class Pokemon(val item: PokemonListItem) : ResolvedLink()
        data class Player(val item: PlayerListItem) : ResolvedLink()
        data class Favorites(val contentType: FavoriteContentType) : ResolvedLink()
        data class Search(val params: SearchParams) : ResolvedLink()
        data object SearchTab : ResolvedLink()
        data object SettingsTab : ResolvedLink()
        data class TopPokemon(val formatId: Int?) : ResolvedLink()
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
        is DeepLinkTarget.TopPokemon -> ResolvedLink.TopPokemon(target.formatId)
    }

    private suspend fun resolveSearch(query: SearchQueryParams): ResolvedLink.Search? {
        // Wait for catalogs to finish loading (up to 5s) so display data is available
        if (catalogStateFlows.isNotEmpty()) {
            withTimeoutOrNull(CATALOG_WAIT_TIMEOUT) {
                coroutineScope {
                    catalogStateFlows.map { flow ->
                        async { flow.first { !it.isLoading } }
                    }.awaitAll()
                }
            }
        }

        val items = itemCatalogProvider?.invoke() ?: emptyList()
        val teraTypes = teraTypeCatalogProvider?.invoke() ?: emptyList()
        val formats = formatCatalogProvider?.invoke() ?: emptyList()
        val abilities = abilityCatalogProvider?.invoke() ?: emptyList()

        // Resolve all Pokemon display data from API (team1 + team2, parallel)
        val allPokemonIds = query.pokemonIds + query.team2PokemonIds
        val allPokemonItems = coroutineScope {
            allPokemonIds.map { id ->
                async {
                    when (val result = apiService.getPokemonById(id)) {
                        is NetworkResult.Success -> result.data.toPokemonListItem()
                        is NetworkResult.Error -> null
                    }
                }
            }.awaitAll()
        }

        val team1Pokemon = allPokemonItems.take(query.pokemonIds.size)
        val team2Pokemon = allPokemonItems.drop(query.pokemonIds.size)

        // Build team1 SearchFilterSlots
        val filters = query.pokemonIds.mapIndexedNotNull { index, _ ->
            val pokemon = team1Pokemon[index] ?: return@mapIndexedNotNull null
            val itemId = query.itemIds.getOrNull(index)
            val teraTypeId = query.teraTypeIds.getOrNull(index)
            val abilityId = query.abilityIds.getOrNull(index)
            val item = itemId?.let { id -> items.find { it.id == id } }
            val teraType = teraTypeId?.let { id -> teraTypes.find { it.id == id } }
            val ability = abilityId?.let { id -> abilities.find { it.id == id } }

            SearchFilterSlot(
                pokemonId = pokemon.id,
                itemId = itemId,
                teraTypeId = teraTypeId,
                abilityId = abilityId,
                pokemonName = pokemon.name,
                pokemonImageUrl = pokemon.imageUrl,
                itemName = item?.name,
                teraTypeImageUrl = teraType?.imageUrl,
                abilityName = ability?.name
            )
        }

        if (filters.isEmpty()) return null

        // Build team2 SearchFilterSlots
        val team2Filters = query.team2PokemonIds.mapIndexedNotNull { index, _ ->
            val pokemon = team2Pokemon[index] ?: return@mapIndexedNotNull null
            val itemId = query.team2ItemIds.getOrNull(index)
            val teraTypeId = query.team2TeraTypeIds.getOrNull(index)
            val abilityId = query.team2AbilityIds.getOrNull(index)
            val item = itemId?.let { id -> items.find { it.id == id } }
            val teraType = teraTypeId?.let { id -> teraTypes.find { it.id == id } }
            val ability = abilityId?.let { id -> abilities.find { it.id == id } }

            SearchFilterSlot(
                pokemonId = pokemon.id,
                itemId = itemId,
                teraTypeId = teraTypeId,
                abilityId = abilityId,
                pokemonName = pokemon.name,
                pokemonImageUrl = pokemon.imageUrl,
                itemName = item?.name,
                teraTypeImageUrl = teraType?.imageUrl,
                abilityName = ability?.name
            )
        }

        val formatName = formats.find { it.id == query.formatId }?.displayName

        return ResolvedLink.Search(
            SearchParams(
                filters = filters,
                team2Filters = team2Filters,
                formatId = query.formatId,
                minimumRating = query.minimumRating,
                maximumRating = query.maximumRating,
                unratedOnly = query.unratedOnly,
                orderBy = query.orderBy,
                timeRangeStart = query.timeRangeStart,
                timeRangeEnd = query.timeRangeEnd,
                playerName = query.playerName,
                formatName = formatName,
                winnerFilter = query.winnerFilter
            )
        )
    }

    companion object {
        private const val CATALOG_WAIT_TIMEOUT = 5000L
    }
}
