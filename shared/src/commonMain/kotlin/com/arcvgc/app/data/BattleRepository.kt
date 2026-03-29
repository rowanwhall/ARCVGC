package com.arcvgc.app.data

import com.arcvgc.app.domain.model.FormatDetail
import com.arcvgc.app.domain.model.NetworkResult
import com.arcvgc.app.domain.model.Pagination
import com.arcvgc.app.domain.model.PlayerListItem
import com.arcvgc.app.domain.model.PlayerProfile
import com.arcvgc.app.domain.model.PokemonProfile
import com.arcvgc.app.domain.model.SearchFilterSlot
import com.arcvgc.app.domain.model.WinnerFilter
import com.arcvgc.app.network.ApiService
import com.arcvgc.app.network.buildSearchRequest
import com.arcvgc.app.ui.mapper.BattleCardUiMapper
import com.arcvgc.app.ui.mapper.BattleDetailUiMapper
import com.arcvgc.app.ui.model.BattleCardUiModel
import com.arcvgc.app.ui.model.BattleDetailUiModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

data class MatchesResult(
    val battles: List<BattleCardUiModel>,
    val pagination: Pagination
)

private const val DEFAULT_PAGE_SIZE = 10

interface BattleRepositoryApi {
    suspend fun searchMatches(
        filters: List<SearchFilterSlot>,
        formatId: Int = 1,
        minimumRating: Int? = null,
        maximumRating: Int? = null,
        unratedOnly: Boolean = false,
        orderBy: String = "rating",
        limit: Int = 10,
        page: Int = 1,
        timeRangeStart: Long? = null,
        timeRangeEnd: Long? = null,
        playerName: String? = null,
        playerId: Int? = null,
        team2Filters: List<SearchFilterSlot> = emptyList(),
        winnerFilter: WinnerFilter = WinnerFilter.NONE
    ): MatchesResult
    suspend fun getBestPreviousDay(formatId: Int): List<BattleCardUiModel>
    suspend fun getMatches(
        limit: Int = 10,
        page: Int = 1,
        orderBy: String? = null,
        ratedOnly: Boolean? = null,
        formatId: Int? = null
    ): MatchesResult
    suspend fun getFormatDetail(formatId: Int, topPokemonCount: Int? = null): FormatDetail
    suspend fun getMatchesByIds(ids: List<Int>): List<BattleCardUiModel>
    suspend fun getPlayerProfile(id: Int, formatId: Int? = null): PlayerProfile
    suspend fun getPokemonProfile(id: Int, formatId: Int? = null): PokemonProfile
    suspend fun getPlayersByNames(names: List<String>): List<PlayerListItem>
    suspend fun searchPlayersByName(name: String): List<PlayerListItem>
}

class BattleRepository(private val apiService: ApiService) : BattleRepositoryApi {

    override suspend fun getBestPreviousDay(formatId: Int): List<BattleCardUiModel> {
        return when (val result = apiService.getBestPreviousDay(formatId)) {
            is NetworkResult.Success -> BattleCardUiMapper.mapList(result.data)
            is NetworkResult.Error -> {
                val error = Exception(result.message)
                captureException(error)
                throw error
            }
        }
    }

    override suspend fun getMatches(
        limit: Int,
        page: Int,
        orderBy: String?,
        ratedOnly: Boolean?,
        formatId: Int?
    ): MatchesResult {
        return when (val result = apiService.getMatches(limit, page, orderBy, ratedOnly, formatId)) {
            is NetworkResult.Success -> {
                val (matches, pagination) = result.data
                MatchesResult(BattleCardUiMapper.mapList(matches), pagination)
            }
            is NetworkResult.Error -> {
                val error = Exception(result.message)
                captureException(error)
                throw error
            }
        }
    }

