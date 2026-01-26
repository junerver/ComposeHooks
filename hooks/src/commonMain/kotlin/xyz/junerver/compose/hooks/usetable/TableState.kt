package xyz.junerver.compose.hooks.usetable

import androidx.compose.runtime.Stable

/*
  Description: State classes for useTable
  Author: Claude
  Date: 2025/1/26
  Email: noreply@anthropic.com
  Version: v1.0
*/

/**
 * Sorting state for a single column.
 */
@Stable
data class ColumnSort(
    val id: ColumnId,
    val direction: SortDirection,
)

/**
 * Table sorting state.
 */
@Stable
data class SortingState(
    val sorting: List<ColumnSort> = emptyList(),
) {
    fun isSorted(columnId: ColumnId): Boolean = sorting.any { it.id == columnId }

    fun getDirection(columnId: ColumnId): SortDirection? = sorting.find { it.id == columnId }?.direction

    fun toggleSort(columnId: ColumnId, multiSort: Boolean = false): SortingState {
        val existing = sorting.find { it.id == columnId }
        return when {
            existing == null -> {
                val newSort = ColumnSort(columnId, SortDirection.ASC)
                if (multiSort) {
                    copy(sorting = sorting + newSort)
                } else {
                    copy(sorting = listOf(newSort))
                }
            }
            existing.direction == SortDirection.ASC -> {
                val updated = existing.copy(direction = SortDirection.DESC)
                copy(sorting = sorting.map { if (it.id == columnId) updated else it })
            }
            else -> {
                if (multiSort) {
                    copy(sorting = sorting.filter { it.id != columnId })
                } else {
                    copy(sorting = emptyList())
                }
            }
        }
    }

    fun clearSort(): SortingState = copy(sorting = emptyList())
}

/**
 * Pagination state.
 */
@Stable
data class PaginationState(
    val pageIndex: Int = 0,
    val pageSize: Int = 10,
) {
    fun setPageIndex(index: Int): PaginationState = copy(pageIndex = index.coerceAtLeast(0))

    fun setPageSize(size: Int): PaginationState = copy(pageSize = size.coerceAtLeast(1), pageIndex = 0)

    fun nextPage(totalPages: Int): PaginationState {
        val maxIndex = (totalPages - 1).coerceAtLeast(0)
        return copy(pageIndex = (pageIndex + 1).coerceAtMost(maxIndex))
    }

    fun previousPage(): PaginationState = copy(pageIndex = (pageIndex - 1).coerceAtLeast(0))

    fun canNextPage(totalPages: Int): Boolean = pageIndex < totalPages - 1

    fun canPreviousPage(): Boolean = pageIndex > 0
}

/**
 * Column filter value.
 */
@Stable
data class ColumnFilter(
    val id: ColumnId,
    val value: Any?,
)

/**
 * Filter state for table.
 */
@Stable
data class FilterState(
    val columnFilters: List<ColumnFilter> = emptyList(),
    val globalFilter: String = "",
) {
    fun getColumnFilter(columnId: ColumnId): Any? = columnFilters.find { it.id == columnId }?.value

    fun setColumnFilter(columnId: ColumnId, value: Any?): FilterState {
        val newFilters = if (value == null || (value is String && value.isEmpty())) {
            columnFilters.filter { it.id != columnId }
        } else {
            val existing = columnFilters.find { it.id == columnId }
            if (existing != null) {
                columnFilters.map { if (it.id == columnId) it.copy(value = value) else it }
            } else {
                columnFilters + ColumnFilter(columnId, value)
            }
        }
        return copy(columnFilters = newFilters)
    }

    fun setGlobalFilter(value: String): FilterState = copy(globalFilter = value)

    fun clearFilters(): FilterState = copy(columnFilters = emptyList(), globalFilter = "")
}

/**
 * Row selection state.
 */
@Stable
data class RowSelectionState(
    val selectedRowIds: Set<RowId> = emptySet(),
) {
    fun isSelected(rowId: RowId): Boolean = rowId in selectedRowIds

    fun toggleSelection(rowId: RowId): RowSelectionState = if (rowId in selectedRowIds) {
        copy(selectedRowIds = selectedRowIds - rowId)
    } else {
        copy(selectedRowIds = selectedRowIds + rowId)
    }

    fun selectAll(rowIds: List<RowId>): RowSelectionState = copy(selectedRowIds = rowIds.toSet())

    fun deselectAll(): RowSelectionState = copy(selectedRowIds = emptySet())

    fun toggleAll(rowIds: List<RowId>): RowSelectionState = if (selectedRowIds.containsAll(rowIds)) {
        deselectAll()
    } else {
        selectAll(rowIds)
    }
}

/**
 * Column visibility state.
 */
@Stable
data class ColumnVisibilityState(
    val hiddenColumnIds: Set<ColumnId> = emptySet(),
) {
    fun isVisible(columnId: ColumnId): Boolean = columnId !in hiddenColumnIds

    fun toggleVisibility(columnId: ColumnId): ColumnVisibilityState = if (columnId in hiddenColumnIds) {
        copy(hiddenColumnIds = hiddenColumnIds - columnId)
    } else {
        copy(hiddenColumnIds = hiddenColumnIds + columnId)
    }

    fun setVisibility(columnId: ColumnId, visible: Boolean): ColumnVisibilityState = if (visible) {
        copy(hiddenColumnIds = hiddenColumnIds - columnId)
    } else {
        copy(hiddenColumnIds = hiddenColumnIds + columnId)
    }

    fun showAll(): ColumnVisibilityState = copy(hiddenColumnIds = emptySet())
}
