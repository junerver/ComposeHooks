package xyz.junerver.compose.hooks.test

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import xyz.junerver.compose.hooks.usetable.ColumnFilter
import xyz.junerver.compose.hooks.usetable.ColumnSort
import xyz.junerver.compose.hooks.usetable.ColumnVisibilityState
import xyz.junerver.compose.hooks.usetable.FilterState
import xyz.junerver.compose.hooks.usetable.PaginationState
import xyz.junerver.compose.hooks.usetable.RowSelectionState
import xyz.junerver.compose.hooks.usetable.SortDirection
import xyz.junerver.compose.hooks.usetable.SortingState

/*
  Description: useTable state classes TDD tests
  Author: Claude
  Date: 2025/1/26
  Email: noreply@anthropic.com
  Version: v1.0
*/

class UseTableStateTest {
    // region SortingState Tests

    @Test
    fun sortingState_initial_state_is_empty() {
        val state = SortingState()
        assertTrue(state.sorting.isEmpty())
    }

    @Test
    fun sortingState_toggleSort_adds_ascending_sort_for_new_column() {
        val state = SortingState()
        val newState = state.toggleSort("name")

        assertEquals(1, newState.sorting.size)
        assertEquals("name", newState.sorting[0].id)
        assertEquals(SortDirection.ASC, newState.sorting[0].direction)
    }

    @Test
    fun sortingState_toggleSort_changes_ascending_to_descending() {
        val state = SortingState(listOf(ColumnSort("name", SortDirection.ASC)))
        val newState = state.toggleSort("name")

        assertEquals(1, newState.sorting.size)
        assertEquals(SortDirection.DESC, newState.sorting[0].direction)
    }

    @Test
    fun sortingState_toggleSort_removes_descending_sort() {
        val state = SortingState(listOf(ColumnSort("name", SortDirection.DESC)))
        val newState = state.toggleSort("name")

        assertTrue(newState.sorting.isEmpty())
    }

    @Test
    fun sortingState_toggleSort_replaces_existing_sort_in_single_sort_mode() {
        val state = SortingState(listOf(ColumnSort("name", SortDirection.ASC)))
        val newState = state.toggleSort("age", multiSort = false)

        assertEquals(1, newState.sorting.size)
        assertEquals("age", newState.sorting[0].id)
    }

    @Test
    fun sortingState_toggleSort_adds_to_existing_in_multi_sort_mode() {
        val state = SortingState(listOf(ColumnSort("name", SortDirection.ASC)))
        val newState = state.toggleSort("age", multiSort = true)

        assertEquals(2, newState.sorting.size)
        assertEquals("name", newState.sorting[0].id)
        assertEquals("age", newState.sorting[1].id)
    }

    @Test
    fun sortingState_isSorted_returns_true_for_sorted_column() {
        val state = SortingState(listOf(ColumnSort("name", SortDirection.ASC)))
        assertTrue(state.isSorted("name"))
        assertFalse(state.isSorted("age"))
    }

    @Test
    fun sortingState_getDirection_returns_correct_direction() {
        val state = SortingState(listOf(ColumnSort("name", SortDirection.DESC)))
        assertEquals(SortDirection.DESC, state.getDirection("name"))
        assertNull(state.getDirection("age"))
    }

    @Test
    fun sortingState_clearSort_removes_all_sorting() {
        val state = SortingState(
            listOf(
                ColumnSort("name", SortDirection.ASC),
                ColumnSort("age", SortDirection.DESC),
            ),
        )
        val newState = state.clearSort()
        assertTrue(newState.sorting.isEmpty())
    }

    // endregion

    // region PaginationState Tests

    @Test
    fun paginationState_initial_state_has_defaults() {
        val state = PaginationState()
        assertEquals(0, state.pageIndex)
        assertEquals(10, state.pageSize)
    }

    @Test
    fun paginationState_setPageIndex_updates_index() {
        val state = PaginationState()
        val newState = state.setPageIndex(5)
        assertEquals(5, newState.pageIndex)
    }

