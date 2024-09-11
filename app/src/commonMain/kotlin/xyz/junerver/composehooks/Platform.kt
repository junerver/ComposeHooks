package xyz.junerver.composehooks

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
