package xyz.junerver.compose.ai

/**
 * Get current timestamp in milliseconds.
 * Desktop (JVM) implementation.
 */
internal actual fun currentTimestamp(): Long = System.currentTimeMillis()
