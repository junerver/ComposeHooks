package xyz.junerver.compose.hooks.usetable.features.rowselection

import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.usetable.core.ColumnDef
import xyz.junerver.compose.hooks.usetable.core.Row
import xyz.junerver.compose.hooks.usetable.core.TableFeature
import xyz.junerver.compose.hooks.usetable.core.TableInstance
import xyz.junerver.compose.hooks.usetable.state.TableState

class RowSelectionFeature<T> : TableFeature<T> {
    override val featureId = "rowSelection"
    // Row selection doesn't filter rows, but it might decorate them.
    // Usually it runs unrelated to the pipeline order, but we can set it to run 
    // to attach selection metadata if needed. 
    // However, since selection is stored in state, we don't necessarily need 
    // to transform the row list unless we want to filter selected rows (rare).
    // So priority is less important here, but let's put it early.
    override val priority = 50 

    @Composable
    override fun initState(instance: TableInstance<T>) {
        // TODO: Register API
    }

    override suspend fun transform(
        rows: List<Row<T>>,
        state: TableState<T>,
        columns: List<ColumnDef<T, *>>
    ): List<Row<T>> {
        // Row selection typically doesn't remove rows from the model.
        // It just ensures the state matches.
        // We could potentially inject "isSelected" metadata here if we wanted, 
        // but it's better to look it up from state during rendering.
        return rows
    }
}
