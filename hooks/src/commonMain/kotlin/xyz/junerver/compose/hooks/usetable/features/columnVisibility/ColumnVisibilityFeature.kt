package xyz.junerver.compose.hooks.usetable.features.columnVisibility

import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.usetable.core.ColumnDef
import xyz.junerver.compose.hooks.usetable.core.Row
import xyz.junerver.compose.hooks.usetable.core.TableFeature
import xyz.junerver.compose.hooks.usetable.core.TableInstance
import xyz.junerver.compose.hooks.usetable.state.TableState

class ColumnVisibilityFeature<T> : TableFeature<T> {
    override val featureId = "columnVisibility"
    override val priority = 10

    @Composable
    override fun initState(instance: TableInstance<T>) {
        // State wiring in Phase 7
    }

    override suspend fun transform(
        rows: List<Row<T>>,
        state: TableState<T>,
        columns: List<ColumnDef<T, *>>
    ): List<Row<T>> {
        // Column visibility is checked during rendering, doesn't affect row model
        return rows
    }
}
