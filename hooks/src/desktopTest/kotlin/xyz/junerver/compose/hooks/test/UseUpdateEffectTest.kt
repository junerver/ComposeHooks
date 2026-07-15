package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import xyz.junerver.compose.hooks.SuspendAsyncFn
import xyz.junerver.compose.hooks.rememberUpdateEffect
import xyz.junerver.compose.hooks.useUpdateEffect

@Suppress("DEPRECATION")
class UseUpdateEffectTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun key_change_before_initial_effect_dispatch_is_not_skipped() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val recomposer = Recomposer(dispatcher)
        val composition = Composition(UnitApplier(), recomposer)
        val observedKey = AtomicInteger(-1)
        var key = 0
        val content: @Composable () -> Unit = {
            val currentKey = key
            useUpdateEffect(currentKey) {
                observedKey.set(currentKey)
            }
        }

        try {
            composition.setContent(content)
            key = 1
            composition.setContent(content)
            runCurrent()

            assertEquals(1, observedKey.get())
        } finally {
            composition.dispose()
            recomposer.cancel()
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun update_effect_skips_initial_mount_and_runs_on_dependency_change() = runComposeUiTest {
        val trigger = mutableStateOf(0)
        val effectRuns = mutableStateOf(0)

        setContent {
            useUpdateEffect(trigger.value) {
                effectRuns.value += 1
            }
            Text("value=${trigger.value} runs=${effectRuns.value}")
        }

        waitForIdle()
        onNodeWithText("value=0 runs=0").assertExists()

        trigger.value = 1
        waitForIdle()
        onNodeWithText("value=1 runs=1").assertExists()

        trigger.value = 2
        waitForIdle()
        onNodeWithText("value=2 runs=2").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun callback_only_recomposition_uses_latest_block_on_next_key_change() = runComposeUiTest {
        val trigger = mutableStateOf(0)
        val callbackVersion = mutableStateOf(0)
        val handledBy = AtomicReference<String?>(null)

        setContent {
            val block: SuspendAsyncFn = if (callbackVersion.value == 0) {
                { handledBy.set("stale") }
            } else {
                { handledBy.set("latest") }
            }
            useUpdateEffect(trigger.value, block = block)
            Text("key=${trigger.value} version=${callbackVersion.value}")
        }

        waitForIdle()
        callbackVersion.value = 1
        waitForIdle()
        trigger.value = 1
        waitForIdle()

        assertEquals("latest", handledBy.get())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun key_change_cancels_previous_block_before_starting_next() = runComposeUiTest {
        val trigger = mutableStateOf(0)
        val firstStarted = CompletableDeferred<Unit>()
        val events = mutableListOf<String>()

        setContent {
            val currentKey = trigger.value
            useUpdateEffect(currentKey) {
                when (currentKey) {
                    1 -> {
                        events += "start-1"
                        firstStarted.complete(Unit)
                        try {
                            awaitCancellation()
                        } finally {
                            events += "cancel-1"
                        }
                    }

                    2 -> events += "start-2"
                }
            }
            Text("key=$currentKey")
        }

        waitForIdle()
        trigger.value = 1
        waitForIdle()
        firstStarted.await()

        trigger.value = 2
        waitForIdle()

        assertEquals(listOf("start-1", "cancel-1", "start-2"), events)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun remount_skips_new_initial_key_and_alias_runs_on_later_change() = runComposeUiTest {
        val mounted = mutableStateOf(true)
        val trigger = mutableStateOf(0)
        val runs = AtomicInteger(0)

        setContent {
            if (mounted.value) {
                rememberUpdateEffect(trigger.value) {
                    runs.incrementAndGet()
                }
                Text("key=${trigger.value}")
            }
        }

        waitForIdle()
        assertEquals(0, runs.get())

        trigger.value = 1
        waitForIdle()
        assertEquals(1, runs.get())

        mounted.value = false
        waitForIdle()
        mounted.value = true
        waitForIdle()
        assertEquals(1, runs.get())

        trigger.value = 2
        waitForIdle()
        assertEquals(2, runs.get())
    }
}

private class UnitApplier : AbstractApplier<Unit>(Unit) {
    override fun insertTopDown(index: Int, instance: Unit) = Unit

    override fun insertBottomUp(index: Int, instance: Unit) = Unit

    override fun remove(index: Int, count: Int) = Unit

    override fun move(from: Int, to: Int, count: Int) = Unit

    override fun onClear() = Unit
}
