package com.arcvgc.app.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SetMatchDto(
    val id: Int,
    @SerialName("showdown_id") val showdownId: String,
    @SerialName("position_in_set") val positionInSet: Int? = null
)
