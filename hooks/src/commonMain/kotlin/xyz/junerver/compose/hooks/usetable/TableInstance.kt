package xyz.junerver.compose.hooks.usetable

import androidx.compose.runtime.Stable
import xyz.junerver.compose.hooks.Ref

/*
  Description: Table instance controller for managing table state
  Author: Claude
  Date: 2025/1/26
  Email: noreply@anthropic.com
  Version: v1.0
*/

/**
 * Table controller class that manages table state and operations.
 * This class provides methods to control and interact with table from outside the Table component.
 *
 * The controller must be passed to a [Table] component to function properly.
 *
 * @example
 * ```kotlin
 * val table = Table.useTable<User>()
 *
 * // Use in Table component
 * Table(table, data, columns) {
 *     // Table content
 * }
 *
 * // Control from outside
 * Button(onClick = { table.toggleSorting("name") }) {
 *     Text("Sort by name")
 * }
 * ```
 */
@Stable
class TableInstance<T> {
    /** Internal reference to table state, initialized when mounted to a Table component */
    internal lateinit var tableRef: Ref<TableRef<T>>

    // ==================== State Access ====================

    /**
     * Gets the current sorting state.
     */
    fun getSortingState(): SortingState {
        checkRef()
        return tableRef.current.sortingState.value
    }

    /**
     * Gets the current pagination state.
     */
    fun getPaginationState(): PaginationState {
        checkRef()
        return tableRef.current.paginationState.value
    }

    /**
     * Gets the current filter state.
     */
    fun getFilterState(): FilterState {
        checkRef()
        return tableRef.current.filterState.value
    }

    /**
     * Gets the current row selection state.
     */
    fun getRowSelectionState(): RowSelectionState {
        checkRef()
        return tableRef.current.rowSelectionState.value
    }

    /**
     * Gets the current column visibility state.
     */
    fun getColumnVisibilityState(): ColumnVisibilityState {
        checkRef()
        return tableRef.current.columnVisibilityState.value
    }

    /**
     * Gets the selected rows.
     */
    fun getSelectedRows(): List<Row<T>> {
        checkRef()
        val ref = tableRef.current
        val selectionState = ref.rowSelectionState.value
        return ref.allFilteredSortedRows.filter { selectionState.isSelected(it.id) }
    }

    // ==================== Sorting Operations ====================

    /**
     * Toggles sorting for a column.
     */
    fun toggleSorting(columnId: ColumnId) {
        checkRef()
        val ref = tableRef.current
        if (!ref.options.enableSorting) return

        val column = ref.columns.find { it.id == columnId }
        if (column?.enableSorting == true) {
            ref.sortingState.value = ref.sortingState.value.toggleSort(columnId, ref.options.enableMultiSort)
            ref.operationCount.longValue += 1
        }
    }

    /**
     * Sets the sorting state directly.
     */
    fun setSorting(state: SortingState) {
        checkRef()
        tableRef.current.sortingState.value = state
        tableRef.current.operationCount.longValue += 1
    }

    /**
     * Clears all sorting.
     */
    fun clearSorting() {
        checkRef()
        tableRef.current.sortingState.value = SortingState()
        tableRef.current.operationCount.longValue += 1
    }

    // ==================== Pagination Operations ====================

    /**
     * Sets the current page index.
     */
    fun setPageIndex(index: Int) {
        checkRef()
        val ref = tableRef.current
        if (!ref.options.enablePagination) return

        val maxIndex = (ref.pageCount - 1).coerceAtLeast(0)
        ref.paginationState.value = ref.paginationState.value.setPageIndex(index.coerceIn(0, maxIndex))
        ref.operationCount.longValue += 1
    }

    /**
     * Sets the page size.
     */
    fun setPageSize(size: Int) {
        checkRef()
        val ref = tableRef.current
        if (!ref.options.enablePagination) return

        ref.paginationState.value = ref.paginationState.value.setPageSize(size)
        ref.operationCount.longValue += 1
    }

