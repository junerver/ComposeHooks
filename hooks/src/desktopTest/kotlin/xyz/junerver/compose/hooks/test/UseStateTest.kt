package xyz.junerver.compose.hooks.test

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import xyz.junerver.compose.hooks._rememberState
import xyz.junerver.compose.hooks._useState
import xyz.junerver.compose.hooks.rememberState
import xyz.junerver.compose.hooks.useState

@Suppress("DEPRECATION")
class UseStateTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun state_updates_when_setter_is_called() = runComposeUiTest {
        setContent {
            val state = useState(default = 1)

            SideEffect {
                if (state.value == 1) {
                    state.value = 2
                }
            }

            Text("value=${state.value}")
        }

        waitForIdle()
        onNodeWithText("value=2").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun create_controlled_components() = runComposeUiTest {
        setContent {
            val (state, setState) = useState("")
            OutlinedTextField(value = state, onValueChange = setState, label = { Text("Label") })
        }
        val textField = onNode(hasText("Label"))
        textField.performTextInput("Hello, Compose!")
        onNode(hasText("Hello, Compose!")).assertExists()
        textField.performTextClearance()
        onNode(hasText("")).assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun nullable_state_supports_null_and_later_updates() = runComposeUiTest {
        lateinit var state: MutableState<String?>
        setContent {
            state = _useState<String?>(null)
            Text("value=${state.value ?: "null"}")
        }

        waitForIdle()
        onNodeWithText("value=null").assertExists()

        state.value = "updated"
        waitForIdle()
        onNodeWithText("value=updated").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun derived_state_recomputes_when_dependency_keys_change() = runComposeUiTest {
        var source by mutableStateOf(1)
        var latestValue = 0
        var recomputeCount = 0

        setContent {
            val derived = useState(source) {
                recomputeCount += 1
                source * 10
            }
            latestValue = derived.value
            Text("derived=$latestValue count=$recomputeCount")
        }

        waitForIdle()
        assertEquals(10, latestValue)
        assertEquals(1, recomputeCount)

        source = 2
        waitForIdle()
        assertEquals(20, latestValue)
        assertEquals(2, recomputeCount)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun mutable_state_keeps_identity_and_ignores_later_equal_default() = runComposeUiTest {
        val firstDefault = EqualValue("same")
        val replacementDefault = EqualValue("same")
        val default = mutableStateOf(firstDefault, referentialEqualityPolicy())
        lateinit var state: MutableState<EqualValue>

        setContent {
            state = useState(default.value)
        }

        waitForIdle()
        val initialState = state
        val local = EqualValue("local")
        state.value = local
        default.value = replacementDefault
        waitForIdle()

        assertSame(initialState, state)
        assertSame(local, state.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun numeric_state_applies_rapid_setter_updates() = runComposeUiTest {
        lateinit var state: MutableState<Int>
        lateinit var setValue: (Int) -> Unit

        setContent {
            state = useState(0)
            val (_, setter) = state
            setValue = setter
        }

        waitForIdle()
        setValue(1)
        setValue(2)
        setValue(3)
        assertEquals(3, state.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun nullable_and_non_null_state_aliases_keep_their_contracts() = runComposeUiTest {
        lateinit var nullable: MutableState<String?>
        lateinit var nonNull: MutableState<Int>

        setContent {
            nullable = _rememberState(null)
            nonNull = rememberState(1)
        }

        waitForIdle()
        val initialNullable = nullable
        val initialNonNull = nonNull
        nullable.value = "value"
        nullable.value = null
        nonNull.value = 2
        waitForIdle()

        assertSame(initialNullable, nullable)
        assertSame(initialNonNull, nonNull)
        assertEquals(null, nullable.value)
        assertEquals(2, nonNull.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun derived_state_reuses_equal_keys_and_rebuilds_for_distinct_keys() = runComposeUiTest {
        val first = EqualValue("same")
        val replacement = EqualValue("same")
        val next = EqualValue("next")
        val key = mutableStateOf(first, referentialEqualityPolicy())
        lateinit var derived: State<EqualValue>

        setContent {
            val currentKey = key.value
            derived = useState(currentKey) { currentKey }
        }

        waitForIdle()
        val initialState = derived
        assertSame(first, derived.value)

        key.value = replacement
        waitForIdle()
        assertSame(initialState, derived)
        assertSame(first, derived.value)

        key.value = next
        waitForIdle()
        assertNotSame(initialState, derived)
        assertSame(next, derived.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun derived_state_without_keys_tracks_snapshot_reads_and_alias_matches() = runComposeUiTest {
        val source = mutableStateOf(1)
        lateinit var derived: State<Int>
        lateinit var alias: State<Int>

        setContent {
            derived = useState { source.value * 2 }
            alias = rememberState("stable") { source.value * 3 }
        }

        waitForIdle()
        val initialDerived = derived
        val initialAlias = alias
        assertEquals(2, derived.value)
        assertEquals(3, alias.value)

        source.value = 2
        waitForIdle()
        assertSame(initialDerived, derived)
        assertSame(initialAlias, alias)
        assertEquals(4, derived.value)
        assertEquals(6, alias.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun state_is_recreated_after_unmount() = runComposeUiTest {
        val mounted = mutableStateOf(true)
        lateinit var state: MutableState<String>

        setContent {
            if (mounted.value) {
                state = _useState("initial")
            }
        }

        waitForIdle()
        val firstState = state
        state.value = "local"
        mounted.value = false
        waitForIdle()
        mounted.value = true
        waitForIdle()

        assertNotSame(firstState, state)
        assertEquals("initial", state.value)
    }

    private data class EqualValue(val value: String)
}
