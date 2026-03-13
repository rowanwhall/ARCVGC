package com.arcvgc.app.network.mapper

import com.arcvgc.app.domain.model.Format
import com.arcvgc.app.network.model.FormatListItemDto

fun FormatListItemDto.toDomain(): Format = Format(
    id = id,
    name = name,
    formattedName = formattedName
)
