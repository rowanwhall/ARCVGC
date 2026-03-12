package com.example.showdown26.ui.model

import kotlinx.serialization.Serializable

@Serializable
data class TeraTypeUiModel(
    val id: Int,
    val name: String,
    val imageUrl: String?
)
