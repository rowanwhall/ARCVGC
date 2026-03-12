package com.example.showdown26.network.mapper

import com.example.showdown26.domain.model.Format
import com.example.showdown26.network.model.FormatListItemDto

fun FormatListItemDto.toDomain(): Format = Format(
    id = id,
    name = name,
    formattedName = formattedName
)
