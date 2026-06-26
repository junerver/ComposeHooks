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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.collections.immutable.persistentListOf
import xyz.junerver.compose.hooks.SelectionMode
import xyz.junerver.compose.hooks.uselastchanged.useLastChangedImpl
import xyz.junerver.compose.hooks.useselectable.useSelectableImpl
import xyz.junerver.compose.hooks.useselectable.useSelectableImpl
import xyz.junerver.compose.hooks.usesorted.useSortedImpl
import xyz.junerver.compose.hooks.useimmutablelist.useImmutableListImpl
import xyz.junerver.compose.hooks.useimmutablelist.useImmutableListReduceImpl
import xyz.junerver.compose.hooks.useimmutablelist.useImmutableListReduceOrNullImpl
import xyz.junerver.compose.hooks.useimmutablelist.useImmutableListFoldImpl

/*
  Description: Batch tests for small hooks packages with 0% coverage
  Author: MiMoCode
  Date: 2026/6/26
  Email: junerver@gmail.com
  Version: v1.0
*/

@OptIn(ExperimentalTestApi::class)
class SmallHooksTest {

    // region useSorted

    @Test
    fun sortedDefaultComparableInts() = runComposeUiTest {
        var result by mutableStateOf<List<Int>>(emptyList())
        setContent {
            val sorted by useSortedImpl(listOf(5, 3, 1, 4, 2))
            result = sorted
        }
        waitForIdle()
        assertEquals(listOf(1, 2, 3, 4, 5), result)
    }

    @Test
    fun sortedDefaultComparableStrings() = runComposeUiTest {
        var result by mutableStateOf<List<String>>(emptyList())
        setContent {
            val sorted by useSortedImpl(listOf("banana", "apple", "cherry"))
            result = sorted
        }
        waitForIdle()
        assertEquals(listOf("apple", "banana", "cherry"), result)
    }

    @Test
    fun sortedCustomCompareFn() = runComposeUiTest {
        var result by mutableStateOf<List<Int>>(emptyList())
        setContent {
            val sorted by useSortedImpl(listOf(1, 2, 3, 4, 5)) { a, b -> b - a }
            result = sorted
        }
        waitForIdle()
        assertEquals(listOf(5, 4, 3, 2, 1), result)
    }

    @Test
    fun sortedOptionsWithCustomCompare() = runComposeUiTest {
        data class Person(val name: String, val age: Int)

        var result by mutableStateOf<List<Person>>(emptyList())
        setContent {
            val sorted by useSortedImpl(
                listOf(Person("Alice", 30), Person("Bob", 20), Person("Charlie", 25)),
                optionsOf = {
                    compareFn = { a, b -> a.age - b.age }
                },
            )
            result = sorted
        }
        waitForIdle()
        assertEquals(listOf("Bob", "Charlie", "Alice"), result.map { it.name })
    }

    @Test
    fun sortedEmptyList() = runComposeUiTest {
        var result by mutableStateOf<List<Int>>(emptyList())
        setContent {
            val sorted by useSortedImpl(emptyList<Int>())
            result = sorted
        }
        waitForIdle()
        assertEquals(emptyList(), result)
    }

    @Test
    fun sortedSingleElement() = runComposeUiTest {
        var result by mutableStateOf<List<Int>>(emptyList())
        setContent {
            val sorted by useSortedImpl(listOf(42))
            result = sorted
        }
        waitForIdle()
        assertEquals(listOf(42), result)
    }

    @Test
    fun sortedAlreadySorted() = runComposeUiTest {
        var result by mutableStateOf<List<Int>>(emptyList())
        setContent {
            val sorted by useSortedImpl(listOf(1, 2, 3, 4, 5))
            result = sorted
        }
        waitForIdle()
        assertEquals(listOf(1, 2, 3, 4, 5), result)
    }

