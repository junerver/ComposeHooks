package xyz.junerver.compose.hooks.usetable.state

import androidx.compose.runtime.Stable

@Stable
data class ColumnSizingState(
    // Map of Column ID to width in pixels
    val columnSizing: Map<String, Float> = emptyMap()
)
