package com.fintrack.shared

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform