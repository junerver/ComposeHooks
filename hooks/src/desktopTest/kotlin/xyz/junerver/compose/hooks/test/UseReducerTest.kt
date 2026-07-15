package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import xyz.junerver.compose.hooks.Dispatch
import xyz.junerver.compose.hooks.Middleware
import xyz.junerver.compose.hooks.Reducer
import xyz.junerver.compose.hooks.ReducerHolder
import xyz.junerver.compose.hooks.rememberReducer
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

private data class NullableAction(val value: String?)

@Suppress("DEPRECATION")
class UseReducerTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun concurrent_dispatches_reduce_every_action_without_lost_updates() = runComposeUiTest {
        val firstReducerEntered = CountDownLatch(1)
        val secondReducerEntered = CountDownLatch(1)
        val releaseFirstReducer = CountDownLatch(1)
        val reducerCalls = java.util.concurrent.atomic.AtomicInteger(0)
        val failures = ConcurrentLinkedQueue<Throwable>()
        lateinit var state: State<Int>
        lateinit var dispatch: Dispatch<CounterAction>

        val coordinatedReducer: Reducer<Int, CounterAction> = { previous, action ->
            when (reducerCalls.incrementAndGet()) {
                1 -> {
                    firstReducerEntered.countDown()
                    releaseFirstReducer.await(5, TimeUnit.SECONDS)
                }

                2 -> secondReducerEntered.countDown()
            }
            when (action) {
                CounterAction.Inc -> previous + 1
                is CounterAction.Add -> previous + action.delta
            }
        }

        setContent {
            val holder = useReducer(coordinatedReducer, 0)
            state = holder.state
            dispatch = holder.dispatch
            Text("count=${state.value}")
        }
        waitForIdle()

        val first = thread(name = "reducer-dispatch-1") {
            runCatching { dispatch(CounterAction.Inc) }.exceptionOrNull()?.let(failures::add)
        }
        assertTrue(firstReducerEntered.await(5, TimeUnit.SECONDS))
        val second = thread(name = "reducer-dispatch-2") {
            runCatching { dispatch(CounterAction.Inc) }.exceptionOrNull()?.let(failures::add)
        }

        secondReducerEntered.await(250, TimeUnit.MILLISECONDS)
        releaseFirstReducer.countDown()
        first.join(5_000)
        second.join(5_000)
        waitForIdle()

