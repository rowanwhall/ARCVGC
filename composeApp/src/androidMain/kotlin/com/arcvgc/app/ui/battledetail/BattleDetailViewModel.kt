package com.arcvgc.app.ui.battledetail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcvgc.app.data.repository.BattleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BattleDetailViewModel @Inject constructor(
    private val repository: BattleRepository
) : ViewModel() {

    companion object {
        private const val TAG = "BattleDetailViewModel"
    }

    private val _state = MutableStateFlow(BattleDetailState())
    val state: StateFlow<BattleDetailState> = _state.asStateFlow()

    fun loadBattleDetail(battleId: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            repository.getMatchDetail(battleId)
                .onSuccess { detail ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            battleDetail = detail,
                            error = null
                        )
                    }
                }
                .onFailure { throwable ->
                    Log.e(TAG, "Failed to load battle detail (id=$battleId)", throwable)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Unknown error"
                        )
                    }
                }
        }
    }
}
