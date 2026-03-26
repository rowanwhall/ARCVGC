package com.arcvgc.app.domain.model

data class MatchPreview(
    val id: Int,
    val showdownId: String,
    val uploadTime: String,
    val rating: Int?,
    val isPrivate: Boolean,
    val format: Format,
    val players: List<PlayerPreview>,
    val setId: String? = null,
    val positionInSet: Int? = null
)
