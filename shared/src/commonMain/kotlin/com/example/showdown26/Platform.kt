package com.example.showdown26

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform