package xyz.junerver.compose.hooks.usetable.features.expansion

import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.usetable.core.ColumnDef
import xyz.junerver.compose.hooks.usetable.core.Row
import xyz.junerver.compose.hooks.usetable.core.TableFeature
import xyz.junerver.compose.hooks.usetable.core.TableInstance
import xyz.junerver.compose.hooks.usetable.state.TableState

class ExpansionFeature<T> : TableFeature<T> {
    override val featureId = "expansion"
    override val priority = 300

    @Composable
    override fun initState(instance: TableInstance<T>) {
        // State wiring will be done in Phase 7 (useTable hook)
    }

    override suspend fun transform(
        rows: List<Row<T>>,
        state: TableState<T>,
        columns: List<ColumnDef<T, *>>
    ): List<Row<T>> {
        val expandedState = state.expanded.expanded
        if (expandedState.isEmpty()) return rows

        val flatRows = mutableListOf<Row<T>>()
        
        fun flatten(rowList: List<Row<T>>) {
            for (row in rowList) {
                flatRows.add(row)
                val isExpanded = expandedState[row.id] == true
                if (isExpanded && row.subRows.isNotEmpty()) {
                    flatten(row.subRows)
                }
            }
        }
        
        flatten(rows)
        return flatRows
    }
}
