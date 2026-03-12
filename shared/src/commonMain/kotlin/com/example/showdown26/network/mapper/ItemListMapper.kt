package com.example.showdown26.network.mapper

import com.example.showdown26.domain.model.DomainItem
import com.example.showdown26.network.normalizeImageUrl
import com.example.showdown26.network.model.ItemListItemDto

fun ItemListItemDto.toDomain(): DomainItem {
    return DomainItem(
        id = id,
        name = name,
        imageUrl = normalizeImageUrl(imageUrl)
    )
}
