package com.arcvgc.app.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class LookbackWindow(val value: String, val displayName: String) {
    @SerialName("all") All("all", "All time"),
    @SerialName("30days") ThirtyDays("30days", "Last 30 days"),
    @SerialName("week") Week("week", "This week"),
    @SerialName("day") Day("day", "Today");

    companion object {
        fun fromValue(value: String?): LookbackWindow? =
            value?.let { v -> entries.firstOrNull { it.value == v } }
    }
}
