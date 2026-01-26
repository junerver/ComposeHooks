package xyz.junerver.compose.hooks.test

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import xyz.junerver.compose.hooks.userequest.Fetch
import xyz.junerver.compose.hooks.userequest.FetchState
import xyz.junerver.compose.hooks.userequest.OnBeforeReturn
import xyz.junerver.compose.hooks.userequest.PluginLifecycle
import xyz.junerver.compose.hooks.userequest.UseRequestOptions

/**
 * TDD 测试套件：useRequest 边缘场景和缺陷检测
 *
 * 目标：通过测试用例发现 Fetch.kt 中的生产级缺陷
 *
 * 测试分类：
 * - P0 (Critical): 并发竞态、取消逻辑缺陷
 * - P1 (High): 插件生命周期、错误传播
 * - P2 (Medium): 边缘情况处理
 */
class FetchEdgeCaseTest {
    private data class StateBundle<T>(
        val state: State<T>,
        val set: (T) -> Unit,
    )

    private fun <T> createStateBundle(initial: T): StateBundle<T> {
        val mutableState = mutableStateOf(initial)
        return StateBundle(
            state = mutableState,
            set = { mutableState.value = it },
        )
    }

    private fun createFetch(
        options: UseRequestOptions<String, Int>,
        requestFn: suspend (String) -> Int,
        pluginImpls: Array<PluginLifecycle<String, Int>> = emptyArray(),
    ): Triple<Fetch<String, Int>, StateBundle<Int?>, StateBundle<Boolean>> {
        val dataBundle = createStateBundle<Int?>(null)
        val loadingBundle = createStateBundle(false)
        val errorBundle = createStateBundle<Throwable?>(null)

        val fetch = Fetch(options).apply {
            fetchState = FetchState()
            this.dataState = dataBundle.state
            this.setData = dataBundle.set
            this.loadingState = loadingBundle.state
            this.setLoading = loadingBundle.set
            this.errorState = errorBundle.state
            this.setError = errorBundle.set
            this.requestFn = requestFn
            this.pluginImpls = pluginImpls
        }

        return Triple(fetch, dataBundle, loadingBundle)
    }

    // ==================== P0: 并发竞态测试 ====================

    @Test
    fun concurrent_requests_should_only_keep_last_result() = runTest {
        val options = UseRequestOptions.optionOf<String, Int> {}
        val (fetch, dataBundle, _) = createFetch(
            options = options,
            requestFn = { params ->
                val delayTime = when (params) {
                    "fast" -> 100L
                    "slow" -> 500L
                    else -> 200L
                }
                delay(delayTime)
                params.length
            },
        )

        // 快速连续发起 3 个请求
        launch { fetch._runAsync("slow") } // 应该被丢弃
        advanceTimeBy(50)
        launch { fetch._runAsync("medium") } // 应该被丢弃
        advanceTimeBy(50)
        launch { fetch._runAsync("fast") } // 应该保留

        advanceTimeBy(600)
        runCurrent()

        // 预期：只有最后一个请求的结果被保留
        assertEquals(4, dataBundle.state.value) // "fast".length = 4
    }

    @Test
    fun cancel_during_request_should_prevent_state_update() = runTest {
        val options = UseRequestOptions.optionOf<String, Int> {}
        var requestExecuted = false
        val (fetch, dataBundle, loadingBundle) = createFetch(
            options = options,
            requestFn = {
                requestExecuted = true
                delay(1000)
                42
            },
        )

        val job = launch { fetch._runAsync("test") }
        advanceTimeBy(100)
        runCurrent()

        assertTrue(requestExecuted)
        assertTrue(loadingBundle.state.value)

        // 取消请求
        fetch.cancel()
        assertFalse(loadingBundle.state.value)

        advanceTimeBy(1000)
        runCurrent()
        job.join()

        // 预期：数据不应该被更新
        assertNull(dataBundle.state.value)
    }

    @Test
    fun multiple_cancel_calls_should_be_safe() = runTest {
        val options = UseRequestOptions.optionOf<String, Int> {}
        val (fetch, _, _) = createFetch(
            options = options,
            requestFn = {
                delay(1000)
                42
            },
        )

        launch { fetch._runAsync("test") }
        advanceTimeBy(100)

        // 多次调用 cancel
        fetch.cancel()
        fetch.cancel()
        fetch.cancel()

        advanceTimeBy(1000)
        runCurrent()

        // 预期：不应该崩溃
        assertFalse(fetch.loadingState.value)
    }

    // ==================== P0: 取消逻辑缺陷 ====================

