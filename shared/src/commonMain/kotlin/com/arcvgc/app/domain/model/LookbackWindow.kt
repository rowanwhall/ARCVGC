package com.arcvgc.app.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class LookbackWindow(val value: String, val displayName: String) {
    @SerialName("all") All("all", "All"),
    @SerialName("30days") ThirtyDays("30days", "30 days"),
    @SerialName("week") Week("week", "Week"),
    @SerialName("day") Day("day", "Today");

    companion object {
        fun fromValue(value: String?): LookbackWindow? =
            value?.let { v -> entries.firstOrNull { it.value == v } }
    }
}
