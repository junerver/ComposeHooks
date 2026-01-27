package xyz.junerver.compose.hooks.usetable.state

import androidx.compose.runtime.Stable

/**
 * Global state container for the table.
 */
@Stable
data class TableState<T>(
    // Core state
    val core: Map<String, Any?> = emptyMap(),
    
    // Feature states
    val sorting: SortingState = SortingState(),
    val filtering: FilteringState = FilteringState(),
    val pagination: PaginationState = PaginationState(),
    val rowSelection: RowSelectionState = RowSelectionState(),
    val expanded: ExpandedState = ExpandedState(),
    val grouping: GroupingState = GroupingState(),
    val columnSizing: ColumnSizingState = ColumnSizingState(),
    val columnVisibility: ColumnVisibilityState = ColumnVisibilityState(),
    
    // Extensible storage
    val featureStates: Map<String, Any?> = emptyMap()
)
