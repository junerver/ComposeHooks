package xyz.junerver.compose.hooks.usetable.features.filtering

import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.usetable.core.ColumnDef
import xyz.junerver.compose.hooks.usetable.core.Row
import xyz.junerver.compose.hooks.usetable.core.TableFeature
import xyz.junerver.compose.hooks.usetable.core.TableInstance
import xyz.junerver.compose.hooks.usetable.state.TableState

class FilteringFeature<T> : TableFeature<T> {
    override val featureId = "filtering"
    override val priority = 100 // Execute BEFORE sorting (200)

    @Composable
    override fun initState(instance: TableInstance<T>) {
        // TODO: Register state and API
    }

    override fun transform(
        rows: List<Row<T>>,
        state: TableState<T>,
        columns: List<ColumnDef<T, *>>
    ): List<Row<T>> {
        val globalFilter = state.filtering.globalFilter
        val columnFilters = state.filtering.columnFilters

        if (globalFilter.isBlank() && columnFilters.isEmpty()) {
            return rows
        }

        val filterableColumns = columns.filter { it.enableFiltering }
        if (filterableColumns.isEmpty()) {
            return rows
        }

        return rows.filter { row ->
            val passColumnFilters = columnFilters.all { (colId, filterValue) ->
                if (filterValue == null) return@all true
                val column = filterableColumns.find { it.id == colId } ?: return@all true

                val cellValue = row.getValue(column)
                cellValue.toString().contains(filterValue.toString(), ignoreCase = true)
            }

            if (!passColumnFilters) return@filter false

            if (globalFilter.isNotBlank()) {
                return@filter filterableColumns.any { column ->
                    val cellValue = row.getValue(column)
                    cellValue.toString().contains(globalFilter, ignoreCase = true)
                }
            }

            true
        }
    }
}
