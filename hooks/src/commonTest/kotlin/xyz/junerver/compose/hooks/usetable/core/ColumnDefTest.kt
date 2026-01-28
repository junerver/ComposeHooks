package xyz.junerver.compose.hooks.usetable.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/*
  Description: useTable column def tests
  Author: Junerver
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

class ColumnDefTest {
    data class User(val name: String, val age: Int)

    @Test
    fun `column helper should respect defaults`() {
        val col = column<User, String>("name") { it.name }

        assertEquals("name", col.id)
        assertEquals("name", col.header)
        assertTrue(col.enableSorting)
        assertTrue(col.enableFiltering)
    }

    @Test
    fun `column helper should apply overrides`() {
        val col = column<User, Int>(
            id = "age",
            header = "Age",
            enableSorting = false,
            enableFiltering = false,
        ) { it.age }

        assertEquals("Age", col.header)
        assertTrue(!col.enableSorting)
        assertTrue(!col.enableFiltering)
    }
}
