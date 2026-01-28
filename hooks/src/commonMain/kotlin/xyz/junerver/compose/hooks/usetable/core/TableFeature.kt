package xyz.junerver.compose.hooks.usetable.core

import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.usetable.state.TableState

/**
 * Interface for table features (plugins).
 */
interface TableFeature<T> {
    val featureId: String
    val priority: Int

    @Composable
    fun initState(instance: TableInstance<T>)

    /**
     * Transform the row model.
     * Now includes [columns] to allow features to look up column definitions (e.g. for sorting/filtering).
     */
    fun transform(
        rows: List<Row<T>>,
        state: TableState<T>,
        columns: List<ColumnDef<T, *>>
    ): List<Row<T>>
}
