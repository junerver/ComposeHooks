package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import xyz.junerver.compose.hooks.useUpdateEffect

@Suppress("DEPRECATION")
class UseUpdateEffectTest {
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
}
