package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.Test
import xyz.junerver.compose.hooks.Table as RootTable
import xyz.junerver.compose.hooks.rememberTable as rememberRootTable
import xyz.junerver.compose.hooks.rememberTableInstance as rememberRootTableInstance
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.usetable.Table
import xyz.junerver.compose.hooks.usetable.TableHolder
import xyz.junerver.compose.hooks.usetable.core.column
import xyz.junerver.compose.hooks.usetable.useTable

/*
  Description: useTable desktop integration tests
  Author: Junerver
  Date: 2026/06/17
  Email: junerver@gmail.com
  Version: v1.0
*/

@Suppress("DEPRECATION")
class UseTableTest {
    private data class User(val name: String, val age: Int)

    private val users = listOf(
        User("Charlie", 30),
        User("Alice", 25),
        User("Bob", 20),
    )

    private val columns = listOf(
        column<User, String>("name") { it.name },
        column<User, Int>("age") { it.age },
    )

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun holder_api_updates_sorting_and_filtering_state() = runComposeUiTest {
        setContent {
            var phase by useState(default = 0)
            val table = useTable(
                data = users,
                columns = columns,
                optionsOf = {
                    enableSorting = true
                    enableFiltering = true
                },
            )

            SideEffect {
                when (phase) {
                    0 -> {
                        table.toggleSorting("name", false)
                        phase = 1
                    }

                    1 -> {
                        table.setGlobalFilter("o")
                        phase = 2
                    }
                }
            }

            val rows = table.rowModel.value.rows.joinToString(",") { it.original.name }
            Text(
                text = "phase=$phase rows=$rows total=${table.rowModel.value.totalRows} " +
                    "sorting=${table.state.value.sorting.sorting.size} filter=${table.state.value.filtering.globalFilter}",
            )
        }

        waitForIdle()
        onNodeWithText("phase=2 rows=Bob total=1 sorting=1 filter=o").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun holder_api_updates_pagination_and_row_selection_state() = runComposeUiTest {
        setContent {
            var phase by useState(default = 0)
            val table = useTable(
                data = users,
                columns = columns,
                optionsOf = {
                    enablePagination = true
                    enableRowSelection = true
                    pageSize = 2
                },
            )

            SideEffect {
                when (phase) {
                    0 -> {
                        table.setPageIndex(1)
                        phase = 1
                    }

                    1 -> {
                        table.toggleRowSelection("2")
                        phase = 2
                    }
                }
            }

            val rows = table.rowModel.value.rows.joinToString(",") { it.original.name }
            Text(
                text = "phase=$phase page=${table.state.value.pagination.pageIndex} rows=$rows " +
                    "selected=${table.state.value.rowSelection.selectedRowIds.size}",
            )
        }

        waitForIdle()
        onNodeWithText("phase=2 page=1 rows=Bob selected=1").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun column_filter_null_clears_existing_filter() = runComposeUiTest {
        setContent {
            var phase by useState(default = 0)
            val table = useTable(
                data = users,
                columns = columns,
                optionsOf = {
                    enableFiltering = true
                },
            )

            SideEffect {
                when (phase) {
                    0 -> {
                        table.setColumnFilter("name", "Alice")
                        phase = 1
                    }

                    1 -> {
                        table.setColumnFilter("name", null)
                        phase = 2
                    }
                }
            }

            Text(
                text = "phase=$phase total=${table.rowModel.value.totalRows} " +
                    "filters=${table.state.value.filtering.columnFilters.size}",
            )
        }

        waitForIdle()
        onNodeWithText("phase=2 total=3 filters=0").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun dynamic_get_row_id_rebuilds_rows_and_bulk_selection_with_matching_ids() = runComposeUiTest {
        var useNameIds by mutableStateOf(false)
        lateinit var table: TableHolder<User>

        setContent {
            table = useTable(
                data = users,
                columns = columns,
                optionsOf = {
                    enableRowSelection = true
                    getRowId = if (useNameIds) {
                        { user, _ -> "name-${user.name}" }
                    } else {
                        { _, index -> "index-$index" }
                    }
                },
            )
            Text(table.rowModel.value.rows.joinToString(",") { it.id })
        }

        waitForIdle()
        assertEquals(listOf("index-0", "index-1", "index-2"), table.rowModel.value.rows.map { it.id })

        useNameIds = true
        waitForIdle()
        assertEquals(listOf("name-Charlie", "name-Alice", "name-Bob"), table.rowModel.value.rows.map { it.id })

        runOnIdle { table.toggleAllRowsSelection(true) }
        waitForIdle()
        assertEquals(
            table.rowModel.value.rows.map { it.id }.toSet(),
            table.state.value.rowSelection.selectedRowIds,
        )
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun remember_table_alias_keeps_holder_and_action_identity() = runComposeUiTest {
        var marker by mutableStateOf(0)
        lateinit var table: TableHolder<User>
        var initialHolder: TableHolder<User>? = null

        setContent {
            table = rememberRootTable(users, columns)
            if (initialHolder == null) initialHolder = table
            Text("marker=$marker rows=${table.rowModel.value.rows.size}")
        }

        waitForIdle()
        val first = initialHolder ?: error("table was not composed")
        val initialToggleSorting = first.toggleSorting
        val initialSetPageSize = first.setPageSize
        val initialToggleAllRowsSelection = first.toggleAllRowsSelection

        marker = 1
        waitForIdle()

        assertSame(first, table)
        assertSame(initialToggleSorting, table.toggleSorting)
        assertSame(initialSetPageSize, table.setPageSize)
        assertSame(initialToggleAllRowsSelection, table.toggleAllRowsSelection)
        assertSame(first.rowModel, table.rowModel)
        assertSame(first.state, table.state)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun deprecated_table_context_entries_fail_with_guidance() {
        val useTableError = assertFailsWith<IllegalStateException> {
            runComposeUiTest {
                setContent {
                    Table.useTable<User>()
                }
            }
        }
        assertEquals(
            "Table.useTable() is deprecated. Use useTable(data, columns) with explicit data and columns instead.",
            useTableError.message,
        )

        val instanceError = assertFailsWith<IllegalStateException> {
            runComposeUiTest {
                setContent {
                    Table.useTableInstance<User>()
                }
            }
        }
        assertEquals(
            "useTableInstance is deprecated. Use the useTable hook directly.",
            instanceError.message,
        )
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun deprecated_root_table_remember_entries_fail_with_guidance() {
        val rememberTableError = assertFailsWith<IllegalStateException> {
            runComposeUiTest {
                setContent {
                    RootTable.rememberRootTable<User>()
                }
            }
        }
        assertEquals(
            "Table.useTable() is deprecated. Use useTable(data, columns) with explicit data and columns instead.",
            rememberTableError.message,
        )

        val rememberInstanceError = assertFailsWith<IllegalStateException> {
            runComposeUiTest {
                setContent {
                    RootTable.rememberRootTableInstance<User>()
                }
            }
        }
        assertEquals(
            "useTableInstance is deprecated. Use the useTable hook directly.",
            rememberInstanceError.message,
        )
    }
}
