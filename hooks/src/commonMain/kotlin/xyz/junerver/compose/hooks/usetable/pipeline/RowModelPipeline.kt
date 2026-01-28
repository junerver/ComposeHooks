package xyz.junerver.compose.hooks.usetable.pipeline

import xyz.junerver.compose.hooks.usetable.core.ColumnDef
import xyz.junerver.compose.hooks.usetable.core.Row
import xyz.junerver.compose.hooks.usetable.core.TableFeature
import xyz.junerver.compose.hooks.usetable.state.TableState

/**
 * Manages the data transformation pipeline.
 */
class RowModelPipeline<T>(
    private val features: List<TableFeature<T>>
) {
    private val sortedFeatures = features.sortedBy { it.priority }

    /**
     * Execute the pipeline transformations in order.
     */
    fun execute(
        rows: List<Row<T>>,
        state: TableState<T>,
        columns: List<ColumnDef<T, *>>
    ): List<Row<T>> {
        var currentRows = rows
        for (feature in sortedFeatures) {
            currentRows = feature.transform(currentRows, state, columns)
        }
        return currentRows
    }
}
