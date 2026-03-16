package com.arcvgc.app.data.repository

import com.arcvgc.app.domain.model.Pagination
import com.arcvgc.app.domain.model.PlayerListItem
import com.arcvgc.app.domain.model.PlayerProfile
import com.arcvgc.app.domain.model.PokemonListItem
import com.arcvgc.app.domain.model.SearchFilterSlot
import com.arcvgc.app.network.ApiService
import com.arcvgc.app.ui.model.BattleCardUiModel
import com.arcvgc.app.ui.model.BattleDetailUiModel
import com.arcvgc.app.data.BattleRepository as SharedBattleRepository
import javax.inject.Inject
import javax.inject.Singleton

private const val DEFAULT_PAGE_SIZE = 10

interface BattleRepository {
    suspend fun getMatches(limit: Int = DEFAULT_PAGE_SIZE, page: Int = 1): Result<Pair<List<BattleCardUiModel>, Pagination>>
    suspend fun getMatchDetail(id: Int): Result<BattleDetailUiModel>
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
    ): Result<Pair<List<BattleCardUiModel>, Pagination>>
    suspend fun getMatchesByIds(ids: List<Int>): Result<List<BattleCardUiModel>>
    suspend fun getPokemonByIds(ids: List<Int>): Result<List<PokemonListItem>>
    suspend fun getPlayerProfile(id: Int): Result<PlayerProfile>
    suspend fun getPlayersByNames(names: List<String>): Result<List<PlayerListItem>>
}

@Singleton
class BattleRepositoryImpl @Inject constructor(
    apiService: ApiService
) : BattleRepository {

    internal val shared = SharedBattleRepository(apiService)

    override suspend fun getMatches(limit: Int, page: Int): Result<Pair<List<BattleCardUiModel>, Pagination>> {
        return try {
            val result = shared.getMatches(limit, page)
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
        playerName: String?
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
                playerName = playerName
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

    override suspend fun getMatchesByIds(ids: List<Int>): Result<List<BattleCardUiModel>> {
        return try {
            Result.success(shared.getMatchesByIds(ids))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPokemonByIds(ids: List<Int>): Result<List<PokemonListItem>> {
        return try {
            Result.success(shared.getPokemonByIds(ids))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPlayerProfile(id: Int): Result<PlayerProfile> {
        return try {
            Result.success(shared.getPlayerProfile(id))
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
