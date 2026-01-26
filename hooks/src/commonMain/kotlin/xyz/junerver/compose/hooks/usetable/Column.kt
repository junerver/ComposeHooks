package xyz.junerver.compose.hooks.usetable

import androidx.compose.runtime.Stable

/*
  Description: Column definition for useTable
  Author: Claude
  Date: 2025/1/26
  Email: noreply@anthropic.com
  Version: v1.0
*/

/**
 * Column definition for table.
 *
 * @param T The type of the row data
 * @param V The type of the cell value
 * @property id Unique identifier for the column
 * @property accessorFn Function to extract cell value from row data
 * @property header Display text for column header
 * @property enableSorting Whether sorting is enabled for this column
 * @property enableFiltering Whether filtering is enabled for this column
 * @property sortingFn Custom comparator for sorting
 * @property filterFn Custom filter function
 */
@Stable
data class ColumnDef<T, V>(
    val id: ColumnId,
    val accessorFn: (T) -> V,
    val header: String = id,
    val enableSorting: Boolean = true,
    val enableFiltering: Boolean = true,
    val sortingFn: Comparator<V>? = null,
    val filterFn: ((V, Any?) -> Boolean)? = null,
)

/**
 * Runtime column instance with resolved state.
 */
@Stable
data class Column<T, V>(
    val id: ColumnId,
    val def: ColumnDef<T, V>,
    val isVisible: Boolean = true,
    val isSorted: Boolean = false,
    val sortDirection: SortDirection? = null,
) {
    fun getValue(row: T): V = def.accessorFn(row)
}

/**
 * Sort direction enum.
 */
enum class SortDirection {
    ASC,
    DESC,
    ;

    fun toggle(): SortDirection = when (this) {
        ASC -> DESC
        DESC -> ASC
    }
}

/**
 * DSL builder for creating column definitions.
 */
fun <T, V> column(
    id: ColumnId,
    header: String = id,
    enableSorting: Boolean = true,
    enableFiltering: Boolean = true,
    sortingFn: Comparator<V>? = null,
    filterFn: ((V, Any?) -> Boolean)? = null,
    accessorFn: (T) -> V,
): ColumnDef<T, V> = ColumnDef(
    id = id,
    accessorFn = accessorFn,
    header = header,
    enableSorting = enableSorting,
    enableFiltering = enableFiltering,
    sortingFn = sortingFn,
    filterFn = filterFn,
)
