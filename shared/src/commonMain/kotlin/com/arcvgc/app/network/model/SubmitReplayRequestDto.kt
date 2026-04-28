package com.arcvgc.app.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubmitReplayRequestDto(
    @SerialName("replay_url") val replayUrl: String
)
