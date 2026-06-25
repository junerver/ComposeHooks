package xyz.junerver.compose.ai

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

/**
 * Get current timestamp in milliseconds.
 * iOS implementation.
 */
internal actual fun currentTimestamp(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()
