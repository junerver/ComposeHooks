package xyz.junerver.compose.hooks.usetable.features.rowselection

import kotlin.test.Test
import kotlin.test.assertEquals
import xyz.junerver.compose.hooks.usetable.core.Row
import xyz.junerver.compose.hooks.usetable.core.column
import xyz.junerver.compose.hooks.usetable.state.RowSelectionState
import xyz.junerver.compose.hooks.usetable.state.TableState

/*
  Description: useTable row selection feature tests
  Author: Junerver
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

class RowSelectionFeatureTest {
    data class User(val name: String)

    private val columns = listOf(
        column<User, String>("name") { it.name },
    )

    @Test
    fun `row selection should not affect row model`() {
        val feature = RowSelectionFeature<User>()
        val rows = listOf(Row(id = "1", original = User("A"), index = 0))
        val state = TableState<User>(rowSelection = RowSelectionState(setOf("1")))

        val result = feature.transform(rows, state, columns)

        assertEquals(rows, result)
    }
}
