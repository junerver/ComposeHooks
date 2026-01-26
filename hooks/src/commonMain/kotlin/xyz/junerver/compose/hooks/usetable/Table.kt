@file:Suppress("unused")

package xyz.junerver.compose.hooks.usetable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import xyz.junerver.compose.hooks._useState
import xyz.junerver.compose.hooks.useState

/*
  Description: Headless Table Hook
  Author: Claude
  Date: 2025/1/26
  Email: noreply@anthropic.com
  Version: v1.0
*/

/**
 * Configuration options for useTable.
 */
@Stable
class UseTableOptions<T> {
    var enableSorting: Boolean = true
    var enableMultiSort: Boolean = false
    var enableFiltering: Boolean = true
    var enablePagination: Boolean = true
    var enableRowSelection: Boolean = true
    var enableColumnVisibility: Boolean = true

    var pageSize: Int = 10
    var initialPageIndex: Int = 0

    var getRowId: GetRowIdFn<T> = { _, index -> index.toString() }

    var initialSortingState: SortingState = SortingState()
    var initialFilterState: FilterState = FilterState()
    var initialRowSelectionState: RowSelectionState = RowSelectionState()
    var initialColumnVisibilityState: ColumnVisibilityState = ColumnVisibilityState()
}

/**
 * Table holder containing all table state and control functions.
 *
 * @param T The type of the row data
 */
@Stable
data class TableHolder<T>(
    val rows: State<List<Row<T>>>,
    val allRows: State<List<Row<T>>>,
    val columns: List<ColumnDef<T, *>>,
    val sortingState: State<SortingState>,
    val setSorting: (SortingState) -> Unit,
    val toggleSorting: ToggleSortingFn,
    val clearSorting: () -> Unit,
    val paginationState: State<PaginationState>,
    val setPageIndex: SetPageIndexFn,
    val setPageSize: SetPageSizeFn,
    val nextPage: NextPageFn,
    val previousPage: PreviousPageFn,
    val canNextPage: State<Boolean>,
    val canPreviousPage: State<Boolean>,
    val pageCount: State<Int>,
    val filterState: State<FilterState>,
    val setColumnFilter: SetColumnFilterFn,
    val setGlobalFilter: SetGlobalFilterFn,
    val clearFilters: () -> Unit,
    val rowSelectionState: State<RowSelectionState>,
    val toggleRowSelection: ToggleRowSelectionFn,
    val toggleAllRowsSelection: ToggleAllRowsSelectionFn,
    val clearRowSelection: () -> Unit,
    val selectedRows: State<List<Row<T>>>,
    val columnVisibilityState: State<ColumnVisibilityState>,
    val toggleColumnVisibility: ToggleColumnVisibilityFn,
    val visibleColumns: State<List<ColumnDef<T, *>>>,
)

/**
 * A headless table hook that provides table state management.
 *
 * This hook provides a way to manage table state including sorting, filtering,
 * pagination, row selection, and column visibility without imposing any UI constraints.
 *
 * @param data The data to display in the table
 * @param columns The column definitions
 * @param optionsOf Configuration factory function for table options
 * @return [TableHolder] containing all table state and control functions
 *
 * @example
 * ```kotlin
 * data class User(val name: String, val age: Int, val email: String)
 *
 * val users = listOf(
 *     User("Alice", 25, "alice@example.com"),
 *     User("Bob", 30, "bob@example.com"),
 * )
 *
 * val table = useTable(
 *     data = users,
 *     columns = listOf(
 *         column("name") { it.name },
 *         column("age") { it.age },
 *         column("email") { it.email },
 *     ),
 *     optionsOf = {
 *         enableSorting = true
 *         enablePagination = true
 *         pageSize = 10
 *     }
 * )
 *
 * // Access table state
 * val rows by table.rows
 * val sortingState by table.sortingState
 *
 * // Control table
 * table.toggleSorting("name")
 * table.nextPage()
 * table.setGlobalFilter("alice")
 * ```
 */
