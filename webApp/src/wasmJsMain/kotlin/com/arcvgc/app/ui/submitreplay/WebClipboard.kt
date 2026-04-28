package com.arcvgc.app.ui.submitreplay

import kotlin.js.Promise
import kotlinx.coroutines.await

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("() => navigator.clipboard.readText().catch(() => '')")
private external fun readClipboardJs(): Promise<JsString>

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
suspend fun readClipboardText(): String? {
    return try {
        val promise: Promise<JsString> = readClipboardJs()
        val text: JsString = promise.await()
        text.toString().takeIf { it.isNotEmpty() }
    } catch (_: Throwable) {
        null
    }
}
