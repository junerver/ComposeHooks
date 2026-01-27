package xyz.junerver.compose.hooks.usetable.core

import androidx.compose.runtime.MutableState
import xyz.junerver.compose.hooks.Ref
import xyz.junerver.compose.hooks.usetable.state.SortDescriptor
import xyz.junerver.compose.hooks.usetable.state.TableState

/**
 * The Table Instance (Controller).
 */
class TableInstance<T> internal constructor() {
    // Make these public for access from hooks.kt
    lateinit var stateRef: Ref<TableState<T>>
    val features = mutableListOf<TableFeature<T>>()
    
    // ========== Sorting API ==========
    var setSorting: (List<SortDescriptor>) -> Unit = {}
    var toggleSorting: (String, Boolean?) -> Unit = { _, _ -> }
    var clearSorting: () -> Unit = {}

    // ========== Filtering API ==========
    var setGlobalFilter: (String) -> Unit = {}
    var setColumnFilter: (String, Any?) -> Unit = { _, _ -> }
    var clearFilters: () -> Unit = {}

    // ========== Pagination API ==========
    var setPageIndex: (Int) -> Unit = {}
    var setPageSize: (Int) -> Unit = {}
    var nextPage: () -> Unit = {}
    var previousPage: () -> Unit = {}
    var getPageCount: () -> Int = { 0 }
    var getCanNextPage: () -> Boolean = { false }
    var getCanPreviousPage: () -> Boolean = { false }

    // ========== Row Selection API ==========
    var toggleRowSelection: (String) -> Unit = {}
    var toggleAllRowsSelection: (Boolean?) -> Unit = {}
    var clearRowSelection: () -> Unit = {}
    
    // ========== Expansion API ==========
    var toggleRowExpanded: (String) -> Unit = {}
    var expandAll: () -> Unit = {}
    var collapseAll: () -> Unit = {}
    var getIsRowExpanded: (String) -> Boolean = { false }
    
    // ========== Grouping API ==========
    var setGrouping: (List<String>) -> Unit = {}
    
    // ========== Column Sizing API ==========
    var setColumnSize: (String, Float) -> Unit = { _, _ -> }
    var resetColumnSizing: () -> Unit = {}
    
    // ========== Column Visibility API ==========
    var setColumnVisibility: (String, Boolean) -> Unit = { _, _ -> }
    var toggleAllColumnsVisible: () -> Unit = {}
    
    internal fun getState(): TableState<T> = stateRef.current
}
