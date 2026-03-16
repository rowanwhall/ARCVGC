package com.arcvgc.app.data

import java.util.TimeZone

actual fun getUtcOffsetSeconds(): Int =
    TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 1000
