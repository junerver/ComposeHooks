package xyz.junerver.compose.hooks.usetable.core

import androidx.compose.runtime.Stable

/**
 * Row data model representing a single row in the table.
 */
@Stable
data class Row<T>(
    val id: String,
    val original: T,
    val index: Int,
    val depth: Int = 0,
    val parentId: String? = null,
    val subRows: List<Row<T>> = emptyList(),
    val metadata: Map<String, Any?> = emptyMap()
) {
    /**
     * Get value from a column using its accessor.
     */
    fun <V> getValue(column: ColumnDef<T, V>): V {
        return column.accessorFn(original)
    }
}

/**
 * The full row model containing the current state of rows.
 */
@Stable
data class RowModel<T>(
    val rows: List<Row<T>>,
    val flatRows: List<Row<T>>, // Flattened view (including expanded sub-rows)
    val totalRows: Int = rows.size
)
