package xyz.junerver.compose.hooks.usetable.core

import kotlin.test.Test
import kotlin.test.assertEquals

class RowModelTest {

    data class User(val name: String, val age: Int)

    @Test
    fun `Row should extract column value with type safety`() {
        val nameColumn = column<User, String>(
            id = "name",
            accessorFn = { it.name }
        )
        
        val row = Row(
            id = "1",
            original = User("Alice", 28),
            index = 0
        )
        
        assertEquals("Alice", row.getValue(nameColumn))
    }

    @Test
    fun `Row with subRows should support tree structure`() {
        val parent = Row(
            id = "1", 
            original = User("Parent", 50), 
            index = 0
        )
        
        val child = Row(
            id = "1.1", 
            original = User("Child", 20), 
            index = 0, 
            parentId = "1", 
            depth = 1
        )
        
        val rowWithChildren = parent.copy(subRows = listOf(child))
        
        assertEquals(1, rowWithChildren.subRows.size)
        assertEquals("1", rowWithChildren.subRows[0].parentId)
        assertEquals(1, rowWithChildren.subRows[0].depth)
    }
}
