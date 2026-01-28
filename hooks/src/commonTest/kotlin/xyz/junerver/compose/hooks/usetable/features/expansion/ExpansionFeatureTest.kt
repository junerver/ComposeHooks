package xyz.junerver.compose.hooks.usetable.features.expansion

import kotlin.test.Test
import kotlin.test.assertEquals
import xyz.junerver.compose.hooks.usetable.core.Row
import xyz.junerver.compose.hooks.usetable.state.ExpandedState
import xyz.junerver.compose.hooks.usetable.state.TableState

/*
  Description: useTable expansion feature tests
  Author: Junerver
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

class ExpansionFeatureTest {
    data class User(val name: String)

    private fun row(id: String, name: String, subRows: List<Row<User>> = emptyList()) =
        Row(id = id, original = User(name), index = 0, subRows = subRows)

    @Test
    fun `expansion should keep rows when no expanded`() {
        val feature = ExpansionFeature<User>()
        val rows = listOf(row("1", "A"), row("2", "B"))
        val state = TableState<User>(expanded = ExpandedState())

        val result = feature.transform(rows, state, emptyList())

        assertEquals(rows, result)
    }

    @Test
    fun `expansion should flatten expanded rows`() {
        val feature = ExpansionFeature<User>()
        val child = row("1.1", "A-1")
        val rows = listOf(row("1", "A", listOf(child)), row("2", "B"))
        val state = TableState<User>(expanded = ExpandedState(mapOf("1" to true)))

        val result = feature.transform(rows, state, emptyList())

        assertEquals(listOf("1", "1.1", "2"), result.map { it.id })
    }

    @Test
    fun `expansion should not flatten when row not expanded`() {
        val feature = ExpansionFeature<User>()
        val child = row("1.1", "A-1")
        val rows = listOf(row("1", "A", listOf(child)), row("2", "B"))
        val state = TableState<User>(expanded = ExpandedState(mapOf("1" to false)))

        val result = feature.transform(rows, state, emptyList())

        assertEquals(listOf("1", "2"), result.map { it.id })
    }
}
