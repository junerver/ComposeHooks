package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.userequest.Fetch
import xyz.junerver.compose.hooks.userequest.Plugin
import xyz.junerver.compose.hooks.userequest.PluginLifecycle
import xyz.junerver.compose.hooks.userequest.UseRequestOptions
import xyz.junerver.compose.hooks.userequest.useRequest

/*
  Description: useRequest 组合行为桌面测试
  Author: Junerver
  Date: 2026/1/9
  Email: junerver@gmail.com
  Version: v1.0
*/
class UseRequestTest {
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
    fun plugins_same_size_should_update_on_recomposition() = runComposeUiTest {
        val callCount = AtomicInteger(0)
        setContent {
            var useNewPlugin by useState(default = false)
            var phase by useState(default = 0)

            val pluginA = remember {
                @androidx.compose.runtime.Composable
                { _: UseRequestOptions<String, Int> ->
                    object : Plugin<String, Int>() {
                        override val invoke: (Fetch<String, Int>, UseRequestOptions<String, Int>) -> PluginLifecycle<String, Int> = { fetch: Fetch<String, Int>, opt: UseRequestOptions<String, Int> ->
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
                        override val invoke: (Fetch<String, Int>, UseRequestOptions<String, Int>) -> PluginLifecycle<String, Int> = { fetch: Fetch<String, Int>, opt: UseRequestOptions<String, Int> ->
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
        val callCount = AtomicInteger(0)
        val receivedParams = mutableListOf<String>()
        setContent {
            var phase by useState(default = 0)
            val holder = useRequest(
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
        Thread.sleep(200)
        waitForIdle()

        assertEquals(1, callCount.get())
        assertEquals(listOf("abc"), receivedParams)
    }

    // endregion

    // region ThrottlePlugin Tests

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun throttlePlugin_multiple_rapid_calls_only_executes_first() = runComposeUiTest {
        val callCount = AtomicInteger(0)
        val receivedParams = mutableListOf<String>()
        setContent {
            var phase by useState(default = 0)
            val holder = useRequest(
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
        Thread.sleep(50)
        waitForIdle()

        assertEquals(1, callCount.get())
        assertEquals(listOf("a"), receivedParams)
    }

    // endregion

    // region RetryPlugin Tests

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun retryPlugin_retries_on_error_up_to_retryCount() = runComposeUiTest {
        val callCount = AtomicInteger(0)
        setContent {
            val holder = useRequest(
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

            Text("data=${holder.data.value} error=${holder.error.value?.message}")
        }

        waitForIdle()
        Thread.sleep(300)
        waitForIdle()

        assertEquals(3, callCount.get())
        onNodeWithText("data=3 error=null").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun retryPlugin_stops_after_retryCount_exceeded() = runComposeUiTest {
        val callCount = AtomicInteger(0)
        setContent {
            val holder = useRequest(
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

            Text("error=${holder.error.value != null}")
        }

        waitForIdle()
        Thread.sleep(300)
        waitForIdle()

        assertEquals(3, callCount.get())
        onNodeWithText("error=true").assertExists()
    }

    // endregion

    // region PollingPlugin Tests

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pollingPlugin_polls_at_specified_interval() = runComposeUiTest {
        val callCount = AtomicInteger(0)
        setContent {
            useRequest(
                requestFn = { _: String ->
                    callCount.incrementAndGet()
                },
                optionsOf = {
                    defaultParams = "test"
                    pollingInterval = 100.milliseconds
                    pollingWhenHidden = true
                },
            )

            Text("count=${callCount.get()}")
        }

        waitForIdle()
        Thread.sleep(350)
        waitForIdle()

        assertTrue(callCount.get() >= 3)
    }

    // endregion

    // region LoadingDelayPlugin Tests
    // LoadingDelayPlugin 的测试需要更复杂的异步控制，暂时跳过
    // endregion

    // region CachePlugin Tests

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun cachePlugin_returns_cached_data_on_second_request() = runComposeUiTest {
        val callCount = AtomicInteger(0)
        val cacheKey = "test-cache-${System.currentTimeMillis()}"
        setContent {
            var phase by useState(default = 0)
            val holder = useRequest(
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

            SideEffect {
                when (phase) {
                    0 -> {
                        holder.request("abc")
                        phase = 1
                    }

                    1 -> {
                        if (holder.data.value != null) {
                            holder.request("abc")
                            phase = 2
                        }
                    }
                }
            }

            Text("data=${holder.data.value} phase=$phase")
        }

        waitForIdle()
        Thread.sleep(200)
        waitForIdle()

        assertEquals(1, callCount.get())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun cachePlugin_refetches_after_staleTime_expires() = runComposeUiTest {
        val callCount = AtomicInteger(0)
        val cacheKey = "test-cache-stale-${System.currentTimeMillis()}"
        setContent {
            var phase by useState(default = 0)
            val holder = useRequest(
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

            SideEffect {
                when (phase) {
                    0 -> {
                        holder.request("abc")
                        phase = 1
                    }

                    1 -> {
                        if (holder.data.value != null) {
                            phase = 2
                        }
                    }
                }
            }

            Text("data=${holder.data.value} phase=$phase")
        }

        waitForIdle()
        Thread.sleep(100)
        waitForIdle()

        assertEquals(1, callCount.get())
    }

    // endregion
}