    @Test
    fun paginationState_setPageIndex_coerces_negative_to_zero() {
        val state = PaginationState()
        val newState = state.setPageIndex(-1)
        assertEquals(0, newState.pageIndex)
    }

    @Test
    fun paginationState_setPageSize_updates_size_and_resets_index() {
        val state = PaginationState(pageIndex = 5, pageSize = 10)
        val newState = state.setPageSize(20)
        assertEquals(20, newState.pageSize)
        assertEquals(0, newState.pageIndex)
    }

    @Test
    fun paginationState_setPageSize_coerces_to_minimum_one() {
        val state = PaginationState()
        val newState = state.setPageSize(0)
        assertEquals(1, newState.pageSize)
    }

    @Test
    fun paginationState_nextPage_increments_index() {
        val state = PaginationState(pageIndex = 0)
        val newState = state.nextPage(totalPages = 5)
        assertEquals(1, newState.pageIndex)
    }

    @Test
    fun paginationState_nextPage_does_not_exceed_max() {
        val state = PaginationState(pageIndex = 4)
        val newState = state.nextPage(totalPages = 5)
        assertEquals(4, newState.pageIndex)
    }

    @Test
    fun paginationState_previousPage_decrements_index() {
        val state = PaginationState(pageIndex = 3)
        val newState = state.previousPage()
        assertEquals(2, newState.pageIndex)
    }

    @Test
    fun paginationState_previousPage_does_not_go_below_zero() {
        val state = PaginationState(pageIndex = 0)
        val newState = state.previousPage()
        assertEquals(0, newState.pageIndex)
    }

    @Test
    fun paginationState_canNextPage_returns_correct_value() {
        val state = PaginationState(pageIndex = 3)
        assertTrue(state.canNextPage(totalPages = 5))
        assertFalse(state.canNextPage(totalPages = 4))
    }

    @Test
    fun paginationState_canPreviousPage_returns_correct_value() {
        assertTrue(PaginationState(pageIndex = 1).canPreviousPage())
        assertFalse(PaginationState(pageIndex = 0).canPreviousPage())
    }

    // endregion

    // region FilterState Tests

    @Test
    fun filterState_initial_state_is_empty() {
        val state = FilterState()
        assertTrue(state.columnFilters.isEmpty())
        assertEquals("", state.globalFilter)
    }

    @Test
    fun filterState_setColumnFilter_adds_new_filter() {
        val state = FilterState()
        val newState = state.setColumnFilter("name", "Alice")

        assertEquals(1, newState.columnFilters.size)
        assertEquals("Alice", newState.getColumnFilter("name"))
    }

    @Test
    fun filterState_setColumnFilter_updates_existing_filter() {
        val state = FilterState(listOf(ColumnFilter("name", "Alice")))
        val newState = state.setColumnFilter("name", "Bob")

        assertEquals(1, newState.columnFilters.size)
        assertEquals("Bob", newState.getColumnFilter("name"))
    }

    @Test
    fun filterState_setColumnFilter_removes_filter_when_null() {
        val state = FilterState(listOf(ColumnFilter("name", "Alice")))
        val newState = state.setColumnFilter("name", null)

        assertTrue(newState.columnFilters.isEmpty())
    }

    @Test
    fun filterState_setColumnFilter_removes_filter_when_empty_string() {
        val state = FilterState(listOf(ColumnFilter("name", "Alice")))
        val newState = state.setColumnFilter("name", "")

        assertTrue(newState.columnFilters.isEmpty())
    }

    @Test
    fun filterState_setGlobalFilter_updates_global_filter() {
        val state = FilterState()
        val newState = state.setGlobalFilter("search term")

        assertEquals("search term", newState.globalFilter)
    }

    @Test
    fun filterState_clearFilters_removes_all_filters() {
        val state = FilterState(
            columnFilters = listOf(ColumnFilter("name", "Alice")),
            globalFilter = "search",
        )
        val newState = state.clearFilters()

        assertTrue(newState.columnFilters.isEmpty())
        assertEquals("", newState.globalFilter)
    }

