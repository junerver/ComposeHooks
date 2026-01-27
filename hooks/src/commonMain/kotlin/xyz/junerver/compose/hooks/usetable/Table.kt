package xyz.junerver.compose.hooks.usetable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlin.math.ceil
import xyz.junerver.compose.hooks.createContext
import xyz.junerver.compose.hooks.useContext
import xyz.junerver.compose.hooks.usetable.core.ColumnDef
import xyz.junerver.compose.hooks.usetable.core.Row
import xyz.junerver.compose.hooks.usetable.state.TableState

/**
 * Internal table context for managing table state across components.
 * This context provides access to the table instance throughout the component tree.
 */
@Suppress("UNCHECKED_CAST")
internal val TableContext by lazy { createContext<TableHolder<Any>?>(null) }

/**
 * Headless Table Component.
 * Provides a scope [TableScope] to access table state and logic helpers.
 * This component does NOT render any UI itself, it only manages context and derived state.
 * 
 * @param table The table holder returned by [useTable]
 * @param modifier Optional modifier for the container (if any layout is implied, though here it's logic-only, we pass it to content or ignore)
 * @param content The content block where you can use [TableScope] methods.
 */
@Composable
fun <T> Table(
    table: TableHolder<T>,
    content: @Composable TableScope<T>.() -> Unit
) {
    val scope = remember(table) { TableScope(table) }
    @Suppress("UNCHECKED_CAST")
    TableContext.Provider(table as TableHolder<Any>) {
        scope.content()
    }
}

/**
 * Data class representing the state and actions available for pagination.
 */
@Stable
data class PaginationScope(
    val pageIndex: Int,
    val pageCount: Int,
    val pageSize: Int,
    val canNext: Boolean,
    val canPrev: Boolean,
    val nextPage: () -> Unit,
    val previousPage: () -> Unit,
    val setPageIndex: (Int) -> Unit,
    val setPageSize: (Int) -> Unit,
    val totalRows: Int
)

@Stable
class TableScope<T>(val table: TableHolder<T>) {
    
    /**
     * Exposes table header state to the content.
     * 
     * @param content Composable lambda that receives the list of columns and current table state (for sorting status etc).
     */
    @Composable
    fun TableHeader(
        content: @Composable (columns: List<ColumnDef<T, *>>, state: TableState<T>) -> Unit
    ) {
        val columns by table.columns
        val tableState by table.state
        content(columns, tableState)
    }

    /**
     * Exposes table body rows to the content.
     * 
     * @param content Composable lambda that receives the list of processed rows.
     */
    @Composable
    fun TableBody(
        content: @Composable (rows: List<Row<T>>) -> Unit
    ) {
        val rowModel by table.rowModel
        content(rowModel.rows)
    }

    /**
     * Exposes pagination state and actions to the content.
     * This component handles the calculation of page counts and navigation logic.
     * 
     * @param content Composable lambda that receives [PaginationScope] with all pagination data and functions.
     */
    @Composable
    fun TablePagination(
        content: @Composable (PaginationScope) -> Unit
    ) {
        val tableState by table.state
        val rowModel by table.rowModel
        
        val pageIndex = tableState.pagination.pageIndex
        val pageSize = tableState.pagination.pageSize
        val totalRows = rowModel.totalRows
        
        // Logic calculation for pagination
        val pageCount = if (pageSize <= 0) 1 else ceil(totalRows.toDouble() / pageSize).toInt().coerceAtLeast(1)
        val canNext = pageIndex < pageCount - 1
        val canPrev = pageIndex > 0

        val scope = PaginationScope(
            pageIndex = pageIndex,
            pageCount = pageCount,
            pageSize = pageSize,
            canNext = canNext,
            canPrev = canPrev,
            nextPage = table.nextPage,
            previousPage = table.previousPage,
            setPageIndex = table.setPageIndex,
            setPageSize = table.setPageSize,
            totalRows = totalRows
        )
        
        content(scope)
    }
}
