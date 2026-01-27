package xyz.junerver.compose.hooks.usetable.features.sorting

import kotlinx.coroutines.test.runTest
import xyz.junerver.compose.hooks.usetable.core.Row
import xyz.junerver.compose.hooks.usetable.core.column
import xyz.junerver.compose.hooks.usetable.state.SortDescriptor
import xyz.junerver.compose.hooks.usetable.state.SortingState
import xyz.junerver.compose.hooks.usetable.state.TableState
import kotlin.test.Test
import kotlin.test.assertEquals

class SortingFeatureTest {

    data class User(val name: String, val age: Int)

    private val testData = listOf(
        User("Charlie", 30),
        User("Alice", 25),
        User("Bob", 30),
        User("Alice", 20)
    )

    private val columns = listOf(
        column<User, String>("name") { it.name },
        column<User, Int>("age") { it.age }
    )

    private fun createRows(data: List<User>): List<Row<User>> {
        return data.mapIndexed { index, user ->
            Row(id = index.toString(), original = user, index = index)
        }
    }

    @Test
    fun `transform should sort rows by name ascending`() = runTest {
        val feature = SortingFeature<User>()
        val rows = createRows(testData)
        
        val state = TableState<User>(
            sorting = SortingState(listOf(SortDescriptor("name", desc = false)))
        )

        val result = feature.transform(rows, state, columns)

        assertEquals("Alice", result[0].original.name)
        assertEquals("Alice", result[1].original.name)
        assertEquals("Bob", result[2].original.name)
        assertEquals("Charlie", result[3].original.name)
    }

    @Test
    fun `transform should sort rows by age descending`() = runTest {
        val feature = SortingFeature<User>()
        val rows = createRows(testData)
        
        val state = TableState<User>(
            sorting = SortingState(listOf(SortDescriptor("age", desc = true)))
        )

        val result = feature.transform(rows, state, columns)

        assertEquals("Charlie", result[0].original.name) // 30
        assertEquals("Bob", result[1].original.name)     // 30
        assertEquals("Alice", result[2].original.name)   // 25
        assertEquals("Alice", result[3].original.name)   // 20
    }

    @Test
    fun `transform should support multi-column sorting`() = runTest {
        val feature = SortingFeature<User>()
        val rows = createRows(testData)
        
        // Sort by Name ASC, then Age ASC
        val state = TableState<User>(
            sorting = SortingState(listOf(
                SortDescriptor("name", desc = false),
                SortDescriptor("age", desc = false)
            ))
        )

        val result = feature.transform(rows, state, columns)

        // Alices should be sorted by age
        assertEquals("Alice", result[0].original.name)
        assertEquals(20, result[0].original.age)
        
        assertEquals("Alice", result[1].original.name)
        assertEquals(25, result[1].original.age)
        
        assertEquals("Bob", result[2].original.name)
        assertEquals("Charlie", result[3].original.name)
    }
}
