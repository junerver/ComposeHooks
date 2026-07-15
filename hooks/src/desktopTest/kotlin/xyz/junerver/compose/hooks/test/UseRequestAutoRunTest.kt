package xyz.junerver.compose.hooks.test

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import xyz.junerver.compose.hooks.userequest.RequestHolder
import xyz.junerver.compose.hooks.userequest.Fetch
import xyz.junerver.compose.hooks.userequest.GenPluginLifecycleFn
import xyz.junerver.compose.hooks.userequest.Plugin
import xyz.junerver.compose.hooks.userequest.PluginLifecycle
import xyz.junerver.compose.hooks.userequest.UseRequestOptions
import xyz.junerver.compose.hooks.userequest.useRequest

/*
  Description: useRequest AutoRun plugin lifecycle tests
  Author: Junerver
  Date: 2026/7/15
  Email: junerver@gmail.com
  Version: v1.0
*/
class UseRequestAutoRunTest {
    private class CapturingPlugin : Plugin<String, Int>() {
        lateinit var fetch: Fetch<String, Int>

        override val invoke: GenPluginLifecycleFn<String, Int>
            get() = { fetch, options ->
                initFetch(fetch, options)
                this.fetch = fetch
                object : PluginLifecycle<String, Int>() {}
            }
    }

