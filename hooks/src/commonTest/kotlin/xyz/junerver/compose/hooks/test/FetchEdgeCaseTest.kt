package xyz.junerver.compose.hooks.test

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
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
        scope: CoroutineScope,
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
            this.scope = scope
        }

        return Triple(fetch, dataBundle, loadingBundle)
    }

    @Test
    fun cancel_should_clear_error_state() = runTest {
        val options = UseRequestOptions.optionOf<String, Int> {}
        val (fetch, _, _) = createFetch(
            scope = backgroundScope,
            options = options,
            requestFn = { throw IllegalStateException("error") },
        )

        // 使用 run() 而不是 _runAsync()
        fetch.run("test")
        advanceUntilIdle()
        delay(100)
        assertNotNull(fetch.errorState.value)

        fetch.cancel()

        assertNull(fetch.errorState.value)
        assertFalse(fetch.loadingState.value)
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
            scope = backgroundScope,
            options = options,
            requestFn = { 42 },
            pluginImpls = arrayOf(faultyPlugin),
        )

        fetch.run("test")
        advanceUntilIdle()
        delay(100)

        assertEquals(42, dataBundle.state.value)
        assertFalse(fetch.loadingState.value)
        assertNull(fetch.errorState.value)
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
            scope = backgroundScope,
            options = options,
            requestFn = { 42 },
            pluginImpls = arrayOf(plugin1, faultyPlugin, plugin3),
        )

        // 使用 run() 而不是 _runAsync()
        fetch.run("test")
        advanceUntilIdle()
        delay(100)

        assertTrue(events.contains("plugin1:onSuccess"))
        assertTrue(events.contains("plugin3:onSuccess"))
        assertFalse(fetch.loadingState.value)
        assertEquals(42, fetch.dataState.value)
        assertNull(fetch.errorState.value)
    }

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
            scope = backgroundScope,
            options = options,
            requestFn = { error("should not be called") },
            pluginImpls = arrayOf(plugin),
        )

        // 使用 run() 而不是 _runAsync()
        fetch.run(null)
        advanceUntilIdle()
        delay(100)

        assertFalse(fetch.loadingState.value)
        assertNull(fetch.dataState.value)
        assertIs<IllegalStateException>(fetch.errorState.value)
        assertTrue(events.contains("plugin:onError"))
        assertTrue(events.contains("plugin:onFinally"))
        assertTrue(events.contains("options:onError"))
    }

    @Test
    fun null_params_should_clear_previous_data() = runTest {
        val options = UseRequestOptions.optionOf<String, Int> {
            defaultParams = null
        }

        val (fetch, dataBundle, _) = createFetch(
            scope = backgroundScope,
            options = options,
            requestFn = { 42 },
        )

        // 先成功请求一次
        // 使用 run() 而不是 _runAsync()
        fetch.run("test")
        advanceUntilIdle()
        delay(100)
        assertEquals(42, dataBundle.state.value)

        // 然后用 null 参数请求
        fetch.run(null)
        advanceUntilIdle()
        delay(100)

        assertNull(dataBundle.state.value)
        assertIs<IllegalStateException>(fetch.errorState.value)
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
            scope = backgroundScope,
            options = options,
            requestFn = { throw originalError },
        )

        // 使用 run() 而不是 _runAsync()
        fetch.run("test")
        advanceUntilIdle()
        delay(100)

        assertEquals(originalError.message, fetch.errorState.value?.message)
        assertFalse(fetch.loadingState.value)
        assertNull(fetch.dataState.value)
    }

    @Test
    fun refresh_without_previous_request_keeps_state_unchanged() = runTest {
        val options = UseRequestOptions.optionOf<String, Int> {}
        val (fetch, dataBundle, _) = createFetch(
            scope = backgroundScope,
            options = options,
            requestFn = { 42 },
        )

        fetch.refresh()

        assertNull(dataBundle.state.value)
        assertFalse(fetch.loadingState.value)
        assertNull(fetch.errorState.value)
    }

    @Test
    fun mutate_with_exception_should_not_corrupt_state() = runTest {
        val options = UseRequestOptions.optionOf<String, Int> {}
        val (fetch, dataBundle, _) = createFetch(
            scope = backgroundScope,
            options = options,
            requestFn = { 42 },
        )

        // 使用 run() 而不是 _runAsync()
        fetch.run("test")
        advanceUntilIdle()
        delay(100)
        assertEquals(42, dataBundle.state.value)

        fetch.mutate { throw RuntimeException("mutate error") }

        assertEquals(42, dataBundle.state.value)
        assertFalse(fetch.loadingState.value)
    }
}
