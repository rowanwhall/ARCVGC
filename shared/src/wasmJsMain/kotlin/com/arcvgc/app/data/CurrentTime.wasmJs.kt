package com.arcvgc.app.data

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("() => Date.now()")
private external fun dateNow(): Double

actual fun currentTimeMillis(): Long = dateNow().toLong()