    @Test
    fun runAsync_called_directly_should_be_cancellable() = runTest {
        val options = UseRequestOptions.optionOf<String, Int> {}
        val (fetch, dataBundle, _) = createFetch(
            options = options,
            requestFn = {
                delay(1000)
                99
            },
        )

        // 直接调用 _runAsync (不通过 _run)
        val job = launch { fetch._runAsync("test") }
        advanceTimeBy(100)

        fetch.cancel()

        advanceTimeBy(1000)
        job.join()

        // 预期：请求应该被取消，数据不应该更新
        // 实际：由于 SupervisorJob() 的问题，这个测试会失败
        assertNull(dataBundle.state.value)
    }

    @Test
    fun cancel_should_clear_error_state() = runTest {
        val options = UseRequestOptions.optionOf<String, Int> {}
        val (fetch, _, _) = createFetch(
            options = options,
            requestFn = { throw IllegalStateException("error") },
        )

        fetch._runAsync("test")
        assertNotNull(fetch.errorState.value)

        fetch.cancel()

        // 预期：错误状态应该被清除
        // 实际：当前实现不清除 error
        assertNull(fetch.errorState.value)
    }

    // ==================== P1: 插件生命周期问题 ====================

    @Test
    fun plugin_onFinally_should_always_execute_even_when_cancelled() = runTest {
        val events = mutableListOf<String>()
        val options = UseRequestOptions.optionOf<String, Int> {
            onFinally = { _, _, _ -> events += "options:onFinally" }
        }

        val plugin = object : PluginLifecycle<String, Int>() {
            override val onFinally: ((String, Int?, Throwable?) -> Unit)? = { _, _, _ ->
                events += "plugin:onFinally"
            }
        }

        val (fetch, _, _) = createFetch(
            options = options,
            requestFn = {
                delay(1000)
                42
            },
            pluginImpls = arrayOf(plugin),
        )

        launch { fetch._runAsync("test") }
        advanceTimeBy(100)
        fetch.cancel()
        advanceTimeBy(1000)
        runCurrent()

        // 预期：插件的 onFinally 应该被调用（用于清理资源）
        // 实际：由于 count 检查，插件的 onFinally 可能被跳过
        assertTrue(events.contains("plugin:onFinally"))
    }

    @Test
    fun plugin_exception_should_not_crash_request() = runTest {
        val options = UseRequestOptions.optionOf<String, Int> {}

        val faultyPlugin = object : PluginLifecycle<String, Int>() {
            override val onBefore: ((String) -> OnBeforeReturn<String, Int>?)? = {
                throw RuntimeException("Plugin error")
            }
        }

        val (fetch, dataBundle, _) = createFetch(
            options = options,
            requestFn = { 42 },
            pluginImpls = arrayOf(faultyPlugin),
        )

        // 预期：插件异常应该被捕获，请求继续执行
        // 实际：插件异常会导致整个请求失败
        try {
            fetch._runAsync("test")
            assertEquals(42, dataBundle.state.value)
        } catch (e: Exception) {
            // 如果抛出异常，说明没有正确处理插件错误
            throw AssertionError("Plugin exception should be caught", e)
        }
    }

    @Test
    fun multiple_plugins_one_fails_others_should_continue() = runTest {
        val events = mutableListOf<String>()
        val options = UseRequestOptions.optionOf<String, Int> {}

        val plugin1 = object : PluginLifecycle<String, Int>() {
            override val onSuccess: ((Int, String) -> Unit)? = { _, _ ->
                events += "plugin1:onSuccess"
            }
        }

        val faultyPlugin = object : PluginLifecycle<String, Int>() {
            override val onSuccess: ((Int, String) -> Unit)? = { _, _ ->
                events += "faultyPlugin:onSuccess"
                throw RuntimeException("Plugin2 error")
            }
        }

        val plugin3 = object : PluginLifecycle<String, Int>() {
            override val onSuccess: ((Int, String) -> Unit)? = { _, _ ->
                events += "plugin3:onSuccess"
            }
        }

        val (fetch, _, _) = createFetch(
            options = options,
            requestFn = { 42 },
            pluginImpls = arrayOf(plugin1, faultyPlugin, plugin3),
        )

        fetch._runAsync("test")

        // 预期：plugin1 和 plugin3 应该都执行
        // 实际：faultyPlugin 的异常会中断后续插件
        assertTrue(events.contains("plugin1:onSuccess"))
        assertTrue(events.contains("plugin3:onSuccess"))
    }

    // ==================== P1: 错误传播缺陷 ====================

