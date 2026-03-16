package com.arcvgc.app.ui

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("() => { window.history.pushState({}, ''); }")
internal external fun pushHistoryState()

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("(delta) => { window.history.go(delta); }")
internal external fun historyGo(delta: Int)
