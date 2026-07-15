package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import xyz.junerver.compose.hooks.Dispatch
import xyz.junerver.compose.hooks.rememberDispatch
import xyz.junerver.compose.hooks.rememberSelector
import xyz.junerver.compose.hooks.useDispatch
import xyz.junerver.compose.hooks.useSelector
import xyz.junerver.compose.hooks.useredux.ReduxProvider
import xyz.junerver.compose.hooks.useredux.createStore

/*
  Description: Redux routing and selector behavior tests
  Author: Junerver
  Date: 2026/7/15
  Email: junerver@gmail.com
  Version: v1.0
*/

private sealed interface RoutingAction {
    data class Add(val value: Int) : RoutingAction
}

private val routingReducer: (Int, RoutingAction) -> Int = { state, action ->
    when (action) {
        is RoutingAction.Add -> state + action.value
    }
}

@Suppress("DEPRECATION")
class UseReduxRoutingTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun sync_dispatch_tracks_latest_alias_after_recomposition() = runComposeUiTest {
        val alias = mutableStateOf("first")
        lateinit var dispatch: Dispatch<RoutingAction>
        lateinit var firstState: State<Int>
        lateinit var secondState: State<Int>

        setContent {
            val store = createStore {
                named<Int, RoutingAction>("first") { routingReducer with 1 }
                named<Int, RoutingAction>("second") { routingReducer with 10 }
            }
            ReduxProvider(store) {
                dispatch = useDispatch(alias.value)
                firstState = useSelector(alias = "first")
                secondState = useSelector(alias = "second")
                Text("first=${firstState.value} second=${secondState.value}")
            }
        }

        waitForIdle()
        dispatch(RoutingAction.Add(2))
        waitForIdle()
        assertEquals(3, firstState.value)
        assertEquals(10, secondState.value)

        runOnIdle { alias.value = "second" }
        waitForIdle()
        dispatch(RoutingAction.Add(5))
        waitForIdle()

        assertEquals(3, firstState.value)
        assertEquals(15, secondState.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun explicit_missing_alias_does_not_fall_back_to_type_route() = runComposeUiTest {
        val error = assertFailsWith<IllegalStateException> {
            setContent {
                val store = createStore { routingReducer with 0 }
                ReduxProvider(store) {
                    useDispatch<RoutingAction>(alias = "missing")
                }
            }
            waitForIdle()
        }

        assertEquals(
            "Please confirm alias:<missing> that you have correctly registered in `createStore`!",
            error.message,
        )
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun projected_selector_tracks_latest_alias_and_projector_with_stable_identity() = runComposeUiTest {
        val alias = mutableStateOf("first")
        val projectorVersion = mutableStateOf(0)
        lateinit var selected: State<String>
        var initialSelected: State<String>? = null

        setContent {
            val store = createStore {
                named<Int, RoutingAction>("first") { routingReducer with 1 }
                named<Int, RoutingAction>("second") { routingReducer with 10 }
            }
            ReduxProvider(store) {
                val projector: Int.() -> String = if (projectorVersion.value == 0) {
                    { "old:$this" }
                } else {
                    { "latest:$this" }
                }
                selected = useSelector(alias.value, projector)
                if (initialSelected == null) initialSelected = selected
                Text("selected=${selected.value}")
            }
        }

        waitForIdle()
        onNodeWithText("selected=old:1").assertExists()

        projectorVersion.value = 1
        waitForIdle()
        onNodeWithText("selected=latest:1").assertExists()

        alias.value = "second"
        waitForIdle()
        onNodeWithText("selected=latest:10").assertExists()
        assertSame(initialSelected, selected)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun alias_type_and_remember_routes_share_registered_state_and_dispatch() = runComposeUiTest {
        lateinit var typeState: State<Int>
        lateinit var aliasState: State<Int>
        lateinit var rememberedState: State<Int>
        lateinit var typeDispatch: Dispatch<RoutingAction>
        lateinit var aliasDispatch: Dispatch<RoutingAction>
        lateinit var rememberedDispatch: Dispatch<RoutingAction>

        setContent {
            val store = createStore {
                named<Int, RoutingAction>("counter") { routingReducer with 0 }
            }
            ReduxProvider(store) {
                typeState = useSelector()
                aliasState = useSelector(alias = "counter")
                rememberedState = rememberSelector(alias = "counter")
                typeDispatch = useDispatch()
                aliasDispatch = useDispatch(alias = "counter")
                rememberedDispatch = rememberDispatch(alias = "counter")
                Text("count=${typeState.value}")
            }
        }

        waitForIdle()
        assertSame(typeState, aliasState)
        assertSame(typeState, rememberedState)
        assertSame(typeDispatch, aliasDispatch)
        assertSame(typeDispatch, rememberedDispatch)

        aliasDispatch(RoutingAction.Add(1))
        typeDispatch(RoutingAction.Add(2))
        waitForIdle()
        assertEquals(3, typeState.value)
        assertEquals(3, aliasState.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun nested_provider_isolates_inner_type_route_from_outer_provider() = runComposeUiTest {
        lateinit var outerState: State<Int>
        lateinit var innerState: State<Int>
        lateinit var outerDispatch: Dispatch<RoutingAction>
        lateinit var innerDispatch: Dispatch<RoutingAction>

        setContent {
            val outerStore = createStore {
                named<Int, RoutingAction>("outer") { routingReducer with 1 }
            }
            val innerStore = createStore {
                named<Int, RoutingAction>("inner") { routingReducer with 100 }
            }
            ReduxProvider(outerStore) {
                outerState = useSelector()
                outerDispatch = useDispatch()
                ReduxProvider(innerStore) {
                    innerState = useSelector()
                    innerDispatch = useDispatch()
                    Text("inner=${innerState.value}")
                }
                Text("outer=${outerState.value}")
            }
        }

        waitForIdle()
        innerDispatch(RoutingAction.Add(10))
        outerDispatch(RoutingAction.Add(2))
        waitForIdle()

        assertEquals(3, outerState.value)
        assertEquals(110, innerState.value)
    }
}
