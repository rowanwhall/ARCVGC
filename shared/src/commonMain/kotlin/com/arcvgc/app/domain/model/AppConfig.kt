package com.arcvgc.app.domain.model

data class AppConfig(
    val defaultFormat: Format,
    val minAndroidVersion: Int,
    val minIosVersion: Int,
    val minWebVersion: Int,
    val minCatalogVersion: Int
)
