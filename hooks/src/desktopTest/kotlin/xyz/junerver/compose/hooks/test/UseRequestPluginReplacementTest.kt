/*
  Description: useRequest custom plugin replacement boundary tests
  Author: Junerver
  Date: 2026/07/15
  Email: junerver@gmail.com
  Version: v1.0
*/

package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import xyz.junerver.compose.hooks.userequest.ComposablePluginGenFn
import xyz.junerver.compose.hooks.userequest.Fetch
import xyz.junerver.compose.hooks.userequest.Plugin
import xyz.junerver.compose.hooks.userequest.PluginLifecycle
import xyz.junerver.compose.hooks.userequest.RequestHolder
import xyz.junerver.compose.hooks.userequest.UseRequestOptions
import xyz.junerver.compose.hooks.userequest.useRequest

class UseRequestPluginReplacementTest {
    private fun trackingPlugin(
        label: String,
        events: MutableList<String>,
        cancelCount: AtomicInteger,
    ): Plugin<String, Int> = object : Plugin<String, Int>() {
        override val invoke: (
            Fetch<String, Int>,
            UseRequestOptions<String, Int>,
        ) -> PluginLifecycle<String, Int> = { fetch, options ->
            initFetch(fetch, options)
            object : PluginLifecycle<String, Int>() {
                override val onSuccess: ((Int, String) -> Unit)? = { _, params ->
                    events += "$label:success:$params"
                }

                override val onFinally: ((String, Int?, Throwable?) -> Unit)? = { params, _, _ ->
                    events += "$label:finally:$params"
                }

                override val onCancel: (() -> Unit)? = {
                    cancelCount.incrementAndGet()
                    events += "$label:cancel"
                }
            }
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun replacingPluginCancelsInflightRequestWithoutRetargetingItsLifecycle() = runComposeUiTest {
        val usePluginB = mutableStateOf(false)
        val started = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val slowExited = CompletableDeferred<Unit>()
        val seedError = IllegalStateException("seed error")
        val events = CopyOnWriteArrayList<String>()
        val pluginACancelCount = AtomicInteger(0)
        val pluginBCancelCount = AtomicInteger(0)
        var exposedError: Throwable? = null
        val pluginA: ComposablePluginGenFn<String, Int> = {
            trackingPlugin("A", events, pluginACancelCount)
        }
        val pluginB: ComposablePluginGenFn<String, Int> = {
            trackingPlugin("B", events, pluginBCancelCount)
        }
        lateinit var holder: RequestHolder<String, Int>

        setContent {
            holder = useRequest(
                requestFn = { params: String ->
                    when (params) {
                        "fail" -> throw seedError
                        "slow" -> {
                            started.complete(Unit)
                            try {
                                release.await()
                                params.length
                            } finally {
                                slowExited.complete(Unit)
                            }
                        }

                        else -> params.length
                    }
                },
                optionsOf = {
                    manual = true
                    onError = { _, _ -> }
                },
                plugins = arrayOf(if (usePluginB.value) pluginB else pluginA),
            )
            Text("data=${holder.data.value} loading=${holder.isLoading.value} error=${holder.error.value?.message}")
        }

        waitForIdle()
        val initialHolder = holder
        val initialDataState = holder.data
        val initialLoadingState = holder.isLoading
        val initialErrorState = holder.error
        val initialRequest = holder.request
        val initialMutate = holder.mutate
        val initialRefresh = holder.refresh
        val initialCancel = holder.cancel

        try {
            runOnIdle { initialRequest("fail") }
            waitForIdle()
            runOnIdle {
                assertEquals(seedError.message, holder.error.value?.message, "Expected the seed failure before replacing the plugin")
                exposedError = holder.error.value
                assertFalse(holder.isLoading.value)
                assertNull(holder.data.value)
            }

            runOnIdle { initialRequest("slow") }
            waitForIdle()
            runOnIdle {
                assertTrue(started.isCompleted, "Expected the slow request to start under plugin A")
                assertTrue(holder.isLoading.value)
                assertSame(exposedError, holder.error.value)
            }

            runOnIdle { usePluginB.value = true }
            waitForIdle()

            runOnIdle {
                assertSame(initialHolder, holder)
                assertSame(initialDataState, holder.data)
                assertSame(initialLoadingState, holder.isLoading)
                assertSame(initialErrorState, holder.error)
                assertSame(initialRequest, holder.request)
                assertSame(initialMutate, holder.mutate)
                assertSame(initialRefresh, holder.refresh)
                assertSame(initialCancel, holder.cancel)
                assertSame(exposedError, holder.error.value, "Plugin replacement must preserve the existing request error")
                assertEquals(1, pluginACancelCount.get(), "Plugin A lifecycle must be cancelled exactly once")
                assertEquals(0, pluginBCancelCount.get())
                assertTrue(
                    slowExited.isCompleted,
                    "Replacing plugin A with B must cancel the in-flight request owned by A",
                )
                assertFalse(holder.isLoading.value)
                assertNull(holder.data.value)
                assertFalse(events.contains("A:success:slow"))
                assertFalse(events.contains("A:finally:slow"))
                assertFalse(events.contains("B:success:slow"))
                assertFalse(events.contains("B:finally:slow"))
            }

            runOnIdle { initialRequest("bb") }
            waitForIdle()
            runOnIdle {
                assertEquals(2, holder.data.value, "Expected the next request to complete through plugin B")
                assertFalse(holder.isLoading.value)
                assertNull(holder.error.value)
                assertEquals(1, events.count { it == "B:success:bb" })
                assertEquals(1, events.count { it == "B:finally:bb" })
                assertFalse(events.contains("A:success:bb"))
                assertFalse(events.contains("A:finally:bb"))
            }
        } finally {
            release.complete(Unit)
            runOnIdle { holder.cancel() }
            waitForIdle()
        }
    }
}
