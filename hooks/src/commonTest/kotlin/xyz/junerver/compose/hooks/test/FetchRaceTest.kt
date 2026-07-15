@file:OptIn(ExperimentalCoroutinesApi::class)

package xyz.junerver.compose.hooks.test

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import xyz.junerver.compose.hooks.userequest.Fetch
import xyz.junerver.compose.hooks.userequest.FetchState
import xyz.junerver.compose.hooks.userequest.OnBeforeReturn
import xyz.junerver.compose.hooks.userequest.PluginLifecycle
import xyz.junerver.compose.hooks.userequest.UseRequestOptions

/*
  Description: Fetch 请求竞态回归测试
  Author: Junerver
  Date: 2026/7/15
  Email: junerver@gmail.com
  Version: v1.0
*/
class FetchRaceTest {
    private data class Fixture(
        val fetch: Fetch<String, Int>,
        val data: MutableState<Int?>,
        val loading: MutableState<Boolean>,
        val error: MutableState<Throwable?>,
    )

    private data class FinalEvent(
        val params: String?,
        val data: Int?,
        val errorMessage: String?,
    )

    private fun TestScope.createFixture(
        options: UseRequestOptions<String, Int> = UseRequestOptions.optionOf {},
        pluginImpls: Array<PluginLifecycle<String, Int>> = emptyArray(),
        executionScope: CoroutineScope? = null,
        requestFn: suspend (String) -> Int,
    ): Fixture {
        val data = mutableStateOf<Int?>(null)
        val loading = mutableStateOf(false)
        val error = mutableStateOf<Throwable?>(null)
        val resolvedScope = executionScope ?: backgroundScope
        val fetch = Fetch(options).apply {
            fetchState = FetchState()
            dataState = data
            setData = { data.value = it }
            loadingState = loading
            setLoading = { loading.value = it }
            errorState = error
            setError = { error.value = it }
            this.requestFn = requestFn
            this.pluginImpls = pluginImpls
            scope = resolvedScope
        }
        return Fixture(fetch, data, loading, error)
    }

    @Test
    fun concurrentRunsPassEachInvocationItsOwnParams() = runTest {
        val received = mutableListOf<String>()
        val fixture = createFixture(requestFn = { params ->
            received += params
            params.length
        })

        try {
            fixture.fetch.run("a")
            fixture.fetch.run("bb")
            runCurrent()

            assertEquals(listOf("a", "bb"), received)
            assertEquals(2, fixture.data.value)
            assertFalse(fixture.loading.value)
        } finally {
            fixture.fetch.cancel()
            runCurrent()
        }
    }

    @Test
    fun supersededRequestFinallyKeepsItsOwnContext() = runTest {
        val started = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val events = mutableListOf<FinalEvent>()
        val pluginEvents = mutableListOf<FinalEvent>()
        val options = UseRequestOptions.optionOf<String, Int> {
            onFinally = { params, data, error ->
                events += FinalEvent(params, data, error?.message)
            }
        }
        val lifecycle = object : PluginLifecycle<String, Int>() {
            override val onFinally: ((String, Int?, Throwable?) -> Unit)? = { params, data, error ->
                pluginEvents += FinalEvent(params, data, error?.message)
            }
        }
        val fixture = createFixture(options = options, pluginImpls = arrayOf(lifecycle), requestFn = { params ->
            if (params == "slow") {
                started.complete(Unit)
                release.await()
            }
            params.length
        })

        try {
            fixture.fetch.run("slow")
            runCurrent()
            assertTrue(started.isCompleted)

            fixture.fetch.run("x")
            runCurrent()
            release.complete(Unit)
            runCurrent()

            assertEquals(2, events.size)
            assertEquals(
                setOf(
                    FinalEvent("slow", 4, null),
                    FinalEvent("x", 1, null),
                ),
                events.toSet(),
            )
            assertEquals(listOf(FinalEvent("x", 1, null)), pluginEvents)
            assertEquals(1, fixture.data.value)
            assertFalse(fixture.loading.value)
        } finally {
            release.complete(Unit)
            fixture.fetch.cancel()
            runCurrent()
        }
    }

    @Test
    fun invalidParamsInvalidateOlderInflightRequest() = runTest {
        val started = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val options = UseRequestOptions.optionOf<String, Int> {
            defaultParams = null
            onError = { _, _ -> }
        }
        val fixture = createFixture(options = options, requestFn = {
            started.complete(Unit)
            release.await()
            7
        })

        try {
            fixture.fetch.run("valid")
            runCurrent()
            assertTrue(started.isCompleted)

            fixture.fetch.run(null)
            runCurrent()
            val invalidError = assertIs<IllegalStateException>(fixture.error.value)
            assertNull(fixture.data.value)
            assertFalse(fixture.loading.value)

            release.complete(Unit)
            runCurrent()

            assertSame(invalidError, fixture.error.value)
            assertNull(fixture.data.value)
            assertFalse(fixture.loading.value)
        } finally {
            release.complete(Unit)
            fixture.fetch.cancel()
            runCurrent()
        }
    }

    @Test
    fun stopNowDoesNotInvalidateOlderInflightRequest() = runTest {
        var stop = false
        val started = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val calls = mutableListOf<String>()
        val lifecycle = object : PluginLifecycle<String, Int>() {
            override val onBefore: ((String) -> OnBeforeReturn<String, Int>?)? = {
                if (stop) OnBeforeReturn(stopNow = true) else null
            }
        }
        val fixture = createFixture(pluginImpls = arrayOf(lifecycle), requestFn = { params ->
            calls += params
            started.complete(Unit)
            release.await()
            params.length
        })

        try {
            fixture.fetch.run("slow")
            runCurrent()
            assertTrue(started.isCompleted)

            stop = true
            fixture.fetch.run("blocked")
            runCurrent()
            assertEquals(listOf("slow"), calls)

            release.complete(Unit)
            runCurrent()

            assertEquals(4, fixture.data.value)
            assertFalse(fixture.loading.value)
            assertNull(fixture.error.value)
        } finally {
            release.complete(Unit)
            fixture.fetch.cancel()
            runCurrent()
        }
    }

    @Test
    fun completedRequestsAreRemovedFromActiveRegistry() = runTest {
        val fixture = createFixture(requestFn = { it.length })

        try {
            repeat(32) { index ->
                fixture.fetch.run("p$index")
            }
            runCurrent()

            assertEquals(0, fixture.fetch.activeRequestJobCount)
        } finally {
            fixture.fetch.cancel()
            runCurrent()
        }
    }

    @Test
    fun reentrantCancelCannotMissJobRegistration() = runTest {
        val immediateScope = CoroutineScope(
            backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler),
        )
        val exited = CompletableDeferred<Unit>()
        lateinit var fetch: Fetch<String, Int>
        val fixture = createFixture(executionScope = immediateScope, requestFn = {
            try {
                fetch.cancel()
                awaitCancellation()
            } finally {
                exited.complete(Unit)
            }
        })
        fetch = fixture.fetch

        try {
            fixture.fetch.run("reentrant")
            runCurrent()

            assertTrue(exited.isCompleted)
            assertEquals(0, fixture.fetch.activeRequestJobCount)
            assertFalse(fixture.loading.value)
        } finally {
            fixture.fetch.cancel()
            runCurrent()
        }
    }
}