    @Test
    fun sortedDirtyModeModifiesSource() = runComposeUiTest {
        var result by mutableStateOf<List<Int>>(emptyList())
        val source = mutableListOf(5, 3, 1, 4, 2)
        setContent {
            val sorted by useSortedImpl(
                source,
                optionsOf = {
                    dirty = true
                    compareFn = { a, b -> a - b }
                },
            )
            result = sorted
        }
        waitForIdle()
        assertEquals(listOf(1, 2, 3, 4, 5), result)
        assertEquals(listOf(1, 2, 3, 4, 5), source)
    }

    @Test
    fun sortedCustomSortFn() = runComposeUiTest {
        var result by mutableStateOf<List<Int>>(emptyList())
        setContent {
            val sorted by useSortedImpl(
                listOf(3, 1, 2),
                optionsOf = {
                    sortFn = { arr, compareFn ->
                        val comparator = Comparator<Int> { a, b -> compareFn(a, b) }
                        arr.sortedWith(comparator.reversed())
                    }
                    compareFn = { a, b -> a - b }
                },
            )
            result = sorted
        }
        waitForIdle()
        assertEquals(listOf(3, 2, 1), result)
    }

    // endregion

    // region useImmutableList

    @Test
    fun immutableListInitialElements() = runComposeUiTest {
        var result by mutableStateOf(persistentListOf<Int>())
        setContent {
            val holder = useImmutableListImpl(1, 2, 3)
            result = holder.list.value
        }
        waitForIdle()
        assertEquals(persistentListOf(1, 2, 3), result)
    }

    @Test
    fun immutableListMutateAdd() = runComposeUiTest {
        var result by mutableStateOf(persistentListOf<Int>())
        var mutateFn: ((MutableList<Int>) -> Unit) -> Unit = {}
        setContent {
            val holder = useImmutableListImpl(1, 2, 3)
            result = holder.list.value
            mutateFn = holder.mutate
        }
        waitForIdle()
        assertEquals(persistentListOf(1, 2, 3), result)
        mutateFn { list -> list.add(4) }
        waitForIdle()
        assertEquals(persistentListOf(1, 2, 3, 4), result)
    }

    @Test
    fun immutableListMutateRemove() = runComposeUiTest {
        var result by mutableStateOf(persistentListOf<Int>())
        var mutateFn: ((MutableList<Int>) -> Unit) -> Unit = {}
        setContent {
            val holder = useImmutableListImpl(1, 2, 3)
            result = holder.list.value
            mutateFn = holder.mutate
        }
        waitForIdle()
        mutateFn { list -> list.remove(2) }
        waitForIdle()
        assertEquals(persistentListOf(1, 3), result)
    }

    @Test
    fun immutableListEmpty() = runComposeUiTest {
        var result by mutableStateOf(persistentListOf<Int>())
        setContent {
            val holder = useImmutableListImpl<Int>()
            result = holder.list.value
        }
        waitForIdle()
        assertTrue(result.isEmpty())
    }

    @Test
    fun immutableListReduce() = runComposeUiTest {
        var result by mutableStateOf(0)
        setContent {
            val state by useImmutableListReduceImpl(persistentListOf(1, 2, 3, 4, 5)) { acc, v -> acc + v }
            result = state
        }
        waitForIdle()
        assertEquals(15, result)
    }

    @Test
    fun immutableListReduceOrNullOnEmpty() = runComposeUiTest {
        var result by mutableStateOf<Int?>(-1)
        setContent {
            val state by useImmutableListReduceOrNullImpl(persistentListOf<Int>()) { acc, v -> acc + v }
            result = state
        }
        waitForIdle()
        assertEquals(null, result)
    }

    @Test
    fun immutableListReduceOrNullOnNonEmpty() = runComposeUiTest {
        var result by mutableStateOf<Int?>(null)
        setContent {
            val state by useImmutableListReduceOrNullImpl(persistentListOf(10, 20, 30)) { acc, v -> acc + v }
            result = state
        }
        waitForIdle()
        assertEquals(60, result)
    }

    @Test
    fun immutableListFold() = runComposeUiTest {
        var result by mutableStateOf("")
        setContent {
            val state by useImmutableListFoldImpl(
                persistentListOf("a", "b", "c"),
                initial = "",
            ) { acc, v -> acc + v }
            result = state
        }
        waitForIdle()
        assertEquals("abc", result)
    }

