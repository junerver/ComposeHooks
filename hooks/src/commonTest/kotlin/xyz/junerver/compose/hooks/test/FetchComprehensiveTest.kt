package xyz.junerver.compose.hooks.test

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import xyz.junerver.compose.hooks.SuspendNormalFunction
import xyz.junerver.compose.hooks.userequest.Fetch
import xyz.junerver.compose.hooks.userequest.FetchState
import xyz.junerver.compose.hooks.userequest.OnBeforeReturn
import xyz.junerver.compose.hooks.userequest.OnRequestReturn
import xyz.junerver.compose.hooks.userequest.PluginLifecycle
import xyz.junerver.compose.hooks.userequest.UseRequestOptions

/*
  Description: Fetch 核心行为单元测试
  Author: Junerver
  Date: 2026/1/9
  Email: junerver@gmail.com
  Version: v1.0
*/
class FetchComprehensiveTest {
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

    @Test
    fun runAsync_success_updatesStates_and_calls_callbacks_in_order() = runTest {
        val events = mutableListOf<String>()
        val options = UseRequestOptions.optionOf<String, Int> {
            onBefore = { events += "options:onBefore" }
            onSuccess = { _, _ -> events += "options:onSuccess" }
            onError = { _, _ -> events += "options:onError" }
            onFinally = { _, _, _ -> events += "options:onFinally" }
        }

        val plugin = object : PluginLifecycle<String, Int>() {
            override val onBefore: ((String) -> OnBeforeReturn<String, Int>?)? = { _: String ->
                events += "plugin:onBefore"
                null
            }

            override val onRequest: ((SuspendNormalFunction<String, Int>, String) -> OnRequestReturn<Int>?)? =
                { _: SuspendNormalFunction<String, Int>, _: String ->
                    events += "plugin:onRequest"
                    null
                }

            override val onSuccess: ((Int, String) -> Unit)? = { _: Int, _: String ->
                events += "plugin:onSuccess"
            }

            override val onError: ((Throwable, String) -> Unit)? = { _: Throwable, _: String ->
                events += "plugin:onError"
            }

            override val onFinally: ((String, Int?, Throwable?) -> Unit)? = { _: String, _: Int?, _: Throwable? ->
                events += "plugin:onFinally"
            }
        }

        val (fetch, _, loadingBundle) = createFetch(
            options = options,
            requestFn = { params ->
                assertEquals("p", params)
                42
            },
            pluginImpls = arrayOf(plugin),
        )

        val loadingTransitions = mutableListOf<Boolean>()
        val originalSetLoading = loadingBundle.set
        fetch.setLoading = { loading ->
            loadingTransitions += loading
            originalSetLoading(loading)
        }

        fetch._runAsync("p")

        assertEquals(listOf(true, false), loadingTransitions)
        assertFalse(fetch.loadingState.value)
        assertEquals(42, fetch.dataState.value)
        assertNull(fetch.errorState.value)
        assertEquals(
            listOf(
                "plugin:onBefore",
                "options:onBefore",
                "plugin:onRequest",
                "options:onSuccess",
                "plugin:onSuccess",
                "options:onFinally",
                "plugin:onFinally",
            ),
            events,
        )
    }

    @Test
    fun runAsync_error_setsError_and_calls_callbacks_in_order() = runTest {
        val events = mutableListOf<String>()
        val options = UseRequestOptions.optionOf<String, Int> {
            onBefore = { events += "options:onBefore" }
            onSuccess = { _, _ -> events += "options:onSuccess" }
            onError = { _, _ -> events += "options:onError" }
            onFinally = { _, _, _ -> events += "options:onFinally" }
        }

        val plugin = object : PluginLifecycle<String, Int>() {
            override val onBefore: ((String) -> OnBeforeReturn<String, Int>?)? = { _: String ->
                events += "plugin:onBefore"
                null
            }

            override val onRequest: ((SuspendNormalFunction<String, Int>, String) -> OnRequestReturn<Int>?)? =
                { _: SuspendNormalFunction<String, Int>, _: String ->
                    events += "plugin:onRequest"
                    null
                }

            override val onError: ((Throwable, String) -> Unit)? = { _: Throwable, _: String ->
                events += "plugin:onError"
            }

            override val onFinally: ((String, Int?, Throwable?) -> Unit)? = { _: String, _: Int?, _: Throwable? ->
                events += "plugin:onFinally"
            }
        }

        val failure = IllegalArgumentException("boom")
        val (fetch, _, _) = createFetch(
            options = options,
            requestFn = { throw failure },
            pluginImpls = arrayOf(plugin),
        )

        fetch._runAsync("p")

        assertFalse(fetch.loadingState.value)
        assertNull(fetch.dataState.value)
        assertEquals(failure.message, fetch.errorState.value?.message)
        assertEquals(
            listOf(
                "plugin:onBefore",
                "options:onBefore",
                "plugin:onRequest",
                "options:onError",
                "plugin:onError",
                "options:onFinally",
                "plugin:onFinally",
            ),
            events,
        )
    }

