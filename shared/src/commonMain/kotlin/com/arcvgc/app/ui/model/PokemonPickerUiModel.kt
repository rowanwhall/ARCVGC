package com.arcvgc.app.ui.model

import kotlinx.serialization.Serializable

@Serializable
data class PokemonPickerUiModel(
    val id: Int,
    val name: String,
    val imageUrl: String?,
    val types: List<TypeUiModel>
)
