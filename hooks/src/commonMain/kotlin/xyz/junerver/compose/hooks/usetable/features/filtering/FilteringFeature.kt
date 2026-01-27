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

    override suspend fun transform(
        rows: List<Row<T>>,
        state: TableState<T>,
        columns: List<ColumnDef<T, *>>
    ): List<Row<T>> {
        val globalFilter = state.filtering.globalFilter
        val columnFilters = state.filtering.columnFilters

        if (globalFilter.isBlank() && columnFilters.isEmpty()) {
            return rows
        }

        return rows.filter { row ->
            // 1. Column Filters
            val passColumnFilters = columnFilters.all { (colId, filterValue) ->
                if (filterValue == null) return@all true
                val column = columns.find { it.id == colId } ?: return@all true
                
                val cellValue = row.getValue(column)
                // Simple equality check for Phase 1. 
                // Phase 3 refactor will add FilterFns (includes, equals, etc.)
                cellValue.toString().contains(filterValue.toString(), ignoreCase = true)
            }

            if (!passColumnFilters) return@filter false

            // 2. Global Filter
            if (globalFilter.isNotBlank()) {
                // Search across all columns
                return@filter columns.any { column ->
                    val cellValue = row.getValue(column)
                    cellValue.toString().contains(globalFilter, ignoreCase = true)
                }
            }

            true
        }
    }
}
