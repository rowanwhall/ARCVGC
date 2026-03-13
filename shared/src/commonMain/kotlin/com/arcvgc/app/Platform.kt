package com.arcvgc.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform