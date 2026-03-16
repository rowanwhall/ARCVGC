package com.arcvgc.app.testutil

import com.arcvgc.app.data.BattleRepositoryApi
import com.arcvgc.app.data.MatchesResult
import com.arcvgc.app.domain.model.Pagination
import com.arcvgc.app.domain.model.PlayerListItem
import com.arcvgc.app.domain.model.PlayerProfile
import com.arcvgc.app.domain.model.PokemonListItem
import com.arcvgc.app.domain.model.SearchFilterSlot
import com.arcvgc.app.ui.model.BattleCardUiModel

class FakeBattleRepository : BattleRepositoryApi {

    var searchMatchesResult: MatchesResult = MatchesResult(
        battles = emptyList(),
        pagination = Pagination(page = 1, itemsPerPage = 10, totalItems = 0, totalPages = 1)
    )
    var searchMatchesError: Exception? = null

    var matchesByIdsResult: List<BattleCardUiModel> = emptyList()
    var matchesByIdsError: Exception? = null

    var pokemonByIdsResult: List<PokemonListItem> = emptyList()
    var pokemonByIdsError: Exception? = null

    var playerProfileResult: PlayerProfile? = null
    var playerProfileError: Exception? = null

    var playersByNamesResult: List<PlayerListItem> = emptyList()
    var playersByNamesError: Exception? = null

    var searchMatchesCalls = mutableListOf<SearchMatchesCall>()

    data class SearchMatchesCall(
        val filters: List<SearchFilterSlot>,
        val formatId: Int,
        val orderBy: String,
        val page: Int,
        val playerName: String?
    )

    override suspend fun searchMatches(
        filters: List<SearchFilterSlot>,
        formatId: Int,
        minimumRating: Int?,
        maximumRating: Int?,
        unratedOnly: Boolean,
        orderBy: String,
        limit: Int,
        page: Int,
        timeRangeStart: Long?,
        timeRangeEnd: Long?,
        playerName: String?
    ): MatchesResult {
        searchMatchesCalls.add(SearchMatchesCall(filters, formatId, orderBy, page, playerName))
        searchMatchesError?.let { throw it }
        return searchMatchesResult
    }

    override suspend fun getMatchesByIds(ids: List<Int>): List<BattleCardUiModel> {
        matchesByIdsError?.let { throw it }
        return matchesByIdsResult
    }

    override suspend fun getPokemonByIds(ids: List<Int>): List<PokemonListItem> {
        pokemonByIdsError?.let { throw it }
        return pokemonByIdsResult
    }

    override suspend fun getPlayerProfile(id: Int): PlayerProfile {
        playerProfileError?.let { throw it }
        return playerProfileResult ?: throw Exception("No player profile configured")
    }

    override suspend fun getPlayersByNames(names: List<String>): List<PlayerListItem> {
        playersByNamesError?.let { throw it }
        return playersByNamesResult
    }
}
