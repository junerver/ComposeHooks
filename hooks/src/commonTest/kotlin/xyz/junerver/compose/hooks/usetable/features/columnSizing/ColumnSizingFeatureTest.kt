package xyz.junerver.compose.hooks.usetable.features.columnSizing

import kotlin.test.Test
import kotlin.test.assertEquals
import xyz.junerver.compose.hooks.usetable.core.Row
import xyz.junerver.compose.hooks.usetable.core.column
import xyz.junerver.compose.hooks.usetable.state.ColumnSizingState
import xyz.junerver.compose.hooks.usetable.state.TableState

/*
  Description: useTable column sizing feature tests
  Author: Junerver
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

class ColumnSizingFeatureTest {
    data class User(val name: String, val age: Int)

    private val columns = listOf(
        column<User, String>("name") { it.name },
        column<User, Int>("age") { it.age },
    )

    @Test
    fun `column sizing should not affect row model`() {
        val feature = ColumnSizingFeature<User>()
        val rows = listOf(Row(id = "1", original = User("A", 1), index = 0))
        val state = TableState<User>(columnSizing = ColumnSizingState(mapOf("name" to 120f)))

        val result = feature.transform(rows, state, columns)

        assertEquals(rows, result)
    }
}
