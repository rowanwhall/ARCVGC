package com.arcvgc.app.network.mapper

import com.arcvgc.app.domain.model.DomainItem
import com.arcvgc.app.network.normalizeImageUrl
import com.arcvgc.app.network.model.ItemListItemDto

fun ItemListItemDto.toDomain(): DomainItem {
    return DomainItem(
        id = id,
        name = name,
        imageUrl = normalizeImageUrl(imageUrl)
    )
}
