package com.arcvgc.app.ui.submitreplay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcvgc.app.data.BattleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SubmitReplayState(
    val isSubmitting: Boolean = false,
    val error: String? = null
)

class SubmitReplayViewModel(
    private val repository: BattleRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SubmitReplayState())
    val state: StateFlow<SubmitReplayState> = _state.asStateFlow()

    fun submit(replayUrl: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null) }
            try {
                repository.submitReplay(replayUrl)
                _state.update { SubmitReplayState() }
                onSuccess()
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        error = e.message ?: "Submission failed"
                    )
                }
            }
        }
    }

    fun reset() {
        _state.update { SubmitReplayState() }
    }
}
