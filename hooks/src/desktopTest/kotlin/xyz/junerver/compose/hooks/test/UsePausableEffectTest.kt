package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlinx.coroutines.awaitCancellation
import xyz.junerver.compose.hooks.PausableEffectHolder
import xyz.junerver.compose.hooks.rememberPausableEffect
import xyz.junerver.compose.hooks.usePausableEffect

@Suppress("DEPRECATION")
class UsePausableEffectTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pause_cancels_running_effect_and_resume_runs_latest_block() = runComposeUiTest {
        val trigger = mutableStateOf(0)
        val callback = mutableStateOf("first")
        val events = CopyOnWriteArrayList<String>()
        lateinit var holder: PausableEffectHolder

        setContent {
            val currentCallback = callback.value
            holder = usePausableEffect(trigger.value) {
                events += "start:$currentCallback"
                try {
                    awaitCancellation()
                } finally {
                    events += "cancel:$currentCallback"
                }
            }
            Text("trigger=${trigger.value}")
        }

        waitForIdle()
        assertEquals(listOf("start:first"), events.toList())

        runOnIdle { holder.pause() }
        waitForIdle()
        assertEquals(listOf("start:first", "cancel:first"), events.toList())

        runOnIdle {
            callback.value = "latest"
            trigger.value = 1
        }
        waitForIdle()
        assertEquals(listOf("start:first", "cancel:first"), events.toList())

        runOnIdle { holder.resume() }
        waitForIdle()
        assertEquals(
            listOf("start:first", "cancel:first", "start:latest"),
            events.toList(),
        )
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stop_cancels_running_effect_and_cannot_be_resumed() = runComposeUiTest {
        val trigger = mutableStateOf(0)
        val events = CopyOnWriteArrayList<String>()
        lateinit var holder: PausableEffectHolder

        setContent {
            val currentTrigger = trigger.value
            holder = usePausableEffect(currentTrigger) {
                events += "start:$currentTrigger"
                try {
                    awaitCancellation()
                } finally {
                    events += "cancel:$currentTrigger"
                }
            }
            Text("trigger=$currentTrigger")
        }

        waitForIdle()
        assertEquals(listOf("start:0"), events.toList())

        runOnIdle { holder.stop() }
        waitForIdle()
        assertEquals(listOf("start:0", "cancel:0"), events.toList())

        runOnIdle {
            holder.resume()
            trigger.value = 1
        }
        waitForIdle()
        assertEquals(listOf("start:0", "cancel:0"), events.toList())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun remember_alias_keeps_holder_and_function_identities_stable() = runComposeUiTest {
        val trigger = mutableStateOf(0)
        var initialHolder: PausableEffectHolder? = null
        lateinit var latestHolder: PausableEffectHolder
        var initialStop: (() -> Unit)? = null
        var initialPause: (() -> Unit)? = null
        var initialResume: (() -> Unit)? = null

        setContent {
            latestHolder = rememberPausableEffect(trigger.value) {}
            if (initialHolder == null) {
                initialHolder = latestHolder
                initialStop = latestHolder.stop
                initialPause = latestHolder.pause
                initialResume = latestHolder.resume
            }
            Text("trigger=${trigger.value}")
        }

        waitForIdle()
        runOnIdle { trigger.value = 1 }
        waitForIdle()

        assertSame(initialHolder, latestHolder)
        assertSame(initialStop, latestHolder.stop)
        assertSame(initialPause, latestHolder.pause)
        assertSame(initialResume, latestHolder.resume)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pausable_effect_runs_on_mount_and_can_be_paused_and_resumed() = runComposeUiTest {
        val trigger = mutableStateOf(0)
        val effectRuns = mutableStateOf(0)
        lateinit var holder: PausableEffectHolder

        setContent {
            holder = usePausableEffect(trigger.value) {
                effectRuns.value += 1
            }
            Text("trigger=${trigger.value} runs=${effectRuns.value}")
        }

        waitForIdle()
        onNodeWithText("trigger=0 runs=1").assertExists()

        holder.pause()
        trigger.value = 1
        waitForIdle()
        onNodeWithText("trigger=1 runs=1").assertExists()

        holder.resume()
        trigger.value = 2
        waitForIdle()
        onNodeWithText("trigger=2 runs=2").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pausable_effect_stop_prevents_future_dependency_reactions() = runComposeUiTest {
        val trigger = mutableStateOf(0)
        val effectRuns = mutableStateOf(0)
        lateinit var holder: PausableEffectHolder

        setContent {
            holder = usePausableEffect(trigger.value) {
                effectRuns.value += 1
            }
            Text("trigger=${trigger.value} runs=${effectRuns.value}")
        }

        waitForIdle()
        onNodeWithText("trigger=0 runs=1").assertExists()

        holder.stop()
        trigger.value = 1
        waitForIdle()

        onNodeWithText("trigger=1 runs=1").assertExists()
    }
}
