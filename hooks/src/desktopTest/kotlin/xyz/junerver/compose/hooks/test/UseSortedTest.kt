package xyz.junerver.compose.hooks.test

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import xyz.junerver.compose.hooks.useSorted

@Suppress("DEPRECATION")
class UseSortedTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun sorted_uses_default_order_for_comparable_items() = runComposeUiTest {
        var result: State<List<Int>>? = null

        setContent {
            result = useSorted(listOf(5, 3, 1, 4, 2))
        }

        waitForIdle()
        assertEquals(listOf(1, 2, 3, 4, 5), result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun sorted_supports_custom_compare_function() = runComposeUiTest {
        var result: State<List<Int>>? = null

        setContent {
            result = useSorted(listOf(1, 2, 3, 4, 5)) { a, b -> b - a }
        }

        waitForIdle()
        assertEquals(listOf(5, 4, 3, 2, 1), result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun sorted_recomputes_when_source_changes() = runComposeUiTest {
        var source by mutableStateOf(listOf(3, 1, 2))
        var result: State<List<Int>>? = null

        setContent {
            result = useSorted(source)
        }

        waitForIdle()
        assertEquals(listOf(1, 2, 3), result!!.value)

        source = listOf(9, 7, 8)
        waitForIdle()
        assertEquals(listOf(7, 8, 9), result!!.value)
    }
}
