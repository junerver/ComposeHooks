package xyz.junerver.compose.hooks.usetable

/*
  Description: Type aliases for useTable
  Author: Claude
  Date: 2025/1/26
  Email: noreply@anthropic.com
  Version: v1.0
*/

typealias RowId = String
typealias ColumnId = String

typealias ToggleSortingFn = (ColumnId) -> Unit
typealias SetPageIndexFn = (Int) -> Unit
typealias SetPageSizeFn = (Int) -> Unit
typealias NextPageFn = () -> Unit
typealias PreviousPageFn = () -> Unit
typealias SetColumnFilterFn = (ColumnId, Any?) -> Unit
typealias SetGlobalFilterFn = (String) -> Unit
typealias ToggleRowSelectionFn = (RowId) -> Unit
typealias ToggleAllRowsSelectionFn = () -> Unit
typealias ToggleColumnVisibilityFn = (ColumnId) -> Unit
typealias GetRowIdFn<T> = (T, Int) -> RowId
