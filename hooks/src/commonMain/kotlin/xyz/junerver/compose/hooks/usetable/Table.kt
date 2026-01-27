@file:Suppress("unused")

package xyz.junerver.compose.hooks.usetable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import xyz.junerver.compose.hooks.Ref
import xyz.junerver.compose.hooks._useState
import xyz.junerver.compose.hooks.createContext
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.useState

/*
  Description: Headless Table Component
  Author: Claude
  Date: 2025/1/26
  Email: noreply@anthropic.com
  Version: v1.0
*/

/**
 * Internal table context for managing table state across components.
 */
internal val TableContext by lazy { createContext<TableInstance<*>?>(null) }

/**
 * Configuration options for Table component.
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
 * A headless table component that provides table state management and data processing.
 *
 * This component creates a table context and provides table functionality to its children
 * without imposing any UI constraints. It allows you to:
 * - Manage table data with sorting, filtering, pagination
 * - Handle row selection
 * - Control column visibility
 * - Create custom table layouts
 *
 * @param T The type of the row data
 * @param tableInstance The table instance to use
 * @param data The data to display in the table
 * @param columns The column definitions
 * @param optionsOf Configuration factory function for table options
 * @param content The table content with access to table functionality through [TableScope]
 *
 * @example
 * ```kotlin
 * val table = Table.useTable<User>()
 *
 * Table(table, users, columns) {
 *     TableHeader {
 *         HeaderRow {
 *             visibleColumns.forEach { column ->
 *                 HeaderCell(column) {
 *                     Text(column.header)
 *                 }
 *             }
 *         }
 *     }
 *
 *     TableBody {
 *         rows.forEach { row ->
 *             TableRow(row) {
 *                 visibleColumns.forEach { column ->
 *                     TableCell(column) {
 *                         Text(row.getValue(column).toString())
 *                     }
 *                 }
 *             }
 *         }
 *     }
 * }
 * ```
 */
