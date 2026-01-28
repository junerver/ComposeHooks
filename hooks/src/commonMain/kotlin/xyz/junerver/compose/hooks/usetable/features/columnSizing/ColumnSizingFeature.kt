package xyz.junerver.compose.hooks.usetable.features.columnSizing

import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.usetable.core.ColumnDef
import xyz.junerver.compose.hooks.usetable.core.Row
import xyz.junerver.compose.hooks.usetable.core.TableFeature
import xyz.junerver.compose.hooks.usetable.core.TableInstance
import xyz.junerver.compose.hooks.usetable.state.TableState

class ColumnSizingFeature<T> : TableFeature<T> {
    override val featureId = "columnSizing"
    override val priority = 500

    @Composable
    override fun initState(instance: TableInstance<T>) {
        // State wiring in Phase 7
    }

    override fun transform(
        rows: List<Row<T>>,
        state: TableState<T>,
        columns: List<ColumnDef<T, *>>
    ): List<Row<T>> {
        // Column sizing doesn't affect row model
        return rows
    }
}
