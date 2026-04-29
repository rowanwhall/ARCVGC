package com.arcvgc.app.ui.model

import com.arcvgc.app.domain.model.WinnerFilter

data class SearchUiState(
    val filterSlots: List<SearchFilterSlotUiModel> = emptyList(),
    val team2FilterSlots: List<SearchFilterSlotUiModel> = emptyList(),
    val selectedFormat: FormatUiModel? = null,
    val userSelectedFormat: Boolean = false,
    val selectedMinRating: Int? = null,
    val selectedMaxRating: Int? = null,
    val unratedOnly: Boolean = false,
    val selectedOrderBy: String = "rating",
    val timeRangeStart: Long? = null,
    val timeRangeEnd: Long? = null,
    val playerName: String = "",
    val winnerFilter: WinnerFilter = WinnerFilter.NONE
) {
    val canAddMoreTeam1: Boolean get() = filterSlots.size < 6
    val canAddMoreTeam2: Boolean get() = team2FilterSlots.size < 6
    val hasTeam2: Boolean get() = team2FilterSlots.isNotEmpty()
}
