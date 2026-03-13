package com.arcvgc.app.ui.search

import com.arcvgc.app.ui.model.FormatUiModel
import com.arcvgc.app.ui.model.SearchFilterSlotUiModel

data class SearchUiState(
    val filterSlots: List<SearchFilterSlotUiModel> = emptyList(),
    val selectedFormat: FormatUiModel? = null,
    val selectedMinRating: Int? = null,
    val selectedMaxRating: Int? = null,
    val unratedOnly: Boolean = false,
    val selectedOrderBy: String = "rating",
    val timeRangeStart: Long? = null,
    val timeRangeEnd: Long? = null,
    val playerName: String = ""
) {
    val canAddMore: Boolean get() = filterSlots.size < 6
}
