package xyz.junerver.compose.hooks.test

import androidx.compose.runtime.State
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import xyz.junerver.compose.hooks.userequest.Fetch
import xyz.junerver.compose.hooks.userequest.FetchState
import xyz.junerver.compose.hooks.userequest.PluginLifecycle
import xyz.junerver.compose.hooks.userequest.UseRequestOptions

/*
  Description: Fetch parallel state transition tests
  Author: Junerver
  Date: 2026/7/15
  Email: junerver@gmail.com
  Version: v1.0
*/
class FetchParallelTransitionTest {
    private class AtomicState<T>(initialValue: T) : State<T> {
        private val reference = AtomicReference(initialValue)

        override val value: T
            get() = reference.get()

        fun set(value: T) {
            reference.set(value)
        }
    }

    @Test
    fun newer_start_cannot_interleave_with_older_terminal_state_commit() {
        val serviceAStarted = CountDownLatch(1)
        val releaseServiceA = CountDownLatch(1)
        val aTerminalEntered = CountDownLatch(1)
        val releaseATerminal = CountDownLatch(1)
        val aFinallyCompleted = CountDownLatch(1)
        val bBeforeCompleted = CountDownLatch(1)
        val bStartCommitted = CountDownLatch(1)
        val shouldBlockNextTerminal = AtomicBoolean(true)
        val loadingTrueCount = AtomicInteger(0)
        val data = AtomicState<Int?>(null)
        val loading = AtomicState(false)
        val error = AtomicState<Throwable?>(null)
        val executionScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val options = UseRequestOptions.optionOf<String, Int> {
            onFinally = { params, _, _ ->
                if (params == "A") aFinallyCompleted.countDown()
            }
        }
        val lifecycle = object : PluginLifecycle<String, Int>() {
            override val onBefore = { params: String ->
                if (params == "B") bBeforeCompleted.countDown()
                null
            }
        }
        val fetch = Fetch(options).apply {
            fetchState = FetchState()
            dataState = data
            setData = data::set
            loadingState = loading
            setLoading = { value ->
                if (value) {
                    if (loadingTrueCount.incrementAndGet() == 2) bStartCommitted.countDown()
                } else if (shouldBlockNextTerminal.compareAndSet(true, false)) {
                    aTerminalEntered.countDown()
                    assertTrue(releaseATerminal.await(5, TimeUnit.SECONDS))
                }
                loading.set(value)
            }
            errorState = error
            setError = error::set
            requestFn = { params ->
                if (params == "A") {
                    serviceAStarted.countDown()
                    assertTrue(releaseServiceA.await(5, TimeUnit.SECONDS))
                    1
                } else {
                    awaitCancellation()
                }
            }
            pluginImpls = arrayOf(lifecycle)
            scope = executionScope
        }

        try {
            fetch.run("A")
            assertTrue(serviceAStarted.await(5, TimeUnit.SECONDS))

            releaseServiceA.countDown()
            assertTrue(aTerminalEntered.await(5, TimeUnit.SECONDS))

            fetch.run("B")
            assertTrue(bBeforeCompleted.await(5, TimeUnit.SECONDS))
            val bCommittedBeforeAReleased = bStartCommitted.await(250, TimeUnit.MILLISECONDS)

            releaseATerminal.countDown()
            assertTrue(bStartCommitted.await(5, TimeUnit.SECONDS))
            assertTrue(aFinallyCompleted.await(5, TimeUnit.SECONDS))

            assertFalse(bCommittedBeforeAReleased, "B must wait until A's terminal state commit is complete")
            assertTrue(loading.value, "B's loading=true start state must remain visible")
            assertEquals("B", fetch.fetchState.params)
        } finally {
            releaseServiceA.countDown()
            releaseATerminal.countDown()
            fetch.cancel()
            executionScope.cancel()
        }
    }
}
