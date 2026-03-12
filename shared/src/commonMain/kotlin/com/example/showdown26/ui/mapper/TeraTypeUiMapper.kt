package com.example.showdown26.ui.mapper

import com.example.showdown26.domain.model.TeraType
import com.example.showdown26.ui.model.TeraTypeUiModel

object TeraTypeUiMapper {

    fun map(teraType: TeraType): TeraTypeUiModel {
        return TeraTypeUiModel(
            id = teraType.id,
            name = teraType.name,
            imageUrl = teraType.imageUrl
        )
    }

    fun mapList(teraTypes: List<TeraType>): List<TeraTypeUiModel> {
        return teraTypes.map { map(it) }
    }
}
