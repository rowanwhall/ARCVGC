package com.arcvgc.app.ui

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("() => { window.history.pushState({}, ''); }")
internal external fun pushHistoryState()

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("(delta) => { window.history.go(delta); }")
internal external fun historyGo(delta: Int)

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("(path) => { window.history.pushState({}, '', path); }")
internal external fun pushHistoryStateWithPath(path: String)

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("(path) => { window.history.replaceState({}, '', path); }")
internal external fun replaceHistoryStateWithPath(path: String)

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("() => { return window.location.pathname; }")
internal external fun getLocationPathname(): String

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("() => { return window.location.pathname + window.location.search; }")
internal external fun getLocationPathAndSearch(): String
