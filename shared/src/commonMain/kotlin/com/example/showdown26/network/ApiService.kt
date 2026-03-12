package com.example.showdown26.network

import com.example.showdown26.domain.model.DomainItem
import com.example.showdown26.domain.model.Format
import com.example.showdown26.domain.model.MatchDetail
import com.example.showdown26.domain.model.MatchPreview
import com.example.showdown26.domain.model.NetworkResult
import com.example.showdown26.domain.model.Pagination
import com.example.showdown26.domain.model.PlayerListItem
import com.example.showdown26.domain.model.PlayerProfile
import com.example.showdown26.domain.model.PokemonListItem
import com.example.showdown26.domain.model.TeraType
import com.example.showdown26.network.mapper.toDomain
import com.example.showdown26.network.model.FormatListResponseDto
import com.example.showdown26.network.model.ItemListResponseDto
import com.example.showdown26.network.model.MatchDetailResponseDto
import com.example.showdown26.network.model.MatchesResponseDto
import com.example.showdown26.network.model.PlayerListResponseDto
import com.example.showdown26.network.model.PlayerProfileResponseDto
import com.example.showdown26.network.model.PokemonDetailResponseDto
import com.example.showdown26.network.model.PokemonListResponseDto
import com.example.showdown26.network.model.TeraTypeListResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import com.example.showdown26.network.model.SearchRequestDto
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
            })
        }
    }

    suspend fun getMatches(
        limit: Int = 50,
        page: Int = 1
    ): NetworkResult<Pair<List<MatchPreview>, Pagination>> {
        return try {
            val response: MatchesResponseDto = client
                .get("${ApiConstants.BASE_URL}${ApiConstants.MATCHES_ENDPOINT}") {
                    parameter("limit", limit)
                    parameter("page", page)
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
                    response.data.map { it.toDomain() } to response.pagination.toDomain()
                )
            } else {
                NetworkResult.Error("API returned success=false")
            }
        } catch (e: Exception) {
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
            NetworkResult.Error(e.message ?: "Unknown error", e)
        }
    }

    suspend fun getPokemonById(id: Int): NetworkResult<PokemonListItem> {
        return try {
            val response: PokemonDetailResponseDto = client
                .get("${ApiConstants.BASE_URL}${ApiConstants.POKEMON_ENDPOINT}$id")
                .body()

            if (response.success) {
                NetworkResult.Success(response.data.toDomain())
            } else {
                NetworkResult.Error("Pokémon not found")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown error", e)
        }
    }

    suspend fun getMatchDetail(id: Int): NetworkResult<MatchDetail> {
        return try {
            val response: MatchDetailResponseDto = client
                .get("${ApiConstants.BASE_URL}${ApiConstants.MATCHES_ENDPOINT}$id")
                .body()

            if (response.success && response.data.isNotEmpty()) {
                NetworkResult.Success(response.data.first().toDomain())
            } else {
                NetworkResult.Error("Match not found")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown error", e)
        }
    }

    suspend fun getPlayerById(id: Int): NetworkResult<PlayerProfile> {
        return try {
            val response: PlayerProfileResponseDto = client
                .get("${ApiConstants.BASE_URL}${ApiConstants.PLAYERS_ENDPOINT}$id")
                .body()

            if (response.success) {
                NetworkResult.Success(response.data.toDomain())
            } else {
                NetworkResult.Error("Player not found")
            }
        } catch (e: Exception) {
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
            NetworkResult.Error(e.message ?: "Unknown error", e)
        }
    }
}
