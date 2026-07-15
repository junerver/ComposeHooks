package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import xyz.junerver.compose.hooks.Ref
import xyz.junerver.compose.hooks.rememberCreation
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.hooks.useState

/*
  Description: useCreation comprehensive TDD tests
  Author: AI Assistant
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

@Suppress("DEPRECATION")
class UseCreationTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useCreation_creates_value_once() = runComposeUiTest {
        var creationCount = 0

        setContent {
            var recomposeCount by useState(0)
            val ref = useCreation {
                creationCount++
                "created"
            }

            SideEffect {
                if (recomposeCount < 3) {
                    recomposeCount++
                }
            }

            Text("value=${ref.current} recomposeCount=$recomposeCount")
        }
        waitForIdle()
        onNodeWithText("value=created recomposeCount=3").assertExists()
        // Creation should only happen once
        assert(creationCount == 1) { "Expected creationCount=1, got $creationCount" }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useCreation_recreates_when_key_changes() = runComposeUiTest {
        var creationCount = 0

        setContent {
            var key by useState(1)
            val ref = useCreation(key) {
                creationCount++
                "value-$key"
            }

            SideEffect {
                if (key < 3) {
                    key++
                }
            }

            Text("value=${ref.current} key=$key")
        }
        waitForIdle()
        onNodeWithText("value=value-3 key=3").assertExists()
        // Creation should happen 3 times (for key 1, 2, 3)
        assert(creationCount == 3) { "Expected creationCount=3, got $creationCount" }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useCreation_with_multiple_keys() = runComposeUiTest {
        var creationCount = 0

        setContent {
            var key1 by useState(1)
            var key2 by useState("a")
            var phase by useState(0)

            val ref = useCreation(key1, key2) {
                creationCount++
                "$key1-$key2"
            }

            SideEffect {
                when (phase) {
                    0 -> {
                        key1 = 2
                        phase = 1
                    }
                    1 -> {
                        key2 = "b"
                        phase = 2
                    }
                }
            }

            Text("value=${ref.current} phase=$phase")
        }
        waitForIdle()
        onNodeWithText("value=2-b phase=2").assertExists()
        // Creation should happen 3 times (initial, key1 change, key2 change)
        assert(creationCount == 3) { "Expected creationCount=3, got $creationCount" }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useCreation_returns_ref_with_current() = runComposeUiTest {
        setContent {
            val ref = useCreation { 42 }
            Text("value=${ref.current}")
        }
        waitForIdle()
        onNodeWithText("value=42").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useCreation_ref_current_is_accessible() = runComposeUiTest {
        setContent {
            val ref = useCreation { 100 }
            Text("value=${ref.current}")
        }
        waitForIdle()
        onNodeWithText("value=100").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useCreation_with_complex_object() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            var size by useState(3)
            val ref = useCreation {
                mutableListOf(1, 2, 3)
            }

            SideEffect {
                if (!fired) {
                    fired = true
                    ref.current.add(4)
                    size = ref.current.size
                }
            }

            Text("size=$size")
        }
        waitForIdle()
        onNodeWithText("size=4").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useCreation_no_keys_never_recreates() = runComposeUiTest {
        var creationCount = 0

        setContent {
            var counter by useState(0)
            val ref = useCreation {
                creationCount++
                "constant"
            }

            SideEffect {
                if (counter < 5) {
                    counter++
                }
            }

            Text("value=${ref.current} counter=$counter")
        }
        waitForIdle()
        onNodeWithText("value=constant counter=5").assertExists()
        assert(creationCount == 1) { "Expected creationCount=1, got $creationCount" }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun reference_identity_only_changes_when_key_changes() = runComposeUiTest {
        val key = mutableStateOf(1)
        val unrelated = mutableStateOf(0)
        lateinit var observed: Ref<StringBuilder>
        setContent {
            observed = useCreation(key.value) { StringBuilder("key=${key.value}") }
            Text("unrelated=${unrelated.value} value=${observed.current}")
        }

        waitForIdle()
        val first = observed
        runOnIdle { unrelated.value++ }
        waitForIdle()
        assertSame(first, observed, "Unrelated recomposition must keep the created reference")

        runOnIdle { key.value++ }
        waitForIdle()
        assertNotSame(first, observed, "Changing a key must create a new reference")
        assertEquals("key=2", observed.current.toString())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun equal_keys_keep_reference_identity() = runComposeUiTest {
        val key = mutableStateOf(listOf(1))
        val trigger = mutableStateOf(0)
        lateinit var observed: Ref<Any>
        setContent {
            observed = useCreation(key.value) { Any() }
            Text("trigger=${trigger.value}")
        }

        waitForIdle()
        val first = observed
        runOnIdle {
            key.value = listOf(1)
            trigger.value++
        }
        waitForIdle()

        assertSame(first, observed, "Structurally equal keys should follow Compose remember semantics")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun remember_alias_uses_the_same_key_semantics() = runComposeUiTest {
        val key = mutableStateOf("a")
        lateinit var observed: Ref<String>
        setContent {
            observed = rememberCreation(key.value) { "value-${key.value}" }
            Text(observed.current)
        }

        waitForIdle()
        val first = observed
        runOnIdle { key.value = "b" }
        waitForIdle()

        assertNotSame(first, observed)
        assertEquals("value-b", observed.current)
    }
}
