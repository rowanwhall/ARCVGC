package com.arcvgc.app.ui.mapper

import com.arcvgc.app.domain.model.TeraType
import com.arcvgc.app.ui.model.TeraTypeUiModel

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
