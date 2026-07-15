package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withContext
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.usses.SseHolder
import xyz.junerver.compose.hooks.usses.useSse

/*
  Description: useSse 组合行为桌面测试
  Author: Junerver
  Date: 2026/6/3
  Email: junerver@gmail.com
  Version: v1.1
*/
@Suppress("DEPRECATION")
class UseSseTest {
    private fun waitForCondition(maxAttempts: Int = 80, delayMs: Long = 20, condition: () -> Boolean): Boolean {
        repeat(maxAttempts) {
            if (condition()) return true
            Thread.sleep(delayMs)
        }
        return false
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun autoRun_streams_single_event() = runComposeUiTest {
        val lastData = AtomicReference<String?>(null)
        setContent {
            val holder = useSse(
                streamFn = { params: String ->
                    flow {
                        emit("$params-done")
                    }
                },
                optionsOf = {
                    defaultParams = "hello"
                    onEvent = { lastData.set(it) }
                },
            )

            Text("data=${holder.data.value ?: "null"}")
        }

        waitForIdle()
        assertEquals("hello-done", lastData.get())
        onNodeWithText("data=hello-done").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun manual_mode_does_not_auto_start() = runComposeUiTest {
        val eventCount = AtomicInteger(0)
        setContent {
            val holder = useSse(
                streamFn = { _: String ->
                    flow {
                        emit("event")
                    }
                },
                optionsOf = {
                    manual = true
                    defaultParams = "test"
                    onEvent = { eventCount.incrementAndGet() }
                },
            )

            Text("events=${eventCount.get()} data=${holder.data.value ?: "null"}")
        }

        waitForIdle()
        assertEquals(0, eventCount.get())
        onNodeWithText("events=0 data=null").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun manual_send_starts_stream() = runComposeUiTest {
        val lastData = AtomicReference<String?>(null)
        var holderRef: SseHolder<String, String>? = null
        setContent {
            val holder = useSse(
                streamFn = { params: String ->
                    flow {
                        emit("result-$params")
                    }
                },
                optionsOf = {
                    manual = true
                    onEvent = { lastData.set(it) }
                },
            )
            holderRef = holder

            Text("data=${holder.data.value ?: "null"}")
        }

        waitForIdle()
        assertNull(lastData.get())
        // Manually send
        holderRef!!.send("test-param")
        waitForIdle()
        assertEquals("result-test-param", lastData.get())
        onNodeWithText("data=result-test-param").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun ready_false_blocks_auto_start() = runComposeUiTest {
        val eventCount = AtomicInteger(0)
        setContent {
            val holder = useSse(
                streamFn = { _: String ->
                    flow {
                        emit("event")
                    }
                },
                optionsOf = {
                    ready = false
                    defaultParams = "test"
                    onEvent = { eventCount.incrementAndGet() }
                },
            )

            Text("events=${eventCount.get()} streaming=${holder.isStreaming.value}")
        }

        waitForIdle()
        assertEquals(0, eventCount.get())
        onNodeWithText("events=0 streaming=false").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun error_from_stream_is_captured() = runComposeUiTest {
        var errorMsg: String? = null
        setContent {
            val holder = useSse(
                streamFn = { _: String ->
                    flow<String> {
                        throw RuntimeException("stream error")
                    }
                },
                optionsOf = {
                    defaultParams = "test"
                    onError = { e, _ -> errorMsg = e.message }
                },
            )

            Text("error=${holder.error.value?.message ?: "null"} streaming=${holder.isStreaming.value}")
        }

        waitForIdle()
        assertEquals("stream error", errorMsg)
        onNodeWithText("error=stream error streaming=false").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun cancel_stops_active_stream_and_clears_error() = runComposeUiTest {
        lateinit var holder: SseHolder<String, String>
        val received = mutableListOf<String>()
        setContent {
            holder = useSse(
                streamFn = { params: String ->
                    flow {
                        emit("$params-1")
                        delay(200)
                        emit("$params-2")
                    }
                },
                optionsOf = {
                    manual = true
                    onEvent = { received += it }
                },
            )

            Text("data=${holder.data.value ?: "null"} streaming=${holder.isStreaming.value} error=${holder.error.value?.message}")
        }

        waitForIdle()
        holder.send("demo")
        waitForIdle()
        val started = waitForCondition {
            holder.isStreaming.value &&
                holder.data.value == "demo-1" &&
                received == listOf("demo-1")
        }
        assertTrue(started, "Expected stream to start and emit the first event")

        holder.cancel()
        waitForIdle()
        assertFalse(holder.isStreaming.value)
        assertNull(holder.error.value)

        Thread.sleep(260)
        waitForIdle()
        assertEquals(listOf("demo-1"), received)
        assertEquals("demo-1", holder.data.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun refresh_restarts_stream_with_last_params() = runComposeUiTest {
        val starts = mutableListOf<String>()
        lateinit var holder: SseHolder<String, String>
        setContent {
            holder = useSse(
                streamFn = { params: String ->
                    flow {
                        starts += params
                        emit("$params-event")
                    }
                },
                optionsOf = {
                    manual = true
                },
            )

            Text("data=${holder.data.value ?: "null"} streaming=${holder.isStreaming.value}")
        }

        waitForIdle()
        holder.send("topic-a")
        waitForIdle()
        val firstCompleted = waitForCondition {
            starts == listOf("topic-a") &&
                holder.data.value == "topic-a-event" &&
                holder.isStreaming.value == false
        }
        assertTrue(firstCompleted, "Expected initial manual send to complete with provided params")

        holder.refresh()
        waitForIdle()
        val refreshed = waitForCondition {
            starts == listOf("topic-a", "topic-a") &&
                holder.data.value == "topic-a-event" &&
                holder.isStreaming.value == false
        }
        assertTrue(refreshed, "Expected refresh to restart stream with the last params")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun cancel_finalizes_active_stream_exactly_once() = runComposeUiTest {
        lateinit var holder: SseHolder<String, String>
        val started = CompletableDeferred<Unit>()
        val allowFinalize = CompletableDeferred<Unit>()
        val finallyCalls = CopyOnWriteArrayList<Pair<String?, Throwable?>>()

        setContent {
            holder = useSse(
                streamFn = { _: String ->
                    flow {
                        started.complete(Unit)
                        try {
                            awaitCancellation()
                        } finally {
                            withContext(NonCancellable) { allowFinalize.await() }
                        }
                    }
                },
                optionsOf = {
                    manual = true
                    onFinally = { params, error -> finallyCalls += params to error }
                },
            )
            Text("streaming=${holder.isStreaming.value}")
        }

        runOnIdle { holder.send("topic") }
        assertTrue(waitForCondition { started.isCompleted && holder.isStreaming.value })

        runOnIdle { holder.cancel() }
        allowFinalize.complete(Unit)
        waitForIdle()
        assertTrue(waitForCondition { finallyCalls.size == 1 })
        assertEquals(listOf("topic" to null), finallyCalls.toList())
        assertFalse(holder.isStreaming.value)
        assertNull(holder.error.value)

        runOnIdle { holder.cancel() }
        waitForIdle()
        assertEquals(1, finallyCalls.size)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun superseded_stream_finalizes_without_clearing_new_stream_state() = runComposeUiTest {
        lateinit var holder: SseHolder<String, String>
        val oldStarted = CompletableDeferred<Unit>()
        val newStarted = CompletableDeferred<Unit>()
        val allowOldFinalize = CompletableDeferred<Unit>()
        val finishNew = CompletableDeferred<Unit>()
        val finallyParams = CopyOnWriteArrayList<String?>()

        setContent {
            holder = useSse(
                streamFn = { params: String ->
                    flow {
                        if (params == "old") {
                            oldStarted.complete(Unit)
                            try {
                                awaitCancellation()
                            } finally {
                                withContext(NonCancellable) { allowOldFinalize.await() }
                            }
                        } else {
                            emit("new-event")
                            newStarted.complete(Unit)
                            finishNew.await()
                        }
                    }
                },
                optionsOf = {
                    manual = true
                    onFinally = { params, _ -> finallyParams += params }
                },
            )
            Text("data=${holder.data.value} streaming=${holder.isStreaming.value}")
        }

        runOnIdle { holder.send("old") }
        assertTrue(waitForCondition { oldStarted.isCompleted })
        runOnIdle { holder.send("new") }
        allowOldFinalize.complete(Unit)
        waitForIdle()

        assertTrue(waitForCondition { newStarted.isCompleted && finallyParams.contains("old") })
        assertEquals("new-event", holder.data.value)
        assertTrue(holder.isStreaming.value, "Old stream completion must not clear the new stream state")
        assertNull(holder.error.value)

        finishNew.complete(Unit)
        assertTrue(waitForCondition { finallyParams == listOf("old", "new") })
        assertFalse(holder.isStreaming.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun unmount_finalizes_active_stream() = runComposeUiTest {
        val mounted = mutableStateOf(true)
        val started = CompletableDeferred<Unit>()
        val allowFinalize = CompletableDeferred<Unit>()
        val finallyParams = CopyOnWriteArrayList<String?>()
        lateinit var holder: SseHolder<String, String>

        setContent {
            if (mounted.value) {
                holder = useSse(
                    streamFn = { _: String ->
                        flow {
                            started.complete(Unit)
                            try {
                                awaitCancellation()
                            } finally {
                                withContext(NonCancellable) { allowFinalize.await() }
                            }
                        }
                    },
                    optionsOf = {
                        manual = true
                        onFinally = { params, _ -> finallyParams += params }
                    },
                )
            }
        }

        runOnIdle { holder.send("mounted") }
        assertTrue(waitForCondition { started.isCompleted })
        runOnIdle { mounted.value = false }
        allowFinalize.complete(Unit)
        waitForIdle()

        assertTrue(waitForCondition { finallyParams == listOf("mounted") })
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun onBefore_reentrant_cancel_prevents_stream_start_and_finalizes() = runComposeUiTest {
        lateinit var holder: SseHolder<String, String>
        val streamCalls = AtomicInteger(0)
        val finallyParams = CopyOnWriteArrayList<String?>()

        setContent {
            holder = useSse(
                streamFn = { _: String ->
                    streamCalls.incrementAndGet()
                    flow { emit("unexpected") }
                },
                optionsOf = {
                    manual = true
                    onBefore = { holder.cancel() }
                    onFinally = { params, _ -> finallyParams += params }
                },
            )
            Text("streaming=${holder.isStreaming.value}")
        }

        runOnIdle { holder.send("cancel-me") }

        assertTrue(waitForCondition { finallyParams == listOf("cancel-me") })
        assertEquals(0, streamCalls.get())
        assertFalse(holder.isStreaming.value)
        assertNull(holder.data.value)
        assertNull(holder.error.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun controls_keep_identity_and_use_latest_stream_and_options() = runComposeUiTest {
        val revision = mutableStateOf(1)
        val callbacks = CopyOnWriteArrayList<String>()
        lateinit var holder: SseHolder<String, String>

        setContent {
            val currentRevision = revision.value
            holder = useSse(
                streamFn = { params: String -> flow { emit("stream-$currentRevision-$params") } },
                optionsOf = {
                    manual = true
                    defaultParams = "default-$currentRevision"
                    onEvent = { callbacks += "callback-$currentRevision-$it" }
                },
            )
        }

        waitForIdle()
        val firstSend = holder.send
        val firstCancel = holder.cancel
        val firstRefresh = holder.refresh

        runOnIdle { revision.value = 2 }
        waitForIdle()

        assertSame(firstSend, holder.send)
        assertSame(firstCancel, holder.cancel)
        assertSame(firstRefresh, holder.refresh)

        runOnIdle { firstSend(null) }
        assertTrue(waitForCondition { holder.data.value == "stream-2-default-2" })
        waitForIdle()
        assertEquals(listOf("callback-2-stream-2-default-2"), callbacks.toList())
        assertEquals("default-2", holder.params)

        runOnIdle { firstSend("topic") }
        assertTrue(waitForCondition { holder.data.value == "stream-2-topic" })
        runOnIdle { revision.value = 3 }
        runOnIdle { firstRefresh() }
        assertTrue(waitForCondition { holder.data.value == "stream-3-topic" })
        assertEquals("topic", holder.params)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun ready_manual_and_refreshDeps_control_auto_restart() = runComposeUiTest {
        val ready = mutableStateOf(false)
        val manual = mutableStateOf(false)
        val dependency = mutableStateOf(0)
        val starts = CopyOnWriteArrayList<String>()

        setContent {
            useSse(
                streamFn = { params: String ->
                    flow {
                        starts += params
                        emit(params)
                    }
                },
                optionsOf = {
                    this.ready = ready.value
                    this.manual = manual.value
                    defaultParams = "dep-${dependency.value}"
                    refreshDeps = arrayOf(dependency.value)
                },
            )
        }

        waitForIdle()
        assertTrue(starts.isEmpty())

        runOnIdle { ready.value = true }
        waitForIdle()
        assertTrue(waitForCondition { starts == listOf("dep-0") })

        runOnIdle { dependency.value = 1 }
        waitForIdle()
        assertTrue(waitForCondition { starts == listOf("dep-0", "dep-1") })

        runOnIdle { manual.value = true }
        runOnIdle { dependency.value = 2 }
        waitForIdle()
        assertEquals(listOf("dep-0", "dep-1"), starts.toList())

        runOnIdle { manual.value = false }
        waitForIdle()
        assertTrue(waitForCondition { starts == listOf("dep-0", "dep-1", "dep-2") })
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun nullable_params_are_forwarded_when_no_default_exists() = runComposeUiTest {
        lateinit var holder: SseHolder<String?, String>
        val receivedParam = AtomicReference<String?>("not-null")

        setContent {
            holder = useSse(
                streamFn = { params: String? ->
                    flow {
                        receivedParam.set(params)
                        emit(params ?: "null-param")
                    }
                },
                optionsOf = { manual = true },
            )
        }

        runOnIdle { holder.send(null) }
        assertTrue(waitForCondition { holder.data.value == "null-param" })
        assertNull(receivedParam.get())
        assertNull(holder.params)
        assertNull(holder.error.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun callback_failures_do_not_replace_stream_outcome() = runComposeUiTest {
        lateinit var eventHolder: SseHolder<String, Int>
        val eventFinallyError = AtomicReference<Throwable?>()

        setContent {
            eventHolder = useSse(
                streamFn = { _: String ->
                    flow {
                        emit(1)
                        emit(2)
                    }
                },
                optionsOf = {
                    manual = true
                    onEvent = { error("event callback") }
                    onFinally = { _, error ->
                        eventFinallyError.set(error)
                        error("finally callback")
                    }
                },
            )
        }

        runOnIdle { eventHolder.send("events") }
        assertTrue(waitForCondition { eventHolder.data.value == 2 && !eventHolder.isStreaming.value })
        assertNull(eventHolder.error.value)
        assertNull(eventFinallyError.get())

        lateinit var errorHolder: SseHolder<String, String>
        val sourceError = IllegalStateException("source failure")
        val finalError = AtomicReference<Throwable?>()
        setContent {
            errorHolder = useSse(
                streamFn = { _: String -> flow { throw sourceError } },
                optionsOf = {
                    manual = true
                    onError = { _, _ -> error("error callback") }
                    onFinally = { _, error -> finalError.set(error) }
                },
            )
        }

        runOnIdle { errorHolder.send("error") }
        assertTrue(waitForCondition { !errorHolder.isStreaming.value && errorHolder.error.value != null })
        assertSame(sourceError, errorHolder.error.value)
        assertSame(sourceError, assertNotNull(finalError.get()))
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun onBefore_failure_is_reported_as_stream_failure() = runComposeUiTest {
        lateinit var holder: SseHolder<String, String>
        val callbackErrors = CopyOnWriteArrayList<Throwable>()
        val finallyErrors = CopyOnWriteArrayList<Throwable?>()
        val streamCalls = AtomicInteger(0)

        setContent {
            holder = useSse(
                streamFn = { _: String ->
                    streamCalls.incrementAndGet()
                    flow { emit("unexpected") }
                },
                optionsOf = {
                    manual = true
                    onBefore = { error("before failure") }
                    onError = { error, _ -> callbackErrors += error }
                    onFinally = { _, error -> finallyErrors += error }
                },
            )
        }

        runOnIdle { holder.send("before") }
        assertTrue(waitForCondition { holder.error.value != null && !holder.isStreaming.value })

        assertEquals(0, streamCalls.get())
        assertEquals("before failure", holder.error.value?.message)
        assertEquals(listOf("before failure"), callbackErrors.map { it.message })
        assertEquals(listOf("before failure"), finallyErrors.map { it?.message })
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun active_stream_uses_latest_callbacks_after_recomposition() = runComposeUiTest {
        val revision = mutableStateOf(1)
        val events = Channel<String>(Channel.UNLIMITED)
        val eventCallbacks = CopyOnWriteArrayList<String>()
        val finallyCallbacks = CopyOnWriteArrayList<Int>()
        lateinit var holder: SseHolder<String, String>

        setContent {
            val currentRevision = revision.value
            holder = useSse(
                streamFn = { _: String -> events.receiveAsFlow() },
                optionsOf = {
                    manual = true
                    onEvent = { eventCallbacks += "$currentRevision:$it" }
                    onFinally = { _, _ -> finallyCallbacks += currentRevision }
                },
            )
        }

        runOnIdle { holder.send("topic") }
        assertTrue(waitForCondition { holder.isStreaming.value })
        events.trySend("first").getOrThrow()
        waitForIdle()
        assertTrue(waitForCondition { eventCallbacks == listOf("1:first") })

        runOnIdle { revision.value = 2 }
        waitForIdle()
        events.trySend("second").getOrThrow()
        waitForIdle()
        assertEquals(listOf("1:first", "2:second"), eventCallbacks.toList())

        runOnIdle { holder.cancel() }
        waitForIdle()
        assertEquals(listOf(2), finallyCallbacks.toList())
        events.close()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun cancelled_old_stream_cannot_publish_late_failure_into_new_stream() = runComposeUiTest {
        lateinit var holder: SseHolder<String, String>
        val oldStarted = CompletableDeferred<Unit>()
        val newStarted = CompletableDeferred<Unit>()
        val allowOldFailure = CompletableDeferred<Unit>()
        val finishNew = CompletableDeferred<Unit>()
        val staleError = IllegalStateException("stale failure")
        val errorCallbacks = CopyOnWriteArrayList<Throwable>()
        val finallyCalls = CopyOnWriteArrayList<Pair<String?, Throwable?>>()

        setContent {
            holder = useSse(
                streamFn = { params: String ->
                    flow {
                        if (params == "old") {
                            oldStarted.complete(Unit)
                            try {
                                awaitCancellation()
                            } finally {
                                withContext(NonCancellable) { allowOldFailure.await() }
                                throw staleError
                            }
                        } else {
                            emit("fresh")
                            newStarted.complete(Unit)
                            finishNew.await()
                        }
                    }
                },
                optionsOf = {
                    manual = true
                    onError = { error, _ -> errorCallbacks += error }
                    onFinally = { params, error -> finallyCalls += params to error }
                },
            )
        }

        runOnIdle { holder.send("old") }
        assertTrue(waitForCondition { oldStarted.isCompleted })
        runOnIdle { holder.send("new") }
        allowOldFailure.complete(Unit)
        waitForIdle()

        assertTrue(waitForCondition { newStarted.isCompleted && finallyCalls.any { it.first == "old" } })
        assertEquals("fresh", holder.data.value)
        assertTrue(holder.isStreaming.value)
        assertNull(holder.error.value)
        assertTrue(errorCallbacks.isEmpty())
        assertEquals(listOf("old" to null), finallyCalls.toList())

        finishNew.complete(Unit)
        waitForIdle()
        assertEquals(listOf("old" to null, "new" to null), finallyCalls.toList())
    }
}
