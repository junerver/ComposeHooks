package xyz.junerver.compose.hooks.usetable.features.sorting

import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.usetable.core.ColumnDef
import xyz.junerver.compose.hooks.usetable.core.Row
import xyz.junerver.compose.hooks.usetable.core.TableFeature
import xyz.junerver.compose.hooks.usetable.core.TableInstance
import xyz.junerver.compose.hooks.usetable.state.TableState

class SortingFeature<T> : TableFeature<T> {
    override val featureId = "sorting"
    override val priority = 200

    @Composable
    override fun initState(instance: TableInstance<T>) {
        // TODO: Register state and API
    }

    override fun transform(
        rows: List<Row<T>>,
        state: TableState<T>,
        columns: List<ColumnDef<T, *>>
    ): List<Row<T>> {
        val sorting = state.sorting.sorting
        if (sorting.isEmpty()) return rows

        val enabledSorting = sorting.filter { sortDesc ->
            columns.any { it.id == sortDesc.columnId && it.enableSorting }
        }
        if (enabledSorting.isEmpty()) return rows

        val comparator = Comparator<Row<T>> { r1, r2 ->
            for (sortDesc in enabledSorting) {
                val column = columns.find { it.id == sortDesc.columnId && it.enableSorting } ?: continue

                val v1 = r1.getValue(column)
                val v2 = r2.getValue(column)

                val comparison = compareValues(v1 as? Comparable<Any>, v2 as? Comparable<Any>)

                if (comparison != 0) {
                    return@Comparator if (sortDesc.desc) -comparison else comparison
                }
            }
            0
        }

        return rows.sortedWith(comparator)
    }
}
