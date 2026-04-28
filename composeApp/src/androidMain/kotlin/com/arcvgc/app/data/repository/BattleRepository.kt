package com.arcvgc.app.data.repository

import com.arcvgc.app.domain.model.Pagination
import com.arcvgc.app.domain.model.PlayerListItem
import com.arcvgc.app.domain.model.PlayerProfile
import com.arcvgc.app.domain.model.PokemonProfile
import com.arcvgc.app.domain.model.SearchFilterSlot
import com.arcvgc.app.network.ApiService
import com.arcvgc.app.ui.model.BattleCardUiModel
import com.arcvgc.app.ui.model.BattleDetailUiModel
import com.arcvgc.app.data.BattleRepository as SharedBattleRepository
import javax.inject.Inject
import javax.inject.Singleton

private const val DEFAULT_PAGE_SIZE = 10

interface BattleRepository {
    suspend fun getMatches(
        limit: Int = DEFAULT_PAGE_SIZE,
        page: Int = 1,
        orderBy: String? = null,
        ratedOnly: Boolean? = null,
        formatId: Int? = null
    ): Result<Pair<List<BattleCardUiModel>, Pagination>>
    suspend fun getMatchDetail(id: Int): Result<BattleDetailUiModel>
    suspend fun submitReplay(replayUrl: String): Result<BattleDetailUiModel>
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
        playerName: String? = null,
        playerId: Int? = null,
        team2Filters: List<SearchFilterSlot> = emptyList()
    ): Result<Pair<List<BattleCardUiModel>, Pagination>>
    suspend fun getMatchesByIds(ids: List<Int>): Result<List<BattleCardUiModel>>
    suspend fun getPokemonProfile(id: Int, formatId: Int? = null): Result<PokemonProfile>
    suspend fun getPlayerProfile(id: Int, formatId: Int? = null): Result<PlayerProfile>
    suspend fun getPlayersByNames(names: List<String>): Result<List<PlayerListItem>>
}

@Singleton
class BattleRepositoryImpl @Inject constructor(
    apiService: ApiService
) : BattleRepository {

    internal val shared = SharedBattleRepository(apiService)

    override suspend fun getMatches(
        limit: Int,
        page: Int,
        orderBy: String?,
        ratedOnly: Boolean?,
        formatId: Int?
    ): Result<Pair<List<BattleCardUiModel>, Pagination>> {
        return try {
            val result = shared.getMatches(limit, page, orderBy, ratedOnly, formatId)
            Result.success(result.battles to result.pagination)
        } catch (e: Exception) {
            Result.failure(e)
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
        team2Filters: List<SearchFilterSlot>
    ): Result<Pair<List<BattleCardUiModel>, Pagination>> {
        return try {
            val result = shared.searchMatches(
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
                team2Filters = team2Filters
            )
            Result.success(result.battles to result.pagination)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMatchDetail(id: Int): Result<BattleDetailUiModel> {
        return try {
            Result.success(shared.getMatchDetail(id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun submitReplay(replayUrl: String): Result<BattleDetailUiModel> {
        return try {
            Result.success(shared.submitReplay(replayUrl))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMatchesByIds(ids: List<Int>): Result<List<BattleCardUiModel>> {
        return try {
            Result.success(shared.getMatchesByIds(ids))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPokemonProfile(id: Int, formatId: Int?): Result<PokemonProfile> {
        return try {
            Result.success(shared.getPokemonProfile(id, formatId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPlayerProfile(id: Int, formatId: Int?): Result<PlayerProfile> {
        return try {
            Result.success(shared.getPlayerProfile(id, formatId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPlayersByNames(names: List<String>): Result<List<PlayerListItem>> {
        return try {
            Result.success(shared.getPlayersByNames(names))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
