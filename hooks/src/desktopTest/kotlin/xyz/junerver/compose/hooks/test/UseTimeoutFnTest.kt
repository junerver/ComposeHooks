package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.withContext
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.useTimeoutFn

/*
  Description: useTimeoutFn comprehensive TDD tests
  Author: AI Assistant
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

@Suppress("DEPRECATION")
class UseTimeoutFnTest {
    private fun waitForCondition(maxAttempts: Int = 120, delayMs: Long = 25, condition: () -> Boolean): Boolean {
        repeat(maxAttempts) {
            if (condition()) return true
            Thread.sleep(delayMs)
        }
        return false
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useTimeoutFn_isPending_is_true_while_waiting() = runComposeUiTest {
        setContent {
            val (isPending) = useTimeoutFn(
                fn = { },
                interval = 10.seconds, // Long interval
                optionsOf = { immediate = true },
            )

            Text("isPending=${isPending.value}")
        }

        waitForIdle()
        // Should be pending immediately after start
        onNodeWithText("isPending=true").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useTimeoutFn_stop_cancels_execution() = runComposeUiTest {
        setContent {
            var stopped by useState(false)

            val (isPending, _, stop) = useTimeoutFn(
                fn = { },
                interval = 10.seconds,
                optionsOf = { immediate = true },
            )

            SideEffect {
                if (!stopped && isPending.value) {
                    stopped = true
                    stop()
                }
            }

            Text("isPending=${isPending.value} stopped=$stopped")
        }

        waitForIdle()
        // After stop, isPending should be false
        onNodeWithText("isPending=false stopped=true").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useTimeoutFn_immediate_false_does_not_start() = runComposeUiTest {
        setContent {
            val (isPending) = useTimeoutFn(
                fn = { },
                interval = 100.milliseconds,
                optionsOf = { immediate = false },
            )

            Text("isPending=${isPending.value}")
        }

        waitForIdle()
        onNodeWithText("isPending=false").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useTimeoutFn_manual_start_sets_pending() = runComposeUiTest {
        setContent {
            var started by useState(false)

            val (isPending, start) = useTimeoutFn(
                fn = { },
                interval = 10.seconds,
                optionsOf = { immediate = false },
            )

            SideEffect {
                if (!started) {
                    started = true
                    start()
                }
            }

            Text("isPending=${isPending.value} started=$started")
        }

        waitForIdle()
        // After manual start, should be pending
        onNodeWithText("isPending=true started=true").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun restarted_timeout_ignores_late_completion_from_previous_job() = runComposeUiTest {
        val invocationCount = AtomicInteger(0)
        val firstStarted = CompletableDeferred<Unit>()
        val releaseFirstCompletion = CompletableDeferred<Unit>()
        val firstFinished = CompletableDeferred<Unit>()
        val secondStarted = CompletableDeferred<Unit>()
        lateinit var holder: xyz.junerver.compose.hooks.TimeoutFnHolder

        setContent {
            holder = useTimeoutFn(
                fn = {
                    when (invocationCount.incrementAndGet()) {
                        1 -> {
                            firstStarted.complete(Unit)
                            try {
                                awaitCancellation()
                            } finally {
                                withContext(NonCancellable) {
                                    releaseFirstCompletion.await()
                                    firstFinished.complete(Unit)
                                }
                            }
                        }

                        2 -> {
                            secondStarted.complete(Unit)
                            awaitCancellation()
                        }
                    }
                },
                interval = Duration.ZERO,
                optionsOf = {
                    immediate = false
                },
            )
            Text("pending=${holder.isPending.value}")
        }
        waitForIdle()

        holder.start()
        assertTrue(
            waitForCondition {
                waitForIdle()
                firstStarted.isCompleted
            },
            "Expected the first timeout callback to start",
        )

        holder.start()
        assertTrue(
            waitForCondition {
                waitForIdle()
                secondStarted.isCompleted
            },
            "Expected the replacement timeout callback to start",
        )
        onNodeWithText("pending=true").assertExists()

        releaseFirstCompletion.complete(Unit)
        assertTrue(
            waitForCondition {
                waitForIdle()
                firstFinished.isCompleted
            },
            "Expected the canceled timeout to finish cleanup",
        )

        onNodeWithText("pending=true").assertExists()
        assertTrue(holder.isPending.value, "A stale completion must not clear the replacement timeout state")
        holder.stop()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun negative_interval_is_rejected_before_timeout_starts() {
        val result = runCatching {
            runComposeUiTest {
                setContent {
                    useTimeoutFn(
                        fn = {},
                        interval = (-1).milliseconds,
                        optionsOf = {
                            immediate = false
                        },
                    )
                }
            }
        }

        assertTrue(
            result.exceptionOrNull() is IllegalArgumentException,
            "Expected a negative interval to fail with IllegalArgumentException, actual=${result.exceptionOrNull()}",
        )
    }
}
