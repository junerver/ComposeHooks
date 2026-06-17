package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import xyz.junerver.compose.hooks.useState
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
}