    /**
     * Navigates to the next page.
     */
    fun nextPage() {
        checkRef()
        val ref = tableRef.current
        if (!ref.options.enablePagination) return

        val currentState = ref.paginationState.value
        // Calculate pageCount directly from allFilteredSortedRows
        val totalRows = ref.allFilteredSortedRows.size
        val pageSize = currentState.pageSize
        val pageCount = if (pageSize <= 0) 1 else ((totalRows + pageSize - 1) / pageSize).coerceAtLeast(1)

        val canNext = currentState.canNextPage(pageCount)
        if (!canNext) return

        ref.paginationState.value = currentState.nextPage(pageCount)
        ref.operationCount.longValue += 1
    }

    /**
     * Navigates to the previous page.
     */
    fun previousPage() {
        checkRef()
        val ref = tableRef.current
        if (!ref.options.enablePagination) return

        val currentState = ref.paginationState.value
        val canPrev = currentState.canPreviousPage()
        if (!canPrev) return

        ref.paginationState.value = currentState.previousPage()
        ref.operationCount.longValue += 1
    }

    // ==================== Filter Operations ====================

    /**
     * Sets a column filter.
     */
    fun setColumnFilter(columnId: ColumnId, value: Any?) {
        checkRef()
        val ref = tableRef.current
        if (!ref.options.enableFiltering) return

        ref.filterState.value = ref.filterState.value.setColumnFilter(columnId, value)
        ref.paginationState.value = ref.paginationState.value.setPageIndex(0)
        ref.operationCount.longValue += 1
    }

    /**
     * Sets the global filter.
     */
    fun setGlobalFilter(value: String) {
        checkRef()
        val ref = tableRef.current
        if (!ref.options.enableFiltering) return

        ref.filterState.value = ref.filterState.value.setGlobalFilter(value)
        ref.paginationState.value = ref.paginationState.value.setPageIndex(0)
        ref.operationCount.longValue += 1
    }

    /**
     * Clears all filters.
     */
    fun clearFilters() {
        checkRef()
        val ref = tableRef.current
        ref.filterState.value = FilterState()
        ref.paginationState.value = ref.paginationState.value.setPageIndex(0)
        ref.operationCount.longValue += 1
    }

    // ==================== Row Selection Operations ====================

    /**
     * Toggles selection for a row.
     */
    fun toggleRowSelection(rowId: RowId) {
        checkRef()
        val ref = tableRef.current
        if (!ref.options.enableRowSelection) return

        ref.rowSelectionState.value = ref.rowSelectionState.value.toggleSelection(rowId)
        ref.operationCount.longValue += 1
    }

    /**
     * Toggles selection for all rows.
     */
    fun toggleAllRowsSelection() {
        checkRef()
        val ref = tableRef.current
        if (!ref.options.enableRowSelection) return

        val allRowIds = ref.allFilteredSortedRows.map { it.id }
        ref.rowSelectionState.value = ref.rowSelectionState.value.toggleAll(allRowIds)
        ref.operationCount.longValue += 1
    }

    /**
     * Clears all row selections.
     */
    fun clearRowSelection() {
        checkRef()
        tableRef.current.rowSelectionState.value = RowSelectionState()
        tableRef.current.operationCount.longValue += 1
    }

    /**
     * Checks if a row is selected.
     */
    fun isRowSelected(rowId: RowId): Boolean {
        checkRef()
        return tableRef.current.rowSelectionState.value.isSelected(rowId)
    }

    // ==================== Column Visibility Operations ====================

    /**
     * Toggles visibility for a column.
     */
    fun toggleColumnVisibility(columnId: ColumnId) {
        checkRef()
        val ref = tableRef.current
        if (!ref.options.enableColumnVisibility) return

        ref.columnVisibilityState.value = ref.columnVisibilityState.value.toggleVisibility(columnId)
        ref.operationCount.longValue += 1
    }

    /**
     * Sets visibility for a column.
     */
    fun setColumnVisibility(columnId: ColumnId, visible: Boolean) {
        checkRef()
        val ref = tableRef.current
        if (!ref.options.enableColumnVisibility) return

        ref.columnVisibilityState.value = ref.columnVisibilityState.value.setVisibility(columnId, visible)
        ref.operationCount.longValue += 1
    }

    /**
     * Internal function to verify that the table instance is properly initialized.
     */
    private fun checkRef() {
        require(this::tableRef.isInitialized) {
            "TableInstance must be passed to Table before it can be used"
        }
    }
}
