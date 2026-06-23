@file:Suppress("UnusedReceiverParameter")

package xyz.junerver.compose.hooks.usetable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import xyz.junerver.compose.hooks.useLatestRef
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.usetable.core.ColumnDef
import xyz.junerver.compose.hooks.usetable.core.Row
import xyz.junerver.compose.hooks.usetable.core.RowModel
import xyz.junerver.compose.hooks.usetable.core.TableInstance
import xyz.junerver.compose.hooks.usetable.features.columnSizing.ColumnSizingFeature
import xyz.junerver.compose.hooks.usetable.features.columnVisibility.ColumnVisibilityFeature
import xyz.junerver.compose.hooks.usetable.features.expansion.ExpansionFeature
import xyz.junerver.compose.hooks.usetable.features.filtering.FilteringFeature
import xyz.junerver.compose.hooks.usetable.features.grouping.GroupingFeature
import xyz.junerver.compose.hooks.usetable.features.pagination.PaginationFeature
import xyz.junerver.compose.hooks.usetable.features.rowselection.RowSelectionFeature
import xyz.junerver.compose.hooks.usetable.features.sorting.SortingFeature
import xyz.junerver.compose.hooks.usetable.pipeline.RowModelPipeline
import xyz.junerver.compose.hooks.usetable.state.ColumnSizingState
import xyz.junerver.compose.hooks.usetable.state.ColumnVisibilityState
import xyz.junerver.compose.hooks.usetable.state.ExpandedState
import xyz.junerver.compose.hooks.usetable.state.FilteringState
import xyz.junerver.compose.hooks.usetable.state.GroupingState
import xyz.junerver.compose.hooks.usetable.state.PaginationState
import xyz.junerver.compose.hooks.usetable.state.RowSelectionState
import xyz.junerver.compose.hooks.usetable.state.SortDescriptor
import xyz.junerver.compose.hooks.usetable.state.SortingState
import xyz.junerver.compose.hooks.usetable.state.TableState

/**
 * Configuration options for the table.
 */
class TableOptions<T> {
    // Feature toggles
    var enableSorting: Boolean = false
    var enableFiltering: Boolean = false
    var enablePagination: Boolean = false
    var enableRowSelection: Boolean = false
    var enableExpansion: Boolean = false
    var enableGrouping: Boolean = false
    var enableColumnSizing: Boolean = false
    var enableColumnVisibility: Boolean = false
    
    // Initial state
    var initialState: TableState<T>? = null
    
    // Pagination
    var pageSize: Int = 10
    
    // Row ID generator
    var getRowId: (T, Int) -> String = defaultTableRowId()

    internal fun reset() {
        enableSorting = false
        enableFiltering = false
        enablePagination = false
        enableRowSelection = false
        enableExpansion = false
        enableGrouping = false
        enableColumnSizing = false
        enableColumnVisibility = false
        initialState = null
        pageSize = 10
        getRowId = defaultTableRowId()
    }
}

private val defaultTableRowId: (Any?, Int) -> String = { _, index -> index.toString() }

@Suppress("UNCHECKED_CAST")
private fun <T> defaultTableRowId(): (T, Int) -> String = defaultTableRowId as (T, Int) -> String

/**
 * Return type for useTable hook containing all table state and controls.
 */
@Stable
data class TableHolder<T>(
    // States
    val rowModel: State<RowModel<T>>,
    val columns: State<List<ColumnDef<T, *>>>,
    val state: State<TableState<T>>,
    
    // Sorting
    val setSorting: (List<SortDescriptor>) -> Unit,
    val toggleSorting: (String, Boolean?) -> Unit,
    val clearSorting: () -> Unit,
    
    // Filtering
    val setGlobalFilter: (String) -> Unit,
    val setColumnFilter: (String, Any?) -> Unit,
    val clearFilters: () -> Unit,
    
    // Pagination
    val setPageIndex: (Int) -> Unit,
    val setPageSize: (Int) -> Unit,
    val nextPage: () -> Unit,
    val previousPage: () -> Unit,
    
    // Row Selection
    val toggleRowSelection: (String) -> Unit,
    val toggleAllRowsSelection: (Boolean?) -> Unit,
    val clearRowSelection: () -> Unit,
    
    // Expansion
    val toggleRowExpanded: (String) -> Unit,
    val expandAll: () -> Unit,
    val collapseAll: () -> Unit,
    
    // Grouping
    val setGrouping: (List<String>) -> Unit,
    
    // Column Sizing
    val setColumnSize: (String, Float) -> Unit,
    val resetColumnSizing: () -> Unit,
    
    // Column Visibility
    val setColumnVisibility: (String, Boolean) -> Unit,
    val toggleAllColumnsVisible: () -> Unit
)