    @Test
    fun immutableListFoldWithInitialValue() = runComposeUiTest {
        var result by mutableStateOf(0)
        setContent {
            val state by useImmutableListFoldImpl(
                persistentListOf(1, 2, 3),
                initial = 100,
            ) { acc, v -> acc + v }
            result = state
        }
        waitForIdle()
        assertEquals(106, result)
    }

    // endregion

    // region useLastChanged

    @Test
    fun lastChangedReturnsInitialState() = runComposeUiTest {
        var instant by mutableStateOf<kotlin.time.Instant?>(null)
        setContent {
            val state = useLastChangedImpl("initial")
            instant = state.value
        }
        waitForIdle()
        assertNotNull(instant)
    }

    @Test
    fun lastChangedUpdatesOnSourceChange() = runComposeUiTest {
        var source by mutableStateOf("first")
        var instant1: kotlin.time.Instant? = null
        var instant2: kotlin.time.Instant? = null
        setContent {
            val state = useLastChangedImpl(source)
            if (instant1 == null) {
                instant1 = state.value
            } else {
                instant2 = state.value
            }
        }
        waitForIdle()
        assertNotNull(instant1)
        source = "second"
        waitForIdle()
        assertNotNull(instant2)
    }

    // endregion

    // region useSelectable

    @Test
    fun selectableSingleSelectInitial() = runComposeUiTest {
        var isSelected = false
        var selectedItems = listOf<String>()
        setContent {
            val holder = useSelectableImpl(
                selectionMode = SelectionMode.SingleSelect<String>(),
                items = listOf("a", "b", "c"),
                keyProvider = { it },
            )
            isSelected = holder.isSelected("a")
            selectedItems = holder.selectedItems.value
        }
        waitForIdle()
        assertFalse(isSelected)
        assertTrue(selectedItems.isEmpty())
    }

    @Test
    fun selectableSingleSelectToggle() = runComposeUiTest {
        var isSelectedA = false
        var toggleFn: ((String) -> Unit)? = null
        setContent {
            val holder = useSelectableImpl(
                selectionMode = SelectionMode.SingleSelect<String>(),
                items = listOf("a", "b", "c"),
                keyProvider = { it },
            )
            isSelectedA = holder.isSelected("a")
            toggleFn = holder.toggleSelected
        }
        waitForIdle()
        assertFalse(isSelectedA)
        toggleFn!!("a")
        waitForIdle()
    }

    @Test
    fun selectableMultiSelectSelectAll() = runComposeUiTest {
        var selectedCount = 0
        var selectAllFn: (() -> Unit)? = null
        setContent {
            val holder = useSelectableImpl(
                selectionMode = SelectionMode.MultiSelect<String>(),
                items = listOf("a", "b", "c"),
                keyProvider = { it },
            )
            selectedCount = holder.selectedItems.value.size
            selectAllFn = holder.selectAll
        }
        waitForIdle()
        assertEquals(0, selectedCount)
        selectAllFn!!()
        waitForIdle()
        var updatedCount = 0
        setContent {
            val holder = useSelectableImpl(
                selectionMode = SelectionMode.MultiSelect<String>(),
                items = listOf("a", "b", "c"),
                keyProvider = { it },
            )
            updatedCount = holder.selectedItems.value.size
        }
        waitForIdle()
        assertEquals(3, updatedCount)
    }

    @Test
    fun selectableMultiSelectRevertAll() = runComposeUiTest {
        var revertAllFn: (() -> Unit)? = null
        setContent {
            val holder = useSelectableImpl(
                selectionMode = SelectionMode.MultiSelect<String>(),
                items = listOf("a", "b", "c"),
                keyProvider = { it },
            )
            holder.selectAll()
            revertAllFn = holder.revertAll
        }
        waitForIdle()
        revertAllFn!!()
        waitForIdle()
        var count = -1
        setContent {
            val holder = useSelectableImpl(
                selectionMode = SelectionMode.MultiSelect<String>(),
                items = listOf("a", "b", "c"),
                keyProvider = { it },
            )
            count = holder.selectedItems.value.size
        }
        waitForIdle()
        assertEquals(0, count)
    }

