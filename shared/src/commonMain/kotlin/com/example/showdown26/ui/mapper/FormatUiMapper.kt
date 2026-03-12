package com.example.showdown26.ui.mapper

import com.example.showdown26.domain.model.Format
import com.example.showdown26.ui.model.FormatUiModel

object FormatUiMapper {

    fun map(format: Format): FormatUiModel {
        return FormatUiModel(
            id = format.id,
            displayName = format.formattedName ?: format.name
        )
    }

    fun mapList(formats: List<Format>): List<FormatUiModel> {
        return formats.map { map(it) }
    }
}