@Composable
fun <T> Table(
    tableInstance: TableInstance<T>,
    data: List<T>,
    columns: List<ColumnDef<T, *>>,
    optionsOf: UseTableOptions<T>.() -> Unit = {},
    content: @Composable TableScope<T>.() -> Unit,
) {
    val options = remember { UseTableOptions<T>().apply(optionsOf) }
    val tableRef = useCreation { TableRef<T>() }

    // Initialize states
    val sortingState = _useState(options.initialSortingState)
    val filterState = _useState(options.initialFilterState)
    val paginationState = _useState(PaginationState(options.initialPageIndex, options.pageSize))
    val rowSelectionState = _useState(options.initialRowSelectionState)
    val columnVisibilityState = _useState(options.initialColumnVisibilityState)

    // Setup tableRef
    tableRef.current.data = data
    tableRef.current.columns = columns
    tableRef.current.options = options
    tableRef.current.sortingState = sortingState
    tableRef.current.filterState = filterState
    tableRef.current.paginationState = paginationState
    tableRef.current.rowSelectionState = rowSelectionState
    tableRef.current.columnVisibilityState = columnVisibilityState

    // Connect tableInstance to tableRef
    tableInstance.tableRef = tableRef

    // Process data: filter -> sort -> paginate
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

    val filteredRows = useState(allRows.value, filterState.value, columns) {
        if (!options.enableFiltering) {
            allRows.value
        } else {
            filterRows(allRows.value, columns, filterState.value)
        }
    }

    val sortedRows = useState(filteredRows.value, sortingState.value, columns) {
        if (!options.enableSorting || sortingState.value.sorting.isEmpty()) {
            filteredRows.value
        } else {
            sortRows(filteredRows.value, columns, sortingState.value)
        }
    }

    val pageCount = useState(sortedRows.value.size, paginationState.value.pageSize) {
        if (!options.enablePagination || paginationState.value.pageSize <= 0) {
            1
        } else {
            ((sortedRows.value.size + paginationState.value.pageSize - 1) / paginationState.value.pageSize)
                .coerceAtLeast(1)
        }
    }

    val paginatedRows = useState(sortedRows.value, paginationState.value, pageCount.value) {
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

    val rowsWithSelection = useState(paginatedRows.value, rowSelectionState.value) {
        paginatedRows.value.map { row ->
            row.copy(isSelected = rowSelectionState.value.isSelected(row.id))
        }
    }

    val canNextPage = useState(paginationState.value.pageIndex, pageCount.value) {
        paginationState.value.canNextPage(pageCount.value)
    }

    val canPreviousPage = useState(paginationState.value.pageIndex) {
        paginationState.value.canPreviousPage()
    }

    val visibleColumns = useState(columns, columnVisibilityState.value) {
        if (!options.enableColumnVisibility) {
            columns
        } else {
            columns.filter { columnVisibilityState.value.isVisible(it.id) }
        }
    }

    // Update tableRef with computed values (synchronously)
    tableRef.current.processedRows = rowsWithSelection.value
    tableRef.current.allFilteredSortedRows = sortedRows.value
    tableRef.current.pageCount = pageCount.value
    tableRef.current.canNextPage = canNextPage.value
    tableRef.current.canPreviousPage = canPreviousPage.value
    tableRef.current.visibleColumns = visibleColumns.value

    // Create scope
    val tableScope = remember(tableRef, tableInstance) {
        TableScope.getInstance(tableRef, tableInstance)
    }

    @Suppress("UNCHECKED_CAST")
    TableContext.Provider(tableInstance as TableInstance<*>) {
        tableScope.content()
    }
}

/**
 * Scope class for table components that provides table-specific functionality.
 */
@Stable
class TableScope<T> private constructor(
    private val tableRefRef: Ref<TableRef<T>>,
    private val tableInstance: TableInstance<T>,
) {
    // Read directly from MutableState to ensure Compose tracks changes
    val rows: List<Row<T>>
        get() = tableRefRef.current.processedRows
    val allRows: List<Row<T>>
        get() = tableRefRef.current.allFilteredSortedRows
    val visibleColumns: List<ColumnDef<T, *>>
        get() = tableRefRef.current.visibleColumns
    val sortingState: SortingState
        get() = tableRefRef.current.sortingState.value
    val paginationState: PaginationState
        get() = tableRefRef.current.paginationState.value
    val filterState: FilterState
        get() = tableRefRef.current.filterState.value
    val rowSelectionState: RowSelectionState
        get() = tableRefRef.current.rowSelectionState.value
    val columnVisibilityState: ColumnVisibilityState
        get() = tableRefRef.current.columnVisibilityState.value
    val pageCount: Int
        get() = tableRefRef.current.pageCount
    val canNextPage: Boolean
        get() = tableRefRef.current.canNextPage
    val canPreviousPage: Boolean
        get() = tableRefRef.current.canPreviousPage

    val selectedRows: List<Row<T>>
        get() = allRows.filter { rowSelectionState.isSelected(it.id) }

    val columns: List<ColumnDef<T, *>>
        get() = tableRefRef.current.columns

    // ==================== Operations ====================

    fun toggleSorting(columnId: ColumnId) = tableInstance.toggleSorting(columnId)

    fun clearSorting() = tableInstance.clearSorting()

    fun setPageIndex(index: Int) = tableInstance.setPageIndex(index)

    fun setPageSize(size: Int) = tableInstance.setPageSize(size)

    fun nextPage() = tableInstance.nextPage()

    fun previousPage() = tableInstance.previousPage()

    fun setColumnFilter(columnId: ColumnId, value: Any?) = tableInstance.setColumnFilter(columnId, value)

    fun setGlobalFilter(value: String) = tableInstance.setGlobalFilter(value)

    fun clearFilters() = tableInstance.clearFilters()

    fun toggleRowSelection(rowId: RowId) = tableInstance.toggleRowSelection(rowId)

    fun toggleAllRowsSelection() = tableInstance.toggleAllRowsSelection()

    fun clearRowSelection() = tableInstance.clearRowSelection()

    fun isRowSelected(rowId: RowId): Boolean = rowSelectionState.isSelected(rowId)

    fun toggleColumnVisibility(columnId: ColumnId) = tableInstance.toggleColumnVisibility(columnId)

    // ==================== Sub-components ====================

    /**
     * Table header container.
     */
    @Composable
    fun TableHeader(content: @Composable TableHeaderScope<T>.() -> Unit) {
        val headerScope = remember { TableHeaderScope(this) }
        headerScope.content()
    }

    /**
     * Table body container.
     */
    @Composable
    fun TableBody(content: @Composable TableBodyScope<T>.() -> Unit) {
        val bodyScope = remember { TableBodyScope(this) }
        bodyScope.content()
    }

    /**
     * Pagination controls container.
     */
    @Composable
    fun Pagination(content: @Composable PaginationScope.() -> Unit) {
        val paginationScope = remember { PaginationScope(this) }
        paginationScope.content()
    }

    companion object {
        internal fun <T> getInstance(ref: Ref<TableRef<T>>, tableInstance: TableInstance<T>) = TableScope(ref, tableInstance)
    }
}

// ==================== Sub-Scope Classes ====================

/**
 * Scope for table header.
 */
@Stable
class TableHeaderScope<T>(private val tableScope: TableScope<T>) {
    val visibleColumns: List<ColumnDef<T, *>> get() = tableScope.visibleColumns
    val sortingState: SortingState get() = tableScope.sortingState

    fun toggleSorting(columnId: ColumnId) = tableScope.toggleSorting(columnId)

    @Composable
    fun HeaderRow(content: @Composable HeaderRowScope<T>.() -> Unit) {
        val rowScope = remember { HeaderRowScope(this) }
        rowScope.content()
    }
}

/**
 * Scope for header row.
 */
@Stable
class HeaderRowScope<T>(private val headerScope: TableHeaderScope<T>) {
    val visibleColumns: List<ColumnDef<T, *>> get() = headerScope.visibleColumns
    val sortingState: SortingState get() = headerScope.sortingState

    fun toggleSorting(columnId: ColumnId) = headerScope.toggleSorting(columnId)

    fun isSorted(columnId: ColumnId): Boolean = sortingState.isSorted(columnId)

    fun getSortDirection(columnId: ColumnId): SortDirection? = sortingState.getDirection(columnId)

    @Composable
    fun HeaderCell(column: ColumnDef<T, *>, content: @Composable HeaderCellScope<T>.() -> Unit) {
        val cellScope = remember(column) { HeaderCellScope(column, this) }
        cellScope.content()
    }
}

/**
 * Scope for header cell.
 */
@Stable
class HeaderCellScope<T>(
    val column: ColumnDef<T, *>,
    private val rowScope: HeaderRowScope<T>,
) {
    val isSorted: Boolean get() = rowScope.isSorted(column.id)
    val sortDirection: SortDirection? get() = rowScope.getSortDirection(column.id)

    fun toggleSorting() = rowScope.toggleSorting(column.id)
}

/**
 * Scope for table body.
 */
@Stable
class TableBodyScope<T>(private val tableScope: TableScope<T>) {
    val rows: List<Row<T>> get() = tableScope.rows
    val visibleColumns: List<ColumnDef<T, *>> get() = tableScope.visibleColumns

    fun toggleRowSelection(rowId: RowId) = tableScope.toggleRowSelection(rowId)

    fun isRowSelected(rowId: RowId): Boolean = tableScope.isRowSelected(rowId)

    @Composable
    fun TableRow(row: Row<T>, content: @Composable TableRowScope<T>.() -> Unit) {
        val rowScope = remember(row) { TableRowScope(row, this) }
        rowScope.content()
    }
}

/**
 * Scope for table row.
 */
@Stable
class TableRowScope<T>(
    val row: Row<T>,
    private val bodyScope: TableBodyScope<T>,
) {
    val visibleColumns: List<ColumnDef<T, *>> get() = bodyScope.visibleColumns
    val isSelected: Boolean get() = row.isSelected

    fun toggleSelection() = bodyScope.toggleRowSelection(row.id)

    @Composable
    fun <V> TableCell(column: ColumnDef<T, V>, content: @Composable TableCellScope<T, V>.() -> Unit) {
        val cellScope = remember(column) { TableCellScope(row, column) }
        cellScope.content()
    }
}

/**
 * Scope for table cell.
 */
@Stable
class TableCellScope<T, V>(
    val row: Row<T>,
    val column: ColumnDef<T, V>,
) {
    val value: V get() = column.accessorFn(row.original)
}

/**
 * Scope for pagination controls.
 */
@Stable
class PaginationScope(private val tableScope: TableScope<*>) {
    val pageIndex: Int get() = tableScope.paginationState.pageIndex
    val pageSize: Int get() = tableScope.paginationState.pageSize
    val pageCount: Int get() = tableScope.pageCount
    val canNextPage: Boolean get() = tableScope.canNextPage
    val canPreviousPage: Boolean get() = tableScope.canPreviousPage

    fun nextPage() = tableScope.nextPage()

    fun previousPage() = tableScope.previousPage()

    fun setPageIndex(index: Int) = tableScope.setPageIndex(index)

    fun setPageSize(size: Int) = tableScope.setPageSize(size)
}

// ==================== Helper Functions ====================

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