    private fun waitForCondition(maxAttempts: Int = 80, delayMs: Long = 20, condition: () -> Boolean): Boolean {
        repeat(maxAttempts) {
            if (condition()) return true
            Thread.sleep(delayMs)
        }
        return false
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun refreshDepsAction_is_not_called_on_initial_mount_when_not_ready() = runComposeUiTest {
        val serviceCalls = AtomicInteger(0)
        val actionCalls = AtomicInteger(0)

        setContent {
            useRequest(
                requestFn = { params: String ->
                    serviceCalls.incrementAndGet()
                    params.length
                },
                optionsOf = {
                    ready = false
                    defaultParams = "initial"
                    refreshDeps = arrayOf(0)
                    refreshDepsAction = { actionCalls.incrementAndGet() }
                },
            )
        }

        waitForIdle()
        assertEquals(0, serviceCalls.get())
        assertEquals(0, actionCalls.get(), "refreshDepsAction must only run after a dependency changes")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun ready_false_before_debounced_auto_run_clears_loading_and_cancels_service() = runComposeUiTest {
        val readyState = mutableStateOf(true)
        val serviceCalls = AtomicInteger(0)
        lateinit var holder: RequestHolder<String, Int>
        val capturingPlugin = CapturingPlugin()

        setContent {
            val pluginGenerator = remember {
                @androidx.compose.runtime.Composable
                { _: UseRequestOptions<String, Int> -> capturingPlugin }
            }
            holder = useRequest(
                requestFn = { params: String ->
                    serviceCalls.incrementAndGet()
                    params.length
                },
                optionsOf = {
                    ready = readyState.value
                    defaultParams = "initial"
                    debounceOptionsOf = {
                        wait = 400.milliseconds
                    }
                },
                plugins = arrayOf(pluginGenerator),
            )
        }

        waitForIdle()
        assertEquals(true, capturingPlugin.fetch.fetchState.loading)

        runOnIdle { readyState.value = false }
        waitForIdle()

        assertFalse(holder.isLoading.value, "ready=false must clear optimistic auto-run loading")
        assertEquals(false, capturingPlugin.fetch.fetchState.loading, "ready=false must clear internal loading")
        Thread.sleep(550)
        waitForIdle()
        assertEquals(0, serviceCalls.get(), "The canceled debounced auto-run must never reach the service")
        assertFalse(holder.isLoading.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun ready_cycles_run_once_per_activation_with_latest_default_params() = runComposeUiTest {
        val readyState = mutableStateOf(false)
        val defaultParamsState = mutableStateOf("first")
        val receivedParams = CopyOnWriteArrayList<String>()

        setContent {
            useRequest(
                requestFn = { params: String ->
                    receivedParams += params
                    params.length
                },
                optionsOf = {
                    ready = readyState.value
                    defaultParams = defaultParamsState.value
                },
            )
        }

        waitForIdle()
        assertTrue(receivedParams.isEmpty())

        runOnIdle { readyState.value = true }
        waitForIdle()
        assertTrue(waitForCondition { receivedParams.size == 1 })
        assertEquals(listOf("first"), receivedParams.toList())

        runOnIdle { defaultParamsState.value = "ignored-while-ready" }
        waitForIdle()
        assertEquals(1, receivedParams.size, "Changing defaultParams alone must not trigger another request")

        runOnIdle { readyState.value = false }
        waitForIdle()
        runOnIdle { defaultParamsState.value = "second" }
        waitForIdle()
        runOnIdle { readyState.value = true }
        waitForIdle()

        assertTrue(waitForCondition { receivedParams.size == 2 })
        assertEquals(listOf("first", "second"), receivedParams.toList())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun manual_mode_ignores_refresh_deps_and_action_but_allows_request() = runComposeUiTest {
        val dependencyState = mutableStateOf(0)
        val serviceCalls = AtomicInteger(0)
        val actionCalls = AtomicInteger(0)
        lateinit var holder: RequestHolder<String, Int>

        setContent {
            holder = useRequest(
                requestFn = { params: String ->
                    serviceCalls.incrementAndGet()
                    params.length
                },
                optionsOf = {
                    manual = true
                    ready = true
                    defaultParams = "automatic"
                    refreshDeps = arrayOf(dependencyState.value)
                    refreshDepsAction = { actionCalls.incrementAndGet() }
                },
            )
        }

        waitForIdle()
        runOnIdle { dependencyState.value = 1 }
        waitForIdle()

        assertEquals(0, serviceCalls.get())
        assertEquals(0, actionCalls.get())

        runOnIdle { holder.request("manual") }
        assertTrue(waitForCondition { serviceCalls.get() == 1 })
        assertEquals(0, actionCalls.get())
        assertEquals(6, holder.data.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun simultaneous_ready_and_deps_change_runs_once_and_keeps_holder_controls_stable() = runComposeUiTest {
        val readyState = mutableStateOf(false)
        val dependencyState = mutableStateOf(0)
        val defaultParamsState = mutableStateOf("initial")
        val receivedParams = CopyOnWriteArrayList<String>()
        lateinit var latestHolder: RequestHolder<String, Int>

        setContent {
            latestHolder = useRequest(
                requestFn = { params: String ->
                    receivedParams += params
                    params.length
                },
                optionsOf = {
                    ready = readyState.value
                    defaultParams = defaultParamsState.value
                    refreshDeps = arrayOf(dependencyState.value)
                },
            )
        }

        waitForIdle()
        val initialHolder = latestHolder
        val initialData = initialHolder.data
        val initialLoading = initialHolder.isLoading
        val initialError = initialHolder.error
        val initialRequest = initialHolder.request
        val initialMutate = initialHolder.mutate
        val initialRefresh = initialHolder.refresh
        val initialCancel = initialHolder.cancel

        runOnIdle {
            defaultParamsState.value = "latest"
            dependencyState.value = 1
            readyState.value = true
        }
        waitForIdle()

        assertTrue(waitForCondition { receivedParams.size == 1 })
        assertEquals(listOf("latest"), receivedParams.toList())
        assertSame(initialHolder, latestHolder)
        assertSame(initialData, latestHolder.data)
        assertSame(initialLoading, latestHolder.isLoading)
        assertSame(initialError, latestHolder.error)
        assertSame(initialRequest, latestHolder.request)
        assertSame(initialMutate, latestHolder.mutate)
        assertSame(initialRefresh, latestHolder.refresh)
        assertSame(initialCancel, latestHolder.cancel)
    }
}
