package com.arcvgc.app.data

import platform.Foundation.NSTimeZone
import platform.Foundation.localTimeZone
import platform.Foundation.secondsFromGMT

actual fun getUtcOffsetSeconds(): Int =
    NSTimeZone.localTimeZone.secondsFromGMT.toInt()
