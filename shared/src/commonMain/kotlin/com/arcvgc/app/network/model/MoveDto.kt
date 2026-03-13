package com.arcvgc.app.network.model

import kotlinx.serialization.Serializable

@Serializable
data class MoveDto(
    val id: Int,
    val name: String
)
