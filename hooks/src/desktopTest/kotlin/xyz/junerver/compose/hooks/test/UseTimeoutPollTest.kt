package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.useTimeoutPoll

@Suppress("DEPRECATION")
class UseTimeoutPollTest {
    private fun waitForCondition(maxAttempts: Int = 80, delayMs: Long = 25, condition: () -> Boolean): Boolean {
        repeat(maxAttempts) {
            if (condition()) return true
            Thread.sleep(delayMs)
        }
        return false
    }

    @OptIn(ExperimentalTestApi::class)
    private fun androidx.compose.ui.test.ComposeUiTest.waitForText(text: String, maxAttempts: Int = 80, delayMs: Long = 25): Boolean =
        waitForCondition(maxAttempts, delayMs) {
            runCatching { onNodeWithText(text).assertExists() }.isSuccess
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun immediate_true_marks_poll_as_active_on_mount() = runComposeUiTest {
        setContent {
            val holder = useTimeoutPoll(
                fn = {},
                interval = 100.milliseconds,
            )
            Text("active=${holder.isActive.value}")
        }

        waitForIdle()
        onNodeWithText("active=true").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun immediate_false_keeps_poll_inactive_until_resume() = runComposeUiTest {
        val count = AtomicInteger(0)

        setContent {
            val holder = useTimeoutPoll(
                fn = {
                    count.incrementAndGet()
                },
                interval = 1.seconds,
                optionsOf = {
                    immediate = false
                    immediateCallback = true
                },
            )
            Button(
                onClick = holder.resume,
            ) {
                Text("resume")
            }
            Text("active=${holder.isActive.value} count=${count.get()}")
        }

        waitForIdle()
        onNodeWithText("active=false count=0").assertExists()
        assertEquals(0, count.get())

        onNodeWithText("resume").performClick()
        waitForIdle()
        val invoked = waitForCondition { count.get() == 1 }

        assertTrue(invoked, "Expected poll callback to run after resume")
        onNodeWithText("active=true count=1").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pause_stops_future_poll_callbacks() = runComposeUiTest {
        val count = AtomicInteger(0)

        setContent {
            val holder = useTimeoutPoll(
                fn = {
                    count.incrementAndGet()
                },
                interval = 50.milliseconds,
                optionsOf = {
                    immediate = false
                },
            )
            Button(
                onClick = holder.resume,
            ) {
                Text("resume")
            }
            Button(
                onClick = holder.pause,
            ) {
                Text("pause")
            }
            Text("active=${holder.isActive.value} count=${count.get()}")
        }

        onNodeWithText("resume").performClick()
        waitForIdle()
        onNodeWithText("pause").performClick()
        waitForIdle()
        Thread.sleep(180)
        waitForIdle()

        assertEquals(0, count.get())
        onNodeWithText("active=false count=0").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun immediateCallback_runs_once_immediately_on_resume() = runComposeUiTest {
        val count = AtomicInteger(0)

        setContent {
            val holder = useTimeoutPoll(
                fn = {
                    count.incrementAndGet()
                },
                interval = 1.seconds,
                optionsOf = {
                    immediate = false
                    immediateCallback = true
                },
            )
            Button(
                onClick = holder.resume,
            ) {
                Text("resume")
            }
            Text("active=${holder.isActive.value} count=${count.get()}")
        }

        waitForIdle()
        assertEquals(0, count.get())

        onNodeWithText("resume").performClick()
        waitForIdle()
        val immediateInvocation = waitForCondition(maxAttempts = 20, delayMs = 10) { count.get() == 1 }

        assertTrue(immediateInvocation, "Expected immediate callback to run once on resume")
        Thread.sleep(150)
        waitForIdle()
        assertEquals(1, count.get())
        onNodeWithText("active=true count=1").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun resume_after_recomposition_uses_latest_callback() = runComposeUiTest {
        val count = AtomicInteger(0)
        lateinit var updateMultiplier: () -> Unit
        lateinit var holder: xyz.junerver.compose.hooks.TimeoutPollHolder

        setContent {
            var useLargeIncrement by useState(default = false)
            updateMultiplier = { useLargeIncrement = true }
            val increment = if (useLargeIncrement) 10 else 1
            holder = useTimeoutPoll(
                fn = {
                    count.addAndGet(increment)
                },
                interval = 5.seconds,
                optionsOf = {
                    immediate = false
                    immediateCallback = true
                },
            )

            Button(
                onClick = holder.resume,
            ) {
                Text("resume")
            }
            Button(
                onClick = holder.pause,
            ) {
                Text("pause")
            }
            Text("large=$useLargeIncrement count=${count.get()} active=${holder.isActive.value}")
        }

        waitForIdle()
        updateMultiplier()
        waitForIdle()
        onNodeWithText("large=true count=0 active=false").assertExists()

        holder.resume()
        assertTrue(
            waitForCondition { count.get() == 10 },
            "Expected second resume to use recomposed callback body, actual=${count.get()}",
        )
        onNodeWithText("large=true count=10 active=true").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun unmount_stops_pending_poll_before_interval_fires() = runComposeUiTest {
        val count = AtomicInteger(0)
        lateinit var unmount: () -> Unit

        setContent {
            var mounted by useState(default = true)
            unmount = { mounted = false }

            if (mounted) {
                val holder = useTimeoutPoll(
                    fn = {
                        count.incrementAndGet()
                    },
                    interval = 120.milliseconds,
                    optionsOf = {
                        immediate = false
                    },
                )
                Button(
                    onClick = holder.resume,
                ) {
                    Text("resume")
                }
                Button(
                    onClick = unmount,
                ) {
                    Text("unmount")
                }
            }

            Text("mounted=$mounted count=${count.get()}")
        }

        onNodeWithText("resume").performClick()
        waitForIdle()
        unmount()
        waitForIdle()
        onNodeWithText("mounted=false count=0").assertExists()
        Thread.sleep(220)
        waitForIdle()
        assertEquals(0, count.get())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun immediate_callback_finishes_before_next_poll_is_scheduled() = runComposeUiTest {
        val count = AtomicInteger(0)
        val releaseCallback = CompletableDeferred<Unit>()
        lateinit var holder: xyz.junerver.compose.hooks.TimeoutPollHolder

        setContent {
            holder = useTimeoutPoll(
                fn = {
                    count.incrementAndGet()
                    releaseCallback.await()
                },
                interval = 50.milliseconds,
                optionsOf = {
                    immediate = false
                    immediateCallback = true
                },
            )
        }
        waitForIdle()

        holder.resume()
        assertTrue(
            waitForCondition {
                waitForIdle()
                count.get() >= 1
            },
            "Expected the immediate callback to start",
        )

        mainClock.advanceTimeBy(180)
        waitForIdle()
        val countBeforeRelease = count.get()
        holder.pause()
        releaseCallback.complete(Unit)

        assertEquals(1, countBeforeRelease, "Polling must wait for the current callback before scheduling the next timeout")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pause_cancels_running_immediate_callback() = runComposeUiTest {
        val started = CompletableDeferred<Unit>()
        val canceled = AtomicBoolean(false)
        lateinit var holder: xyz.junerver.compose.hooks.TimeoutPollHolder

        setContent {
            holder = useTimeoutPoll(
                fn = {
                    started.complete(Unit)
                    try {
                        awaitCancellation()
                    } finally {
                        canceled.set(true)
                    }
                },
                interval = 5.seconds,
                optionsOf = {
                    immediate = false
                    immediateCallback = true
                },
            )
        }
        waitForIdle()

        holder.resume()
        assertTrue(
            waitForCondition {
                waitForIdle()
                started.isCompleted
            },
            "Expected the immediate callback to start",
        )
        holder.pause()

        assertTrue(
            waitForCondition {
                waitForIdle()
                canceled.get()
            },
            "Expected pause to cancel the running immediate callback",
        )
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun non_positive_poll_interval_is_rejected_before_polling_starts() {
        listOf(Duration.ZERO, (-1).milliseconds).forEach { invalidInterval ->
            val result = runCatching {
                runComposeUiTest {
                    setContent {
                        useTimeoutPoll(
                            fn = {},
                            interval = invalidInterval,
                            optionsOf = {
                                immediate = false
                            },
                        )
                    }
                }
            }

            assertTrue(
                result.exceptionOrNull() is IllegalArgumentException,
                "Expected interval=$invalidInterval to fail with IllegalArgumentException, actual=${result.exceptionOrNull()}",
            )
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun concurrent_resume_calls_start_only_one_immediate_callback() = runComposeUiTest {
        val count = AtomicInteger(0)
        lateinit var holder: xyz.junerver.compose.hooks.TimeoutPollHolder

        setContent {
            holder = useTimeoutPoll(
                fn = {
                    count.incrementAndGet()
                    awaitCancellation()
                },
                interval = 5.seconds,
                optionsOf = {
                    immediate = false
                    immediateCallback = true
                },
            )
        }
        waitForIdle()

        val callers = 32
        val ready = CountDownLatch(callers)
        val start = CountDownLatch(1)
        val done = CountDownLatch(callers)
        val errors = ConcurrentLinkedQueue<Throwable>()
        repeat(callers) {
            Thread {
                try {
                    ready.countDown()
                    start.await()
                    holder.resume()
                } catch (error: Throwable) {
                    errors.add(error)
                } finally {
                    done.countDown()
                }
            }.start()
        }

        assertTrue(ready.await(5, TimeUnit.SECONDS), "Concurrent callers did not become ready")
        start.countDown()
        assertTrue(done.await(5, TimeUnit.SECONDS), "Concurrent poll resumes did not complete")
        assertTrue(errors.isEmpty(), "Concurrent resume threw: ${errors.firstOrNull()}")
        assertTrue(
            waitForCondition {
                waitForIdle()
                count.get() >= 1
            },
            "Expected the immediate callback to start",
        )

        assertEquals(1, count.get(), "Concurrent resume calls must share one poll job")
        holder.pause()
    }
}
