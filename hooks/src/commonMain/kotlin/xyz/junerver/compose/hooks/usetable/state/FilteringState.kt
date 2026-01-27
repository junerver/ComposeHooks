package xyz.junerver.compose.hooks.usetable.state

import androidx.compose.runtime.Stable

/**
 * State for filtering feature.
 */
@Stable
data class FilteringState(
    val globalFilter: String = "",
    val columnFilters: Map<String, Any?> = emptyMap()
)
