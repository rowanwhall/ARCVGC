package com.arcvgc.app.data

import com.arcvgc.app.domain.model.DeepLinkTarget
import com.arcvgc.app.domain.model.NetworkResult
import com.arcvgc.app.domain.model.PlayerListItem
import com.arcvgc.app.domain.model.PokemonListItem
import com.arcvgc.app.network.ApiService

class DeepLinkResolver(private val apiService: ApiService) {

    sealed class ResolvedLink {
        data class Battle(val id: Int) : ResolvedLink()
        data class Pokemon(val item: PokemonListItem) : ResolvedLink()
        data class Player(val item: PlayerListItem) : ResolvedLink()
    }

    suspend fun resolve(target: DeepLinkTarget): ResolvedLink? = when (target) {
        is DeepLinkTarget.Battle -> ResolvedLink.Battle(target.id)
        is DeepLinkTarget.Pokemon -> {
            when (val result = apiService.getPokemonById(target.id)) {
                is NetworkResult.Success -> ResolvedLink.Pokemon(result.data)
                is NetworkResult.Error -> null
            }
        }
        is DeepLinkTarget.Player -> {
            when (val result = apiService.getPlayersByName(target.name)) {
                is NetworkResult.Success -> result.data.firstOrNull()?.let { ResolvedLink.Player(it) }
                is NetworkResult.Error -> null
            }
        }
    }
}
