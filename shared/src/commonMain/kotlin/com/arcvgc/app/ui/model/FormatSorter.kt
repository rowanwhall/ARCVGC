package com.arcvgc.app.ui.model

object FormatSorter {

    fun sorted(formats: List<FormatUiModel>, defaultFormatId: Int?): List<FormatUiModel> {
        val sorted = formats.sortedByDescending { it.id }
        if (defaultFormatId == null) return sorted
        val defaultFormat = sorted.find { it.id == defaultFormatId } ?: return sorted
        val rest = sorted.filter { it.id != defaultFormat.id }
        val (matching, other) = rest.partition { it.isOfficial == defaultFormat.isOfficial }
        return listOf(defaultFormat) + matching + other
    }
}
