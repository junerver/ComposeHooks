package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import xyz.junerver.compose.hooks.PersistentHolder
import xyz.junerver.compose.hooks.PersistentContext
import xyz.junerver.compose.hooks.notifyDefaultPersistentObserver
import xyz.junerver.compose.hooks.rememberPersistent
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

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun persistent_dynamic_key_routes_state_and_actions_to_latest_key() = runComposeUiTest {
        var key by mutableStateOf("persistent-dynamic-key-a")
        lateinit var holder: PersistentHolder<String>

        setContent {
            holder = usePersistent(key, "default")
            Text("$key=${holder.state.value}")
        }

        waitForIdle()
        val originalHolder = holder
        val originalSave = holder.save
        val originalClear = holder.clear
        holder.save("value-a")
        waitForIdle()
        onNodeWithText("persistent-dynamic-key-a=value-a").assertExists()

        key = "persistent-dynamic-key-b"
        waitForIdle()
        onNodeWithText("persistent-dynamic-key-b=default").assertExists()
        assertSame(originalHolder, holder)
        assertSame(originalSave, holder.save)
        assertSame(originalClear, holder.clear)

        holder.save("value-b")
        waitForIdle()
        onNodeWithText("persistent-dynamic-key-b=value-b").assertExists()
        holder.clear()
        waitForIdle()
        onNodeWithText("persistent-dynamic-key-b=default").assertExists()

        key = "persistent-dynamic-key-a"
        waitForIdle()
        onNodeWithText("persistent-dynamic-key-a=value-a").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun persistent_dynamic_default_applies_only_when_storage_is_empty() = runComposeUiTest {
        var defaultValue by mutableStateOf("first-default")
        lateinit var holder: PersistentHolder<String>

        setContent {
            holder = usePersistent("persistent-dynamic-default", defaultValue)
            Text(holder.state.value)
        }

        waitForIdle()
        onNodeWithText("first-default").assertExists()
        defaultValue = "second-default"
        waitForIdle()
        onNodeWithText("second-default").assertExists()

        holder.save("saved")
        waitForIdle()
        defaultValue = "third-default"
        waitForIdle()
        onNodeWithText("saved").assertExists()

        holder.clear()
        waitForIdle()
        onNodeWithText("third-default").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun persistent_dynamic_backend_reloads_and_uses_latest_callbacks() = runComposeUiTest {
        val key = "persistent-dynamic-backend"
        val firstBackend = TestPersistentBackend(mapOf(key to "first-value"))
        val secondBackend = TestPersistentBackend(mapOf(key to "second-value"))
        var useSecondBackend by mutableStateOf(false)
        lateinit var holder: PersistentHolder<String>

        setContent {
            PersistentContext.Provider(
                value = if (useSecondBackend) secondBackend.contextValue else firstBackend.contextValue,
            ) {
                holder = usePersistent(key, "default")
                Text(holder.state.value)
            }
        }

        waitForIdle()
        onNodeWithText("first-value").assertExists()
        val originalHolder = holder
        val originalSave = holder.save
        val originalClear = holder.clear

        useSecondBackend = true
        waitForIdle()
        onNodeWithText("second-value").assertExists()
        assertSame(originalHolder, holder)
        assertSame(originalSave, holder.save)
        assertSame(originalClear, holder.clear)

        holder.save("second-updated")
        waitForIdle()
        assertEquals("first-value", firstBackend.values[key])
        assertEquals("second-updated", secondBackend.values[key])
        onNodeWithText("second-updated").assertExists()

        holder.clear()
        waitForIdle()
        assertEquals("first-value", firstBackend.values[key])
        assertEquals(null, secondBackend.values[key])
        onNodeWithText("default").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun persistent_dynamic_force_memory_switches_storage_without_recreating_holder() = runComposeUiTest {
        val key = "persistent-dynamic-force-memory"
        val backend = TestPersistentBackend(mapOf(key to "custom-value"))
        var forceUseMemory by mutableStateOf(false)
        lateinit var holder: PersistentHolder<String>

        setContent {
            PersistentContext.Provider(value = backend.contextValue) {
                holder = usePersistent(key, "memory-default", forceUseMemory)
                Text(holder.state.value)
            }
        }

        waitForIdle()
        onNodeWithText("custom-value").assertExists()
        val originalHolder = holder

        forceUseMemory = true
        waitForIdle()
        onNodeWithText("memory-default").assertExists()
        assertSame(originalHolder, holder)
        holder.save("memory-value")
        waitForIdle()
        onNodeWithText("memory-value").assertExists()
        assertEquals("custom-value", backend.values[key])

        forceUseMemory = false
        waitForIdle()
        onNodeWithText("custom-value").assertExists()
        forceUseMemory = true
        waitForIdle()
        onNodeWithText("memory-value").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun persistent_unrelated_recomposition_does_not_read_storage_again() = runComposeUiTest {
        val key = "persistent-read-once"
        val backend = TestPersistentBackend(mapOf(key to "stored"))
        var marker by mutableStateOf(0)

        setContent {
            PersistentContext.Provider(value = backend.contextValue) {
                val holder = usePersistent(key, "default")
                Text("${holder.state.value}:$marker")
            }
        }

        waitForIdle()
        val readsAfterMount = backend.readCount
        marker = 1
        waitForIdle()
        onNodeWithText("stored:1").assertExists()
        assertEquals(readsAfterMount, backend.readCount)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun remember_persistent_matches_dynamic_key_behavior() = runComposeUiTest {
        var key by mutableStateOf("remember-persistent-a")
        lateinit var holder: PersistentHolder<String>

        setContent {
            holder = rememberPersistent(key, "default")
            Text(holder.state.value)
        }

        waitForIdle()
        holder.save("value-a")
        waitForIdle()
        key = "remember-persistent-b"
        waitForIdle()
        onNodeWithText("default").assertExists()
        holder.save("value-b")
        waitForIdle()
        onNodeWithText("value-b").assertExists()
    }
}

private class TestPersistentBackend(initialValues: Map<String, Any?> = emptyMap()) {
    val values = initialValues.toMutableMap()
    var readCount = 0

    val contextValue = Triple(
        first = { key: String, defaultValue: Any ->
            readCount += 1
            values[key] ?: defaultValue
        },
        second = { key: String, value: Any? ->
            values[key] = value
            notifyDefaultPersistentObserver(key)
        },
        third = { key: String ->
            values.remove(key)
            notifyDefaultPersistentObserver(key)
        },
    )
}
