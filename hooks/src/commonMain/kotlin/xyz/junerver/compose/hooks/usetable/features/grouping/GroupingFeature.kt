package xyz.junerver.compose.hooks.usetable.features.grouping

import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.usetable.core.ColumnDef
import xyz.junerver.compose.hooks.usetable.core.Row
import xyz.junerver.compose.hooks.usetable.core.TableFeature
import xyz.junerver.compose.hooks.usetable.core.TableInstance
import xyz.junerver.compose.hooks.usetable.state.TableState

class GroupingFeature<T> : TableFeature<T> {
    override val featureId = "grouping"
    override val priority = 250

    @Composable
    override fun initState(instance: TableInstance<T>) {
        // State wiring in Phase 7
    }

    override fun transform(
        rows: List<Row<T>>,
        state: TableState<T>,
        columns: List<ColumnDef<T, *>>
    ): List<Row<T>> {
        val grouping = state.grouping.grouping
        if (grouping.isEmpty()) return rows

        // Simplified grouping: Group rows by first column in grouping list
        val groupColumn = columns.find { it.id == grouping.first() } ?: return rows
        
        val groups = rows.groupBy { row -> row.getValue(groupColumn) }

        return groups.map { (groupValue, groupRows) ->
            val groupKey = groupValue?.toString() ?: "null"
            Row(
                id = "group-${groupColumn.id}-$groupKey",
                original = groupRows.first().original,
                index = 0,
                depth = 0,
                subRows = groupRows,
                metadata = mapOf("isGroupHeader" to true, "groupValue" to groupValue, "groupKey" to groupKey)
            )
        }
    }
}
