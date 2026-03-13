package com.arcvgc.app.ui.model

import kotlinx.serialization.Serializable

@Serializable
data class ItemUiModel(
    val id: Int,
    val name: String,
    val imageUrl: String?
)