    @Test
    fun cancel_prevents_late_result_overwrite() = runTest {
        val options = UseRequestOptions.optionOf<String, Int> {
            onError = { _, _ -> }
        }
        var requestCount = 0
        val (fetch, _, _) = createFetch(
            options = options,
            requestFn = {
                requestCount += 1
                delay(1_000)
                7
            },
        )

        val job = launch {
            fetch._runAsync("p")
        }
        runCurrent()
        assertTrue(fetch.loadingState.value)

        fetch.cancel()
        assertFalse(fetch.loadingState.value)

        advanceTimeBy(1_000)
        runCurrent()
        job.join()

        assertEquals(1, requestCount)
        assertNull(fetch.dataState.value)
        assertNull(fetch.errorState.value)
    }

    @Test
    fun refreshAsync_uses_last_params() = runTest {
        val options = UseRequestOptions.optionOf<String, Int> {
            onError = { _, _ -> }
        }
        val receivedParams = mutableListOf<String>()
        val (fetch, _, _) = createFetch(
            options = options,
            requestFn = { params ->
                receivedParams += params
                if (receivedParams.size == 1) 1 else 2
            },
        )

        fetch._runAsync("a")
        assertEquals(listOf("a"), receivedParams)
        assertEquals(1, fetch.dataState.value)

        fetch.refreshAsync()
        assertEquals(listOf("a", "a"), receivedParams)
        assertEquals(2, fetch.dataState.value)
    }

    @Test
    fun mutate_updates_data_and_triggers_onMutate() = runTest {
        val events = mutableListOf<String>()
        val options = UseRequestOptions.optionOf<String, Int> {
            onError = { _, _ -> }
        }

        val plugin = object : PluginLifecycle<String, Int>() {
            override val onMutate: ((Int) -> Unit)? = { data: Int ->
                events += "plugin:onMutate:$data"
            }
        }

        val (fetch, _, _) = createFetch(
            options = options,
            requestFn = { 40 },
            pluginImpls = arrayOf(plugin),
        )

        fetch._runAsync("p")
        assertEquals(40, fetch.dataState.value)

        fetch.mutate { current ->
            (current ?: 0) + 2
        }
        assertEquals(42, fetch.dataState.value)
        assertEquals(listOf("plugin:onMutate:42"), events)
    }

    @Test
    fun onBefore_returnNow_short_circuits_requestFn_and_option_callbacks() = runTest {
        val events = mutableListOf<String>()
        val options = UseRequestOptions.optionOf<String, Int> {
            onBefore = { events += "options:onBefore" }
            onSuccess = { _, _ -> events += "options:onSuccess" }
            onError = { _, _ -> events += "options:onError" }
            onFinally = { _, _, _ -> events += "options:onFinally" }
        }

        var requestCount = 0
        val plugin = object : PluginLifecycle<String, Int>() {
            override val onBefore: ((String) -> OnBeforeReturn<String, Int>?)? = { _: String ->
                OnBeforeReturn(
                    loading = false,
                    data = 99,
                    returnNow = true,
                )
            }

            override val onRequest: ((SuspendNormalFunction<String, Int>, String) -> OnRequestReturn<Int>?)? =
                { _: SuspendNormalFunction<String, Int>, _: String ->
                    events += "plugin:onRequest"
                    null
                }
        }

        val (fetch, _, _) = createFetch(
            options = options,
            requestFn = {
                requestCount += 1
                1
            },
            pluginImpls = arrayOf(plugin),
        )

        fetch._runAsync("p")

        assertEquals(0, requestCount)
        assertFalse(fetch.loadingState.value)
        assertEquals(99, fetch.dataState.value)
        assertNull(fetch.errorState.value)
        assertEquals(emptyList(), events)
    }

    @Test
    fun null_params_and_null_defaultParams_sets_error_instead_of_crashing() = runTest {
        val events = mutableListOf<String>()
        val options = UseRequestOptions.optionOf<String, Int> {
            defaultParams = null
            onError = { _, _ -> events += "options:onError" }
            onFinally = { _, _, _ -> events += "options:onFinally" }
        }

        val (fetch, _, _) = createFetch(
            options = options,
            requestFn = { error("should not be called") },
        )

        fetch._runAsync(null)

        assertFalse(fetch.loadingState.value)
        assertNull(fetch.dataState.value)
        assertIs<IllegalStateException>(fetch.errorState.value)
        assertEquals(listOf("options:onError", "options:onFinally"), events)
    }
}
