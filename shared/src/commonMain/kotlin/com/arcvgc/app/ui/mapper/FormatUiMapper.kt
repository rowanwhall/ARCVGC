package com.arcvgc.app.ui.mapper

import com.arcvgc.app.domain.model.Format
import com.arcvgc.app.ui.model.FormatUiModel

object FormatUiMapper {

    fun map(format: Format): FormatUiModel {
        return FormatUiModel(
            id = format.id,
            displayName = format.formattedName ?: format.name,
            isHistoric = format.isHistoric,
            isOpenTeamsheet = format.isOpenTeamsheet,
            isOfficial = format.isOfficial,
            hasSeries = format.hasSeries
        )
    }

    fun mapList(formats: List<Format>): List<FormatUiModel> {
        return formats.map { map(it) }
    }
}
