package com.arcvgc.app.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class OrderBy(val value: String) {
    @SerialName("time") Time("time"),
    @SerialName("rating") Rating("rating");

    companion object {
        fun fromValue(value: String?): OrderBy? =
            value?.let { v -> entries.firstOrNull { it.value == v } }
    }
}
