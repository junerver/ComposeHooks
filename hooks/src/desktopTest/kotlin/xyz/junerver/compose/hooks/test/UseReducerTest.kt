package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import xyz.junerver.compose.hooks.Dispatch
import xyz.junerver.compose.hooks.Middleware
import xyz.junerver.compose.hooks.Reducer
import xyz.junerver.compose.hooks.useReducer
import xyz.junerver.compose.hooks.useState

/*
  Description: useReducer 桌面测试
  Author: Junerver
  Date: 2026/1/9
  Email: junerver@gmail.com
  Version: v1.0
*/

private sealed interface CounterAction {
    data object Inc : CounterAction

    data class Add(val delta: Int) : CounterAction
}

private val counterReducer: Reducer<Int, CounterAction> = { state, action ->
    when (action) {
        CounterAction.Inc -> state + 1
        is CounterAction.Add -> state + action.delta
    }
}

class UseReducerTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun dispatch_updates_state() = runComposeUiTest {
        setContent {
            var fired by useState(default = false)
            val (state, dispatch) = useReducer(counterReducer, 0)

            SideEffect {
                if (fired) return@SideEffect
                fired = true
                dispatch(CounterAction.Inc)
            }

            Text("count=${state.value}")
        }

        waitForIdle()
        onNodeWithText("count=1").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun middleware_chain_applies_left_to_right() = runComposeUiTest {
        val order = mutableListOf<String>()

        val m1: Middleware<Int, CounterAction> = { next: Dispatch<CounterAction>, _: Int ->
            { action ->
                order += "m1-before"
                next(action)
                order += "m1-after"
            }
        }

        val m2: Middleware<Int, CounterAction> = { next: Dispatch<CounterAction>, _: Int ->
            { action ->
                order += "m2-before"
                next(action)
                order += "m2-after"
            }
        }

        setContent {
            var fired by useState(default = false)
            val (state, dispatch) = useReducer(counterReducer, 0, arrayOf(m1, m2))

            SideEffect {
                if (fired) return@SideEffect
                fired = true
                dispatch(CounterAction.Inc)
            }

            Text("count=${state.value}")
        }

        waitForIdle()
        assertEquals(
            listOf("m1-before", "m2-before", "m2-after", "m1-after"),
            order,
        )
        onNodeWithText("count=1").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun reducer_change_takes_effect_on_recompose() = runComposeUiTest {
        val reducerAddOne: Reducer<Int, CounterAction> = { state, action ->
            when (action) {
                CounterAction.Inc -> state + 1
                is CounterAction.Add -> state + action.delta
            }
        }

        val reducerAddTen: Reducer<Int, CounterAction> = { state, action ->
            when (action) {
                CounterAction.Inc -> state + 10
                is CounterAction.Add -> state + action.delta
            }
        }

        setContent {
            var phase by useState(default = 0)
            val reducer = if (phase == 0) reducerAddOne else reducerAddTen
            val (state, dispatch) = useReducer(reducer, 0)

            SideEffect {
                when (phase) {
                    0 -> {
                        dispatch(CounterAction.Inc) // +1
                        phase = 1
                    }

                    1 -> {
                        dispatch(CounterAction.Inc) // +10
                        phase = 2
                    }
                }
            }

            Text("count=${state.value} phase=$phase")
        }

        waitForIdle()
        onNodeWithText("count=11 phase=2").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun middleware_change_takes_effect_on_recompose() = runComposeUiTest {
        val order = mutableListOf<String>()

        val m1: Middleware<Int, CounterAction> = { next: Dispatch<CounterAction>, _: Int ->
            { action ->
                order += "m1"
                next(action)
            }
        }

        val m2: Middleware<Int, CounterAction> = { next: Dispatch<CounterAction>, _: Int ->
            { action ->
                order += "m2"
                next(action)
            }
        }

        setContent {
            var phase by useState(default = 0)
            val middlewares = if (phase == 0) arrayOf(m1) else arrayOf(m2)
            val (state, dispatch) = useReducer(counterReducer, 0, middlewares)

            SideEffect {
                when (phase) {
                    0 -> {
                        dispatch(CounterAction.Inc)
                        phase = 1
                    }

                    1 -> {
                        dispatch(CounterAction.Inc)
                        phase = 2
                    }
                }
            }

            Text("count=${state.value} phase=$phase")
        }

        waitForIdle()
        assertEquals(listOf("m1", "m2"), order)
        onNodeWithText("count=2 phase=2").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun dispatchAsync_returns_action_and_dispatches() = runComposeUiTest {
        setContent {
            var fired by useState(default = false)
            val (state, _, dispatchAsync) = useReducer(counterReducer, 0)

            SideEffect {
                if (fired) return@SideEffect
                fired = true
                dispatchAsync {
                    CounterAction.Add(5)
                }
            }

            Text("count=${state.value}")
        }

        val expected = "count=5"
        for (i in 0 until 20) {
            waitForIdle()
            if (runCatching { onNodeWithText(expected).assertExists() }.isSuccess) return@runComposeUiTest
            Thread.sleep(20)
        }
        onNodeWithText(expected).assertExists()
    }
}
