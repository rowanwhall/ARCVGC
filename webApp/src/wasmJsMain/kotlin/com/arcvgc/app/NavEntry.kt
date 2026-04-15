package com.arcvgc.app

import com.arcvgc.app.domain.model.encodeTopPokemonPath
import com.arcvgc.app.ui.BattleOverlayRequest

internal sealed class NavEntry {
    data class BattleDetail(val request: BattleOverlayRequest) : NavEntry()
    data class Pokemon(
        val id: Int,
        val name: String,
        val imageUrl: String?,
        val typeImageUrls: List<String> = emptyList(),
        val formatId: Int? = null
    ) : NavEntry()
    data class Player(val id: Int, val name: String, val formatId: Int? = null) : NavEntry()
    data class TopPokemon(val formatId: Int? = null) : NavEntry()
}

internal fun navEntryToPath(entry: NavEntry): String = when (entry) {
    is NavEntry.BattleDetail -> "/battle/${entry.request.battleId}"
    is NavEntry.Pokemon -> "/pokemon/${entry.id}"
    is NavEntry.Player -> "/player/${entry.name}"
    is NavEntry.TopPokemon -> encodeTopPokemonPath(entry.formatId)
}