    override suspend fun getFormatDetail(formatId: Int, topPokemonCount: Int?): FormatDetail {
        return when (val result = apiService.getFormatDetail(formatId, topPokemonCount)) {
            is NetworkResult.Success -> result.data
            is NetworkResult.Error -> {
                val error = Exception(result.message)
                captureException(error)
                throw error
            }
        }
    }

    suspend fun getFormatDetailOrNull(formatId: Int, topPokemonCount: Int? = null): FormatDetail? {
        return try {
            getFormatDetail(formatId, topPokemonCount)
        } catch (e: Exception) {
            null
        }
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
        val request = buildSearchRequest(
            filters = filters,
            formatId = formatId,
            minimumRating = minimumRating,
            maximumRating = maximumRating,
            unratedOnly = unratedOnly,
            orderBy = orderBy,
            limit = limit,
            page = page,
            timeRangeStart = timeRangeStart,
            timeRangeEnd = timeRangeEnd,
            playerName = playerName,
            playerId = playerId,
            team2Filters = team2Filters,
            winnerFilter = winnerFilter
        )
        return when (val result = apiService.searchMatches(request)) {
            is NetworkResult.Success -> {
                val (matches, pagination) = result.data
                MatchesResult(BattleCardUiMapper.mapList(matches), pagination)
            }
            is NetworkResult.Error -> {
                val error = Exception(result.message)
                captureException(error)
                throw error
            }
        }
    }

    suspend fun getMatchDetail(id: Int): BattleDetailUiModel {
        return when (val result = apiService.getMatchDetail(id)) {
            is NetworkResult.Success -> BattleDetailUiMapper.map(result.data)
            is NetworkResult.Error -> {
                val error = Exception(result.message)
                captureException(error)
                throw error
            }
        }
    }

    /**
     * Non-throwing variant for iOS — returns null on error instead of throwing.
     * Errors are reported to Sentry automatically.
     */
    suspend fun getMatchDetailOrNull(id: Int): BattleDetailUiModel? {
        return try {
            getMatchDetail(id)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getMatchesByIds(ids: List<Int>): List<BattleCardUiModel> {
        return coroutineScope {
            ids.map { id ->
                async {
                    when (val result = apiService.getMatchDetail(id)) {
                        is NetworkResult.Success -> BattleCardUiMapper.map(result.data)
                        is NetworkResult.Error -> null
                    }
                }
            }.awaitAll().filterNotNull()
        }
    }

    override suspend fun getPokemonProfile(id: Int, formatId: Int?): PokemonProfile {
        return when (val result = apiService.getPokemonById(id, formatId)) {
            is NetworkResult.Success -> result.data
            is NetworkResult.Error -> {
                val error = Exception(result.message)
                captureException(error)
                throw error
            }
        }
    }

    /**
     * Non-throwing variant for iOS — returns null on error instead of throwing.
     * Errors are reported to Sentry automatically.
     */
    suspend fun getPokemonProfileOrNull(id: Int, formatId: Int? = null): PokemonProfile? {
        return try {
            getPokemonProfile(id, formatId)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getPlayerProfile(id: Int, formatId: Int?): PlayerProfile {
        return when (val result = apiService.getPlayerById(id, formatId)) {
            is NetworkResult.Success -> result.data
            is NetworkResult.Error -> {
                val error = Exception(result.message)
                captureException(error)
                throw error
            }
        }
    }

    override suspend fun getPlayersByNames(names: List<String>): List<PlayerListItem> {
        return coroutineScope {
            names.map { name ->
                async {
                    when (val result = apiService.getPlayersByName(name)) {
                        is NetworkResult.Success -> result.data.firstOrNull()
                        is NetworkResult.Error -> null
                    }
                }
            }.awaitAll().filterNotNull()
        }
    }

    override suspend fun searchPlayersByName(name: String): List<PlayerListItem> {
        return when (val result = apiService.getPlayersByName(name)) {
            is NetworkResult.Success -> result.data
            is NetworkResult.Error -> emptyList()
        }
    }
}
