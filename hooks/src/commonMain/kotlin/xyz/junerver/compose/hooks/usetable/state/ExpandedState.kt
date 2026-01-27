package xyz.junerver.compose.hooks.usetable.state

import androidx.compose.runtime.Stable

@Stable
data class ExpandedState(
    // Map of Row ID to expanded state (true = expanded)
    val expanded: Map<String, Boolean> = emptyMap()
)
