package xyz.junerver.compose.hooks.usetable

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf

/*
  Description: Internal state container for Table component
  Author: Claude
  Date: 2025/1/26
  Email: noreply@anthropic.com
  Version: v1.0
*/

/**
 * Internal reference class that holds all table state.
 * Similar to FormRef in the Form component.
 */
internal class TableRef<T> {
    /** Counter for tracking state changes */
    val operationCount = mutableLongStateOf(0L)

    /** Original data */
    var data: List<T> = emptyList()

    /** Column definitions */
    var columns: List<ColumnDef<T, *>> = emptyList()

    /** Table options */
    var options: UseTableOptions<T> = UseTableOptions()

    /** Sorting state */
    lateinit var sortingState: MutableState<SortingState>

    /** Pagination state */
    lateinit var paginationState: MutableState<PaginationState>

    /** Filter state */
    lateinit var filterState: MutableState<FilterState>

    /** Row selection state */
    lateinit var rowSelectionState: MutableState<RowSelectionState>

    /** Column visibility state */
    lateinit var columnVisibilityState: MutableState<ColumnVisibilityState>

    /** Processed rows (after filtering, sorting, pagination) - MutableState for reactivity */
    val processedRowsState = mutableStateOf<List<Row<T>>>(emptyList())
    var processedRows: List<Row<T>>
        get() = processedRowsState.value
        set(value) { processedRowsState.value = value }

    /** All rows (before pagination) - MutableState for reactivity */
    val allFilteredSortedRowsState = mutableStateOf<List<Row<T>>>(emptyList())
    var allFilteredSortedRows: List<Row<T>>
        get() = allFilteredSortedRowsState.value
        set(value) { allFilteredSortedRowsState.value = value }

    /** Page count - MutableState for reactivity */
    val pageCountState = mutableIntStateOf(1)
    var pageCount: Int
        get() = pageCountState.intValue
        set(value) { pageCountState.intValue = value }

    /** Can navigate to next page - MutableState for reactivity */
    val canNextPageState = mutableStateOf(false)
    var canNextPage: Boolean
        get() = canNextPageState.value
        set(value) { canNextPageState.value = value }

    /** Can navigate to previous page - MutableState for reactivity */
    val canPreviousPageState = mutableStateOf(false)
    var canPreviousPage: Boolean
        get() = canPreviousPageState.value
        set(value) { canPreviousPageState.value = value }

    /** Visible columns (after applying column visibility) - MutableState for reactivity */
    val visibleColumnsState = mutableStateOf<List<ColumnDef<T, *>>>(emptyList())
    var visibleColumns: List<ColumnDef<T, *>>
        get() = visibleColumnsState.value
        set(value) { visibleColumnsState.value = value }
}
