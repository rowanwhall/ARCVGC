package com.arcvgc.app.ui.battledetail

import com.arcvgc.app.ui.model.BattleDetailUiModel

data class BattleDetailState(
    val isLoading: Boolean = false,
    val battleDetail: BattleDetailUiModel? = null,
    val error: String? = null
)
