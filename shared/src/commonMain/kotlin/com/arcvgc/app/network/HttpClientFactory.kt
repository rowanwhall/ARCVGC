package com.arcvgc.app.network

import io.ktor.client.HttpClient

expect fun createPlatformHttpClient(): HttpClient
