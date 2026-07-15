package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import xyz.junerver.compose.hooks.Dispatch
import xyz.junerver.compose.hooks.DispatchAsync
import xyz.junerver.compose.hooks.useDispatchAsync
import xyz.junerver.compose.hooks.useredux.ReduxProvider
import xyz.junerver.compose.hooks.useredux.createStore
import xyz.junerver.compose.hooks.useredux.useSelector

/*
  Description: useDispatchAsync desktop behavior tests
  Author: Junerver
  Date: 2026/7/15
  Email: junerver@gmail.com
  Version: v1.0
*/

private sealed interface AsyncDispatchAction {
    data class Add(val value: Int) : AsyncDispatchAction
}

private val asyncDispatchReducer: (Int, AsyncDispatchAction) -> Int = { state, action ->
    when (action) {
        is AsyncDispatchAction.Add -> state + action.value
    }
}

@Suppress("DEPRECATION")
class UseDispatchAsyncTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun concurrent_calls_dispatch_every_result_and_finish_after_each_block() = runComposeUiTest {
        val firstGate = CompletableDeferred<Unit>()
        val secondGate = CompletableDeferred<Unit>()
        val beforeCount = AtomicInteger(0)
        val finallyCount = AtomicInteger(0)
        lateinit var dispatchAsync: DispatchAsync<AsyncDispatchAction>

        setContent {
            val store = createStore { asyncDispatchReducer with 0 }
            ReduxProvider(store) {
                dispatchAsync = useDispatchAsync(
                    onBefore = { beforeCount.incrementAndGet() },
                    onFinally = { finallyCount.incrementAndGet() },
                )
                val state = useSelector<Int>()
                Text("count=${state.value}")
            }
        }

        waitForIdle()
        dispatchAsync {
            firstGate.await()
            AsyncDispatchAction.Add(1)
        }
        dispatchAsync {
            secondGate.await()
            AsyncDispatchAction.Add(10)
        }

        waitForIdle()
        assertEquals(2, beforeCount.get())
        assertEquals(0, finallyCount.get())

        secondGate.complete(Unit)
        repeat(20) {
            waitForIdle()
            if (finallyCount.get() == 1) return@repeat
            Thread.sleep(10)
        }
        onNodeWithText("count=10").assertExists()
        assertEquals(1, finallyCount.get())

        firstGate.complete(Unit)
        repeat(20) {
            waitForIdle()
            if (finallyCount.get() == 2) return@repeat
            Thread.sleep(10)
        }
        onNodeWithText("count=11").assertExists()
        assertEquals(2, finallyCount.get())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun runner_identity_is_stable_and_callbacks_are_latest_after_recomposition() = runComposeUiTest {
        lateinit var dispatchAsync: DispatchAsync<AsyncDispatchAction>
        var initialDispatchAsync: DispatchAsync<AsyncDispatchAction>? = null
        val beforePhase = AtomicInteger(-1)
        val finallyPhase = AtomicInteger(-1)
        val phase = mutableStateOf(0)

        setContent {
            val store = createStore { asyncDispatchReducer with 0 }
            ReduxProvider(store) {
                val onBefore: (Dispatch<AsyncDispatchAction>) -> Unit = if (phase.value == 0) {
                    { beforePhase.set(0) }
                } else {
                    { beforePhase.set(1) }
                }
                val onFinally: (Dispatch<AsyncDispatchAction>) -> Unit = if (phase.value == 0) {
                    { finallyPhase.set(0) }
                } else {
                    { finallyPhase.set(1) }
                }
                dispatchAsync = useDispatchAsync(
                    onBefore = onBefore,
                    onFinally = onFinally,
                )
                if (initialDispatchAsync == null) initialDispatchAsync = dispatchAsync
                val state = useSelector<Int>()
                SideEffect {
                    if (phase.value == 0) phase.value = 1
                }
                Text("count=${state.value} phase=${phase.value}")
            }
        }

        waitForIdle()
        assertSame(initialDispatchAsync, dispatchAsync)

        dispatchAsync { AsyncDispatchAction.Add(1) }
        repeat(20) {
            waitForIdle()
            if (finallyPhase.get() == 1) return@repeat
            Thread.sleep(10)
        }

        onNodeWithText("count=1 phase=1").assertExists()
        assertEquals(1, beforePhase.get())
        assertEquals(1, finallyPhase.get())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun unmount_cancels_running_block_before_finally_callback() = runComposeUiTest {
        val mounted = mutableStateOf(true)
        val started = CompletableDeferred<Unit>()
        val cancelled = AtomicBoolean(false)
        val finallyCount = AtomicInteger(0)
        lateinit var dispatchAsync: DispatchAsync<AsyncDispatchAction>

        setContent {
            if (mounted.value) {
                val store = createStore { asyncDispatchReducer with 0 }
                ReduxProvider(store) {
                    dispatchAsync = useDispatchAsync(onFinally = { finallyCount.incrementAndGet() })
                    Text("mounted=true")
                }
            }
        }

        waitForIdle()
        dispatchAsync {
            started.complete(Unit)
            try {
                awaitCancellation()
            } finally {
                cancelled.set(true)
            }
        }
        started.await()
        assertEquals(0, finallyCount.get())

        runOnIdle { mounted.value = false }
        waitForIdle()

        assertTrue(cancelled.get())
        assertEquals(1, finallyCount.get())
    }
}
