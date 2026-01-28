package xyz.junerver.compose.hooks.usetable.features.filtering

import xyz.junerver.compose.hooks.usetable.core.Row
import xyz.junerver.compose.hooks.usetable.core.column
import xyz.junerver.compose.hooks.usetable.state.FilteringState
import xyz.junerver.compose.hooks.usetable.state.TableState
import kotlin.test.Test
import kotlin.test.assertEquals

class FilteringFeatureTest {

    data class User(val name: String, val email: String)

    private val testData = listOf(
        User("Alice", "alice@example.com"),
        User("Bob", "bob@test.com"),
        User("Charlie", "charlie@example.com")
    )

    private val columns = listOf(
        column<User, String>("name") { it.name },
        column<User, String>("email") { it.email }
    )

    private fun createRows(data: List<User>): List<Row<User>> {
        return data.mapIndexed { index, user ->
            Row(id = index.toString(), original = user, index = index)
        }
    }

    @Test
    fun `Global filter should search across all columns`() {
        val feature = FilteringFeature<User>()
        val rows = createRows(testData)
        
        // Search "example" -> Should match Alice and Charlie
        val state = TableState<User>(
            filtering = FilteringState(globalFilter = "example")
        )

        val result = feature.transform(rows, state, columns)

        assertEquals(2, result.size)
        assertEquals("Alice", result[0].original.name)
        assertEquals("Charlie", result[1].original.name)
    }

    @Test
    fun `Column filter should filter specific column`() {
        val feature = FilteringFeature<User>()
        val rows = createRows(testData)
        
        // Filter name containing "Bo" -> Should match Bob
        val state = TableState<User>(
            filtering = FilteringState(
                columnFilters = mapOf("name" to "Bo")
            )
        )

        val result = feature.transform(rows, state, columns)

        assertEquals(1, result.size)
        assertEquals("Bob", result[0].original.name)
    }

    @Test
    fun `Column filter should ignore disabled column`() {
        val feature = FilteringFeature<User>()
        val rows = createRows(testData)
        val disabledColumns = listOf(
            column<User, String>("name", enableFiltering = false) { it.name },
            column<User, String>("email", enableFiltering = false) { it.email }
        )

        val state = TableState<User>(
            filtering = FilteringState(
                columnFilters = mapOf("name" to "Alice")
            )
        )

        val result = feature.transform(rows, state, disabledColumns)

        assertEquals(3, result.size)
    }

    @Test
    fun `Global filter should ignore disabled columns`() {
        val feature = FilteringFeature<User>()
        val rows = createRows(testData)
        val disabledColumns = listOf(
            column<User, String>("name", enableFiltering = false) { it.name },
            column<User, String>("email", enableFiltering = false) { it.email }
        )

        val state = TableState<User>(
            filtering = FilteringState(globalFilter = "Alice")
        )

        val result = feature.transform(rows, state, disabledColumns)

        assertEquals(3, result.size)
    }
}
