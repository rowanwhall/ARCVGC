package com.arcvgc.app.ui.submitreplay

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arcvgc.app.ui.components.SubmitReplayDialog

@Composable
fun SubmitReplayDialogHost(
    onDismiss: () -> Unit,
    viewModel: SubmitReplayViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboard = remember(context) {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }
    var hasClipboardText by remember { mutableStateOf(clipboard.hasReadableText()) }

    DisposableEffect(clipboard) {
        val listener = ClipboardManager.OnPrimaryClipChangedListener {
            hasClipboardText = clipboard.hasReadableText()
        }
        clipboard.addPrimaryClipChangedListener(listener)
        onDispose { clipboard.removePrimaryClipChangedListener(listener) }
    }

    SubmitReplayDialog(
        onDismiss = {
            viewModel.reset()
            onDismiss()
        },
        onSubmit = { url ->
            viewModel.submit(url) {
                onDismiss()
            }
        },
        isSubmitting = state.isSubmitting,
        error = state.error,
        hasClipboardText = hasClipboardText,
        onPasteClipboard = {
            clipboard.primaryClip?.getItemAt(0)?.text?.toString()
        }
    )
}

private fun ClipboardManager.hasReadableText(): Boolean {
    if (!hasPrimaryClip()) return false
    val description = primaryClipDescription ?: return false
    return description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) ||
        description.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML)
}
