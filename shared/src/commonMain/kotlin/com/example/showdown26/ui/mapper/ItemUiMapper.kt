package com.example.showdown26.ui.mapper

import com.example.showdown26.domain.model.DomainItem
import com.example.showdown26.ui.model.ItemUiModel

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
