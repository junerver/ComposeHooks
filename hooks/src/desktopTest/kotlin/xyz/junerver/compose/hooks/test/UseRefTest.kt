package xyz.junerver.compose.hooks.test

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import xyz.junerver.compose.hooks.MutableRef
import xyz.junerver.compose.hooks.Ref
import xyz.junerver.compose.hooks.observeAsState
import xyz.junerver.compose.hooks.rememberRef
import xyz.junerver.compose.hooks.useRef
import xyz.junerver.compose.hooks.useUpdate

/*
  Description:
  Author: Junerver
  Date: 2024/3/18-15:00
  Email: junerver@gmail.com
  Version: v1.0
*/
@Suppress("DEPRECATION")
class UseRefTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testRefWhenSetCurrent() = runComposeUiTest {
        var refresh = 0
        setContent {
            val countRef = useRef(default = 0)
            val update = useUpdate()
            SideEffect {
                refresh++
            }
            Column {
                Text(text = "Result:${countRef.current}")
                Button(onClick = { countRef.current += 1 }) {
                    Text(text = "+1")
                }
                Button(onClick = { update() }) {
                    Text(text = "update")
                }
            }
        }
        val buttonNode = onNodeWithText("+1")
        val updateNode = onNodeWithText("update")
        onNodeWithText("Result:0").assertExists()
        // when click button will not recompose
        buttonNode.performClick() // ref -> 1
        onNodeWithText("Result:0").assertExists()
        assertEquals(1, refresh)
        buttonNode.performClick() // ref -> 2
        assertEquals(1, refresh) // will not execute sideEffect
        updateNode.performClick() // recompose ui
        waitForIdle()
        onNodeWithText("Result:2").assertExists()
        assertEquals(2, refresh) // component recompose sideEffect executed
    }

    @Test
    fun observers_can_remove_themselves_during_notification() {
        val ref = MutableRef(0)
        val events = mutableListOf<String>()
        lateinit var firstObserver: (Int) -> Unit
        firstObserver = { value ->
            events += "first:$value"
            if (value == 1) {
                ref.removeObserver(firstObserver)
            }
        }
        val secondObserver: (Int) -> Unit = { value -> events += "second:$value" }

        ref.observe(firstObserver)
        ref.observe(secondObserver)
        ref.current = 1
        ref.current = 2

        assertEquals(
            listOf("first:0", "second:0", "first:1", "second:1", "second:2"),
            events,
        )
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun observe_as_state_switches_to_the_latest_ref_and_keeps_state_identity() = runComposeUiTest {
        val first = MutableRef("first")
        val second = MutableRef("second")
        val activeRef = mutableStateOf<Ref<String>>(first, referentialEqualityPolicy())
        lateinit var observed: State<String>

        setContent {
            observed = activeRef.value.observeAsState()
            Text("value=${observed.value}")
        }

        waitForIdle()
        val initialState = observed
        first.current = "first-updated"
        waitForIdle()
        assertEquals("first-updated", observed.value)

        activeRef.value = second
        waitForIdle()
        assertSame(initialState, observed)
        assertEquals("second", observed.value)

        first.current = "stale"
        waitForIdle()
        assertEquals("second", observed.value)

        second.current = "second-updated"
        waitForIdle()
        assertEquals("second-updated", observed.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun observe_as_state_unsubscribes_on_unmount() = runComposeUiTest {
        val ref = MutableRef(0)
        val mounted = mutableStateOf(true)
        lateinit var observed: State<Int>

        setContent {
            if (mounted.value) {
                observed = ref.observeAsState()
                Text("value=${observed.value}")
            }
        }

        waitForIdle()
        ref.current = 1
        waitForIdle()
        assertEquals(1, observed.value)

        mounted.value = false
        waitForIdle()
        ref.current = 2
        assertEquals(1, observed.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun use_ref_and_alias_keep_initial_nullable_state_and_identity() = runComposeUiTest {
        val default = mutableStateOf<String?>(null, referentialEqualityPolicy())
        lateinit var ref: MutableRef<String?>
        lateinit var alias: MutableRef<Int>

        setContent {
            ref = useRef(default.value)
            alias = rememberRef(1)
        }

        waitForIdle()
        val initialRef = ref
        val initialAlias = alias
        ref.current = "local"
        alias.current = 2
        default.value = "replacement"
        waitForIdle()

        assertSame(initialRef, ref)
        assertSame(initialAlias, alias)
        assertEquals("local", ref.current)
        assertEquals(2, alias.current)
    }
}
