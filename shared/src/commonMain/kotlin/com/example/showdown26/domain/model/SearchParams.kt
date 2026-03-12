package com.example.showdown26.domain.model

data class SearchParams(
    val filters: List<SearchFilterSlot>,
    val formatId: Int,
    val minimumRating: Int? = null,
    val maximumRating: Int? = null,
    val unratedOnly: Boolean = false,
    val orderBy: String,
    val timeRangeStart: Long? = null,
    val timeRangeEnd: Long? = null,
    val playerName: String? = null,
    val formatName: String? = null
) {
    private val hasTimeRange: Boolean get() = timeRangeStart != null && timeRangeEnd != null

    private val isValid: Boolean
        get() = filters.isNotEmpty()
                || minimumRating != null
                || maximumRating != null
                || unratedOnly
                || !playerName.isNullOrBlank()
                || hasTimeRange

    fun canRemovePokemonAt(index: Int): Boolean =
        copy(filters = filters.filterIndexed { i, _ -> i != index }).isValid

    fun removePokemonAt(index: Int): SearchParams =
        copy(filters = filters.filterIndexed { i, _ -> i != index })

    fun canRemoveMinRating(): Boolean =
        copy(minimumRating = null).isValid

    fun removeMinRating(): SearchParams =
        copy(minimumRating = null)

    fun canRemoveMaxRating(): Boolean =
        copy(maximumRating = null).isValid

    fun removeMaxRating(): SearchParams =
        copy(maximumRating = null)

    fun canRemoveUnrated(): Boolean =
        copy(unratedOnly = false).isValid

    fun removeUnrated(): SearchParams =
        copy(unratedOnly = false)

    fun canRemovePlayerName(): Boolean =
        copy(playerName = null).isValid

    fun removePlayerName(): SearchParams =
        copy(playerName = null)

    fun canRemoveTimeRange(): Boolean =
        copy(timeRangeStart = null, timeRangeEnd = null).isValid

    fun removeTimeRange(): SearchParams =
        copy(timeRangeStart = null, timeRangeEnd = null)
}
