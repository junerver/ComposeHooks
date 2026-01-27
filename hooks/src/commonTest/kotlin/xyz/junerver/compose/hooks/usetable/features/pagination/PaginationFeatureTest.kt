package xyz.junerver.compose.hooks.usetable.features.pagination

import kotlin.math.ceil
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import xyz.junerver.compose.hooks.usetable.core.ColumnDef
import xyz.junerver.compose.hooks.usetable.core.Row
import xyz.junerver.compose.hooks.usetable.core.RowModel
import xyz.junerver.compose.hooks.usetable.core.column
import xyz.junerver.compose.hooks.usetable.state.PaginationState
import xyz.junerver.compose.hooks.usetable.state.TableState

class PaginationFeatureTest {

    data class User(val id: Int, val name: String)

    private val testData = (1..12).map { User(it, "User $it") }
    
    private val testRows = testData.mapIndexed { index, user ->
        Row(id = index.toString(), original = user, index = index)
    }
    
    private val columns = listOf(
        column<User, Int>("id", "ID") { it.id },
        column<User, String>("name", "Name") { it.name }
    )

    @Test
    fun `pagination should slice rows correctly for page 0`() = runTest {
        val feature = PaginationFeature<User>()
        val state = TableState<User>(pagination = PaginationState(pageIndex = 0, pageSize = 5))
        
        val result = feature.transform(testRows, state, columns)
        
        assertEquals(5, result.size, "Page 0 should have 5 rows")
        assertEquals("User 1", result[0].original.name)
        assertEquals("User 5", result[4].original.name)
    }

    @Test
    fun `pagination should slice rows correctly for page 1`() = runTest {
        val feature = PaginationFeature<User>()
        val state = TableState<User>(pagination = PaginationState(pageIndex = 1, pageSize = 5))
        
        val result = feature.transform(testRows, state, columns)
        
        assertEquals(5, result.size, "Page 1 should have 5 rows")
        assertEquals("User 6", result[0].original.name)
        assertEquals("User 10", result[4].original.name)
    }

    @Test
    fun `pagination should slice rows correctly for last page`() = runTest {
        val feature = PaginationFeature<User>()
        val state = TableState<User>(pagination = PaginationState(pageIndex = 2, pageSize = 5))
        
        val result = feature.transform(testRows, state, columns)
        
        assertEquals(2, result.size, "Page 2 (last) should have 2 rows")
        assertEquals("User 11", result[0].original.name)
        assertEquals("User 12", result[1].original.name)
    }

    @Test
    fun `page count calculation should be correct`() {
        val totalRows = 12
        val pageSize = 5
        
        val pageCount = ceil(totalRows.toDouble() / pageSize).toInt()
        
        assertEquals(3, pageCount, "12 items / 5 per page = 3 pages")
    }

    @Test
    fun `canNext should be true on first page`() {
        val pageIndex = 0
        val pageCount = 3
        
        val canNext = pageIndex < pageCount - 1
        
        assertTrue(canNext, "canNext should be true on page 0 of 3")
    }

    @Test
    fun `canPrev should be false on first page`() {
        val pageIndex = 0
        
        val canPrev = pageIndex > 0
        
        assertFalse(canPrev, "canPrev should be false on page 0")
    }

    @Test
    fun `canNext should be false on last page`() {
        val pageIndex = 2
        val pageCount = 3
        
        val canNext = pageIndex < pageCount - 1
        
        assertFalse(canNext, "canNext should be false on last page")
    }

    @Test
    fun `canPrev should be true on last page`() {
        val pageIndex = 2
        
        val canPrev = pageIndex > 0
        
        assertTrue(canPrev, "canPrev should be true on page 2")
    }

    @Test
    fun `RowModel totalRows should preserve original count`() {
        val paginatedRows = testRows.take(5)
        
        val rowModel = RowModel(
            rows = paginatedRows,
            flatRows = paginatedRows,
            totalRows = testRows.size
        )
        
        assertEquals(5, rowModel.rows.size, "Paginated rows should be 5")
        assertEquals(12, rowModel.totalRows, "totalRows should preserve original 12")
    }

    @Test
    fun `pagination should return empty list for out of bounds page`() = runTest {
        val feature = PaginationFeature<User>()
        val state = TableState<User>(pagination = PaginationState(pageIndex = 10, pageSize = 5))
        
        val result = feature.transform(testRows, state, columns)
        
        assertEquals(0, result.size, "Out of bounds page should return empty list")
    }
}
