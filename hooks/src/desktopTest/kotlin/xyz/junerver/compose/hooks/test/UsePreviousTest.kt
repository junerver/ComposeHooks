package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import xyz.junerver.compose.hooks.rememberPrevious
import xyz.junerver.compose.hooks.usePrevious
import xyz.junerver.compose.hooks.useState

/*
  Description: usePrevious comprehensive TDD tests
  Author: AI Assistant
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

@Suppress("DEPRECATION")
class UsePreviousTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun initial_previous_is_null() = runComposeUiTest {
        setContent {
            val previous = usePrevious("current")
            Text("previous=${previous.value}")
        }
        waitForIdle()
        onNodeWithText("previous=null").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun previous_tracks_value_changes() = runComposeUiTest {
        setContent {
            var current by useState("a")
            val previous = usePrevious(current)

            SideEffect {
                if (current == "a") {
                    current = "b"
                }
            }

            Text("current=$current previous=${previous.value}")
        }
        waitForIdle()
        onNodeWithText("current=b previous=a").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun previous_updates_on_multiple_changes() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            var current by useState("a")
            val previous = usePrevious(current)

            SideEffect {
                when (phase) {
                    0 -> {
                        current = "b"
                        phase = 1
                    }
                    1 -> {
                        current = "c"
                        phase = 2
                    }
                }
            }

            Text("current=$current previous=${previous.value} phase=$phase")
        }
        waitForIdle()
        onNodeWithText("current=c previous=b phase=2").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun previous_with_integer_values() = runComposeUiTest {
        setContent {
            var current by useState(1)
            val previous = usePrevious(current)

            SideEffect {
                if (current == 1) {
                    current = 2
                }
            }

            Text("current=$current previous=${previous.value}")
        }
        waitForIdle()
        onNodeWithText("current=2 previous=1").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun previous_with_nullable_values() = runComposeUiTest {
        setContent {
            var current by useState("initial")
            var phase by useState(0)
            val previous = usePrevious(current)

            SideEffect {
                if (phase == 0) {
                    current = "changed"
                    phase = 1
                }
            }

            Text("current=$current previous=${previous.value}")
        }
        waitForIdle()
        onNodeWithText("current=changed previous=initial").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun previous_same_value_no_change() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            var current by useState("same")
            val previous = usePrevious(current)

            SideEffect {
                when (phase) {
                    0 -> {
                        current = "same"
                        phase = 1
                    } // Same value
                }
            }

            Text("previous=${previous.value} phase=$phase")
        }
        waitForIdle()
        // Previous should still be null since value didn't actually change
        onNodeWithText("previous=null phase=1").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun previous_with_object_values() = runComposeUiTest {
        setContent {
            var current by useState(listOf(1, 2, 3))
            val previous = usePrevious(current)

            SideEffect {
                if (current == listOf(1, 2, 3)) {
                    current = listOf(4, 5, 6)
                }
            }

            Text("current=${current.joinToString()} previous=${previous.value?.joinToString()}")
        }
        waitForIdle()
        onNodeWithText("current=4, 5, 6 previous=1, 2, 3").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun previous_three_value_sequence() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            var current by useState("first")
            val previous = usePrevious(current)

            SideEffect {
                when (phase) {
                    0 -> {
                        current = "second"
                        phase = 1
                    }
                    1 -> {
                        current = "third"
                        phase = 2
                    }
                    2 -> {
                        current = "fourth"
                        phase = 3
                    }
                }
            }

            Text("current=$current previous=${previous.value} phase=$phase")
        }
        waitForIdle()
        onNodeWithText("current=fourth previous=third phase=3").assertExists()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun queued_value_changes_preserve_the_immediate_previous_composition() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val recomposer = Recomposer(dispatcher)
        val composition = Composition(PreviousUnitApplier(), recomposer)
        val observed = AtomicReference<State<String?>>()
        var present = "first"
        val content: @Composable () -> Unit = {
            observed.set(usePrevious(present))
        }

        try {
            composition.setContent(content)
            present = "second"
            composition.setContent(content)
            present = "third"
            composition.setContent(content)
            runCurrent()

            assertEquals("second", observed.get().value)
        } finally {
            composition.dispose()
            recomposer.cancel()
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun equal_distinct_present_is_used_before_the_next_distinct_change() = runComposeUiTest {
        val first = EqualValue("same")
        val replacement = EqualValue("same")
        val next = EqualValue("next")
        val present = mutableStateOf(first, referentialEqualityPolicy())
        lateinit var previous: State<EqualValue?>

        setContent {
            previous = usePrevious(present.value)
        }

        waitForIdle()
        val initialState = previous
        assertEquals(null, previous.value)

        present.value = replacement
        waitForIdle()
        assertSame(initialState, previous)
        assertEquals(null, previous.value)

        present.value = next
        waitForIdle()
        assertSame(initialState, previous)
        assertSame(replacement, previous.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun remember_previous_alias_tracks_real_nullable_sequence() = runComposeUiTest {
        val present = mutableStateOf<String?>(null)
        lateinit var previous: State<String?>

        setContent {
            previous = rememberPrevious(present.value)
        }

        waitForIdle()
        val initialState = previous
        assertEquals(null, previous.value)

        present.value = "value"
        waitForIdle()
        assertEquals(null, previous.value)

        present.value = null
        waitForIdle()
        assertSame(initialState, previous)
        assertEquals("value", previous.value)
    }

    private data class EqualValue(val value: String)
}

private class PreviousUnitApplier : AbstractApplier<Unit>(Unit) {
    override fun insertTopDown(index: Int, instance: Unit) = Unit

    override fun insertBottomUp(index: Int, instance: Unit) = Unit

    override fun remove(index: Int, count: Int) = Unit

    override fun move(from: Int, to: Int, count: Int) = Unit

    override fun onClear() = Unit
}
