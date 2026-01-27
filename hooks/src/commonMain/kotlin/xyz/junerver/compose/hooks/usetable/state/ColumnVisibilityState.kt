package xyz.junerver.compose.hooks.usetable.state

import androidx.compose.runtime.Stable

@Stable
data class ColumnVisibilityState(
    // Map of Column ID to visibility (false = hidden, absent = visible)
    val columnVisibility: Map<String, Boolean> = emptyMap()
)
