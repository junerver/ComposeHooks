package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.userequest.Fetch
import xyz.junerver.compose.hooks.userequest.Plugin
import xyz.junerver.compose.hooks.userequest.PluginLifecycle
import xyz.junerver.compose.hooks.userequest.RequestHolder
import xyz.junerver.compose.hooks.userequest.UseRequestOptions
import xyz.junerver.compose.hooks.userequest.useRequest
import xyz.junerver.compose.hooks.utils.instantProvider

/*
  Description: useRequest 组合行为桌面测试
  Author: Junerver
  Date: 2026/1/9
  Email: junerver@gmail.com
  Version: v1.0
*/
@Suppress("DEPRECATION")
class UseRequestTest {
    private fun waitForCondition(maxAttempts: Int = 80, delayMs: Long = 20, condition: () -> Boolean): Boolean {
        repeat(maxAttempts) {
            if (condition()) return true
            Thread.sleep(delayMs)
        }
        return false
    }

    private var currentInstant: Instant = Clock.System.now()

    private fun resetInstantProvider() {
        currentInstant = Clock.System.now()
        instantProvider = { currentInstant }
    }

    private fun advanceInstant(duration: kotlin.time.Duration) {
        currentInstant += duration
    }

    @AfterTest
    fun tearDown() {
        instantProvider = { Clock.System.now() }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun autoRun_runs_once_with_defaultParams_and_not_rerun_on_recompose() = runComposeUiTest {
        val callCount = AtomicInteger(0)
        setContent {
            var tick by useState(default = 0)
            val holder = useRequest(
                requestFn = { params: String ->
                    callCount.incrementAndGet()
                    params.length
                },
                optionsOf = {
                    defaultParams = "abc"
                },
            )

            SideEffect {
                if (tick == 0) tick = 1
            }

            Text("tick=$tick data=${holder.data.value}")
        }

        waitForIdle()
        assertEquals(1, callCount.get())
        onNodeWithText("tick=1 data=3").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun ready_false_blocks_autoRun_and_manual_request() = runComposeUiTest {
        val callCount = AtomicInteger(0)
        setContent {
            val holder = useRequest(
                requestFn = { params: String ->
                    callCount.incrementAndGet()
                    params.length
                },
                optionsOf = {
                    ready = false
                    defaultParams = "abc"
                },
            )

            SideEffect {
                holder.request("x")
            }

            Text("data=${holder.data.value}")
        }

        waitForIdle()
        assertEquals(0, callCount.get())
        onNodeWithText("data=null").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun manual_true_disables_autoRun_but_allows_manual_request() = runComposeUiTest {
        val callCount = AtomicInteger(0)
        setContent {
            var fired by useState(default = false)
            val holder = useRequest(
                requestFn = { params: String ->
                    callCount.incrementAndGet()
                    params.length
                },
                optionsOf = {
                    manual = true
                    ready = true
                    defaultParams = "abc"
                },
            )

            SideEffect {
                if (fired) return@SideEffect
                fired = true
                holder.request("abcd")
            }

            Text("data=${holder.data.value}")
        }

        waitForIdle()
        assertEquals(1, callCount.get())
        onNodeWithText("data=4").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun manual_request_recovers_from_error_and_clears_error_on_success() = runComposeUiTest {
        val callCount = AtomicInteger(0)
        lateinit var holder: RequestHolder<String, Int>

        setContent {
            holder = useRequest(
                requestFn = { params: String ->
                    delay(50)
                    if (callCount.incrementAndGet() == 1) {
                        throw RuntimeException("boom")
                    }
                    params.length
                },
                optionsOf = {
                    manual = true
                    onError = { _, _ -> }
                },
            )

            Text(
                "data=${holder.data.value} loading=${holder.isLoading.value} " +
                    "error=${holder.error.value?.message}",
            )
        }

        waitForIdle()
        holder.request("abc")
        waitForIdle()
        onNodeWithText("data=null loading=true error=null").assertExists()
        mainClock.advanceTimeBy(60)
        waitForIdle()

        val firstFailed =
            !holder.isLoading.value &&
                holder.data.value == null &&
                holder.error.value?.message == "boom"
        assertTrue(firstFailed, "Expected first manual request to expose error state")
        onNodeWithText("data=null loading=false error=boom").assertExists()

        holder.request("abcd")
        waitForIdle()
        onNodeWithText("data=null loading=true error=boom").assertExists()
        mainClock.advanceTimeBy(60)
        waitForIdle()

        val recovered =
            !holder.isLoading.value &&
                holder.data.value == 4 &&
                holder.error.value == null
        assertTrue(recovered, "Expected successful retry to update data and clear prior error")
        onNodeWithText("data=4 loading=false error=null").assertExists()
        assertEquals(2, callCount.get())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun recomposition_applies_latest_custom_plugin_behavior() = runComposeUiTest {
        val callCount = AtomicInteger(0)
        setContent {
            var useNewPlugin by useState(default = false)
            var phase by useState(default = 0)

            val pluginA = remember {
                @androidx.compose.runtime.Composable
                { _: UseRequestOptions<String, Int> ->
                    object : Plugin<String, Int>() {
                        override val invoke: (
                            Fetch<String, Int>,
                            UseRequestOptions<String, Int>,
                        ) -> PluginLifecycle<String, Int> = { fetch: Fetch<String, Int>, opt: UseRequestOptions<String, Int> ->
                            initFetch(fetch, opt)
                            object : PluginLifecycle<String, Int>() {
                                override val onSuccess: ((Int, String) -> Unit)? = { _: Int, _: String ->
                                    callCount.addAndGet(1)
                                }
                            }
                        }
                    }
                }
            }

            val pluginB = remember {
                @androidx.compose.runtime.Composable
                { _: UseRequestOptions<String, Int> ->
                    object : Plugin<String, Int>() {
                        override val invoke: (
                            Fetch<String, Int>,
                            UseRequestOptions<String, Int>,
                        ) -> PluginLifecycle<String, Int> = { fetch: Fetch<String, Int>, opt: UseRequestOptions<String, Int> ->
                            initFetch(fetch, opt)
                            object : PluginLifecycle<String, Int>() {
                                override val onSuccess: ((Int, String) -> Unit)? = { _: Int, _: String ->
                                    callCount.addAndGet(100)
                                }
                            }
                        }
                    }
                }
            }

            val holder = useRequest(
                requestFn = { params: String -> params.length },
                optionsOf = {
                    manual = true
                    ready = true
                    defaultParams = "a"
                },
                plugins = arrayOf(if (useNewPlugin) pluginB else pluginA),
            )

            SideEffect {
                when (phase) {
                    0 -> {
                        holder.request("a")
                        useNewPlugin = true
                        phase = 1
                    }

                    1 -> {
                        holder.request("a")
                        phase = 2
                    }
                }
            }

            Text("data=${holder.data.value}")
        }

        waitForIdle()
        assertEquals(101, callCount.get())
    }

    // region AutoRunPlugin Tests

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun autoRunPlugin_ready_changes_from_false_to_true_triggers_request() = runComposeUiTest {
        val callCount = AtomicInteger(0)
        setContent {
            var ready by useState(default = false)
            val holder = useRequest(
                requestFn = { params: String ->
                    callCount.incrementAndGet()
                    params.length
                },
                optionsOf = {
                    this.ready = ready
                    defaultParams = "test"
                },
            )

            SideEffect {
                if (!ready) ready = true
            }

            Text("data=${holder.data.value}")
        }

        waitForIdle()
        assertEquals(1, callCount.get())
        onNodeWithText("data=4").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun autoRunPlugin_refreshDeps_change_triggers_refresh() = runComposeUiTest {
        val callCount = AtomicInteger(0)
        val receivedParams = mutableListOf<String>()
        setContent {
            var dep by useState(default = 0)
            val holder = useRequest(
                requestFn = { params: String ->
                    receivedParams.add(params)
                    callCount.incrementAndGet()
                    params.length
                },
                optionsOf = {
                    defaultParams = "abc"
                    refreshDeps = arrayOf(dep)
                },
            )

            SideEffect {
                if (dep == 0) dep = 1
            }

            Text("data=${holder.data.value} dep=$dep")
        }

        waitForIdle()
        assertEquals(2, callCount.get())
        assertEquals(listOf("abc", "abc"), receivedParams)
    }

    // endregion

    // region DebouncePlugin Tests

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun debouncePlugin_multiple_rapid_calls_only_executes_last() = runComposeUiTest {
        resetInstantProvider()
        val callCount = AtomicInteger(0)
        val receivedParams = mutableListOf<String>()
        lateinit var holder: RequestHolder<String, Int>
        setContent {
            var phase by useState(default = 0)
            holder = useRequest(
                requestFn = { params: String ->
                    receivedParams.add(params)
                    callCount.incrementAndGet()
                    params.length
                },
                optionsOf = {
                    manual = true
                    debounceOptionsOf = {
                        wait = 100.milliseconds
                    }
                },
            )

            SideEffect {
                when (phase) {
                    0 -> {
                        holder.request("a")
                        holder.request("ab")
                        holder.request("abc")
                        phase = 1
                    }
                }
            }

            Text("data=${holder.data.value}")
        }

        waitForIdle()
        assertEquals(0, callCount.get())

        advanceInstant(100.milliseconds)
        mainClock.advanceTimeBy(100)
        waitForIdle()

        val completed = waitForCondition {
            callCount.get() == 1 &&
                receivedParams == listOf("abc") &&
                holder.data.value == 3 &&
                holder.isLoading.value == false &&
                holder.error.value == null
        }
        assertTrue(completed, "Expected debounce to invoke once with the latest params")
        assertEquals(1, callCount.get())
        assertEquals(listOf("abc"), receivedParams)
        assertEquals(3, holder.data.value)
        assertFalse(holder.isLoading.value)
        assertNull(holder.error.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun debouncePlugin_cancel_resets_leading_window() = runComposeUiTest {
        val receivedParams = mutableListOf<String>()
        lateinit var holder: RequestHolder<String, Int>
        setContent {
            holder = useRequest(
                requestFn = { params: String ->
                    receivedParams += params
                    params.length
                },
                optionsOf = {
                    manual = true
                    debounceOptionsOf = {
                        wait = 10.seconds
                        leading = true
                        trailing = false
                    }
                },
            )
            Text("data=${holder.data.value}")
        }

        waitForIdle()
        runOnIdle { holder.request("first") }
        waitForIdle()
        assertEquals(listOf("first"), receivedParams)

        runOnIdle {
            holder.cancel()
            holder.request("second")
        }
        waitForIdle()

        assertEquals(listOf("first", "second"), receivedParams)
        assertEquals(6, holder.data.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun debouncePlugin_cancel_drops_pending_trailing_request() = runComposeUiTest {
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
                    debounceOptionsOf = {
                        wait = 50.milliseconds
                        leading = false
                        trailing = true
                    }
                },
            )
            Text("data=${holder.data.value}")
        }

        waitForIdle()
        runOnIdle {
            holder.request("stale")
            holder.cancel()
        }

        val unexpectedlyRan = waitForCondition(maxAttempts = 20, delayMs = 10) {
            callCount.get() > 0
        }
        waitForIdle()

        assertFalse(unexpectedlyRan)
        assertEquals(0, callCount.get())
        assertNull(holder.data.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun debouncePlugin_can_be_enabled_after_recomposition_with_stable_controls() = runComposeUiTest {
        val enabled = mutableStateOf(false)
        val callCount = AtomicInteger(0)
        val receivedParams = CopyOnWriteArrayList<String>()
        lateinit var holder: RequestHolder<String, Int>
        setContent {
            holder = useRequest(
                requestFn = { params: String ->
                    receivedParams += params
                    callCount.incrementAndGet()
                    params.length
                },
                optionsOf = {
                    manual = true
                    debounceOptionsOf = {
                        wait = if (enabled.value) 500.milliseconds else 0.milliseconds
                    }
                },
            )
            Text("data=${holder.data.value}")
        }

        waitForIdle()
        val initialHolder = holder
        val initialRequest = holder.request
        runOnIdle { initialRequest("direct") }
        waitForIdle()
        assertEquals(1, callCount.get())

        runOnIdle { enabled.value = true }
        waitForIdle()
        assertSame(initialHolder, holder)
        assertSame(initialRequest, holder.request)

        runOnIdle { initialRequest("delayed") }
        waitForIdle()
        assertEquals(1, callCount.get())

        val completed = waitForCondition {
            callCount.get() == 2
        }
        waitForIdle()

        assertTrue(completed, "Expected dynamically enabled debounce to delay the request")
        assertEquals(listOf("direct", "delayed"), receivedParams.toList())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun debouncePlugin_can_be_disabled_and_cancels_pending_request() = runComposeUiTest {
        val enabled = mutableStateOf(true)
        val callCount = AtomicInteger(0)
        val receivedParams = CopyOnWriteArrayList<String>()
        lateinit var holder: RequestHolder<String, Int>
        setContent {
            holder = useRequest(
                requestFn = { params: String ->
                    receivedParams += params
                    callCount.incrementAndGet()
                    params.length
                },
                optionsOf = {
                    manual = true
                    debounceOptionsOf = {
                        wait = if (enabled.value) 300.milliseconds else 0.milliseconds
                    }
                },
            )
            Text("data=${holder.data.value}")
        }

        waitForIdle()
        val initialHolder = holder
        val initialRequest = holder.request
        runOnIdle { initialRequest("stale") }
        waitForIdle()
        assertEquals(0, callCount.get())

        runOnIdle { enabled.value = false }
        waitForIdle()
        assertSame(initialHolder, holder)
        assertSame(initialRequest, holder.request)

        val unexpectedlyRan = waitForCondition(maxAttempts = 25, delayMs = 20) {
            callCount.get() > 0
        }
        assertFalse(unexpectedlyRan)

        runOnIdle { initialRequest("direct") }
        waitForIdle()

        assertEquals(1, callCount.get())
        assertEquals(listOf("direct"), receivedParams.toList())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun debouncePlugin_applies_changed_positive_wait_after_recomposition() = runComposeUiTest {
        val useShortWait = mutableStateOf(false)
        val callCount = AtomicInteger(0)
        val receivedParams = CopyOnWriteArrayList<String>()
        lateinit var holder: RequestHolder<String, Int>
        setContent {
            holder = useRequest(
                requestFn = { params: String ->
                    receivedParams += params
                    callCount.incrementAndGet()
                    params.length
                },
                optionsOf = {
                    manual = true
                    debounceOptionsOf = {
                        wait = if (useShortWait.value) 50.milliseconds else 1.seconds
                    }
                },
            )
            Text("data=${holder.data.value}")
        }

        waitForIdle()
        val initialHolder = holder
        val initialRequest = holder.request
        runOnIdle { initialRequest("stale") }
        waitForIdle()

        runOnIdle { useShortWait.value = true }
        waitForIdle()
        assertSame(initialHolder, holder)
        assertSame(initialRequest, holder.request)

        runOnIdle { initialRequest("latest") }
        waitForIdle()
        assertEquals(0, callCount.get())

        val completed = waitForCondition(maxAttempts = 30, delayMs = 20) {
            callCount.get() == 1
        }
        waitForIdle()

        assertTrue(completed, "Expected the updated debounce wait to take effect")
        assertEquals(listOf("latest"), receivedParams.toList())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun timingPlugin_recomposition_preserves_fetch_state_for_mutate_and_refresh() = runComposeUiTest {
        val enabled = mutableStateOf(false)
        val receivedParams = CopyOnWriteArrayList<String>()
        lateinit var holder: RequestHolder<String, Int>
        setContent {
            holder = useRequest(
                requestFn = { params: String ->
                    receivedParams += params
                    params.length
                },
                optionsOf = {
                    manual = true
                    debounceOptionsOf = {
                        wait = if (enabled.value) 10.seconds else 0.milliseconds
                    }
                },
            )
            Text("data=${holder.data.value}")
        }

        waitForIdle()
        val initialHolder = holder
        val initialMutate = holder.mutate
        val initialRefresh = holder.refresh
        runOnIdle { holder.request("seed") }
        waitForIdle()
        assertEquals(4, holder.data.value)

        runOnIdle { enabled.value = true }
        waitForIdle()
        assertSame(initialHolder, holder)
        assertSame(initialMutate, holder.mutate)
        assertSame(initialRefresh, holder.refresh)
        assertEquals(4, holder.data.value)

        runOnIdle { initialMutate { data -> (data ?: -100) + 1 } }
        waitForIdle()
        assertEquals(5, holder.data.value)

        runOnIdle { initialRefresh() }
        waitForIdle()

        assertEquals(listOf("seed", "seed"), receivedParams.toList())
        assertEquals(4, holder.data.value)
    }

    // endregion

    // region ThrottlePlugin Tests

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun throttlePlugin_multiple_rapid_calls_only_executes_first() = runComposeUiTest {
        resetInstantProvider()
        val callCount = AtomicInteger(0)
        val receivedParams = mutableListOf<String>()
        lateinit var holder: RequestHolder<String, Int>
        setContent {
            var phase by useState(default = 0)
            holder = useRequest(
                requestFn = { params: String ->
                    receivedParams.add(params)
                    callCount.incrementAndGet()
                    params.length
                },
                optionsOf = {
                    manual = true
                    throttleOptionsOf = {
                        wait = 200.milliseconds
                        leading = true
                        trailing = false
                    }
                },
            )

            SideEffect {
                when (phase) {
                    0 -> {
                        holder.request("a")
                        holder.request("ab")
                        holder.request("abc")
                        phase = 1
                    }
                }
            }

            Text("data=${holder.data.value}")
        }

        waitForIdle()
        advanceInstant(50.milliseconds)
        mainClock.advanceTimeBy(50)
        waitForIdle()

        assertEquals(1, callCount.get())
        assertEquals(listOf("a"), receivedParams)
        assertEquals(1, holder.data.value)
        assertFalse(holder.isLoading.value)
        assertNull(holder.error.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun throttlePlugin_cancel_resets_leading_window() = runComposeUiTest {
        val receivedParams = mutableListOf<String>()
        lateinit var holder: RequestHolder<String, Int>
        setContent {
            holder = useRequest(
                requestFn = { params: String ->
                    receivedParams += params
                    params.length
                },
                optionsOf = {
                    manual = true
                    throttleOptionsOf = {
                        wait = 10.seconds
                        leading = true
                        trailing = false
                    }
                },
            )
            Text("data=${holder.data.value}")
        }

        waitForIdle()
        runOnIdle { holder.request("first") }
        waitForIdle()
        assertEquals(listOf("first"), receivedParams)

        runOnIdle {
            holder.cancel()
            holder.request("second")
        }
        waitForIdle()

        assertEquals(listOf("first", "second"), receivedParams)
        assertEquals(6, holder.data.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun throttlePlugin_cancel_drops_pending_trailing_request() = runComposeUiTest {
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
                    throttleOptionsOf = {
                        wait = 50.milliseconds
                        leading = false
                        trailing = true
                    }
                },
            )
            Text("data=${holder.data.value}")
        }

        waitForIdle()
        runOnIdle {
            holder.request("stale")
            holder.cancel()
        }

        val unexpectedlyRan = waitForCondition(maxAttempts = 20, delayMs = 10) {
            callCount.get() > 0
        }
        waitForIdle()

        assertFalse(unexpectedlyRan)
        assertEquals(0, callCount.get())
        assertNull(holder.data.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun throttlePlugin_can_be_enabled_after_recomposition_with_stable_controls() = runComposeUiTest {
        val enabled = mutableStateOf(false)
        val receivedParams = CopyOnWriteArrayList<String>()
        lateinit var holder: RequestHolder<String, Int>
        setContent {
            holder = useRequest(
                requestFn = { params: String ->
                    receivedParams += params
                    params.length
                },
                optionsOf = {
                    manual = true
                    throttleOptionsOf = {
                        wait = if (enabled.value) 10.seconds else 0.milliseconds
                        leading = true
                        trailing = false
                    }
                },
            )
            Text("data=${holder.data.value}")
        }

        waitForIdle()
        val initialHolder = holder
        val initialRequest = holder.request
        runOnIdle { initialRequest("direct") }
        waitForIdle()
        assertEquals(listOf("direct"), receivedParams.toList())

        runOnIdle { enabled.value = true }
        waitForIdle()
        assertSame(initialHolder, holder)
        assertSame(initialRequest, holder.request)

        runOnIdle {
            initialRequest("first")
            initialRequest("suppressed")
        }
        waitForIdle()

        assertEquals(listOf("direct", "first"), receivedParams.toList())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun throttlePlugin_can_be_disabled_after_recomposition_with_stable_controls() = runComposeUiTest {
        val enabled = mutableStateOf(true)
        val receivedParams = CopyOnWriteArrayList<String>()
        lateinit var holder: RequestHolder<String, Int>
        setContent {
            holder = useRequest(
                requestFn = { params: String ->
                    receivedParams += params
                    params.length
                },
                optionsOf = {
                    manual = true
                    throttleOptionsOf = {
                        wait = if (enabled.value) 10.seconds else 0.milliseconds
                        leading = true
                        trailing = false
                    }
                },
            )
            Text("data=${holder.data.value}")
        }

        waitForIdle()
        val initialHolder = holder
        val initialRequest = holder.request
        runOnIdle {
            initialRequest("first")
            initialRequest("suppressed")
        }
        waitForIdle()
        assertEquals(listOf("first"), receivedParams.toList())

        runOnIdle { enabled.value = false }
        waitForIdle()
        assertSame(initialHolder, holder)
        assertSame(initialRequest, holder.request)

        runOnIdle { initialRequest("direct") }
        waitForIdle()

        assertEquals(listOf("first", "direct"), receivedParams.toList())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun throttlePlugin_applies_changed_options_after_recomposition() = runComposeUiTest {
        val useTrailingMode = mutableStateOf(false)
        val callCount = AtomicInteger(0)
        val receivedParams = CopyOnWriteArrayList<String>()
        lateinit var holder: RequestHolder<String, Int>
        setContent {
            holder = useRequest(
                requestFn = { params: String ->
                    receivedParams += params
                    callCount.incrementAndGet()
                    params.length
                },
                optionsOf = {
                    manual = true
                    throttleOptionsOf = {
                        if (useTrailingMode.value) {
                            wait = 50.milliseconds
                            leading = false
                            trailing = true
                        } else {
                            wait = 10.seconds
                            leading = true
                            trailing = false
                        }
                    }
                },
            )
            Text("data=${holder.data.value}")
        }

        waitForIdle()
        val initialHolder = holder
        val initialRequest = holder.request
        runOnIdle {
            initialRequest("first")
            initialRequest("suppressed")
        }
        waitForIdle()
        assertEquals(listOf("first"), receivedParams.toList())

        runOnIdle { useTrailingMode.value = true }
        waitForIdle()
        assertSame(initialHolder, holder)
        assertSame(initialRequest, holder.request)

        runOnIdle { initialRequest("delayed") }
        waitForIdle()
        assertEquals(1, callCount.get())

        val completed = waitForCondition(maxAttempts = 30, delayMs = 20) {
            callCount.get() == 2
        }
        waitForIdle()

        assertTrue(completed, "Expected the updated throttle options to take effect")
        assertEquals(listOf("first", "delayed"), receivedParams.toList())
    }

    // endregion

    // region RetryPlugin Tests

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun retryPlugin_retries_on_error_up_to_retryCount() = runComposeUiTest {
        val callCount = AtomicInteger(0)
        lateinit var holder: RequestHolder<String, Int>
        setContent {
            holder = useRequest(
                requestFn = { _: String ->
                    val count = callCount.incrementAndGet()
                    if (count <= 2) {
                        throw RuntimeException("Error $count")
                    }
                    count
                },
                optionsOf = {
                    defaultParams = "test"
                    retryCount = 3
                    retryInterval = 50.milliseconds
                    onError = { _, _ -> }
                },
            )

            Text("data=${holder.data.value} loading=${holder.isLoading.value} error=${holder.error.value?.message}")
        }

        waitForIdle()
        val completed = waitForCondition(maxAttempts = 80, delayMs = 30) {
            waitForIdle()
            callCount.get() == 3 &&
                holder.data.value == 3 &&
                holder.isLoading.value == false &&
                holder.error.value == null
        }
        assertTrue(completed, "Expected retry plugin to eventually succeed within retryCount")
        assertEquals(3, callCount.get())
        assertEquals(3, holder.data.value)
        assertFalse(holder.isLoading.value)
        assertNull(holder.error.value)
        onNodeWithText("data=3 loading=false error=null").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun retryPlugin_stops_after_retryCount_exceeded() = runComposeUiTest {
        val callCount = AtomicInteger(0)
        lateinit var holder: RequestHolder<String, Int>
        setContent {
            holder = useRequest(
                requestFn = { _: String ->
                    callCount.incrementAndGet()
                    throw RuntimeException("Always fails")
                },
                optionsOf = {
                    defaultParams = "test"
                    retryCount = 2
                    retryInterval = 50.milliseconds
                    onError = { _, _ -> }
                },
            )

            Text("data=${holder.data.value} loading=${holder.isLoading.value} error=${holder.error.value?.message}")
        }

        waitForIdle()
        val completed = waitForCondition(maxAttempts = 80, delayMs = 30) {
            waitForIdle()
            callCount.get() == 3 &&
                holder.data.value == null &&
                holder.isLoading.value == false &&
                holder.error.value?.message == "Always fails"
        }
        assertTrue(completed, "Expected retry plugin to stop after retryCount is exceeded")
        assertEquals(3, callCount.get())
        assertNull(holder.data.value)
        assertFalse(holder.isLoading.value)
        assertEquals("Always fails", holder.error.value?.message)
        onNodeWithText("data=null loading=false error=Always fails").assertExists()
    }

    // endregion

    // region PollingPlugin Tests

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pollingPlugin_polls_at_specified_interval() = runComposeUiTest {
        val callCount = AtomicInteger(0)
        lateinit var holder: RequestHolder<String, Unit>
        setContent {
            holder = useRequest(
                requestFn = { _: String ->
                    callCount.incrementAndGet()
                },
                optionsOf = {
                    defaultParams = "test"
                    pollingInterval = 100.milliseconds
                    pollingWhenHidden = true
                },
            )

            Text("count=${callCount.get()} loading=${holder.isLoading.value} error=${holder.error.value?.message}")
        }

        waitForIdle()
        assertEquals(1, callCount.get())

        mainClock.advanceTimeBy(250)
        waitForIdle()

        val completed = waitForCondition {
            callCount.get() >= 3 &&
                holder.isLoading.value == false &&
                holder.error.value == null
        }
        assertTrue(completed, "Expected polling to trigger repeated refreshes")
        assertEquals(false, holder.isLoading.value)
        assertEquals(null, holder.error.value)
    }

    // endregion

    // region LoadingDelayPlugin Tests
    // LoadingDelayPlugin 的测试需要更复杂的异步控制，暂时跳过
    // endregion

    // region CachePlugin Tests

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun cachePlugin_returns_cached_data_on_second_request() = runComposeUiTest {
        resetInstantProvider()
        val callCount = AtomicInteger(0)
        val cacheKey = "test-cache-${System.currentTimeMillis()}"
        lateinit var requestAgain: (String) -> Unit
        lateinit var holder: RequestHolder<String, Int>
        setContent {
            holder = useRequest(
                requestFn = { params: String ->
                    delay(50)
                    callCount.incrementAndGet()
                    params.length
                },
                optionsOf = {
                    manual = true
                    this.cacheKey = cacheKey
                    staleTime = 10.seconds
                },
            )
            requestAgain = { params -> holder.request(params) }

            Text("data=${holder.data.value} loading=${holder.isLoading.value} error=${holder.error.value?.message}")
        }

        waitForIdle()
        requestAgain("abc")
        waitForIdle()
        mainClock.advanceTimeBy(60)
        waitForIdle()

        val firstLoaded = waitForCondition {
            callCount.get() == 1 &&
                holder.data.value == 3 &&
                holder.isLoading.value == false &&
                holder.error.value == null
        }
        assertTrue(firstLoaded, "Expected first request to complete")
        assertEquals(1, callCount.get())
        assertEquals(3, holder.data.value)
        assertEquals(false, holder.isLoading.value)
        assertEquals(null, holder.error.value)
        onNodeWithText("data=3 loading=false error=null").assertExists()

        requestAgain("abc")
        waitForIdle()
        mainClock.advanceTimeBy(1)
        waitForIdle()

        val cached = waitForCondition {
            callCount.get() == 1 &&
                holder.data.value == 3 &&
                holder.isLoading.value == false &&
                holder.error.value == null
        }
        assertTrue(cached, "Expected cached request to avoid duplicate fetch")
        assertEquals(1, callCount.get())
        assertEquals(3, holder.data.value)
        assertEquals(false, holder.isLoading.value)
        assertEquals(null, holder.error.value)
        onNodeWithText("data=3 loading=false error=null").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun cachePlugin_refetches_after_staleTime_expires() = runComposeUiTest {
        resetInstantProvider()
        val callCount = AtomicInteger(0)
        val cacheKey = "test-cache-stale-${System.currentTimeMillis()}"
        lateinit var requestAgain: (String) -> Unit
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
                    staleTime = 50.milliseconds
                },
            )
            requestAgain = { params -> holder.request(params) }

            Text("data=${holder.data.value} loading=${holder.isLoading.value} error=${holder.error.value?.message}")
        }

        waitForIdle()
        requestAgain("abc")
        waitForIdle()

        val firstLoaded = waitForCondition {
            callCount.get() == 1 &&
                holder.data.value == 3 &&
                holder.isLoading.value == false &&
                holder.error.value == null
        }
        assertTrue(firstLoaded, "Expected first request to complete")
        assertEquals(1, callCount.get())
        assertEquals(3, holder.data.value)
        assertFalse(holder.isLoading.value)
        assertNull(holder.error.value)
        onNodeWithText("data=3 loading=false error=null").assertExists()

        advanceInstant(80.milliseconds)
        requestAgain("abc")
        waitForIdle()

        val refetched = waitForCondition {
            callCount.get() == 2 &&
                holder.data.value == 3 &&
                holder.isLoading.value == false &&
                holder.error.value == null
        }
        assertTrue(refetched, "Expected stale cache to trigger a second fetch")
        assertEquals(2, callCount.get())
        assertEquals(3, holder.data.value)
        assertFalse(holder.isLoading.value)
        assertNull(holder.error.value)
        onNodeWithText("data=3 loading=false error=null").assertExists()
    }

    // endregion
}
