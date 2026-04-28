package com.arcvgc.app.ui.submitreplay

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

data class SubmitReplayState(
    val isSubmitting: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SubmitReplayViewModel @Inject constructor(
    private val repository: BattleRepository
) : ViewModel() {

    companion object {
        private const val TAG = "SubmitReplayViewModel"
    }

    private val _state = MutableStateFlow(SubmitReplayState())
    val state: StateFlow<SubmitReplayState> = _state.asStateFlow()

    fun submit(replayUrl: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null) }
            repository.submitReplay(replayUrl)
                .onSuccess {
                    _state.update { SubmitReplayState() }
                    onSuccess()
                }
                .onFailure { throwable ->
                    Log.e(TAG, "Failed to submit replay", throwable)
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            error = throwable.message ?: "Submission failed"
                        )
                    }
                }
        }
    }

    fun reset() {
        _state.update { SubmitReplayState() }
    }
}
