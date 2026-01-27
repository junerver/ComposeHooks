package xyz.junerver.compose.hooks.usetable.core

import androidx.compose.runtime.Stable

/**
 * Type-safe column definition.
 */
@Stable
data class ColumnDef<T, V>(
    val id: String,
    val header: String = id,
    val accessorFn: (T) -> V,
    val enableSorting: Boolean = true,
    val enableFiltering: Boolean = true
)

/**
 * DSL helper to create a column definition.
 */
fun <T, V> column(
    id: String,
    header: String = id,
    enableSorting: Boolean = true,
    enableFiltering: Boolean = true,
    accessorFn: (T) -> V
): ColumnDef<T, V> = ColumnDef(
    id = id,
    header = header,
    accessorFn = accessorFn,
    enableSorting = enableSorting,
    enableFiltering = enableFiltering
)
