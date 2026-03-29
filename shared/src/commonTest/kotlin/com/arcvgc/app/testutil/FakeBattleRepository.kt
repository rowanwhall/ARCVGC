package com.arcvgc.app.testutil

import com.arcvgc.app.data.BattleRepositoryApi
import com.arcvgc.app.data.MatchesResult
import kotlinx.coroutines.delay
import com.arcvgc.app.domain.model.FormatDetail
import com.arcvgc.app.domain.model.Pagination
import com.arcvgc.app.domain.model.PlayerListItem
import com.arcvgc.app.domain.model.PlayerProfile
import com.arcvgc.app.domain.model.PokemonProfile
import com.arcvgc.app.domain.model.SearchFilterSlot
import com.arcvgc.app.domain.model.WinnerFilter
import com.arcvgc.app.ui.model.BattleCardUiModel

class FakeBattleRepository : BattleRepositoryApi {

    var bestPreviousDayResult: List<BattleCardUiModel> = emptyList()
    var bestPreviousDayError: Exception? = null

    var searchMatchesResult: MatchesResult = MatchesResult(
        battles = emptyList(),
        pagination = Pagination(page = 1, itemsPerPage = 10, hasNext = false)
    )
    var searchMatchesError: Exception? = null

    var matchesByIdsResult: List<BattleCardUiModel> = emptyList()
    var matchesByIdsError: Exception? = null

    var playerProfileResult: PlayerProfile? = null
    var playerProfileError: Exception? = null

    var pokemonProfileResult: PokemonProfile? = null
    var pokemonProfileError: Exception? = null

    var formatDetailResult: FormatDetail? = null
    var formatDetailError: Exception? = null

    var playersByNamesResult: List<PlayerListItem> = emptyList()
    var playersByNamesError: Exception? = null

    var searchMatchesDelayMs: Long = 0
    var pokemonProfileDelayMs: Long = 0

    var searchMatchesCalls = mutableListOf<SearchMatchesCall>()

    data class SearchMatchesCall(
        val filters: List<SearchFilterSlot>,
        val formatId: Int,
        val orderBy: String,
        val page: Int,
        val playerName: String?,
        val team2Filters: List<SearchFilterSlot>
    )

    override suspend fun getBestPreviousDay(formatId: Int): List<BattleCardUiModel> {
        bestPreviousDayError?.let { throw it }
        return bestPreviousDayResult
    }

    override suspend fun getMatches(
        limit: Int,
        page: Int,
        orderBy: String?,
        ratedOnly: Boolean?,
        formatId: Int?
    ): MatchesResult {
        return searchMatchesResult
    }

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
        playerName: String?,
        playerId: Int?,
        team2Filters: List<SearchFilterSlot>,
        winnerFilter: WinnerFilter
    ): MatchesResult {
        searchMatchesCalls.add(SearchMatchesCall(filters, formatId, orderBy, page, playerName, team2Filters))
        if (searchMatchesDelayMs > 0) delay(searchMatchesDelayMs)
        searchMatchesError?.let { throw it }
        return searchMatchesResult
    }

    override suspend fun getFormatDetail(formatId: Int, topPokemonCount: Int?): FormatDetail {
        formatDetailError?.let { throw it }
        return formatDetailResult ?: throw Exception("No format detail configured")
    }

    override suspend fun getMatchesByIds(ids: List<Int>): List<BattleCardUiModel> {
        matchesByIdsError?.let { throw it }
        return matchesByIdsResult
    }

    override suspend fun getPlayerProfile(id: Int, formatId: Int?): PlayerProfile {
        playerProfileError?.let { throw it }
        return playerProfileResult ?: throw Exception("No player profile configured")
    }

    override suspend fun getPokemonProfile(id: Int, formatId: Int?): PokemonProfile {
        if (pokemonProfileDelayMs > 0) delay(pokemonProfileDelayMs)
        pokemonProfileError?.let { throw it }
        return pokemonProfileResult ?: throw Exception("No pokemon profile configured")
    }

    override suspend fun getPlayersByNames(names: List<String>): List<PlayerListItem> {
        playersByNamesError?.let { throw it }
        return playersByNamesResult
    }

    override suspend fun searchPlayersByName(name: String): List<PlayerListItem> {
        playersByNamesError?.let { throw it }
        return playersByNamesResult
    }
}
