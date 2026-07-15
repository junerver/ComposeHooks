package xyz.junerver.compose.hooks.test

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.collections.immutable.persistentListOf
import xyz.junerver.compose.hooks.ImmutableListHolder
import xyz.junerver.compose.hooks.rememberImmutableList
import xyz.junerver.compose.hooks.rememberImmutableListFold
import xyz.junerver.compose.hooks.rememberImmutableListReduce
import xyz.junerver.compose.hooks.rememberImmutableListReduceOrNull
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

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun immutable_list_keeps_initial_values_and_identities_after_recomposition() = runComposeUiTest {
        var initial by mutableStateOf("first")
        lateinit var holder: ImmutableListHolder<String?>

        setContent {
            holder = useImmutableList(initial, null)
        }

        waitForIdle()
        val initialHolder = holder
        val initialState = holder.list
        val initialMutate = holder.mutate

        holder.mutate { it.add("mutated") }
        initial = "replacement"
        waitForIdle()

        assertSame(initialHolder, holder)
        assertSame(initialState, holder.list)
        assertSame(initialMutate, holder.mutate)
        assertEquals(persistentListOf("first", null, "mutated"), holder.list.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun reduce_and_fold_observe_order_and_empty_boundaries() = runComposeUiTest {
        var reduced: State<String>? = null
        var reducedOrNull: State<String?>? = null
        var folded: State<String>? = null
        var emptyReduced: State<Int>? = null

        setContent {
            val source = persistentListOf("a", "b", "c")
            reduced = useImmutableListReduce(source) { acc, value -> "($acc>$value)" }
            reducedOrNull = useImmutableListReduceOrNull(persistentListOf()) { acc: String, value -> acc + value }
            folded = useImmutableListFold(source, "seed") { acc, value -> "($acc>$value)" }
            emptyReduced = useImmutableListReduce(persistentListOf<Int>()) { acc, value -> acc + value }
        }

        waitForIdle()
        assertEquals("((a>b)>c)", reduced!!.value)
        assertEquals(null, reducedOrNull!!.value)
        assertEquals("(((seed>a)>b)>c)", folded!!.value)
        assertFailsWith<UnsupportedOperationException> { emptyReduced!!.value }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun single_element_reducers_return_the_exact_element_without_operation() = runComposeUiTest {
        val element = EqualValue("same")
        var reduceCalls = 0
        var reduceOrNullCalls = 0
        var reduced: State<EqualValue>? = null
        var reducedOrNull: State<EqualValue?>? = null

        setContent {
            val source = persistentListOf(element)
            reduced = useImmutableListReduce(source) { _, value ->
                reduceCalls += 1
                value
            }
            reducedOrNull = useImmutableListReduceOrNull(source) { _, value ->
                reduceOrNullCalls += 1
                value
            }
        }

        waitForIdle()
        assertSame(element, reduced!!.value)
        assertSame(element, reducedOrNull!!.value)
        assertEquals(0, reduceCalls)
        assertEquals(0, reduceOrNullCalls)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun reducers_use_latest_operation_and_preserve_state_identity() = runComposeUiTest {
        var separator by mutableStateOf("+")
        var source by mutableStateOf(persistentListOf("a", "b"))
        var reduced: State<String>? = null
        var reducedOrNull: State<String?>? = null

        setContent {
            val currentSeparator = separator
            reduced = useImmutableListReduce(source) { acc, value -> "$acc$currentSeparator$value" }
            reducedOrNull = useImmutableListReduceOrNull(source) { acc, value -> "$acc$currentSeparator$value" }
        }

        waitForIdle()
        val initialReducedState = reduced
        val initialReducedOrNullState = reducedOrNull
        assertEquals("a+b", reduced!!.value)
        assertEquals("a+b", reducedOrNull!!.value)

        separator = "-"
        waitForIdle()
        assertSame(initialReducedState, reduced)
        assertSame(initialReducedOrNullState, reducedOrNull)
        assertEquals("a-b", reduced!!.value)
        assertEquals("a-b", reducedOrNull!!.value)

        source = persistentListOf("x", "y", "z")
        waitForIdle()
        assertSame(initialReducedState, reduced)
        assertSame(initialReducedOrNullState, reducedOrNull)
        assertEquals("x-y-z", reduced!!.value)
        assertEquals("x-y-z", reducedOrNull!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun reducers_and_fold_observe_equal_but_distinct_inputs() = runComposeUiTest {
        val firstElement = EqualValue("element")
        val replacementElement = EqualValue("element")
        val firstInitial = EqualValue("initial")
        val replacementInitial = EqualValue("initial")
        var source by mutableStateOf(persistentListOf(firstElement), referentialEqualityPolicy())
        var initial by mutableStateOf(firstInitial, referentialEqualityPolicy())
        var reduced: State<EqualValue>? = null
        var folded: State<EqualValue>? = null

        setContent {
            reduced = useImmutableListReduce(source) { _, value -> value }
            folded = useImmutableListFold(persistentListOf<EqualValue>(), initial) { acc, _ -> acc }
        }

        waitForIdle()
        val initialReducedState = reduced
        val initialFoldedState = folded
        assertSame(firstElement, reduced!!.value)
        assertSame(firstInitial, folded!!.value)

        source = persistentListOf(replacementElement)
        initial = replacementInitial
        waitForIdle()

        assertSame(initialReducedState, reduced)
        assertSame(initialFoldedState, folded)
        assertSame(replacementElement, reduced!!.value)
        assertSame(replacementInitial, folded!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun remember_aliases_support_nullable_and_supertype_results() = runComposeUiTest {
        lateinit var holder: ImmutableListHolder<Int?>
        var reduced: State<Number>? = null
        var reducedOrNull: State<Int?>? = null
        var folded: State<String>? = null

        setContent {
            holder = rememberImmutableList(1, null, 2)
            reduced = rememberImmutableListReduce<Number, Int>(persistentListOf(1, 2, 3)) { acc, value ->
                acc.toDouble() + value
            }
            reducedOrNull = rememberImmutableListReduceOrNull<Int?, Int?>(persistentListOf(null, 2)) { acc, value ->
                (acc ?: 0) + (value ?: 0)
            }
            folded = rememberImmutableListFold(persistentListOf(1, 2), initial = "n") { acc, value -> "$acc$value" }
        }

        waitForIdle()
        assertEquals(persistentListOf(1, null, 2), holder.list.value)
        assertEquals(6.0, reduced!!.value)
        assertEquals(2, reducedOrNull!!.value)
        assertEquals("n12", folded!!.value)
    }

    private data class EqualValue(val value: String)
}
