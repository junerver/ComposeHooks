@file:Suppress("UnusedReceiverParameter")

package xyz.junerver.compose.hooks.usetable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import xyz.junerver.compose.hooks.useContext
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.useRef
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
import kotlin.math.ceil

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
    var getRowId: (T, Int) -> String = { _, index -> index.toString() }
}

/**
 * Return type for useTable hook containing all table state and controls.
 */
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
    val options = TableOptions<T>().apply(optionsOf)
    
    // Create table instance (direct creation, no ref needed)
    val instance = TableInstance<T>()
    
    // Initialize state
    val (tableState, setTableState) = useState(
        options.initialState ?: TableState(
            pagination = PaginationState(pageSize = options.pageSize)
        )
    )
    
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
            if (options.enablePagination) add(PaginationFeature<T>())
            if (options.enableRowSelection) add(RowSelectionFeature<T>())
            if (options.enableColumnSizing) add(ColumnSizingFeature<T>())
        }
    }
    
    // Wire up Sorting API
    instance.setSorting = { sorting: List<SortDescriptor> ->
        setTableState(tableState.copy(sorting = SortingState(sorting)))
    }
    instance.toggleSorting = { columnId: String, desc: Boolean? ->
        val current = tableState.sorting.sorting
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
        setTableState(tableState.copy(filtering = tableState.filtering.copy(globalFilter = filter)))
    }
    instance.setColumnFilter = { columnId: String, value: Any? ->
        val newFilters = tableState.filtering.columnFilters + (columnId to value)
        setTableState(tableState.copy(filtering = tableState.filtering.copy(columnFilters = newFilters)))
    }
    instance.clearFilters = {
        setTableState(tableState.copy(filtering = FilteringState()))
    }
    
    // Wire up Pagination API
    instance.setPageIndex = { index: Int ->
        setTableState(tableState.copy(pagination = tableState.pagination.copy(pageIndex = index)))
    }
    instance.setPageSize = { size: Int ->
        setTableState(tableState.copy(pagination = tableState.pagination.copy(pageSize = size, pageIndex = 0)))
    }
    
    val pageCount = derivedStateOf {
        val totalRows = data.size
        val pageSize = tableState.pagination.pageSize
        if (pageSize <= 0) 1 else ceil(totalRows.toDouble() / pageSize).toInt()
    }
    
    instance.nextPage = {
        val currentIndex = tableState.pagination.pageIndex
        if (currentIndex < pageCount.value - 1) {
            instance.setPageIndex(currentIndex + 1)
        }
    }
    instance.previousPage = {
        val currentIndex = tableState.pagination.pageIndex
        if (currentIndex > 0) {
            instance.setPageIndex(currentIndex - 1)
        }
    }
    instance.getPageCount = { pageCount.value }
    instance.getCanNextPage = { tableState.pagination.pageIndex < pageCount.value - 1 }
    instance.getCanPreviousPage = { tableState.pagination.pageIndex > 0 }
    
    // Wire up Row Selection API
    instance.toggleRowSelection = { rowId: String ->
        val current = tableState.rowSelection.selectedRowIds
        val newSelection = if (rowId in current) {
            current - rowId
        } else {
            current + rowId
        }
        setTableState(tableState.copy(rowSelection = RowSelectionState(newSelection)))
    }
    instance.toggleAllRowsSelection = { selected: Boolean? ->
        val newSelection = if (selected == true) {
            data.indices.map { options.getRowId(data[it], it) }.toSet()
        } else {
            emptySet()
        }
        setTableState(tableState.copy(rowSelection = RowSelectionState(newSelection)))
    }
    instance.clearRowSelection = {
        setTableState(tableState.copy(rowSelection = RowSelectionState()))
    }
    
    // Wire up Expansion API
    instance.toggleRowExpanded = { rowId: String ->
        val current = tableState.expanded.expanded
        val newExpanded = if (current[rowId] == true) {
            current - rowId
        } else {
            current + (rowId to true)
        }
        setTableState(tableState.copy(expanded = ExpandedState(newExpanded)))
    }
    instance.expandAll = {
        val allRowIds = data.indices.map { options.getRowId(data[it], it) }
        val newExpanded = allRowIds.associateWith { true }
        setTableState(tableState.copy(expanded = ExpandedState(newExpanded)))
    }
    instance.collapseAll = {
        setTableState(tableState.copy(expanded = ExpandedState()))
    }
    instance.getIsRowExpanded = { rowId: String ->
        tableState.expanded.expanded[rowId] == true
    }
    
    // Wire up Grouping API
    instance.setGrouping = { grouping: List<String> ->
        setTableState(tableState.copy(grouping = GroupingState(grouping)))
    }
    
    // Wire up Column Sizing API
    instance.setColumnSize = { columnId: String, size: Float ->
        val newSizing = tableState.columnSizing.columnSizing + (columnId to size)
        setTableState(tableState.copy(columnSizing = ColumnSizingState(newSizing)))
    }
    instance.resetColumnSizing = {
        setTableState(tableState.copy(columnSizing = ColumnSizingState()))
    }
    
    // Wire up Column Visibility API
    instance.setColumnVisibility = { columnId: String, visible: Boolean ->
        val newVisibility = tableState.columnVisibility.columnVisibility + (columnId to visible)
        setTableState(tableState.copy(columnVisibility = ColumnVisibilityState(newVisibility)))
    }
    instance.toggleAllColumnsVisible = {
        val allHidden = columns.all { tableState.columnVisibility.columnVisibility[it.id] == false }
        val newVisibility = if (allHidden) {
            emptyMap()
        } else {
            columns.associate { it.id to false }
        }
        setTableState(tableState.copy(columnVisibility = ColumnVisibilityState(newVisibility)))
    }
    
    // Create core rows
    val coreRows = remember(data, options.getRowId) {
        data.mapIndexed { index, item ->
            Row(
                id = options.getRowId(item, index),
                original = item,
                index = index
            )
        }
    }
    
    // Execute pipeline
    val processedRows = remember(coreRows, tableState, columns, featuresList) {
        val pipeline = RowModelPipeline(featuresList)
        kotlin.runCatching {
            kotlinx.coroutines.runBlocking {
                pipeline.execute(coreRows, tableState, columns)
            }
        }.getOrElse { coreRows }
    }
    
    // Create row model
    val rowModel = remember(processedRows) {
        RowModel(
            rows = processedRows,
            flatRows = processedRows
        )
    }
    
    // Return TableHolder
    return TableHolder(
        rowModel = derivedStateOf { rowModel },
        columns = derivedStateOf { columns },
        state = derivedStateOf { tableState },
        setSorting = instance.setSorting,
        toggleSorting = instance.toggleSorting,
        clearSorting = instance.clearSorting,
        setGlobalFilter = instance.setGlobalFilter,
        setColumnFilter = instance.setColumnFilter,
        clearFilters = instance.clearFilters,
        setPageIndex = instance.setPageIndex,
        setPageSize = instance.setPageSize,
        nextPage = instance.nextPage,
        previousPage = instance.previousPage,
        toggleRowSelection = instance.toggleRowSelection,
        toggleAllRowsSelection = instance.toggleAllRowsSelection,
        clearRowSelection = instance.clearRowSelection,
        toggleRowExpanded = instance.toggleRowExpanded,
        expandAll = instance.expandAll,
        collapseAll = instance.collapseAll,
        setGrouping = instance.setGrouping,
        setColumnSize = instance.setColumnSize,
        resetColumnSizing = instance.resetColumnSizing,
        setColumnVisibility = instance.setColumnVisibility,
        toggleAllColumnsVisible = instance.toggleAllColumnsVisible
    )
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