@Composable
fun <T> useTable(data: List<T>, columns: List<ColumnDef<T, *>>, optionsOf: UseTableOptions<T>.() -> Unit = {}): TableHolder<T> {
    val options = remember { UseTableOptions<T>().apply(optionsOf) }

    val sortingState: MutableState<SortingState> = _useState(options.initialSortingState)
    val filterState: MutableState<FilterState> = _useState(options.initialFilterState)
    val paginationState: MutableState<PaginationState> = _useState(
        PaginationState(options.initialPageIndex, options.pageSize),
    )
    val rowSelectionState: MutableState<RowSelectionState> = _useState(options.initialRowSelectionState)
    val columnVisibilityState: MutableState<ColumnVisibilityState> = _useState(options.initialColumnVisibilityState)

    val allRows = useState(data, columns, options) {
        data.mapIndexed { index, item ->
            Row(
                id = options.getRowId(item, index),
                index = index,
                original = item,
                isSelected = rowSelectionState.value.isSelected(options.getRowId(item, index)),
            )
        }
    }

    val filteredRows = useState(allRows, filterState, columns) {
        if (!options.enableFiltering) {
            allRows.value
        } else {
            filterRows(allRows.value, columns, filterState.value)
        }
    }

    val sortedRows = useState(filteredRows, sortingState, columns) {
        if (!options.enableSorting || sortingState.value.sorting.isEmpty()) {
            filteredRows.value
        } else {
            sortRows(filteredRows.value, columns, sortingState.value)
        }
    }

    val pageCount = useState(sortedRows, paginationState) {
        if (!options.enablePagination || paginationState.value.pageSize <= 0) {
            1
        } else {
            ((sortedRows.value.size + paginationState.value.pageSize - 1) / paginationState.value.pageSize)
                .coerceAtLeast(1)
        }
    }

    val paginatedRows = useState(sortedRows, paginationState, pageCount) {
        if (!options.enablePagination) {
            sortedRows.value
        } else {
            val start = paginationState.value.pageIndex * paginationState.value.pageSize
            val end = (start + paginationState.value.pageSize).coerceAtMost(sortedRows.value.size)
            if (start >= sortedRows.value.size) {
                emptyList()
            } else {
                sortedRows.value.subList(start, end)
            }
        }
    }

    val rowsWithSelection = useState(paginatedRows, rowSelectionState) {
        paginatedRows.value.map { row ->
            row.copy(isSelected = rowSelectionState.value.isSelected(row.id))
        }
    }

    val canNextPage = useState(paginationState, pageCount) {
        paginationState.value.canNextPage(pageCount.value)
    }

    val canPreviousPage = useState(paginationState) {
        paginationState.value.canPreviousPage()
    }

    val selectedRows = useState(allRows, rowSelectionState) {
        allRows.value.filter { rowSelectionState.value.isSelected(it.id) }
    }

    val visibleColumns = useState(columns, columnVisibilityState) {
        if (!options.enableColumnVisibility) {
            columns
        } else {
            columns.filter { columnVisibilityState.value.isVisible(it.id) }
        }
    }

    val toggleSorting: ToggleSortingFn = { columnId ->
        if (options.enableSorting) {
            val column = columns.find { it.id == columnId }
            if (column?.enableSorting == true) {
                sortingState.value = sortingState.value.toggleSort(columnId, options.enableMultiSort)
            }
        }
    }

    val clearSorting = {
        sortingState.value = sortingState.value.clearSort()
    }

    val setPageIndex: SetPageIndexFn = { index ->
        if (options.enablePagination) {
            val maxIndex = (pageCount.value - 1).coerceAtLeast(0)
            paginationState.value = paginationState.value.setPageIndex(index.coerceIn(0, maxIndex))
        }
    }

    val setPageSize: SetPageSizeFn = { size ->
        if (options.enablePagination) {
            paginationState.value = paginationState.value.setPageSize(size)
        }
    }

    val nextPage: NextPageFn = {
        if (options.enablePagination && canNextPage.value) {
            paginationState.value = paginationState.value.nextPage(pageCount.value)
        }
    }

    val previousPage: PreviousPageFn = {
        if (options.enablePagination && canPreviousPage.value) {
            paginationState.value = paginationState.value.previousPage()
        }
    }

    val setColumnFilter: SetColumnFilterFn = { columnId, value ->
        if (options.enableFiltering) {
            filterState.value = filterState.value.setColumnFilter(columnId, value)
            paginationState.value = paginationState.value.setPageIndex(0)
        }
    }

    val setGlobalFilter: SetGlobalFilterFn = { value ->
        if (options.enableFiltering) {
            filterState.value = filterState.value.setGlobalFilter(value)
            paginationState.value = paginationState.value.setPageIndex(0)
        }
    }

    val clearFilters = {
        filterState.value = filterState.value.clearFilters()
        paginationState.value = paginationState.value.setPageIndex(0)
    }

    val toggleRowSelection: ToggleRowSelectionFn = { rowId ->
        if (options.enableRowSelection) {
            rowSelectionState.value = rowSelectionState.value.toggleSelection(rowId)
        }
    }

    val toggleAllRowsSelection: ToggleAllRowsSelectionFn = {
        if (options.enableRowSelection) {
            val allRowIds = sortedRows.value.map { it.id }
            rowSelectionState.value = rowSelectionState.value.toggleAll(allRowIds)
        }
    }

    val clearRowSelection = {
        rowSelectionState.value = rowSelectionState.value.deselectAll()
    }

    val toggleColumnVisibility: ToggleColumnVisibilityFn = { columnId ->
        if (options.enableColumnVisibility) {
            columnVisibilityState.value = columnVisibilityState.value.toggleVisibility(columnId)
        }
    }

    val setSorting: (SortingState) -> Unit = { newState ->
        sortingState.value = newState
    }

    return remember(
        rowsWithSelection,
        allRows,
        sortingState,
        paginationState,
        filterState,
        rowSelectionState,
        columnVisibilityState,
    ) {
        TableHolder(
            rows = rowsWithSelection,
            allRows = allRows,
            columns = columns,
            sortingState = sortingState,
            setSorting = setSorting,
            toggleSorting = toggleSorting,
            clearSorting = clearSorting,
            paginationState = paginationState,
            setPageIndex = setPageIndex,
            setPageSize = setPageSize,
            nextPage = nextPage,
            previousPage = previousPage,
            canNextPage = canNextPage,
            canPreviousPage = canPreviousPage,
            pageCount = pageCount,
            filterState = filterState,
            setColumnFilter = setColumnFilter,
            setGlobalFilter = setGlobalFilter,
            clearFilters = clearFilters,
            rowSelectionState = rowSelectionState,
            toggleRowSelection = toggleRowSelection,
            toggleAllRowsSelection = toggleAllRowsSelection,
            clearRowSelection = clearRowSelection,
            selectedRows = selectedRows,
            columnVisibilityState = columnVisibilityState,
            toggleColumnVisibility = toggleColumnVisibility,
            visibleColumns = visibleColumns,
        )
    }
}

