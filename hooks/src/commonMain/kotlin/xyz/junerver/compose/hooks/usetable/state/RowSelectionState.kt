package xyz.junerver.compose.hooks.usetable.state

import androidx.compose.runtime.Stable

@Stable
data class RowSelectionState(
    val selectedRowIds: Set<String> = emptySet()
)
