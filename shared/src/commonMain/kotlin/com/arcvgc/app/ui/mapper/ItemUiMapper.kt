package com.arcvgc.app.ui.mapper

import com.arcvgc.app.domain.model.DomainItem
import com.arcvgc.app.ui.model.ItemUiModel

object ItemUiMapper {

    fun map(item: DomainItem): ItemUiModel {
        return ItemUiModel(
            id = item.id,
            name = item.name,
            imageUrl = item.imageUrl
        )
    }

    fun mapList(items: List<DomainItem>): List<ItemUiModel> {
        return items.map { map(it) }
    }
}
