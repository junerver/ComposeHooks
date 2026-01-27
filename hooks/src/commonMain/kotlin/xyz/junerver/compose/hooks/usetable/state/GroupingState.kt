package xyz.junerver.compose.hooks.usetable.state

import androidx.compose.runtime.Stable

@Stable
data class GroupingState(
    // List of column IDs to group by (in order)
    val grouping: List<String> = emptyList()
)
