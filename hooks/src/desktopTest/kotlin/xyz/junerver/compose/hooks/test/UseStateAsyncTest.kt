package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.withContext
import xyz.junerver.compose.hooks.useStateAsync
import xyz.junerver.compose.hooks.annotation.ExperimentalComputed

/*
  Description: useStateAsync desktop behavior tests
  Author: Junerver
  Date: 2026/7/15
  Email: junerver@gmail.com
  Version: v1.0
*/

@OptIn(ExperimentalComputed::class)
class UseStateAsyncTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun factory_change_without_key_change_does_not_restart_eager_computation() = runComposeUiTest {
        val phase = mutableStateOf(0)
        val calls = AtomicInteger(0)
        lateinit var asyncState: State<String?>
        var initialState: State<String?>? = null

        setContent {
            val factory: suspend () -> String = if (phase.value == 0) {
                {
                    calls.incrementAndGet()
                    "first"
                }
            } else {
                {
                    calls.incrementAndGet()
                    "second"
                }
            }
            asyncState = useStateAsync(factory = factory)
            if (initialState == null) initialState = asyncState
            Text("value=${asyncState.value} phase=${phase.value}")
        }

        repeat(20) {
            waitForIdle()
            if (asyncState.value == "first") return@repeat
            Thread.sleep(10)
        }
        assertEquals("first", asyncState.value)
        assertEquals(1, calls.get())

        runOnIdle { phase.value = 1 }
        waitForIdle()

