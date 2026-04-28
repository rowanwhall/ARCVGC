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

fun List<FormatUiModel>.excludeHistoric(keepId: Int? = null): List<FormatUiModel> =
    filter { !it.isHistoric || it.id == keepId }
