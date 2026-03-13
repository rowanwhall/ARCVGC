package com.arcvgc.app.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FormatDto(
    val id: Int,
    val name: String,
    @SerialName("formatted_name") val formattedName: String?
)
