package xyz.junerver.compose.hooks.test

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.collections.immutable.persistentListOf
import xyz.junerver.compose.hooks.ImmutableListHolder
import xyz.junerver.compose.hooks.useImmutableList
import xyz.junerver.compose.hooks.useImmutableListFold
import xyz.junerver.compose.hooks.useImmutableListReduce
import xyz.junerver.compose.hooks.useImmutableListReduceOrNull

@Suppress("DEPRECATION")
class UseImmutableListTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun immutable_list_exposes_initial_elements_and_mutations() = runComposeUiTest {
        lateinit var holder: ImmutableListHolder<Int>

        setContent {
            holder = useImmutableList(1, 2, 3)
        }

        waitForIdle()
        assertEquals(persistentListOf(1, 2, 3), holder.list.value)

        holder.mutate { list -> list.add(4) }
        waitForIdle()
        assertEquals(persistentListOf(1, 2, 3, 4), holder.list.value)

        holder.mutate { list -> list.remove(2) }
        waitForIdle()
        assertEquals(persistentListOf(1, 3, 4), holder.list.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun immutable_list_supports_empty_initial_state() = runComposeUiTest {
        lateinit var holder: ImmutableListHolder<Int>

        setContent {
            holder = useImmutableList()
        }

        waitForIdle()
        assertTrue(holder.list.value.isEmpty())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun immutable_list_reduce_variants_follow_list_changes() = runComposeUiTest {
        var source by mutableStateOf(persistentListOf(1, 2, 3))
        var reduced: State<Int>? = null
        var reducedOrNull: State<Int?>? = null
        var folded: State<String>? = null

        setContent {
            reduced = useImmutableListReduce(source) { acc, value -> acc + value }
            reducedOrNull = useImmutableListReduceOrNull(source) { acc, value -> acc + value }
            folded = useImmutableListFold(source, initial = "") { acc, value -> acc + value }
        }

        waitForIdle()
        assertEquals(6, reduced!!.value)
        assertEquals(6, reducedOrNull!!.value)
        assertEquals("123", folded!!.value)

        source = persistentListOf()
        waitForIdle()
        assertEquals(null, reducedOrNull!!.value)

        source = persistentListOf(4, 5)
        waitForIdle()
        assertEquals(9, reduced!!.value)
        assertEquals(9, reducedOrNull!!.value)
        assertEquals("45", folded!!.value)
    }
}
