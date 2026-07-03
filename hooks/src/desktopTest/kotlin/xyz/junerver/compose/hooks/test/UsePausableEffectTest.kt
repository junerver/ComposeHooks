package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import xyz.junerver.compose.hooks.PausableEffectHolder
import xyz.junerver.compose.hooks.usePausableEffect

@Suppress("DEPRECATION")
class UsePausableEffectTest {
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
