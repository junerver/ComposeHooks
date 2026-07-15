package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.junerver.compose.hooks.userequest.RequestHolder
import xyz.junerver.compose.hooks.userequest.useRequest

/*
  Description: useRequest loading delay plugin tests
  Author: Junerver
  Date: 2026/7/15
  Email: junerver@gmail.com
  Version: v1.0
*/
class UseRequestLoadingDelayTest {
    private suspend fun waitForCondition(maxAttempts: Int = 80, delayMs: Long = 20, condition: () -> Boolean): Boolean {
        repeat(maxAttempts) {
            if (condition()) return true
            withContext(Dispatchers.Default) {
                Thread.sleep(delayMs)
            }
        }
        return false
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stale_time_does_not_suppress_loading_without_cached_data() = runComposeUiTest {
        mainClock.autoAdvance = false
        val gate = CompletableDeferred<Unit>()
        val callCount = AtomicInteger(0)
        lateinit var holder: RequestHolder<String, Int>
        setContent {
            holder = useRequest(
                requestFn = { params: String ->
                    callCount.incrementAndGet()
                    gate.await()
                    params.length
                },
                optionsOf = {
                    manual = true
                    staleTime = 10.seconds
                    loadingDelay = 200.milliseconds
                },
            )
            Text("data=${holder.data.value} loading=${holder.isLoading.value}")
        }

        waitForIdle()
        runOnIdle {
            holder.request("abc")
            assertFalse(holder.isLoading.value)
        }
        mainClock.advanceTimeBy(1)
        waitForIdle()

        assertEquals(1, callCount.get())
        assertFalse(holder.isLoading.value)
        mainClock.advanceTimeBy(220)
        waitForIdle()
        assertTrue(holder.isLoading.value, "staleTime must not suppress delayed loading for a cache miss")

        gate.complete(Unit)
        mainClock.advanceTimeBy(1)
        waitForIdle()
        assertEquals(3, holder.data.value)
        assertFalse(holder.isLoading.value, "Expected completion to clear delayed loading")
        assertNull(holder.error.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun cache_miss_respects_loading_delay() = runComposeUiTest {
        mainClock.autoAdvance = false
        val cacheKey = "loading-delay-cache-miss-${System.currentTimeMillis()}"
        val gate = CompletableDeferred<Unit>()
        val callCount = AtomicInteger(0)
        lateinit var holder: RequestHolder<String, Int>
        setContent {
            holder = useRequest(
                requestFn = { params: String ->
                    callCount.incrementAndGet()
                    gate.await()
                    params.length
                },
                optionsOf = {
                    manual = true
                    this.cacheKey = cacheKey
                    staleTime = 10.seconds
                    loadingDelay = 200.milliseconds
                },
            )
            Text("data=${holder.data.value} loading=${holder.isLoading.value}")
        }

        waitForIdle()
        runOnIdle {
            holder.request("abc")
            assertFalse(holder.isLoading.value)
        }
        mainClock.advanceTimeBy(1)
        waitForIdle()

        assertEquals(1, callCount.get())
        assertFalse(holder.isLoading.value)
        mainClock.advanceTimeBy(220)
        waitForIdle()
        assertTrue(holder.isLoading.value, "A cache miss must not bypass loadingDelay")

        gate.complete(Unit)
        mainClock.advanceTimeBy(1)
        waitForIdle()
        assertEquals(3, holder.data.value)
        assertFalse(holder.isLoading.value)
        assertNull(holder.error.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun changed_positive_delay_applies_with_stable_holder_and_controls() = runComposeUiTest {
        mainClock.autoAdvance = false
        val delay = mutableStateOf(2.seconds)
        val gate = CompletableDeferred<Unit>()
        val callCount = AtomicInteger(0)
        lateinit var holder: RequestHolder<String, Int>
        setContent {
            holder = useRequest(
                requestFn = { params: String ->
                    callCount.incrementAndGet()
                    gate.await()
                    params.length
                },
                optionsOf = {
                    manual = true
                    loadingDelay = delay.value
                },
            )
            Text("data=${holder.data.value} loading=${holder.isLoading.value}")
        }

        waitForIdle()
        val initialHolder = holder
        val initialRequest = holder.request
        runOnIdle { delay.value = 150.milliseconds }
        mainClock.advanceTimeByFrame()
        waitForIdle()

        assertSame(initialHolder, holder)
        assertSame(initialRequest, holder.request)
        runOnIdle { initialRequest("abcd") }
        mainClock.advanceTimeBy(1)
        waitForIdle()

        assertEquals(1, callCount.get())
        assertFalse(holder.isLoading.value)
        mainClock.advanceTimeBy(170)
        waitForIdle()
        assertTrue(holder.isLoading.value, "Expected the changed positive loadingDelay to take effect")

        gate.complete(Unit)
        mainClock.advanceTimeBy(1)
        waitForIdle()
        assertEquals(4, holder.data.value)
        assertFalse(holder.isLoading.value, "Expected completion to clear delayed loading")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun fresh_cache_hit_never_leaves_a_delayed_loading_timer() = runComposeUiTest {
        mainClock.autoAdvance = false
        val cacheKey = "loading-delay-fresh-cache-${System.currentTimeMillis()}"
        val callCount = AtomicInteger(0)
        lateinit var holder: RequestHolder<String, Int>
        setContent {
            holder = useRequest(
                requestFn = { params: String ->
                    callCount.incrementAndGet()
                    params.length
                },
                optionsOf = {
                    manual = true
                    this.cacheKey = cacheKey
                    staleTime = 500.milliseconds
                    loadingDelay = 700.milliseconds
                },
            )
            Text("data=${holder.data.value} loading=${holder.isLoading.value}")
        }

        waitForIdle()
        runOnIdle { holder.request("abc") }
        mainClock.advanceTimeBy(1)
        waitForIdle()
        assertEquals(1, callCount.get())
        assertEquals(3, holder.data.value)
        assertFalse(holder.isLoading.value, "Expected the first request to populate a fresh cache entry")

        runOnIdle { holder.request("abc") }
        mainClock.advanceTimeBy(1)
        waitForIdle()
        assertEquals(1, callCount.get())
        assertFalse(holder.isLoading.value)

        withContext(Dispatchers.Default) {
            Thread.sleep(850)
        }
        waitForIdle()

        assertFalse(holder.isLoading.value, "A fresh cache hit must not leave a delayed loading timer")
        assertEquals(3, holder.data.value)
        assertNull(holder.error.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun cancel_clears_pending_delayed_loading() = runComposeUiTest {
        mainClock.autoAdvance = false
        val gate = CompletableDeferred<Unit>()
        val callCount = AtomicInteger(0)
        lateinit var holder: RequestHolder<String, Int>
        setContent {
            holder = useRequest(
                requestFn = { params: String ->
                    callCount.incrementAndGet()
                    gate.await()
                    params.length
                },
                optionsOf = {
                    manual = true
                    loadingDelay = 150.milliseconds
                },
            )
            Text("data=${holder.data.value} loading=${holder.isLoading.value}")
        }

        waitForIdle()
        runOnIdle { holder.request("abc") }
        mainClock.advanceTimeBy(1)
        waitForIdle()
        assertEquals(1, callCount.get())
        runOnIdle { holder.cancel() }

        withContext(Dispatchers.Default) {
            Thread.sleep(200)
        }
        waitForIdle()

        assertFalse(holder.isLoading.value)
        assertNull(holder.data.value)
        assertNull(holder.error.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun disabling_delay_cancels_pending_timer_with_stable_controls() = runComposeUiTest {
        mainClock.autoAdvance = false
        val loadingDelay = mutableStateOf(150.milliseconds)
        val gates = listOf(CompletableDeferred<Unit>(), CompletableDeferred<Unit>())
        val callCount = AtomicInteger(0)
        lateinit var holder: RequestHolder<String, Int>
        setContent {
            holder = useRequest(
                requestFn = { params: String ->
                    val callIndex = callCount.getAndIncrement()
                    gates[callIndex].await()
                    params.length
                },
                optionsOf = {
                    manual = true
                    this.loadingDelay = loadingDelay.value
                },
            )
            Text("data=${holder.data.value} loading=${holder.isLoading.value}")
        }

        waitForIdle()
        val initialHolder = holder
        val initialRequest = holder.request
        val initialCancel = holder.cancel
        runOnIdle { initialRequest("abc") }
        mainClock.advanceTimeBy(1)
        waitForIdle()
        assertEquals(1, callCount.get())
        assertFalse(holder.isLoading.value)

        runOnIdle { loadingDelay.value = Duration.ZERO }
        mainClock.advanceTimeByFrame()
        waitForIdle()
        assertSame(initialHolder, holder)
        assertSame(initialRequest, holder.request)
        assertSame(initialCancel, holder.cancel)

        mainClock.advanceTimeBy(200)
        withContext(Dispatchers.Default) {
            Thread.sleep(200)
        }
        waitForIdle()
        assertFalse(holder.isLoading.value)

        gates[0].complete(Unit)
        mainClock.advanceTimeBy(1)
        waitForIdle()
        assertEquals(3, holder.data.value)
        assertFalse(holder.isLoading.value)

        runOnIdle { initialRequest("abcd") }
        mainClock.advanceTimeBy(1)
        waitForIdle()
        assertEquals(2, callCount.get())
        assertTrue(holder.isLoading.value, "Disabling loadingDelay must restore immediate loading")

        gates[1].complete(Unit)
        mainClock.advanceTimeBy(1)
        waitForIdle()
        assertEquals(4, holder.data.value)
        assertFalse(holder.isLoading.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun enabling_delay_keeps_inflight_request_and_applies_to_next_request() = runComposeUiTest {
        mainClock.autoAdvance = false
        val loadingDelay = mutableStateOf(Duration.ZERO)
        val gates = listOf(CompletableDeferred<Unit>(), CompletableDeferred<Unit>())
        val callCount = AtomicInteger(0)
        lateinit var holder: RequestHolder<String, Int>
        setContent {
            holder = useRequest(
                requestFn = { params: String ->
                    val callIndex = callCount.getAndIncrement()
                    gates[callIndex].await()
                    params.length
                },
                optionsOf = {
                    manual = true
                    this.loadingDelay = loadingDelay.value
                },
            )
            Text("data=${holder.data.value} loading=${holder.isLoading.value}")
        }

        waitForIdle()
        val initialHolder = holder
        val initialRequest = holder.request
        runOnIdle { initialRequest("abc") }
        mainClock.advanceTimeBy(1)
        waitForIdle()
        assertEquals(1, callCount.get())
        assertTrue(holder.isLoading.value)

        runOnIdle { loadingDelay.value = 150.milliseconds }
        mainClock.advanceTimeByFrame()
        waitForIdle()
        assertSame(initialHolder, holder)
        assertSame(initialRequest, holder.request)
        assertTrue(holder.isLoading.value)

        gates[0].complete(Unit)
        mainClock.advanceTimeBy(1)
        waitForIdle()
        assertEquals(3, holder.data.value)
        assertFalse(holder.isLoading.value)

        runOnIdle { initialRequest("abcd") }
        mainClock.advanceTimeBy(1)
        waitForIdle()
        assertEquals(2, callCount.get())
        assertFalse(holder.isLoading.value)

        mainClock.advanceTimeBy(170)
        waitForIdle()
        assertTrue(holder.isLoading.value)

        gates[1].complete(Unit)
        mainClock.advanceTimeBy(1)
        waitForIdle()
        assertEquals(4, holder.data.value)
        assertFalse(holder.isLoading.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun slow_success_callback_cannot_reenable_loading_after_settlement() = runComposeUiTest {
        val callbackFinished = AtomicBoolean(false)
        lateinit var holder: RequestHolder<String, Int>
        setContent {
            holder = useRequest(
                requestFn = { params: String -> params.length },
                optionsOf = {
                    manual = true
                    loadingDelay = 50.milliseconds
                    onSuccess = { _, _ ->
                        Thread.sleep(300)
                        callbackFinished.set(true)
                    }
                },
            )
            Text("data=${holder.data.value} loading=${holder.isLoading.value}")
        }

        waitForIdle()
        runOnIdle { holder.request("abc") }

        assertTrue(waitForCondition { callbackFinished.get() }, "Expected the success callback to finish")
        waitForIdle()
        assertEquals(3, holder.data.value)
        assertFalse(holder.isLoading.value, "A settled success must remain non-loading")
        assertNull(holder.error.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun slow_error_callback_cannot_reenable_loading_after_settlement() = runComposeUiTest {
        val callbackFinished = AtomicBoolean(false)
        lateinit var holder: RequestHolder<String, Int>
        setContent {
            holder = useRequest(
                requestFn = { _: String -> error("loading delay failure") },
                optionsOf = {
                    manual = true
                    loadingDelay = 50.milliseconds
                    onError = { _, _ ->
                        Thread.sleep(300)
                        callbackFinished.set(true)
                    }
                },
            )
            Text("data=${holder.data.value} loading=${holder.isLoading.value}")
        }

        waitForIdle()
        runOnIdle { holder.request("abc") }

        assertTrue(waitForCondition { callbackFinished.get() }, "Expected the error callback to finish")
        waitForIdle()
        assertNull(holder.data.value)
        assertFalse(holder.isLoading.value, "A settled failure must remain non-loading")
        assertEquals("loading delay failure", holder.error.value?.message)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun slow_finally_callback_cannot_reenable_loading_after_settlement() = runComposeUiTest {
        val callbackFinished = AtomicBoolean(false)
        lateinit var holder: RequestHolder<String, Int>
        setContent {
            holder = useRequest(
                requestFn = { params: String -> params.length },
                optionsOf = {
                    manual = true
                    loadingDelay = 50.milliseconds
                    onFinally = { _, _, _ ->
                        Thread.sleep(300)
                        callbackFinished.set(true)
                    }
                },
            )
            Text("data=${holder.data.value} loading=${holder.isLoading.value}")
        }

        waitForIdle()
        runOnIdle { holder.request("abc") }

        assertTrue(waitForCondition { callbackFinished.get() }, "Expected the finally callback to finish")
        waitForIdle()
        assertEquals(3, holder.data.value)
        assertFalse(holder.isLoading.value, "A settled request must remain non-loading")
        assertNull(holder.error.value)
    }
}