    @Test
    fun selectableMultiSelectInvertSelection() = runComposeUiTest {
        var invertFn: (() -> Unit)? = null
        setContent {
            val holder = useSelectableImpl(
                selectionMode = SelectionMode.MultiSelect<String>(),
                items = listOf("a", "b", "c"),
                keyProvider = { it },
            )
            holder.toggleSelected("a")
            invertFn = holder.invertSelection
        }
        waitForIdle()
        invertFn!!()
        waitForIdle()
        var items = listOf<String>()
        setContent {
            val holder = useSelectableImpl(
                selectionMode = SelectionMode.MultiSelect<String>(),
                items = listOf("a", "b", "c"),
                keyProvider = { it },
            )
            items = holder.selectedItems.value
        }
        waitForIdle()
        // a was selected, after invert a becomes unselected, b and c become selected
        assertTrue(items.contains("b"))
        assertTrue(items.contains("c"))
    }

    @Test
    fun selectableMultiSelectWithDefaultKeys() = runComposeUiTest {
        var isSelectedA = false
        var isSelectedB = false
        setContent {
            val holder = useSelectableImpl(
                selectionMode = SelectionMode.MultiSelect<String>(defaultSelectKeys = setOf("a", "b")),
                items = listOf("a", "b", "c"),
                keyProvider = { it },
            )
            isSelectedA = holder.isSelected("a")
            isSelectedB = holder.isSelected("b")
        }
        waitForIdle()
        assertTrue(isSelectedA)
        assertTrue(isSelectedB)
    }

    @Test
    fun selectableSingleSelectWithDefaultKey() = runComposeUiTest {
        var isSelectedA = false
        setContent {
            val holder = useSelectableImpl(
                selectionMode = SelectionMode.SingleSelect<String>(defaultSelectKey = "a"),
                items = listOf("a", "b", "c"),
                keyProvider = { it },
            )
            isSelectedA = holder.isSelected("a")
        }
        waitForIdle()
        assertTrue(isSelectedA)
    }

    @Test
    fun selectableSelectAllThrowsInSingleMode() = runComposeUiTest {
        var selectAllFn: (() -> Unit)? = null
        setContent {
            val holder = useSelectableImpl(
                selectionMode = SelectionMode.SingleSelect<String>(),
                items = listOf("a", "b", "c"),
                keyProvider = { it },
            )
            selectAllFn = holder.selectAll
        }
        waitForIdle()
        assertFailsWith<IllegalStateException> {
            selectAllFn!!()
        }
    }

    @Test
    fun selectableInvertSelectionThrowsInSingleMode() = runComposeUiTest {
        var invertFn: (() -> Unit)? = null
        setContent {
            val holder = useSelectableImpl(
                selectionMode = SelectionMode.SingleSelect<String>(),
                items = listOf("a", "b", "c"),
                keyProvider = { it },
            )
            invertFn = holder.invertSelection
        }
        waitForIdle()
        assertFailsWith<IllegalStateException> {
            invertFn!!()
        }
    }

    @Test
    fun selectableWithIntKeys() = runComposeUiTest {
        var selectedItems = listOf<Int>()
        var selectAllFn: (() -> Unit)? = null
        setContent {
            val holder = useSelectableImpl(
                selectionMode = SelectionMode.MultiSelect<Int>(),
                items = listOf(10, 20, 30),
                keyProvider = { it },
            )
            selectedItems = holder.selectedItems.value
            selectAllFn = holder.selectAll
        }
        waitForIdle()
        assertTrue(selectedItems.isEmpty())
        selectAllFn!!()
        waitForIdle()
        var updatedItems = listOf<Int>()
        setContent {
            val holder = useSelectableImpl(
                selectionMode = SelectionMode.MultiSelect<Int>(),
                items = listOf(10, 20, 30),
                keyProvider = { it },
            )
            updatedItems = holder.selectedItems.value
        }
        waitForIdle()
        assertEquals(3, updatedItems.size)
    }

    // endregion
}
