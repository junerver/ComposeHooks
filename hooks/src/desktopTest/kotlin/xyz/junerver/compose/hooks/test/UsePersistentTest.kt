package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import xyz.junerver.compose.hooks.PersistentHolder
import xyz.junerver.compose.hooks.usePersistent

@Suppress("DEPRECATION")
class UsePersistentTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun persistent_save_updates_state() = runComposeUiTest {
        lateinit var holder: PersistentHolder<String>
        setContent {
            holder = usePersistent("persistent-save", "default")
            Text(holder.state.value)
        }

        waitForIdle()
        onNodeWithText("default").assertExists()
        holder.save("saved")
        waitForIdle()
        onNodeWithText("saved").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun persistent_clear_restores_default_value() = runComposeUiTest {
        lateinit var holder: PersistentHolder<String>
        setContent {
            holder = usePersistent("persistent-clear", "default")
            Text(holder.state.value)
        }

        waitForIdle()
        holder.save("saved")
        waitForIdle()
        onNodeWithText("saved").assertExists()
        holder.clear()
        waitForIdle()
        onNodeWithText("default").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun persistent_same_key_keeps_multiple_consumers_in_sync() = runComposeUiTest {
        lateinit var first: PersistentHolder<String>
        lateinit var second: PersistentHolder<String>
        setContent {
            first = usePersistent("persistent-sync", "default")
            second = usePersistent("persistent-sync", "default")
            Text("first=${first.state.value} second=${second.state.value}")
        }

        waitForIdle()
        first.save("updated")
        waitForIdle()
        onNodeWithText("first=updated second=updated").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun persistent_different_keys_remain_isolated() = runComposeUiTest {
        lateinit var first: PersistentHolder<String>
        lateinit var second: PersistentHolder<String>
        setContent {
            first = usePersistent("persistent-isolated-1", "default1")
            second = usePersistent("persistent-isolated-2", "default2")
            Text("first=${first.state.value} second=${second.state.value}")
        }

        waitForIdle()
        first.save("value1")
        waitForIdle()
        onNodeWithText("first=value1 second=default2").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun persistent_supports_property_delegation() = runComposeUiTest {
        lateinit var readValue: () -> String
        lateinit var writeValue: (String) -> Unit
        setContent {
            val holder = usePersistent("persistent-delegation", "default")
            var value by holder
            readValue = { value }
            writeValue = { value = it }
            Text(value)
        }

        waitForIdle()
        kotlin.test.assertEquals("default", readValue())
        writeValue("delegated")
        waitForIdle()
        onNodeWithText("delegated").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun persistent_force_use_memory_still_persists_within_process() = runComposeUiTest {
        lateinit var holder: PersistentHolder<String>
        setContent {
            holder = usePersistent("persistent-memory", "default", forceUseMemory = true)
            Text(holder.state.value)
        }

        waitForIdle()
        holder.save("memory-value")
        waitForIdle()
        onNodeWithText("memory-value").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun persistent_reloads_saved_value_when_consumer_recomposes_with_same_key() = runComposeUiTest {
        var mounted by mutableStateOf(true)
        lateinit var holder: PersistentHolder<String>

        setContent {
            if (mounted) {
                holder = usePersistent("persistent-remount", "default")
                Text(holder.state.value)
            } else {
                Text("unmounted")
            }
        }

        waitForIdle()
        holder.save("saved-once")
        waitForIdle()
        onNodeWithText("saved-once").assertExists()

        mounted = false
        waitForIdle()
        onNodeWithText("unmounted").assertExists()

        mounted = true
        waitForIdle()
        onNodeWithText("saved-once").assertExists()
    }
}