    @Test
    fun null_params_should_call_plugin_callbacks() = runTest {
        val events = mutableListOf<String>()
        val options = UseRequestOptions.optionOf<String, Int> {
            defaultParams = null
            onError = { _, _ -> events += "options:onError" }
        }

        val plugin = object : PluginLifecycle<String, Int>() {
            override val onError: ((Throwable, String?) -> Unit)? = { _, _ ->
                events += "plugin:onError"
            }
            override val onFinally: ((String?, Int?, Throwable?) -> Unit)? = { _, _, _ ->
                events += "plugin:onFinally"
            }
        }

        val (fetch, _, _) = createFetch(
            options = options,
            requestFn = { error("should not be called") },
            pluginImpls = arrayOf(plugin),
        )

        fetch._runAsync(null)

        // 预期：插件的 onError 和 onFinally 应该被调用
        // 实际：当前实现不调用插件回调
        assertTrue(events.contains("plugin:onError"))
        assertTrue(events.contains("plugin:onFinally"))
    }

    @Test
    fun null_params_should_clear_previous_data() = runTest {
        val options = UseRequestOptions.optionOf<String, Int> {
            defaultParams = null
        }

        val (fetch, dataBundle, _) = createFetch(
            options = options,
            requestFn = { 42 },
        )

        // 先成功请求一次
        fetch._runAsync("test")
        assertEquals(42, dataBundle.state.value)

        // 然后用 null 参数请求
        fetch._runAsync(null)

        // 预期：data 应该被清空
        // 实际：data 保留旧值
        assertNull(dataBundle.state.value)
    }

    @Test
    fun error_in_onError_callback_should_not_lose_original_error() = runTest {
        val originalError = IllegalArgumentException("original")
        val options = UseRequestOptions.optionOf<String, Int> {
            onError = { _, _ ->
                throw RuntimeException("callback error")
            }
        }

        val (fetch, _, _) = createFetch(
            options = options,
            requestFn = { throw originalError },
        )

        fetch._runAsync("test")

        // 预期：errorState 应该包含原始错误
        // 实际：回调异常可能覆盖原始错误
        assertEquals(originalError.message, fetch.errorState.value?.message)
    }

    // ==================== P2: 边缘情况 ====================

    @Test
    fun refresh_without_previous_request_should_fail_gracefully() = runTest {
        val options = UseRequestOptions.optionOf<String, Int> {}
        val (fetch, dataBundle, _) = createFetch(
            options = options,
            requestFn = { 42 },
        )

        // 直接调用 refresh，没有先调用 run
        fetch.refresh()

        // 预期：应该有错误提示或者不执行
        // 实际：静默失败
        assertNull(dataBundle.state.value)
    }

    @Test
    fun mutate_with_exception_should_not_corrupt_state() = runTest {
        val options = UseRequestOptions.optionOf<String, Int> {}
        val (fetch, dataBundle, _) = createFetch(
            options = options,
            requestFn = { 42 },
        )

        fetch._runAsync("test")
        assertEquals(42, dataBundle.state.value)

        // mutate 函数抛出异常
        try {
            fetch.mutate { throw RuntimeException("mutate error") }
        } catch (e: Exception) {
            // 预期：捕获异常
        }

        // 预期：原始数据应该保持不变
        // 实际：状态可能被破坏
        assertEquals(42, dataBundle.state.value)
    }

    @Test
    fun setState_during_setState_should_not_cause_inconsistency() = runTest {
        val options = UseRequestOptions.optionOf<String, Int> {}
        var setDataCallCount = 0
        val dataBundle = createStateBundle<Int?>(null)
        val loadingBundle = createStateBundle(false)
        val errorBundle = createStateBundle<Throwable?>(null)

        val fetch = Fetch(options).apply {
            fetchState = FetchState()
            this.dataState = dataBundle.state
            this.setData = { value ->
                setDataCallCount++
                // 在 setData 中触发另一个状态更新
                if (setDataCallCount == 1) {
                    this.setLoading(true)
                }
                dataBundle.set(value)
            }
            this.loadingState = loadingBundle.state
            this.setLoading = loadingBundle.set
            this.errorState = errorBundle.state
            this.setError = errorBundle.set
            this.requestFn = { 42 }
            this.pluginImpls = emptyArray()
        }

        fetch._runAsync("test")

        // 预期：状态应该保持一致
        assertEquals(42, dataBundle.state.value)
        assertFalse(loadingBundle.state.value)
    }

    @Test
    fun rapid_run_and_cancel_should_not_leak_jobs() = runTest {
        val options = UseRequestOptions.optionOf<String, Int> {
            onError = { _, _ -> } // 忽略错误
        }
        val (fetch, _, _) = createFetch(
            options = options,
            requestFn = {
                delay(1000)
                42
            },
        )

        // 快速执行 runAsync 和 cancel 循环
        repeat(10) {
            launch { fetch._runAsync("test$it") }
            advanceTimeBy(10)
            fetch.cancel()
        }

        advanceTimeBy(2000)
        runCurrent()

        // 预期：所有请求都应该被取消，loading 为 false
        assertFalse(fetch.loadingState.value)
    }
}
