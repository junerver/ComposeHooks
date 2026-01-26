package xyz.junerver.compose.hooks.usetable

import androidx.compose.runtime.Stable

/*
  Description: Row definition for useTable
  Author: Claude
  Date: 2025/1/26
  Email: noreply@anthropic.com
  Version: v1.0
*/

/**
 * Row wrapper containing original data and computed properties.
 *
 * @param T The type of the row data
 * @property id Unique identifier for the row
 * @property index Original index in the data array
 * @property original The original row data
 * @property isSelected Whether the row is selected
 */
@Stable
data class Row<T>(
    val id: RowId,
    val index: Int,
    val original: T,
    val isSelected: Boolean = false,
) {
    /**
     * Get cell value for a specific column.
     */
    fun <V> getValue(column: ColumnDef<T, V>): V = column.accessorFn(original)
}
