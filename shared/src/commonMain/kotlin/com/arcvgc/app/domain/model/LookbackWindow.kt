package com.arcvgc.app.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class LookbackWindow(val value: String) {
    @SerialName("all") All("all"),
    @SerialName("week") Week("week"),
    @SerialName("day") Day("day"),
    @SerialName("30days") ThirtyDays("30days");

    companion object {
        fun fromValue(value: String?): LookbackWindow? =
            value?.let { v -> entries.firstOrNull { it.value == v } }
    }
}
