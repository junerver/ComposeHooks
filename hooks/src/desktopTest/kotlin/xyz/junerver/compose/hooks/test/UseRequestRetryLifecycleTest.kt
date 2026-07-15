/*
  Description: useRequest retry lifecycle regression tests
  Author: Junerver
  Date: 2026/7/15
  Email: junerver@gmail.com
  Version: v1.0
*/

package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CancellationException
import xyz.junerver.compose.hooks.userequest.EmptyPlugin
import xyz.junerver.compose.hooks.userequest.RequestHolder
import xyz.junerver.compose.hooks.userequest.UseRequestOptions
import xyz.junerver.compose.hooks.userequest.plugins.useRetryPlugin
import xyz.junerver.compose.hooks.userequest.useRequest

@Suppress("DEPRECATION")
class UseRequestRetryLifecycleTest {
    private fun waitForCondition(maxAttempts: Int = 80, delayMs: Long = 20, condition: () -> Boolean): Boolean {
        repeat(maxAttempts) {
            if (condition()) return true
            Thread.sleep(delayMs)
        }
        return false
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun retryCountBelowMinusOneKeepsRetryDisabled() = runComposeUiTest {
        lateinit var plugin: Any

        setContent {
            plugin = useRetryPlugin<String, Int>(
                UseRequestOptions.optionOf {
                    retryCount = -2
                },
            )
        }

        waitForIdle()
        assertIs<EmptyPlugin<*, *>>(plugin)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun holderCancelInvalidatesPendingRetry() = runComposeUiTest {
        val calls = AtomicInteger(0)
        lateinit var holder: RequestHolder<String, Int>
        setContent {
            holder = useRequest(
                requestFn = { _: String ->
                    calls.incrementAndGet()
                    error("failure")
                },
                optionsOf = {
                    manual = true
                    retryCount = 2
                    retryInterval = 100.milliseconds
                    onError = { _, _ -> }
                },
            )
            Text("calls=${calls.get()} loading=${holder.isLoading.value}")
        }

        waitForIdle()
        runOnIdle { holder.request("cancel") }
        waitForIdle()
        assertEquals(1, calls.get())

        runOnIdle { holder.cancel() }
        val retried = waitForCondition(maxAttempts = 20, delayMs = 10) { calls.get() > 1 }
        waitForIdle()

        assertFalse(retried)
        assertEquals(1, calls.get())
        assertFalse(holder.isLoading.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun infiniteRetryContinuesUntilSuccess() = runComposeUiTest {
        val calls = AtomicInteger(0)
        lateinit var holder: RequestHolder<String, Int>
        setContent {
            holder = useRequest(
                requestFn = { _: String ->
                    val call = calls.incrementAndGet()
                    if (call <= 3) error("failure $call")
                    call
                },
                optionsOf = {
                    manual = true
                    retryCount = -1
                    retryInterval = 20.milliseconds
                    onError = { _, _ -> }
                },
            )
            Text("calls=${calls.get()} data=${holder.data.value}")
        }

        waitForIdle()
        runOnIdle { holder.request("infinite") }
        val completed = waitForCondition {
            waitForIdle()
            calls.get() == 4 && holder.data.value == 4
        }
        runOnIdle { holder.cancel() }

        assertTrue(completed)
        assertEquals(4, calls.get())
        assertEquals(4, holder.data.value)
        assertNull(holder.error.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun successfulRetryResetsBudgetForNextManualChain() = runComposeUiTest {
        val perParamCalls = mutableMapOf<String, Int>()
        val received = CopyOnWriteArrayList<String>()
        lateinit var holder: RequestHolder<String, Int>
        setContent {
            holder = useRequest(
                requestFn = { params: String ->
                    received += params
                    val call = (perParamCalls[params] ?: 0) + 1
                    perParamCalls[params] = call
                    if (call == 1) error("first $params")
                    params.length
                },
                optionsOf = {
                    manual = true
                    retryCount = 1
                    retryInterval = 20.milliseconds
                    onError = { _, _ -> }
                },
            )
            Text("data=${holder.data.value} error=${holder.error.value?.message}")
        }

        waitForIdle()
        runOnIdle { holder.request("one") }
        assertTrue(waitForCondition { received.size == 2 && holder.data.value == 3 })

        runOnIdle { holder.request("second") }
        assertTrue(waitForCondition { received.size == 4 && holder.data.value == 6 })
        runOnIdle { holder.cancel() }

        assertEquals(listOf("one", "one", "second", "second"), received.toList())
        assertNull(holder.error.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun cancellationExceptionDoesNotScheduleRetry() = runComposeUiTest {
        val calls = AtomicInteger(0)
        lateinit var holder: RequestHolder<String, Int>
        setContent {
            holder = useRequest(
                requestFn = { _: String ->
                    calls.incrementAndGet()
                    throw CancellationException("cancel request")
                },
                optionsOf = {
                    manual = true
                    retryCount = -1
                    retryInterval = 20.milliseconds
                    onError = { _, _ -> }
                },
            )
            Text("calls=${calls.get()} loading=${holder.isLoading.value}")
        }

        waitForIdle()
        runOnIdle { holder.request("cancel") }
        waitForIdle()
        val retried = waitForCondition(maxAttempts = 20, delayMs = 10) { calls.get() > 1 }

        assertFalse(retried)
        assertEquals(1, calls.get())
        assertFalse(holder.isLoading.value)
        runOnIdle { holder.cancel() }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun disablingRetryCancelsOldPendingChain() = runComposeUiTest {
        val enabled = mutableStateOf(true)
        val calls = AtomicInteger(0)
        lateinit var holder: RequestHolder<String, Int>
        setContent {
            holder = useRequest(
                requestFn = { _: String ->
                    calls.incrementAndGet()
                    error("failure")
                },
                optionsOf = {
                    manual = true
                    retryCount = if (enabled.value) 1 else 0
                    retryInterval = 100.milliseconds
                    onError = { _, _ -> }
                },
            )
            Text("calls=${calls.get()} loading=${holder.isLoading.value}")
        }

        waitForIdle()
        runOnIdle { holder.request("disable") }
        waitForIdle()
        assertEquals(1, calls.get())

        runOnIdle { enabled.value = false }
        waitForIdle()
        val retried = waitForCondition(maxAttempts = 20, delayMs = 10) { calls.get() > 1 }

        assertFalse(retried)
        assertEquals(1, calls.get())
        runOnIdle { holder.cancel() }
    }
}
