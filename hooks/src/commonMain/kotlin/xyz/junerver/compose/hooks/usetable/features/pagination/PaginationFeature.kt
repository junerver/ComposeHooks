package xyz.junerver.compose.hooks.usetable.features.pagination

import androidx.compose.runtime.Composable
import kotlin.math.ceil
import kotlin.math.min
import xyz.junerver.compose.hooks.usetable.core.ColumnDef
import xyz.junerver.compose.hooks.usetable.core.Row
import xyz.junerver.compose.hooks.usetable.core.TableFeature
import xyz.junerver.compose.hooks.usetable.core.TableInstance
import xyz.junerver.compose.hooks.usetable.state.PaginationState
import xyz.junerver.compose.hooks.usetable.state.TableState

class PaginationFeature<T> : TableFeature<T> {
    override val featureId = "pagination"
    override val priority = 400

    @Composable
    override fun initState(instance: TableInstance<T>) {
    }

    override fun transform(
        rows: List<Row<T>>,
        state: TableState<T>,
        columns: List<ColumnDef<T, *>>
    ): List<Row<T>> {
        return paginate(rows, state.pagination)
    }

    companion object {
        fun pageCount(totalRows: Int, pageSize: Int): Int {
            if (pageSize <= 0) return 1
            return ceil(totalRows.toDouble() / pageSize).toInt().coerceAtLeast(1)
        }

        fun canNext(pageIndex: Int, pageCount: Int): Boolean {
            return pageIndex < pageCount - 1
        }

        fun canPrev(pageIndex: Int): Boolean {
            return pageIndex > 0
        }

        fun <T> paginate(rows: List<Row<T>>, pagination: PaginationState): List<Row<T>> {
            val pageSize = pagination.pageSize
            if (pageSize <= 0) return rows

            val startIndex = (pagination.pageIndex * pageSize).coerceAtLeast(0)
            if (startIndex >= rows.size) return emptyList()

            val endIndex = min(startIndex + pageSize, rows.size)
            return rows.subList(startIndex, endIndex)
        }
    }
}
