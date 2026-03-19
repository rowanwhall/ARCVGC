package com.arcvgc.app.domain.model

data class DeepLink(
    val target: DeepLinkTarget,
    val battleId: Int? = null
)

sealed class DeepLinkTarget {
    data object Home : DeepLinkTarget()
    data class Pokemon(val id: Int) : DeepLinkTarget()
    data class Player(val name: String) : DeepLinkTarget()
    data class Favorites(val contentType: String) : DeepLinkTarget()
    data class Search(val params: SearchQueryParams) : DeepLinkTarget()
    data object SearchTab : DeepLinkTarget()
    data object SettingsTab : DeepLinkTarget()
}

data class SearchQueryParams(
    val pokemonIds: List<Int>,
    val itemIds: List<Int?> = emptyList(),
    val teraTypeIds: List<Int?> = emptyList(),
    val formatId: Int,
    val minimumRating: Int? = null,
    val maximumRating: Int? = null,
    val unratedOnly: Boolean = false,
    val orderBy: String = "rating",
    val timeRangeStart: Long? = null,
    val timeRangeEnd: Long? = null,
    val playerName: String? = null
)

private val VALID_FAVORITES_TYPES = setOf("battles", "pokemon", "players")

fun parseDeepLink(path: String): DeepLink? {
    // Split path and query string
    val questionIndex = path.indexOf('?')
    val pathPart = if (questionIndex >= 0) path.substring(0, questionIndex) else path
    val queryString = if (questionIndex >= 0) path.substring(questionIndex + 1) else null

    // Extract battle param from query string
    val queryParams = queryString?.let { parseQueryParams(it) } ?: emptyMap()
    val battleId = queryParams["battle"]?.toIntOrNull()

    val segments = pathPart.trimStart('/').split('/')
    val target = when {
        // /battle/{id} → backwards compat: Home root with battle ID
        segments.size == 2 && segments[0] == "battle" -> {
            val id = segments[1].toIntOrNull() ?: return null
            return DeepLink(target = DeepLinkTarget.Home, battleId = id)
        }
        segments.size == 2 && segments[0] == "pokemon" ->
            segments[1].toIntOrNull()?.let { DeepLinkTarget.Pokemon(it) }
        segments.size >= 2 && segments[0] == "player" -> {
            val name = segments.drop(1).joinToString("/")
            name.takeIf { it.isNotBlank() }?.let { DeepLinkTarget.Player(it) }
        }
        segments.size == 2 && segments[0] == "favorites" && segments[1] in VALID_FAVORITES_TYPES ->
            DeepLinkTarget.Favorites(segments[1])
        segments.size == 1 && segments[0] == "search" && queryParams.containsKey("p") ->
            parseSearchQuery(queryParams)
        segments.size == 1 && segments[0] == "search" ->
            DeepLinkTarget.SearchTab
        segments.size == 1 && segments[0] == "settings" ->
            DeepLinkTarget.SettingsTab
        // Root path (/) or /?battle=X
        segments.size == 1 && segments[0].isEmpty() ->
            DeepLinkTarget.Home
        else -> null
    } ?: return null

    return DeepLink(target = target, battleId = battleId)
}

private fun parseSearchQuery(params: Map<String, String>): DeepLinkTarget.Search? {
    val pokemonIds = params["p"]?.split(",")?.mapNotNull { it.toIntOrNull() }
    if (pokemonIds.isNullOrEmpty()) return null

    val formatId = params["f"]?.toIntOrNull() ?: return null

    val itemIds = params["i"]?.split(",")?.map { it.toIntOrNull() } ?: emptyList()
    val teraTypeIds = params["t"]?.split(",")?.map { it.toIntOrNull() } ?: emptyList()

    return DeepLinkTarget.Search(
        SearchQueryParams(
            pokemonIds = pokemonIds,
            itemIds = itemIds,
            teraTypeIds = teraTypeIds,
            formatId = formatId,
            minimumRating = params["min"]?.toIntOrNull(),
            maximumRating = params["max"]?.toIntOrNull(),
            unratedOnly = params.containsKey("unrated"),
            orderBy = params["order"] ?: "rating",
            timeRangeStart = params["start"]?.toLongOrNull(),
            timeRangeEnd = params["end"]?.toLongOrNull(),
            playerName = params["player"]
        )
    )
}

private fun parseQueryParams(queryString: String): Map<String, String> {
    return queryString.split("&").mapNotNull { part ->
        val eqIndex = part.indexOf('=')
        if (eqIndex > 0) {
            val key = part.substring(0, eqIndex)
            val value = decodePercent(part.substring(eqIndex + 1))
            key to value
        } else if (part.isNotBlank()) {
            part to ""
        } else null
    }.toMap()
}

private fun decodePercent(value: String): String {
    val sb = StringBuilder()
    var i = 0
    while (i < value.length) {
        if (value[i] == '%' && i + 2 < value.length) {
            val hex = value.substring(i + 1, i + 3)
            val code = hex.toIntOrNull(16)
            if (code != null) {
                sb.append(code.toChar())
                i += 3
                continue
            }
        } else if (value[i] == '+') {
            sb.append(' ')
            i++
            continue
        }
        sb.append(value[i])
        i++
    }
    return sb.toString()
}

fun encodeSearchPath(params: SearchParams): String {
    val parts = mutableListOf<String>()

    val pokemonIds = params.filters.joinToString(",") { it.pokemonId.toString() }
    parts.add("p=$pokemonIds")

    val itemIds = params.filters.joinToString(",") { slot ->
        slot.itemId?.toString() ?: "_"
    }
    if (params.filters.any { it.itemId != null }) {
        parts.add("i=$itemIds")
    }

    val teraTypeIds = params.filters.joinToString(",") { slot ->
        slot.teraTypeId?.toString() ?: "_"
    }
    if (params.filters.any { it.teraTypeId != null }) {
        parts.add("t=$teraTypeIds")
    }

    parts.add("f=${params.formatId}")

    params.minimumRating?.takeIf { it > 0 }?.let { parts.add("min=$it") }
    params.maximumRating?.takeIf { it > 0 }?.let { parts.add("max=$it") }
    if (params.unratedOnly) parts.add("unrated")
    parts.add("order=${params.orderBy}")
    params.timeRangeStart?.let { parts.add("start=$it") }
    params.timeRangeEnd?.let { parts.add("end=$it") }
    params.playerName?.takeIf { it.isNotBlank() }?.let { parts.add("player=${encodePercent(it)}") }

    return "/search?${parts.joinToString("&")}"
}

fun appendBattleParam(basePath: String, battleId: Int?): String {
    if (battleId == null) return basePath
    val separator = if ('?' in basePath) "&" else "?"
    return "${basePath}${separator}battle=$battleId"
}

internal fun encodePercent(value: String): String {
    val sb = StringBuilder()
    for (c in value) {
        when {
            c.isLetterOrDigit() || c in "-_.~" -> sb.append(c)
            c == ' ' -> sb.append('+')
            else -> {
                val byte = c.code
                sb.append('%')
                sb.append(byte.toString(16).uppercase().padStart(2, '0'))
            }
        }
    }
    return sb.toString()
}
