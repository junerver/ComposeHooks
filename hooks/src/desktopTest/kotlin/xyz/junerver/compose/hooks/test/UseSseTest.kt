package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.flow.flow
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
}