        assertSame(initialState, asyncState)
        assertEquals("first", asyncState.value)
        assertEquals(1, calls.get())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun lazy_repeated_reads_start_only_one_computation() = runComposeUiTest {
        val started = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val calls = AtomicInteger(0)
        lateinit var asyncState: State<String?>

        setContent {
            asyncState = useStateAsync(
                optionsOf = { lazy = true },
                factory = {
                    calls.incrementAndGet()
                    started.complete(Unit)
                    release.await()
                    "value"
                },
            )
            Text("ready=true")
        }

        waitForIdle()
        assertNull(asyncState.value)
        started.await()

        repeat(10) { assertNull(asyncState.value) }
        waitForIdle()
        assertEquals(1, calls.get())

        release.complete(Unit)
        repeat(20) {
            waitForIdle()
            if (asyncState.value == "value") return@repeat
            Thread.sleep(10)
        }
        assertEquals("value", asyncState.value)
        assertEquals(1, calls.get())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun lazy_first_read_uses_latest_factory_after_recomposition() = runComposeUiTest {
        val phase = mutableStateOf(0)
        lateinit var asyncState: State<String?>
        var initialState: State<String?>? = null

        setContent {
            val factory: suspend () -> String = if (phase.value == 0) {
                { "stale" }
            } else {
                { "latest" }
            }
            asyncState = useStateAsync(optionsOf = { lazy = true }, factory = factory)
            if (initialState == null) initialState = asyncState
            SideEffect {
                if (phase.value == 0) phase.value = 1
            }
            Text("phase=${phase.value}")
        }

        waitForIdle()
        assertSame(initialState, asyncState)
        asyncState.value
        repeat(20) {
            waitForIdle()
            if (asyncState.value == "latest") return@repeat
            Thread.sleep(10)
        }
        assertEquals("latest", asyncState.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun key_change_keeps_latest_result_when_cancelled_factory_returns() = runComposeUiTest {
        val key = mutableStateOf(1)
        val firstStarted = CompletableDeferred<Unit>()
        val releaseFirst = CompletableDeferred<Unit>()
        lateinit var asyncState: State<String?>

        setContent {
            val currentKey = key.value
            asyncState = useStateAsync(currentKey, initValue = "initial") {
                if (currentKey == 1) {
                    firstStarted.complete(Unit)
                    withContext(NonCancellable) {
                        releaseFirst.await()
                    }
                    "stale"
                } else {
                    "latest"
                }
            }
            Text("value=${asyncState.value}")
        }

        waitForIdle()
        firstStarted.await()
        runOnIdle { key.value = 2 }
        repeat(20) {
            waitForIdle()
            if (asyncState.value == "latest") return@repeat
            Thread.sleep(10)
        }

        releaseFirst.complete(Unit)
        repeat(20) {
            waitForIdle()
            Thread.sleep(10)
        }
        assertEquals("latest", asyncState.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun active_failure_uses_latest_onError_after_recomposition() = runComposeUiTest {
        val phase = mutableStateOf(0)
        val started = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val handledBy = AtomicReference<String?>(null)
        lateinit var asyncState: State<String?>

        setContent {
            val onError: (Throwable) -> Unit = if (phase.value == 0) {
                { handledBy.set("stale") }
            } else {
                { handledBy.set("latest") }
            }
            asyncState = useStateAsync(
                initValue = "initial",
                optionsOf = { this.onError = onError },
                factory = {
                    started.complete(Unit)
                    release.await()
                    error("boom")
                },
            )
            Text("value=${asyncState.value} phase=${phase.value}")
        }

        waitForIdle()
        started.await()
        runOnIdle { phase.value = 1 }
        waitForIdle()
        release.complete(Unit)
        repeat(20) {
            waitForIdle()
            if (handledBy.get() != null) return@repeat
            Thread.sleep(10)
        }

        assertEquals("latest", handledBy.get())
        assertNull(asyncState.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun state_identity_is_stable_when_lazy_mode_changes() = runComposeUiTest {
        val lazyMode = mutableStateOf(true)
        val calls = AtomicInteger(0)
        lateinit var asyncState: State<String?>
        var initialState: State<String?>? = null

        setContent {
            asyncState = useStateAsync(
                optionsOf = { lazy = lazyMode.value },
                factory = {
                    calls.incrementAndGet()
                    "value"
                },
            )
            if (initialState == null) initialState = asyncState
            Text("lazy=${lazyMode.value}")
        }

        waitForIdle()
        assertEquals(0, calls.get())
        runOnIdle { lazyMode.value = false }
        repeat(20) {
            waitForIdle()
            if (asyncState.value == "value") return@repeat
            Thread.sleep(10)
        }

        assertSame(initialState, asyncState)
        assertEquals("value", asyncState.value)
        assertEquals(1, calls.get())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun unmount_cancels_factory_without_reporting_error() = runComposeUiTest {
        val mounted = mutableStateOf(true)
        val started = CompletableDeferred<Unit>()
        val cancelled = AtomicBoolean(false)
        val errorCalls = AtomicInteger(0)

        setContent {
            if (mounted.value) {
                val state = useStateAsync<String>(
                    optionsOf = { onError = { errorCalls.incrementAndGet() } },
                    factory = {
                        started.complete(Unit)
                        try {
                            awaitCancellation()
                        } finally {
                            cancelled.set(true)
                        }
                    },
                )
                Text("value=${state.value}")
            }
        }

        waitForIdle()
        started.await()
        runOnIdle { mounted.value = false }
        waitForIdle()

        assertTrue(cancelled.get())
        assertEquals(0, errorCalls.get())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun factory_cancellation_exception_is_not_reported_as_error() = runComposeUiTest {
        val exited = CompletableDeferred<Unit>()
        val errorCalls = AtomicInteger(0)
        lateinit var asyncState: State<String?>

        setContent {
            asyncState = useStateAsync(
                initValue = "initial",
                optionsOf = { onError = { errorCalls.incrementAndGet() } },
                factory = {
                    try {
                        throw CancellationException("expected")
                    } finally {
                        exited.complete(Unit)
                    }
                },
            )
            Text("value=${asyncState.value}")
        }

        exited.await()
        waitForIdle()

        assertEquals("initial", asyncState.value)
        assertEquals(0, errorCalls.get())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun nullable_factory_result_is_supported() = runComposeUiTest {
        lateinit var asyncState: State<String?>
        val completed = AtomicBoolean(false)

        setContent {
            asyncState = useStateAsync<String?>(initValue = "initial") {
                completed.set(true)
                null
            }
            Text("value=${asyncState.value}")
        }

        repeat(20) {
            waitForIdle()
            if (completed.get()) return@repeat
            Thread.sleep(10)
        }
        assertTrue(completed.get())
        assertNull(asyncState.value)
        assertFalse(asyncState.value is String)
    }
}