        assertEquals(emptyList(), failures.toList())
        assertEquals(2, reducerCalls.get())
        assertEquals(2, state.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun remember_alias_preserves_holder_and_actions_across_recomposition() = runComposeUiTest {
        val recomposeTick = mutableStateOf(0)
        lateinit var holder: ReducerHolder<Int, CounterAction>
        var initialHolder: ReducerHolder<Int, CounterAction>? = null

        setContent {
            val tick = recomposeTick.value
            holder = rememberReducer(counterReducer, 0)
            if (initialHolder == null) initialHolder = holder
            Text("count=${holder.state.value} tick=$tick")
        }

        waitForIdle()
        runOnIdle { recomposeTick.value = 1 }
        waitForIdle()

        val firstHolder = initialHolder ?: error("holder was not composed")
        assertSame(firstHolder, holder)
        assertSame(firstHolder.state, holder.state)
        assertSame(firstHolder.dispatch, holder.dispatch)
        assertSame(firstHolder.dispatchAsync, holder.dispatchAsync)

        holder.dispatch(CounterAction.Add(4))
        waitForIdle()
        onNodeWithText("count=4 tick=1").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun reducer_failure_preserves_state_and_later_dispatches_recover() = runComposeUiTest {
        lateinit var state: State<Int>
        lateinit var dispatch: Dispatch<CounterAction>
        var shouldFail = true
        val reducer: Reducer<Int, CounterAction> = { previous, action ->
            if (shouldFail) error("reducer failed")
            when (action) {
                CounterAction.Inc -> previous + 1
                is CounterAction.Add -> previous + action.delta
            }
        }

        setContent {
            val holder = useReducer(reducer, 7)
            state = holder.state
            dispatch = holder.dispatch
            Text("count=${state.value}")
        }
        waitForIdle()

        val error = assertFailsWith<IllegalStateException> { dispatch(CounterAction.Inc) }
        assertEquals("reducer failed", error.message)
        assertEquals(7, state.value)

        shouldFail = false
        dispatch(CounterAction.Add(3))
        waitForIdle()
        assertEquals(10, state.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun middleware_order_and_state_snapshot_are_fresh_for_each_dispatch() = runComposeUiTest {
        val trace = mutableListOf<String>()
        lateinit var state: State<Int>
        lateinit var dispatch: Dispatch<CounterAction>
        val first: Middleware<Int, CounterAction> = { next, stateAtStart ->
            { action ->
                trace += "first-before:$stateAtStart"
                next(action)
                trace += "first-after:$stateAtStart"
            }
        }
        val second: Middleware<Int, CounterAction> = { next, stateAtStart ->
            { action ->
                trace += "second-before:$stateAtStart"
                next(action)
                trace += "second-after:$stateAtStart"
            }
        }

        setContent {
            val holder = useReducer(counterReducer, 0, arrayOf(first, second))
            state = holder.state
            dispatch = holder.dispatch
            Text("count=${state.value}")
        }
        waitForIdle()

        dispatch(CounterAction.Inc)
        dispatch(CounterAction.Add(2))
        waitForIdle()

        assertEquals(3, state.value)
        assertEquals(
            listOf(
                "first-before:0",
                "second-before:0",
                "second-after:0",
                "first-after:0",
                "first-before:1",
                "second-before:1",
                "second-after:1",
                "first-after:1",
            ),
            trace,
        )
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun middleware_can_reentrantly_dispatch_without_deadlock() = runComposeUiTest {
        lateinit var state: State<Int>
        lateinit var dispatch: Dispatch<CounterAction>
        var nested = false
        val middleware: Middleware<Int, CounterAction> = { next, _ ->
            { action ->
                if (action == CounterAction.Inc && !nested) {
                    nested = true
                    dispatch(CounterAction.Add(10))
                }
                next(action)
            }
        }

        setContent {
            val holder = useReducer(counterReducer, 0, arrayOf(middleware))
            state = holder.state
            dispatch = holder.dispatch
            Text("count=${state.value}")
        }
        waitForIdle()

        dispatch(CounterAction.Inc)
        waitForIdle()

        assertEquals(11, state.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun nullable_state_round_trips_null_values() = runComposeUiTest {
        lateinit var state: State<String?>
        lateinit var dispatch: Dispatch<NullableAction>

        setContent {
            val holder = useReducer<String?, NullableAction>(
                reducer = { _, action -> action.value },
                initialState = null,
            )
            state = holder.state
            dispatch = holder.dispatch
            Text("value=${state.value}")
        }
        waitForIdle()
        assertNull(state.value)

        dispatch(NullableAction("ready"))
        waitForIdle()
        assertEquals("ready", state.value)

        dispatch(NullableAction(null))
        waitForIdle()
        assertNull(state.value)
    }

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
    fun multiple_middlewares_participate_in_dispatch() = runComposeUiTest {
        var middlewareInvocationCount = 0

        val m1: Middleware<Int, CounterAction> = { next: Dispatch<CounterAction>, _: Int ->
            { action ->
                middlewareInvocationCount += 1
                next(action)
            }
        }

        val m2: Middleware<Int, CounterAction> = { next: Dispatch<CounterAction>, _: Int ->
            { action ->
                middlewareInvocationCount += 1
                next(action)
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
        assertEquals(2, middlewareInvocationCount)
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
        val m1: Middleware<Int, CounterAction> = { next: Dispatch<CounterAction>, _: Int ->
            { action ->
                when (action) {
                    CounterAction.Inc -> next(CounterAction.Add(1))
                    is CounterAction.Add -> next(action)
                }
            }
        }

        val m2: Middleware<Int, CounterAction> = { next: Dispatch<CounterAction>, _: Int ->
            { action ->
                when (action) {
                    CounterAction.Inc -> next(CounterAction.Add(10))
                    is CounterAction.Add -> next(action)
                }
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
        onNodeWithText("count=11 phase=2").assertExists()
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

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun dispatchAsync_uses_latest_reducer_after_change() = runComposeUiTest {
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
            val (state, _, dispatchAsync) = useReducer(reducer, 0)

            SideEffect {
                when (phase) {
                    0 -> {
                        // 启动异步 dispatch，在异步执行期间 reducer 会变化
                        dispatchAsync {
                            withContext(Dispatchers.Default) { delay(120) } // 等待期间 phase 会变成 1
                            CounterAction.Inc
                        }
                        phase = 1 // 立即切换 reducer
                    }
                }
            }

            Text("count=${state.value} phase=$phase")
        }

        val expected = "count=10 phase=1" // 应使用最新的 reducerAddTen
        for (i in 0 until 30) {
            waitForIdle()
            if (runCatching { onNodeWithText(expected).assertExists() }.isSuccess) return@runComposeUiTest
            Thread.sleep(20)
        }
        onNodeWithText(expected).assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun dispatchAsync_uses_latest_middleware_after_change() = runComposeUiTest {
        val m1: Middleware<Int, CounterAction> = { next: Dispatch<CounterAction>, _: Int ->
            { action ->
                when (action) {
                    CounterAction.Inc -> next(CounterAction.Add(1))
                    is CounterAction.Add -> next(action)
                }
            }
        }

        val m2: Middleware<Int, CounterAction> = { next: Dispatch<CounterAction>, _: Int ->
            { action ->
                when (action) {
                    CounterAction.Inc -> next(CounterAction.Add(10))
                    is CounterAction.Add -> next(action)
                }
            }
        }

        setContent {
            var phase by useState(default = 0)
            val middlewares = if (phase == 0) arrayOf(m1) else arrayOf(m2)
            val (state, _, dispatchAsync) = useReducer(counterReducer, 0, middlewares)

            SideEffect {
                when (phase) {
                    0 -> {
                        // 启动异步 dispatch，在异步执行期间 middleware 会变化
                        dispatchAsync {
                            withContext(Dispatchers.Default) { delay(120) } // 等待期间 phase 会变成 1
                            CounterAction.Inc
                        }
                        phase = 1 // 立即切换 middleware
                    }
                }
            }

            Text("count=${state.value} phase=$phase")
        }

        val expected = "count=10 phase=1"
        for (i in 0 until 30) {
            waitForIdle()
            if (runCatching { onNodeWithText(expected).assertExists() }.isSuccess) break
            Thread.sleep(20)
        }
        onNodeWithText(expected).assertExists()
    }
}
