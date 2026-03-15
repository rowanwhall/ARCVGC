package com.arcvgc.app

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.arcvgc.app.data.initializeSentry
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initializeSentry()
    document.getElementById("loading")?.remove()
    ComposeViewport(document.body!!) {
        WebApp()
    }
}
