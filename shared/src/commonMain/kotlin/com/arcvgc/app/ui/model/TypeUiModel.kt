package com.arcvgc.app.ui.model

import kotlinx.serialization.Serializable

@Serializable
data class TypeUiModel(
    val name: String,
    val imageUrl: String?
)
