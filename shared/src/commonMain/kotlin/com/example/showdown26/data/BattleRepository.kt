package com.example.showdown26.data

import com.example.showdown26.domain.model.NetworkResult
import com.example.showdown26.domain.model.Pagination
import com.example.showdown26.domain.model.PlayerListItem
import com.example.showdown26.domain.model.PlayerProfile
import com.example.showdown26.domain.model.PokemonListItem
import com.example.showdown26.domain.model.SearchFilterSlot
import com.example.showdown26.network.ApiService
import com.example.showdown26.network.buildSearchRequest
import com.example.showdown26.ui.mapper.BattleCardUiMapper
import com.example.showdown26.ui.mapper.BattleDetailUiMapper
import com.example.showdown26.ui.model.BattleCardUiModel
import com.example.showdown26.ui.model.BattleDetailUiModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

data class MatchesResult(
    val battles: List<BattleCardUiModel>,
    val pagination: Pagination
)

private const val DEFAULT_PAGE_SIZE = 10

class BattleRepository(private val apiService: ApiService) {

    suspend fun getMatches(limit: Int = DEFAULT_PAGE_SIZE, page: Int = 1): MatchesResult {
        return when (val result = apiService.getMatches(limit, page)) {
            is NetworkResult.Success -> {
                val (matches, pagination) = result.data
                MatchesResult(BattleCardUiMapper.mapList(matches), pagination)
            }
            is NetworkResult.Error -> throw Exception(result.message)
        }
    }

    suspend fun searchMatches(
        filters: List<SearchFilterSlot>,
        formatId: Int = 1,
        minimumRating: Int? = null,
        maximumRating: Int? = null,
        unratedOnly: Boolean = false,
        orderBy: String = "rating",
        limit: Int = DEFAULT_PAGE_SIZE,
        page: Int = 1,
        timeRangeStart: Long? = null,
        timeRangeEnd: Long? = null,
        playerName: String? = null
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
            playerName = playerName
        )
        return when (val result = apiService.searchMatches(request)) {
            is NetworkResult.Success -> {
                val (matches, pagination) = result.data
                MatchesResult(BattleCardUiMapper.mapList(matches), pagination)
            }
            is NetworkResult.Error -> throw Exception(result.message)
        }
    }

    suspend fun getMatchDetail(id: Int): BattleDetailUiModel {
        return when (val result = apiService.getMatchDetail(id)) {
            is NetworkResult.Success -> BattleDetailUiMapper.map(result.data)
            is NetworkResult.Error -> throw Exception(result.message)
        }
    }

    suspend fun getMatchesByIds(ids: List<Int>): List<BattleCardUiModel> {
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

    suspend fun getPokemonByIds(ids: List<Int>): List<PokemonListItem> {
        return coroutineScope {
            ids.map { id ->
                async {
                    when (val result = apiService.getPokemonById(id)) {
                        is NetworkResult.Success -> result.data
                        is NetworkResult.Error -> null
                    }
                }
            }.awaitAll().filterNotNull()
        }
    }

    suspend fun getPlayerProfile(id: Int): PlayerProfile {
        return when (val result = apiService.getPlayerById(id)) {
            is NetworkResult.Success -> result.data
            is NetworkResult.Error -> throw Exception(result.message)
        }
    }

    suspend fun getPlayersByNames(names: List<String>): List<PlayerListItem> {
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
}
