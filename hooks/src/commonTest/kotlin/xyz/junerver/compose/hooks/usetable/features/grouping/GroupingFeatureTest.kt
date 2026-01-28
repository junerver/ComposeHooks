package xyz.junerver.compose.hooks.usetable.features.grouping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import xyz.junerver.compose.hooks.usetable.core.Row
import xyz.junerver.compose.hooks.usetable.core.column
import xyz.junerver.compose.hooks.usetable.state.GroupingState
import xyz.junerver.compose.hooks.usetable.state.TableState

/*
  Description: useTable grouping feature tests
  Author: Junerver
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

class GroupingFeatureTest {
    data class User(val name: String?, val age: Int)

    private val columns = listOf(
        column<User, String?>("name") { it.name },
        column<User, Int>("age") { it.age },
    )

    private fun rowsOf(data: List<User>): List<Row<User>> =
        data.mapIndexed { index, user -> Row(id = index.toString(), original = user, index = index) }

    @Test
    fun `grouping should group rows by first column`() {
        val feature = GroupingFeature<User>()
        val data = listOf(
            User("Alice", 18),
            User("Bob", 20),
            User("Alice", 22),
        )
        val rows = rowsOf(data)
        val state = TableState<User>(grouping = GroupingState(listOf("name")))

        val result = feature.transform(rows, state, columns)

        assertEquals(2, result.size)
        val groupAlice = result.first { it.metadata["groupKey"] == "Alice" }
        val groupBob = result.first { it.metadata["groupKey"] == "Bob" }
        assertTrue(groupAlice.subRows.size == 2)
        assertTrue(groupBob.subRows.size == 1)
    }

    @Test
    fun `grouping should return rows when grouping empty`() {
        val feature = GroupingFeature<User>()
        val rows = rowsOf(listOf(User("Alice", 18)))
        val state = TableState<User>(grouping = GroupingState())

        val result = feature.transform(rows, state, columns)

        assertEquals(rows, result)
    }

    @Test
    fun `grouping should return rows when column missing`() {
        val feature = GroupingFeature<User>()
        val rows = rowsOf(listOf(User("Alice", 18), User("Bob", 20)))
        val state = TableState<User>(grouping = GroupingState(listOf("missing")))

        val result = feature.transform(rows, state, columns)

        assertEquals(rows, result)
    }

    @Test
    fun `grouping should create stable group id and metadata`() {
        val feature = GroupingFeature<User>()
        val rows = rowsOf(listOf(User(null, 18), User("Alice", 20)))
        val state = TableState<User>(grouping = GroupingState(listOf("name")))

        val result = feature.transform(rows, state, columns)
        val nullGroup = result.first { it.metadata["groupKey"] == "null" }

        assertEquals("group-name-null", nullGroup.id)
        assertTrue(nullGroup.metadata["isGroupHeader"] == true)
        assertEquals("null", nullGroup.metadata["groupKey"])
    }
}
