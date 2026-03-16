package com.arcvgc.app.data

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("() => -(new Date().getTimezoneOffset()) * 60")
private external fun jsUtcOffsetSeconds(): Int

actual fun getUtcOffsetSeconds(): Int = jsUtcOffsetSeconds()
