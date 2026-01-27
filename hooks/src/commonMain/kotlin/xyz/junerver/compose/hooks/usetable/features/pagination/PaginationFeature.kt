package xyz.junerver.compose.hooks.usetable.features.pagination

import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.usetable.core.ColumnDef
import xyz.junerver.compose.hooks.usetable.core.Row
import xyz.junerver.compose.hooks.usetable.core.TableFeature
import xyz.junerver.compose.hooks.usetable.core.TableInstance
import xyz.junerver.compose.hooks.usetable.state.TableState
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class PaginationFeature<T> : TableFeature<T> {
    override val featureId = "pagination"
    override val priority = 400 // Execute LAST (after filtering & sorting)

    @Composable
    override fun initState(instance: TableInstance<T>) {
        // In actual hook implementation, we will wire up the state setters here.
        // For now, we define the transform logic.
    }

    override suspend fun transform(
        rows: List<Row<T>>,
        state: TableState<T>,
        columns: List<ColumnDef<T, *>>
    ): List<Row<T>> {
        val pageIndex = state.pagination.pageIndex
        val pageSize = state.pagination.pageSize
        
        if (pageSize <= 0) return rows

        val startIndex = pageIndex * pageSize
        // Boundary check
        if (startIndex >= rows.size) return emptyList()
        
        val endIndex = min(startIndex + pageSize, rows.size)
        return rows.subList(startIndex, endIndex)
    }
}