/**
 * Main hook for creating a table with full feature support.
 */
@Composable
fun <T> useTable(
    data: List<T>,
    columns: List<ColumnDef<T, *>>,
    optionsOf: TableOptions<T>.() -> Unit = {}
): TableHolder<T> {
    val options = remember { TableOptions<T>() }.apply {
        reset()
        optionsOf()
    }
    
    val dataRef = useLatestRef(data)
    val columnsRef = useLatestRef(columns)
    val optionsRef = useLatestRef(options)
    
    // Initialize state
    val (tableState, setTableState) = useState(
        options.initialState ?: TableState(
            pagination = PaginationState(pageSize = options.pageSize)
        )
    )
    val tableStateRef = useLatestRef(tableState)

    // Create one table instance and make its imperative handlers read latest values from refs.
    val instance = remember { TableInstance<T>() }.apply {
        stateRef = tableStateRef
    }
    
    // Register features (collect in local list first)
    val featuresList = remember(
        options.enableSorting, options.enableFiltering, options.enablePagination,
        options.enableRowSelection, options.enableExpansion, options.enableGrouping,
        options.enableColumnSizing, options.enableColumnVisibility
    ) {
        buildList {
            if (options.enableColumnVisibility) add(ColumnVisibilityFeature<T>())
            if (options.enableFiltering) add(FilteringFeature<T>())
            if (options.enableSorting) add(SortingFeature<T>())
            if (options.enableGrouping) add(GroupingFeature<T>())
            if (options.enableExpansion) add(ExpansionFeature<T>())
            // Pagination moved out of pipeline to capture total rows
            if (options.enableRowSelection) add(RowSelectionFeature<T>())
            if (options.enableColumnSizing) add(ColumnSizingFeature<T>())
        }
    }
    
    // Wire up Sorting API
    instance.setSorting = { sorting: List<SortDescriptor> ->
        setTableState(tableStateRef.current.copy(sorting = SortingState(sorting)))
    }
    instance.toggleSorting = { columnId: String, desc: Boolean? ->
        val current = tableStateRef.current.sorting.sorting
        val existing = current.find { it.columnId == columnId }
        val newSorting = if (existing == null) {
            current + SortDescriptor(columnId, desc ?: false)
        } else if (desc == null) {
            if (!existing.desc) {
                current.map { if (it.columnId == columnId) it.copy(desc = true) else it }
            } else {
                current.filter { it.columnId != columnId }
            }
        } else {
            current.map { if (it.columnId == columnId) it.copy(desc = desc) else it }
        }
        instance.setSorting(newSorting)
    }
    instance.clearSorting = {
        instance.setSorting(emptyList())
    }
    
    // Wire up Filtering API
    instance.setGlobalFilter = { filter: String ->
        val currentState = tableStateRef.current
        setTableState(currentState.copy(filtering = currentState.filtering.copy(globalFilter = filter)))
    }
    instance.setColumnFilter = { columnId: String, value: Any? ->
        val currentState = tableStateRef.current
        val newFilters = currentState.filtering.columnFilters + (columnId to value)
        setTableState(currentState.copy(filtering = currentState.filtering.copy(columnFilters = newFilters)))
    }
    instance.clearFilters = {
        setTableState(tableStateRef.current.copy(filtering = FilteringState()))
    }
    
    // Wire up Pagination API
    instance.setPageIndex = { index: Int ->
        val currentState = tableStateRef.current
        setTableState(currentState.copy(pagination = currentState.pagination.copy(pageIndex = index)))
    }
    instance.setPageSize = { size: Int ->
        val currentState = tableStateRef.current
        setTableState(currentState.copy(pagination = currentState.pagination.copy(pageSize = size, pageIndex = 0)))
    }
    
    // Wire up Row Selection API
    instance.toggleRowSelection = { rowId: String ->
        val currentState = tableStateRef.current
        val current = currentState.rowSelection.selectedRowIds
        val newSelection = if (rowId in current) {
            current - rowId
        } else {
            current + rowId
        }
        setTableState(currentState.copy(rowSelection = RowSelectionState(newSelection)))
    }
    instance.toggleAllRowsSelection = { selected: Boolean? ->
        val currentData = dataRef.current
        val currentOptions = optionsRef.current
        val newSelection = if (selected == true) {
            currentData.indices.map { currentOptions.getRowId(currentData[it], it) }.toSet()
        } else {
            emptySet()
        }
        setTableState(tableStateRef.current.copy(rowSelection = RowSelectionState(newSelection)))
    }
    instance.clearRowSelection = {
        setTableState(tableStateRef.current.copy(rowSelection = RowSelectionState()))
    }
    
    // Wire up Expansion API
    instance.toggleRowExpanded = { rowId: String ->
        val currentState = tableStateRef.current
        val current = currentState.expanded.expanded
        val newExpanded = if (current[rowId] == true) {
            current - rowId
        } else {
            current + (rowId to true)
        }
        setTableState(currentState.copy(expanded = ExpandedState(newExpanded)))
    }
    instance.expandAll = {
        val currentData = dataRef.current
        val currentOptions = optionsRef.current
        val allRowIds = currentData.indices.map { currentOptions.getRowId(currentData[it], it) }
        val newExpanded = allRowIds.associateWith { true }
        setTableState(tableStateRef.current.copy(expanded = ExpandedState(newExpanded)))
    }
    instance.collapseAll = {
        setTableState(tableStateRef.current.copy(expanded = ExpandedState()))
    }
    instance.getIsRowExpanded = { rowId: String ->
        tableStateRef.current.expanded.expanded[rowId] == true
    }
    
    // Wire up Grouping API
    instance.setGrouping = { grouping: List<String> ->
        setTableState(tableStateRef.current.copy(grouping = GroupingState(grouping)))
    }
    
    // Wire up Column Sizing API
    instance.setColumnSize = { columnId: String, size: Float ->
        val currentState = tableStateRef.current
        val newSizing = currentState.columnSizing.columnSizing + (columnId to size)
        setTableState(currentState.copy(columnSizing = ColumnSizingState(newSizing)))
    }
    instance.resetColumnSizing = {
        setTableState(tableStateRef.current.copy(columnSizing = ColumnSizingState()))
    }
    
    // Wire up Column Visibility API
    instance.setColumnVisibility = { columnId: String, visible: Boolean ->
        val currentState = tableStateRef.current
        val newVisibility = currentState.columnVisibility.columnVisibility + (columnId to visible)
        setTableState(currentState.copy(columnVisibility = ColumnVisibilityState(newVisibility)))
    }
    instance.toggleAllColumnsVisible = {
        val currentState = tableStateRef.current
        val currentColumns = columnsRef.current
        val allHidden = currentColumns.all { currentState.columnVisibility.columnVisibility[it.id] == false }
        val newVisibility = if (allHidden) {
            emptyMap()
        } else {
            currentColumns.associate { it.id to false }
        }
        setTableState(currentState.copy(columnVisibility = ColumnVisibilityState(newVisibility)))
    }
    
    // Create core rows
    val coreRows = remember(data) {
        data.mapIndexed { index, item ->
            Row(
                id = options.getRowId(item, index),
                original = item,
                index = index
            )
        }
    }
    
    // Execute pipeline (filtering, sorting, etc.)
    val processedRows = remember(coreRows, tableState.sorting, tableState.filtering, tableState.grouping, columns, featuresList) {
        val pipeline = RowModelPipeline(featuresList)
        kotlin.runCatching {
            pipeline.execute(coreRows, tableState, columns)
        }.getOrElse { coreRows }
    }

    // Apply Pagination to get final rows
    val paginatedRows = remember(processedRows, tableState.pagination, options.enablePagination) {
        if (options.enablePagination) {
            PaginationFeature.paginate(processedRows, tableState.pagination)
        } else {
            processedRows
        }
    }
    
    // Calculate page count based on filtered rows (before pagination)
    val pageCount = PaginationFeature.pageCount(processedRows.size, tableState.pagination.pageSize)
    val pageCountRef = useLatestRef(pageCount)
    
    // Wire up pagination navigation functions
    instance.nextPage = {
        val currentIndex = tableStateRef.current.pagination.pageIndex
        if (currentIndex < pageCountRef.current - 1) {
            instance.setPageIndex(currentIndex + 1)
        }
    }
    instance.previousPage = {
        val currentIndex = tableStateRef.current.pagination.pageIndex
        if (currentIndex > 0) {
            instance.setPageIndex(currentIndex - 1)
        }
    }
    instance.getPageCount = { pageCountRef.current }
    instance.getCanNextPage = { tableStateRef.current.pagination.pageIndex < pageCountRef.current - 1 }
    instance.getCanPreviousPage = { tableStateRef.current.pagination.pageIndex > 0 }
    
    // Create row model
    val rowModel = remember(paginatedRows, processedRows) {
        RowModel(
            rows = paginatedRows,
            flatRows = processedRows,
            totalRows = processedRows.size
        )
    }
    
    val rowModelState = rememberUpdatedState(rowModel)
    val columnsState = rememberUpdatedState(columns)
    val tableStateState = rememberUpdatedState(tableState)

    // Return a stable holder with wrapper functions that dispatch to the latest instance handlers.
    return remember(instance, rowModelState, columnsState, tableStateState) {
        TableHolder(
            rowModel = rowModelState,
            columns = columnsState,
            state = tableStateState,
            setSorting = { sorting -> instance.setSorting(sorting) },
            toggleSorting = { columnId, desc -> instance.toggleSorting(columnId, desc) },
            clearSorting = { instance.clearSorting() },
            setGlobalFilter = { filter -> instance.setGlobalFilter(filter) },
            setColumnFilter = { columnId, value -> instance.setColumnFilter(columnId, value) },
            clearFilters = { instance.clearFilters() },
            setPageIndex = { index -> instance.setPageIndex(index) },
            setPageSize = { size -> instance.setPageSize(size) },
            nextPage = { instance.nextPage() },
            previousPage = { instance.previousPage() },
            toggleRowSelection = { rowId -> instance.toggleRowSelection(rowId) },
            toggleAllRowsSelection = { selected -> instance.toggleAllRowsSelection(selected) },
            clearRowSelection = { instance.clearRowSelection() },
            toggleRowExpanded = { rowId -> instance.toggleRowExpanded(rowId) },
            expandAll = { instance.expandAll() },
            collapseAll = { instance.collapseAll() },
            setGrouping = { grouping -> instance.setGrouping(grouping) },
            setColumnSize = { columnId, size -> instance.setColumnSize(columnId, size) },
            resetColumnSizing = { instance.resetColumnSizing() },
            setColumnVisibility = { columnId, visible -> instance.setColumnVisibility(columnId, visible) },
            toggleAllColumnsVisible = { instance.toggleAllColumnsVisible() },
        )
    }
}

/**
 * Object for scoping table-related hooks.
 */
object Table {
    /**
     * Legacy API - delegates to the new useTable hook with default options.
     * @deprecated Use useTable(data, columns, optionsOf {}) directly instead.
     */
    @Composable
    fun <T> useTable(): TableInstance<T> = error("Table.useTable() is deprecated. Use useTable(data, columns) with explicit data and columns instead.")

    /**
     * Accesses the current table instance from context (removed - not needed in new implementation).
     */
    @Composable
    fun <T> useTableInstance(): TableInstance<T> = error("useTableInstance is deprecated. Use the useTable hook directly.")
}
