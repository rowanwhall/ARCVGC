package com.arcvgc.app.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class LookbackWindow(val value: String, val displayName: String, val durationSeconds: Long?) {
    @SerialName("all") All("all", "All", null),
    @SerialName("30days") ThirtyDays("30days", "30 days", 30L * 86_400L),
    @SerialName("week") Week("week", "7 days", 7L * 86_400L),
    @SerialName("day") Day("day", "24 hours", 86_400L);

    /**
     * Returns `(timeRangeStart, timeRangeEnd)` epoch seconds for a time-bounded
     * search that ends at [nowSeconds] and reaches back by [durationSeconds].
     * Returns `(null, null)` for [All] — caller passes nulls to disable the
     * time-range filter in `searchMatches`.
     */
    fun timeRangeSecondsEndingAt(nowSeconds: Long): Pair<Long?, Long?> =
        durationSeconds?.let { (nowSeconds - it) to nowSeconds } ?: (null to null)

    companion object {
        fun fromValue(value: String?): LookbackWindow? =
            value?.let { v -> entries.firstOrNull { it.value == v } }

        /**
         * Lookback options offered on the Home page. Excludes [All] because the
         * `/pokemon/usage` endpoint feeding the Home mover sections returns 500
         * for `all`. [Week] is the middle (default) option.
         */
        val homeOptions: List<LookbackWindow> = listOf(ThirtyDays, Week, Day)
    }
}
