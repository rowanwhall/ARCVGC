package com.arcvgc.app.ui.submitreplay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.arcvgc.app.di.DependencyContainer
import com.arcvgc.app.ui.components.SubmitReplayDialog
import com.arcvgc.app.ui.rememberViewModel

@Composable
fun SubmitReplayDialogHost(onDismiss: () -> Unit) {
    val viewModel = rememberViewModel("submit_replay") {
        SubmitReplayViewModel(DependencyContainer.battleRepository)
    }
    val state by viewModel.state.collectAsState()

    SubmitReplayDialog(
        onDismiss = {
            viewModel.reset()
            onDismiss()
        },
        onSubmit = { url ->
            viewModel.submit(url) { onDismiss() }
        },
        isSubmitting = state.isSubmitting,
        error = state.error,
        hasClipboardText = true,
        onPasteClipboard = { readClipboardText() }
    )
}
