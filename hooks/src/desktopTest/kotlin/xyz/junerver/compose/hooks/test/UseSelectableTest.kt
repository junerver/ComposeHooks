package xyz.junerver.compose.hooks.test

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import xyz.junerver.compose.hooks.SelectionMode
import xyz.junerver.compose.hooks.useSelectable

@Suppress("DEPRECATION")
class UseSelectableTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun single_select_switches_selection_to_latest_item() = runComposeUiTest {
        lateinit var isSelected: (String) -> Boolean
        lateinit var toggleSelected: (String) -> Unit
        var selectedItems by mutableStateOf(emptyList<String>())

        setContent {
            val holder = useSelectable(
                selectionMode = SelectionMode.SingleSelect<String>(),
                items = listOf("a", "b", "c"),
                keyProvider = { it },
            )
            isSelected = holder.isSelected
            toggleSelected = holder.toggleSelected
            selectedItems = holder.selectedItems.value
        }

        waitForIdle()
        assertTrue(selectedItems.isEmpty())

        toggleSelected("a")
        waitForIdle()
        assertTrue(isSelected("a"))
        assertEquals(listOf("a"), selectedItems)

        toggleSelected("b")
        waitForIdle()
        assertFalse(isSelected("a"))
        assertTrue(isSelected("b"))
        assertEquals(listOf("b"), selectedItems)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun multi_select_supports_select_all_invert_and_revert() = runComposeUiTest {
        lateinit var selectAll: () -> Unit
        lateinit var invertSelection: () -> Unit
        lateinit var revertAll: () -> Unit
        var selectedItems by mutableStateOf(emptyList<String>())

        setContent {
            val holder = useSelectable(
                selectionMode = SelectionMode.MultiSelect<String>(defaultSelectKeys = setOf("a")),
                items = listOf("a", "b", "c"),
                keyProvider = { it },
            )
            selectAll = holder.selectAll
            invertSelection = holder.invertSelection
            revertAll = holder.revertAll
            selectedItems = holder.selectedItems.value
        }

        waitForIdle()
        assertEquals(listOf("a"), selectedItems)

        selectAll()
        waitForIdle()
        assertEquals(listOf("a", "b", "c"), selectedItems)

        invertSelection()
        waitForIdle()
        assertTrue(selectedItems.isEmpty())

        selectAll()
        waitForIdle()
        revertAll()
        waitForIdle()
        assertTrue(selectedItems.isEmpty())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun single_select_rejects_multi_select_only_operations() = runComposeUiTest {
        lateinit var selectAll: () -> Unit
        lateinit var invertSelection: () -> Unit

        setContent {
            val holder = useSelectable(
                selectionMode = SelectionMode.SingleSelect<String>(defaultSelectKey = "a"),
                items = listOf("a", "b", "c"),
                keyProvider = { it },
            )
            selectAll = holder.selectAll
            invertSelection = holder.invertSelection
        }

        waitForIdle()
        assertFailsWith<IllegalStateException> { selectAll() }
        assertFailsWith<IllegalStateException> { invertSelection() }
    }
}
