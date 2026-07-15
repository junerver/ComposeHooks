/*
  Description: useRequest polling lifecycle regression tests
  Author: Junerver
  Date: 2026/7/15
  Email: junerver@gmail.com
  Version: v1.0
*/

package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.milliseconds
import xyz.junerver.compose.hooks.userequest.EmptyPlugin
import xyz.junerver.compose.hooks.userequest.RequestHolder
import xyz.junerver.compose.hooks.userequest.UseRequestOptions
import xyz.junerver.compose.hooks.userequest.plugins.usePollingPlugin
import xyz.junerver.compose.hooks.userequest.useRequest

private class PollingLifecycleOwner : LifecycleOwner {
    private val registry = LifecycleRegistry.createUnsafe(this)

    override val lifecycle: Lifecycle = registry

    fun resumeInitially() {
        registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun pause() {
        registry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    fun resume() {
        registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }
}

@Suppress("DEPRECATION")
class UseRequestPollingLifecycleTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun negativePollingIntervalKeepsPollingDisabled() = runComposeUiTest {
        lateinit var plugin: Any

        setContent {
            plugin = usePollingPlugin<String, Int>(
                UseRequestOptions.optionOf {
                    pollingInterval = (-1).milliseconds
                },
            )
        }

        waitForIdle()
        assertIs<EmptyPlugin<*, *>>(plugin)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun composingWhilePausedDoesNotPollAndResumesExactlyOnce() = runComposeUiTest {
        mainClock.autoAdvance = false
        val owner = PollingLifecycleOwner().apply {
            resumeInitially()
            pause()
        }
        val calls = AtomicInteger(0)
        lateinit var holder: RequestHolder<String, Int>

        setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                holder = useRequest(
                    requestFn = { _: String -> calls.incrementAndGet() },
                    optionsOf = {
                        manual = true
                        pollingInterval = 100.milliseconds
                        pollingWhenHidden = false
                    },
                )
                Text("calls=${calls.get()} data=${holder.data.value}")
            }
        }

        waitForIdle()
        runOnIdle { holder.request("paused") }
        mainClock.advanceTimeBy(150)
        waitForIdle()
        assertEquals(1, calls.get())

        runOnIdle { owner.resume() }
        mainClock.advanceTimeBy(1)
        waitForIdle()
        assertEquals(2, calls.get())

        mainClock.advanceTimeBy(100)
        waitForIdle()
        assertEquals(3, calls.get())
        runOnIdle { holder.cancel() }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun replacingPollingPluginKeepsPauseBoundToLatestTimer() = runComposeUiTest {
        mainClock.autoAdvance = false
        val owner = PollingLifecycleOwner().apply { resumeInitially() }
        val interval = mutableStateOf(100.milliseconds)
        val calls = AtomicInteger(0)
        lateinit var holder: RequestHolder<String, Int>

        setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                holder = useRequest(
                    requestFn = { _: String -> calls.incrementAndGet() },
                    optionsOf = {
                        manual = true
                        pollingInterval = interval.value
                        pollingWhenHidden = false
                    },
                )
                Text("calls=${calls.get()} data=${holder.data.value}")
            }
        }

        waitForIdle()
        runOnIdle { holder.request("initial") }
        mainClock.advanceTimeBy(1)
        waitForIdle()
        assertEquals(1, calls.get())

        runOnIdle { interval.value = 200.milliseconds }
        mainClock.advanceTimeByFrame()
        waitForIdle()
        runOnIdle { holder.request("replacement") }
        mainClock.advanceTimeBy(1)
        waitForIdle()
        assertEquals(2, calls.get())

        runOnIdle { owner.pause() }
        mainClock.advanceTimeBy(250)
        waitForIdle()
        assertEquals(2, calls.get())

        runOnIdle { owner.resume() }
        mainClock.advanceTimeBy(1)
        waitForIdle()
        assertEquals(3, calls.get())
        runOnIdle { holder.cancel() }
    }
}