@Suppress("UNCHECKED_CAST")
private fun <T> filterRows(rows: List<Row<T>>, columns: List<ColumnDef<T, *>>, filterState: FilterState): List<Row<T>> {
    var result = rows

    if (filterState.globalFilter.isNotEmpty()) {
        val globalFilter = filterState.globalFilter.lowercase()
        result = result.filter { row ->
            columns.any { column ->
                val value = (column as ColumnDef<T, Any?>).accessorFn(row.original)
                value?.toString()?.lowercase()?.contains(globalFilter) == true
            }
        }
    }

    for (columnFilter in filterState.columnFilters) {
        val column = columns.find { it.id == columnFilter.id } ?: continue
        if (!column.enableFiltering) continue

        result = result.filter { row ->
            val value = (column as ColumnDef<T, Any?>).accessorFn(row.original)
            val filterFn = column.filterFn as? ((Any?, Any?) -> Boolean)
            if (filterFn != null) {
                filterFn(value, columnFilter.value)
            } else {
                val filterValue = columnFilter.value?.toString()?.lowercase() ?: ""
                value?.toString()?.lowercase()?.contains(filterValue) == true
            }
        }
    }

    return result
}

@Suppress("UNCHECKED_CAST")
private fun <T> sortRows(rows: List<Row<T>>, columns: List<ColumnDef<T, *>>, sortingState: SortingState): List<Row<T>> {
    if (sortingState.sorting.isEmpty()) return rows

    val comparator = sortingState.sorting.fold<ColumnSort, Comparator<Row<T>>?>(null) { acc, columnSort ->
        val column = columns.find { it.id == columnSort.id } ?: return@fold acc
        if (!column.enableSorting) return@fold acc

        val columnComparator: Comparator<Row<T>> = Comparator { row1, row2 ->
            val value1 = (column as ColumnDef<T, Any?>).accessorFn(row1.original)
            val value2 = column.accessorFn(row2.original)

            val customComparator = column.sortingFn as? Comparator<Any?>
            val result = if (customComparator != null) {
                customComparator.compare(value1, value2)
            } else {
                compareValues(value1 as? Comparable<Any?>, value2 as? Comparable<Any?>)
            }

            if (columnSort.direction == SortDirection.DESC) -result else result
        }

        if (acc == null) columnComparator else acc.then(columnComparator)
    }

    return if (comparator != null) rows.sortedWith(comparator) else rows
}
