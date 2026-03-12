package com.example.showdown26.ui.battledetail

import com.example.showdown26.ui.model.BattleDetailUiModel

data class BattleDetailState(
    val isLoading: Boolean = false,
    val battleDetail: BattleDetailUiModel? = null,
    val error: String? = null
)
