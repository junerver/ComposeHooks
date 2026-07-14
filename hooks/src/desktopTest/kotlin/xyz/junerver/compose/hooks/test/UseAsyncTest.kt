package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import xyz.junerver.compose.hooks.CancelableAsyncHolder
import xyz.junerver.compose.hooks.useAsync
import xyz.junerver.compose.hooks.useCancelableAsync
import xyz.junerver.compose.hooks.useState

@Suppress("DEPRECATION")
class UseAsyncTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useAsync_runs_suspend_block() = runComposeUiTest {
        val fired = AtomicBoolean(false)
        lateinit var runAsync: () -> Unit
        setContent {
            runAsync = useAsync {
                delay(20.milliseconds)
                fired.set(true)
            }

            Text("ready=true")
        }

        waitForIdle()
        onNodeWithText("ready=true").assertExists()
        runAsync()
        mainClock.advanceTimeBy(80)
        waitForIdle()
        assertTrue(fired.get())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useAsync_uses_latest_block_after_recomposition() = runComposeUiTest {
        lateinit var runAsync: () -> Unit
        var initialRunAsync: (() -> Unit)? = null
        setContent {
            var multiplier by useState(1)
            var observed by useState(0)
            var phase by useState(0)

            runAsync = useAsync {
                observed = multiplier
            }
            if (initialRunAsync == null) initialRunAsync = runAsync

            SideEffect {
                if (phase == 0) {
                    multiplier = 10
                    phase = 1
                }
            }

            Text("multiplier=$multiplier observed=$observed phase=$phase")
        }

        waitForIdle()
        onNodeWithText("multiplier=10 observed=0 phase=1").assertExists()
        assertSame(initialRunAsync, runAsync)

        runAsync()
        waitForIdle()

        onNodeWithText("multiplier=10 observed=10 phase=1").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useAsync_runner_identity_is_stable_after_recomposition() = runComposeUiTest {
        val fired = AtomicBoolean(false)
        lateinit var runAsync: (suspend CoroutineScope.() -> Unit) -> Unit
        var initialRunAsync: Any? = null
        setContent {
            var phase by useState(0)
            runAsync = useAsync()
            if (initialRunAsync == null) initialRunAsync = runAsync

            SideEffect {
                if (phase == 0) phase = 1
            }
            Text("phase=$phase")
        }

        waitForIdle()
        onNodeWithText("phase=1").assertExists()
        assertSame(initialRunAsync, runAsync)

        runAsync { fired.set(true) }
        waitForIdle()
        assertTrue(fired.get())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useAsync_cancels_running_block_when_composition_unmounts() = runComposeUiTest {
        val mounted = mutableStateOf(true)
        val started = CompletableDeferred<Unit>()
        val cancelled = AtomicBoolean(false)
        lateinit var runAsync: () -> Unit
        setContent {
            if (mounted.value) {
                runAsync = useAsync {
                    started.complete(Unit)
                    try {
                        awaitCancellation()
                    } finally {
                        cancelled.set(true)
                    }
                }
                Text("mounted=true")
            }
        }

        waitForIdle()
        runAsync()
        mainClock.advanceTimeBy(1)
        waitForIdle()
        started.await()

        runOnIdle { mounted.value = false }
        waitForIdle()

        assertTrue(cancelled.get())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun cancelableAsync_exposes_active_state_during_execution() = runComposeUiTest {
        lateinit var holder: CancelableAsyncHolder
        setContent {
            holder = useCancelableAsync()
            Text("active=${holder.isActive.value}")
        }

        waitForIdle()
        holder.asyncRun {
            delay(100.milliseconds)
        }
        mainClock.advanceTimeBy(1)
        waitForIdle()
        onNodeWithText("active=true").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun cancelableAsync_cancel_stops_running_state() = runComposeUiTest {
        lateinit var holder: CancelableAsyncHolder
        setContent {
            holder = useCancelableAsync()
            Text("active=${holder.isActive.value}")
        }

        waitForIdle()
        holder.asyncRun {
            delay(100.milliseconds)
        }
        mainClock.advanceTimeBy(1)
        waitForIdle()
        onNodeWithText("active=true").assertExists()
        holder.cancel()
        waitForIdle()
        onNodeWithText("active=false").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun cancelableAsync_latest_run_cancels_previous_job() = runComposeUiTest {
        val firstCompleted = AtomicBoolean(false)
        val secondCompleted = AtomicBoolean(false)
        lateinit var holder: CancelableAsyncHolder
        setContent {
            holder = useCancelableAsync()
            Text("ready=true")
        }

        waitForIdle()
        holder.asyncRun {
            delay(120.milliseconds)
            firstCompleted.set(true)
        }
        holder.asyncRun {
            delay(20.milliseconds)
            secondCompleted.set(true)
        }
        mainClock.advanceTimeBy(180)
        waitForIdle()
        onNodeWithText("ready=true").assertExists()
        assertFalse(firstCompleted.get())
        assertTrue(secondCompleted.get())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun cancelableAsync_previous_cancellation_does_not_clear_latest_active_state() = runComposeUiTest {
        val secondStarted = CompletableDeferred<Unit>()
        val allowSecondToFinish = CompletableDeferred<Unit>()
        lateinit var holder: CancelableAsyncHolder
        setContent {
            holder = useCancelableAsync()
            Text("active=${holder.isActive.value}")
        }

        waitForIdle()
        holder.asyncRun {
            try {
                awaitCancellation()
            } finally {
                withContext(NonCancellable) {
                    secondStarted.await()
                }
            }
        }
        mainClock.advanceTimeBy(1)
        waitForIdle()
        onNodeWithText("active=true").assertExists()

        holder.asyncRun {
            secondStarted.complete(Unit)
            allowSecondToFinish.await()
        }
        mainClock.advanceTimeBy(1)
        waitForIdle()

        onNodeWithText("active=true").assertExists()

        allowSecondToFinish.complete(Unit)
        waitForIdle()
        onNodeWithText("active=false").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun cancelableAsync_failure_resets_state_and_allows_next_run() = runComposeUiTest {
        val secondCompleted = AtomicBoolean(false)
        lateinit var holder: CancelableAsyncHolder
        setContent {
            holder = useCancelableAsync()
            Text("active=${holder.isActive.value}")
        }

        waitForIdle()
        holder.asyncRun {
            delay(20.milliseconds)
            error("boom")
        }
        mainClock.advanceTimeBy(40)
        waitForIdle()
        onNodeWithText("active=false").assertExists()

        holder.asyncRun {
            delay(20.milliseconds)
            secondCompleted.set(true)
        }
        mainClock.advanceTimeBy(1)
        waitForIdle()
        onNodeWithText("active=true").assertExists()

        mainClock.advanceTimeBy(40)
        waitForIdle()
        onNodeWithText("active=false").assertExists()
        assertTrue(secondCompleted.get())
    }
}
