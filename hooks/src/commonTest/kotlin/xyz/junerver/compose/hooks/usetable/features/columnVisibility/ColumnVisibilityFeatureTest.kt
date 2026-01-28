package xyz.junerver.compose.hooks.usetable.features.columnVisibility

import kotlin.test.Test
import kotlin.test.assertEquals
import xyz.junerver.compose.hooks.usetable.core.Row
import xyz.junerver.compose.hooks.usetable.core.column
import xyz.junerver.compose.hooks.usetable.state.ColumnVisibilityState
import xyz.junerver.compose.hooks.usetable.state.TableState

/*
  Description: useTable column visibility feature tests
  Author: Junerver
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

class ColumnVisibilityFeatureTest {
    data class User(val name: String, val age: Int)

    private val columns = listOf(
        column<User, String>("name") { it.name },
        column<User, Int>("age") { it.age },
    )

    @Test
    fun `column visibility should not affect row model`() {
        val feature = ColumnVisibilityFeature<User>()
        val rows = listOf(Row(id = "1", original = User("A", 1), index = 0))
        val state = TableState<User>(columnVisibility = ColumnVisibilityState(mapOf("name" to false)))

        val result = feature.transform(rows, state, columns)

        assertEquals(rows, result)
    }
}
