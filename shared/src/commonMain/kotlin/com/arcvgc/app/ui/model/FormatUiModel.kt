package com.arcvgc.app.ui.model

import kotlinx.serialization.Serializable

@Serializable
data class FormatUiModel(
    val id: Int,
    val displayName: String,
    val isHistoric: Boolean = false,
    val isOpenTeamsheet: Boolean = false,
    val isOfficial: Boolean = false,
    val hasSeries: Boolean = false
)
