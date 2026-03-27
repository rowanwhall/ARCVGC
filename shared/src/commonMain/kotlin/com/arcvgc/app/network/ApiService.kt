package com.arcvgc.app.network

import com.arcvgc.app.data.captureException
import com.arcvgc.app.domain.model.Ability
import com.arcvgc.app.domain.model.AppConfig
import com.arcvgc.app.domain.model.DomainItem
import com.arcvgc.app.domain.model.Format
import com.arcvgc.app.domain.model.FormatDetail
import com.arcvgc.app.domain.model.MatchDetail
import com.arcvgc.app.domain.model.MatchPreview
import com.arcvgc.app.domain.model.MatchSet
import com.arcvgc.app.domain.model.NetworkResult
import com.arcvgc.app.domain.model.Pagination
import com.arcvgc.app.domain.model.SetDetail
import com.arcvgc.app.domain.model.PlayerListItem
import com.arcvgc.app.domain.model.PlayerProfile
import com.arcvgc.app.domain.model.PokemonListItem
import com.arcvgc.app.domain.model.PokemonProfile
import com.arcvgc.app.domain.model.TeraType
import com.arcvgc.app.network.mapper.toDomain
import com.arcvgc.app.network.model.AbilityListResponseDto
import com.arcvgc.app.network.model.AppConfigResponseDto
import com.arcvgc.app.network.model.FormatDetailResponseDto
import com.arcvgc.app.network.model.FormatListResponseDto
import com.arcvgc.app.network.model.ItemListResponseDto
import com.arcvgc.app.network.model.MatchDetailResponseDto
import com.arcvgc.app.network.model.MatchesResponseDto
import com.arcvgc.app.network.model.PlayerListResponseDto
import com.arcvgc.app.network.model.PlayerProfileResponseDto
import com.arcvgc.app.network.model.PokemonDetailResponseDto
import com.arcvgc.app.network.model.PokemonListResponseDto
import com.arcvgc.app.network.model.SetDetailResponseDto
import com.arcvgc.app.network.model.SetListResponseDto
import com.arcvgc.app.network.model.TeraTypeListResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import com.arcvgc.app.network.model.SearchRequestDto
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class ApiService {

    private val client: HttpClient = createPlatformHttpClient().config {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                explicitNulls = false
            })
        }
    }

    suspend fun getMatches(
        limit: Int = 50,
        page: Int = 1,
        orderBy: String? = null,
        ratedOnly: Boolean? = null,
        formatId: Int? = null
    ): NetworkResult<Pair<List<MatchPreview>, Pagination>> {
        return try {
            val response: MatchesResponseDto = client
                .get("${ApiConstants.BASE_URL}${ApiConstants.MATCHES_ENDPOINT}") {
                    parameter("limit", limit)
                    parameter("page", page)
                    orderBy?.let { parameter("order_by", it) }
                    ratedOnly?.let { parameter("rated_only", it) }
                    formatId?.let { parameter("format_id", it) }
                }
                .body()

            if (response.success) {
                NetworkResult.Success(
                    response.data.toDomain() to response.pagination.toDomain()
                )
            } else {
                NetworkResult.Error("API returned success=false")
            }
        } catch (e: Exception) {
            captureException(e)
            NetworkResult.Error(e.message ?: "Unknown error", e)
        }
    }

    suspend fun getPokemonList(
        limit: Int = 50,
        page: Int = 1
    ): NetworkResult<Pair<List<PokemonListItem>, Pagination>> {
        return try {
            val response: PokemonListResponseDto = client
                .get("${ApiConstants.BASE_URL}${ApiConstants.POKEMON_ENDPOINT}") {
                    parameter("exclude_illegal", true)
                    parameter("limit", limit)
                    parameter("page", page)
                }
                .body()

            if (response.success) {
                NetworkResult.Success(
                    response.data.map { it.toDomain() } to response.pagination.toDomain()
                )
            } else {
                NetworkResult.Error("API returned success=false")
            }
        } catch (e: Exception) {
            captureException(e)
            NetworkResult.Error(e.message ?: "Unknown error", e)
        }
    }

    suspend fun getItems(
        limit: Int = 50,
        page: Int = 1
    ): NetworkResult<Pair<List<DomainItem>, Pagination>> {
        return try {
            val response: ItemListResponseDto = client
                .get("${ApiConstants.BASE_URL}${ApiConstants.ITEMS_ENDPOINT}") {
                    parameter("limit", limit)
                    parameter("page", page)
                }
                .body()

            if (response.success) {
                NetworkResult.Success(
                    response.data.map { it.toDomain() } to response.pagination.toDomain()
                )
            } else {
                NetworkResult.Error("API returned success=false")
            }
        } catch (e: Exception) {
            captureException(e)
            NetworkResult.Error(e.message ?: "Unknown error", e)
        }
    }

    suspend fun getAbilities(
        limit: Int = 50,
        page: Int = 1
    ): NetworkResult<Pair<List<Ability>, Pagination>> {
        return try {
            val response: AbilityListResponseDto = client
                .get("${ApiConstants.BASE_URL}${ApiConstants.ABILITIES_ENDPOINT}") {
                    parameter("limit", limit)
                    parameter("page", page)
                }
                .body()

            if (response.success) {
                NetworkResult.Success(
                    response.data.map { it.toDomain() } to response.pagination.toDomain()
                )
            } else {
                NetworkResult.Error("API returned success=false")
            }
        } catch (e: Exception) {
            captureException(e)
            NetworkResult.Error(e.message ?: "Unknown error", e)
        }
    }

    suspend fun getTeraTypes(
        limit: Int = 50,
        page: Int = 1
    ): NetworkResult<Pair<List<TeraType>, Pagination>> {
        return try {
            val response: TeraTypeListResponseDto = client
                .get("${ApiConstants.BASE_URL}${ApiConstants.TERA_TYPES_ENDPOINT}") {
                    parameter("limit", limit)
                    parameter("page", page)
                }
                .body()

            if (response.success) {
                NetworkResult.Success(
                    response.data.mapNotNull { it.toDomain() } to response.pagination.toDomain()
                )
            } else {
                NetworkResult.Error("API returned success=false")
            }
        } catch (e: Exception) {
            captureException(e)
            NetworkResult.Error(e.message ?: "Unknown error", e)
        }
    }

    suspend fun getFormats(
        limit: Int = 50,
        page: Int = 1
    ): NetworkResult<Pair<List<Format>, Pagination>> {
        return try {
            val response: FormatListResponseDto = client
                .get("${ApiConstants.BASE_URL}${ApiConstants.FORMATS_ENDPOINT}") {
                    parameter("limit", limit)
                    parameter("page", page)
                }
                .body()

            if (response.success) {
                NetworkResult.Success(
                    response.data.map { it.toDomain() } to response.pagination.toDomain()
                )
            } else {
                NetworkResult.Error("API returned success=false")
            }
        } catch (e: Exception) {
            captureException(e)
            NetworkResult.Error(e.message ?: "Unknown error", e)
        }
    }

    suspend fun getFormatDetail(
        formatId: Int,
        topPokemonCount: Int? = null
    ): NetworkResult<FormatDetail> {
        return try {
            val response: FormatDetailResponseDto = client
                .get("${ApiConstants.BASE_URL}${ApiConstants.FORMATS_ENDPOINT}$formatId") {
                    topPokemonCount?.let { parameter("top_pokemon_count", it) }
                }
                .body()

            if (response.success) {
                NetworkResult.Success(response.data.toDomain())
            } else {
                NetworkResult.Error("Format not found")
            }
        } catch (e: Exception) {
            captureException(e)
            NetworkResult.Error(e.message ?: "Unknown error", e)
        }
    }

    suspend fun searchMatches(request: SearchRequestDto): NetworkResult<Pair<List<MatchPreview>, Pagination>> {
        return try {
            val response: MatchesResponseDto = client
                .post("${ApiConstants.BASE_URL}${ApiConstants.SEARCH_ENDPOINT}") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
                .body()

            if (response.success) {
                NetworkResult.Success(
                    response.data.toDomain() to response.pagination.toDomain()
                )
            } else {
                NetworkResult.Error("API returned success=false")
            }
        } catch (e: Exception) {
            captureException(e)
            NetworkResult.Error(e.message ?: "Unknown error", e)
        }
    }

    suspend fun getPokemonById(id: Int, formatId: Int? = null): NetworkResult<PokemonProfile> {
        return try {
            val response: PokemonDetailResponseDto = client
                .get("${ApiConstants.BASE_URL}${ApiConstants.POKEMON_ENDPOINT}$id") {
                    formatId?.let { parameter("format_id", it) }
                }
                .body()

            if (response.success) {
                NetworkResult.Success(response.data.toDomain())
            } else {
                NetworkResult.Error("Pokémon not found")
            }
        } catch (e: Exception) {
            captureException(e)
            NetworkResult.Error(e.message ?: "Unknown error", e)
        }
    }

    suspend fun getMatchDetail(id: Int): NetworkResult<MatchDetail> {
        return try {
            val response: MatchDetailResponseDto = client
                .get("${ApiConstants.BASE_URL}${ApiConstants.MATCHES_ENDPOINT}$id")
                .body()

            if (response.success && !response.data.isNullOrEmpty()) {
                NetworkResult.Success(response.data.first().toDomain())
            } else {
                NetworkResult.Error("Match not found")
            }
        } catch (e: Exception) {
            captureException(e)
            NetworkResult.Error(e.message ?: "Unknown error", e)
        }
    }

    suspend fun getPlayerById(id: Int, formatId: Int? = null): NetworkResult<PlayerProfile> {
        return try {
            val response: PlayerProfileResponseDto = client
                .get("${ApiConstants.BASE_URL}${ApiConstants.PLAYERS_ENDPOINT}$id") {
                    formatId?.let { parameter("format_id", it) }
                }
                .body()

            if (response.success) {
                NetworkResult.Success(response.data.toDomain())
            } else {
                NetworkResult.Error("Player not found")
            }
        } catch (e: Exception) {
            captureException(e)
            NetworkResult.Error(e.message ?: "Unknown error", e)
        }
    }

    suspend fun getConfig(): NetworkResult<AppConfig> {
        return try {
            val response: AppConfigResponseDto = client
                .get("${ApiConstants.BASE_URL}${ApiConstants.CONFIG_ENDPOINT}")
                .body()

            if (response.success) {
                NetworkResult.Success(response.data.toDomain())
            } else {
                NetworkResult.Error("API returned success=false")
            }
        } catch (e: Exception) {
            captureException(e)
            NetworkResult.Error(e.message ?: "Unknown error", e)
        }
    }

    suspend fun getPlayersByName(name: String): NetworkResult<List<PlayerListItem>> {
        return try {
            val response: PlayerListResponseDto = client
                .get("${ApiConstants.BASE_URL}${ApiConstants.PLAYERS_ENDPOINT}") {
                    parameter("name", name)
                    parameter("limit", 50)
                    parameter("page", 1)
                }
                .body()

            if (response.success) {
                NetworkResult.Success(
                    response.data.map { PlayerListItem(id = it.id, name = it.name) }
                )
            } else {
                NetworkResult.Error("API returned success=false")
            }
        } catch (e: Exception) {
            captureException(e)
            NetworkResult.Error(e.message ?: "Unknown error", e)
        }
    }

    suspend fun getSets(
        limit: Int = 20,
        page: Int = 1,
        orderBy: String? = null,
        completeOnly: Boolean? = null,
        ratedOnly: Boolean? = null,
        formatId: Int? = null
    ): NetworkResult<Pair<List<MatchSet>, Pagination>> {
        return try {
            val response: SetListResponseDto = client
                .get("${ApiConstants.BASE_URL}${ApiConstants.SETS_ENDPOINT}") {
                    parameter("limit", limit)
                    parameter("page", page)
                    orderBy?.let { parameter("order_by", it) }
                    completeOnly?.let { parameter("complete_only", it) }
                    ratedOnly?.let { parameter("rated_only", it) }
                    formatId?.let { parameter("format_id", it) }
                }
                .body()

            if (response.success) {
                NetworkResult.Success(
                    response.data.map { it.toDomain() } to response.pagination.toDomain()
                )
            } else {
                NetworkResult.Error("API returned success=false")
            }
        } catch (e: Exception) {
            captureException(e)
            NetworkResult.Error(e.message ?: "Unknown error", e)
        }
    }

    suspend fun getSetDetail(setId: Int): NetworkResult<SetDetail> {
        return try {
            val response: SetDetailResponseDto = client
                .get("${ApiConstants.BASE_URL}${ApiConstants.SETS_ENDPOINT}$setId")
                .body()

            if (response.success && response.data.isNotEmpty()) {
                NetworkResult.Success(response.data.first().toDomain())
            } else {
                NetworkResult.Error("Set not found")
            }
        } catch (e: Exception) {
            captureException(e)
            NetworkResult.Error(e.message ?: "Unknown error", e)
        }
    }
}
