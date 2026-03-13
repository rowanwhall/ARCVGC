package com.arcvgc.app.ui.battledetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcvgc.app.data.BattleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BattleDetailViewModel(
    private val repository: BattleRepository,
    battleId: Int
) : ViewModel() {

    private val _state = MutableStateFlow(BattleDetailState())
    val state: StateFlow<BattleDetailState> = _state.asStateFlow()

    init {
        loadBattleDetail(battleId)
    }

    fun loadBattleDetail(battleId: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val detail = repository.getMatchDetail(battleId)
                _state.update {
                    it.copy(
                        isLoading = false,
                        battleDetail = detail,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }
}
