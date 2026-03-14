package com.arcvgc.app.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppConfigResponseDto(
    val success: Boolean,
    val data: AppConfigDataDto
)

@Serializable
data class AppConfigDataDto(
    @SerialName("current_format") val currentFormat: FormatListItemDto,
    @SerialName("min_android_version") val minAndroidVersion: Int,
    @SerialName("min_ios_version") val minIosVersion: Int,
    @SerialName("min_web_version") val minWebVersion: Int,
    @SerialName("min_catalog_version") val minCatalogVersion: Int
)
