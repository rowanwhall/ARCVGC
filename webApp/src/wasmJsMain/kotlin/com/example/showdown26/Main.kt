package com.example.showdown26

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.example.showdown26.data.initializeSentry
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initializeSentry()
    ComposeViewport(document.body!!) {
        WebApp()
    }
}