    // endregion

    // region RowSelectionState Tests

    @Test
    fun rowSelectionState_initial_state_is_empty() {
        val state = RowSelectionState()
        assertTrue(state.selectedRowIds.isEmpty())
    }

    @Test
    fun rowSelectionState_toggleSelection_adds_row() {
        val state = RowSelectionState()
        val newState = state.toggleSelection("row1")

        assertTrue(newState.isSelected("row1"))
    }

    @Test
    fun rowSelectionState_toggleSelection_removes_selected_row() {
        val state = RowSelectionState(setOf("row1"))
        val newState = state.toggleSelection("row1")

        assertFalse(newState.isSelected("row1"))
    }

    @Test
    fun rowSelectionState_selectAll_selects_all_rows() {
        val state = RowSelectionState()
        val newState = state.selectAll(listOf("row1", "row2", "row3"))

        assertTrue(newState.isSelected("row1"))
        assertTrue(newState.isSelected("row2"))
        assertTrue(newState.isSelected("row3"))
    }

    @Test
    fun rowSelectionState_deselectAll_clears_selection() {
        val state = RowSelectionState(setOf("row1", "row2"))
        val newState = state.deselectAll()

        assertTrue(newState.selectedRowIds.isEmpty())
    }

    @Test
    fun rowSelectionState_toggleAll_selects_all_when_not_all_selected() {
        val state = RowSelectionState(setOf("row1"))
        val newState = state.toggleAll(listOf("row1", "row2", "row3"))

        assertEquals(3, newState.selectedRowIds.size)
    }

    @Test
    fun rowSelectionState_toggleAll_deselects_all_when_all_selected() {
        val state = RowSelectionState(setOf("row1", "row2", "row3"))
        val newState = state.toggleAll(listOf("row1", "row2", "row3"))

        assertTrue(newState.selectedRowIds.isEmpty())
    }

    // endregion

    // region ColumnVisibilityState Tests

    @Test
    fun columnVisibilityState_initial_state_shows_all_columns() {
        val state = ColumnVisibilityState()
        assertTrue(state.hiddenColumnIds.isEmpty())
        assertTrue(state.isVisible("anyColumn"))
    }

    @Test
    fun columnVisibilityState_toggleVisibility_hides_visible_column() {
        val state = ColumnVisibilityState()
        val newState = state.toggleVisibility("name")

        assertFalse(newState.isVisible("name"))
    }

    @Test
    fun columnVisibilityState_toggleVisibility_shows_hidden_column() {
        val state = ColumnVisibilityState(setOf("name"))
        val newState = state.toggleVisibility("name")

        assertTrue(newState.isVisible("name"))
    }

    @Test
    fun columnVisibilityState_setVisibility_hides_column() {
        val state = ColumnVisibilityState()
        val newState = state.setVisibility("name", visible = false)

        assertFalse(newState.isVisible("name"))
    }

    @Test
    fun columnVisibilityState_setVisibility_shows_column() {
        val state = ColumnVisibilityState(setOf("name"))
        val newState = state.setVisibility("name", visible = true)

        assertTrue(newState.isVisible("name"))
    }

    @Test
    fun columnVisibilityState_showAll_shows_all_hidden_columns() {
        val state = ColumnVisibilityState(setOf("name", "age", "email"))
        val newState = state.showAll()

        assertTrue(newState.hiddenColumnIds.isEmpty())
    }

    // endregion

    // region SortDirection Tests

    @Test
    fun sortDirection_toggle_changes_asc_to_desc() {
        assertEquals(SortDirection.DESC, SortDirection.ASC.toggle())
    }

    @Test
    fun sortDirection_toggle_changes_desc_to_asc() {
        assertEquals(SortDirection.ASC, SortDirection.DESC.toggle())
    }

    // endregion
}
