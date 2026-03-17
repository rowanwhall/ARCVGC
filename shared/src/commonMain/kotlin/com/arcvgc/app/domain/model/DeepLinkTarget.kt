package com.arcvgc.app.domain.model

sealed class DeepLinkTarget {
    data class Battle(val id: Int) : DeepLinkTarget()
    data class Pokemon(val id: Int) : DeepLinkTarget()
    data class Player(val name: String) : DeepLinkTarget()
}

fun parseDeepLink(path: String): DeepLinkTarget? {
    val segments = path.trimStart('/').split('/')
    return when {
        segments.size == 2 && segments[0] == "battle" ->
            segments[1].toIntOrNull()?.let { DeepLinkTarget.Battle(it) }
        segments.size == 2 && segments[0] == "pokemon" ->
            segments[1].toIntOrNull()?.let { DeepLinkTarget.Pokemon(it) }
        segments.size >= 2 && segments[0] == "player" -> {
            val name = segments.drop(1).joinToString("/")
            name.takeIf { it.isNotBlank() }?.let { DeepLinkTarget.Player(it) }
        }
        else -> null
    }
}
