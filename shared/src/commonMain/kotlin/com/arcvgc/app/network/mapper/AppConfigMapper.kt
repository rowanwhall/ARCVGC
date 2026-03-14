package com.arcvgc.app.network.mapper

import com.arcvgc.app.domain.model.AppConfig
import com.arcvgc.app.network.model.AppConfigDataDto

fun AppConfigDataDto.toDomain(): AppConfig = AppConfig(
    defaultFormat = currentFormat.toDomain(),
    minAndroidVersion = minAndroidVersion,
    minIosVersion = minIosVersion,
    minWebVersion = minWebVersion,
    minCatalogVersion = minCatalogVersion
)
