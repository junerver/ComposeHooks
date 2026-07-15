package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import xyz.junerver.compose.hooks.Plugin
import xyz.junerver.compose.hooks.rememberEmptyPlugin
import xyz.junerver.compose.hooks.useEmptyPlugin

/*
  Description: useEmptyPlugin contract tests
  Author: AI Assistant
  Date: 2026/7/15
  Email: junerver@gmail.com
  Version: v1.0
*/

@Suppress("DEPRECATION")
class UseEmptyPluginTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stale_empty_plugin_scope_rejects_work_after_unmount() = runComposeUiTest {
        val mounted = mutableStateOf(true)
        val executed = AtomicBoolean(false)
        lateinit var plugin: Plugin<Unit, String>

        setContent {
            if (mounted.value) {
                plugin = useEmptyPlugin()
                Text("mounted")
            }
        }

        waitForIdle()
        runOnIdle { mounted.value = false }
        waitForIdle()

        val staleJob = plugin.launch {
            executed.set(true)
        }
        staleJob.join()

        assertTrue(staleJob.isCancelled)
        assertFalse(executed.get())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun regular_cancel_keeps_mounted_empty_plugin_reusable() = runComposeUiTest {
        val firstCancelled = CompletableDeferred<Unit>()
        val secondExecuted = AtomicBoolean(false)
        lateinit var plugin: Plugin<Unit, String>

        setContent {
            plugin = useEmptyPlugin()
        }

        waitForIdle()
        val firstJob = plugin.launch {
            try {
                awaitCancellation()
            } finally {
                firstCancelled.complete(Unit)
            }
        }
        plugin.cancel()
        firstJob.join()
        assertTrue(firstCancelled.isCompleted)

        val secondJob = plugin.launch {
            secondExecuted.set(true)
        }
        secondJob.join()
        assertTrue(secondExecuted.get())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun empty_plugin_cancels_child_coroutines_on_unmount() = runComposeUiTest {
        val mounted = mutableStateOf(true)
        val started = CompletableDeferred<Unit>()
        val cancelled = CompletableDeferred<Unit>()
        lateinit var plugin: Plugin<Unit, String>

        setContent {
            if (mounted.value) {
                plugin = useEmptyPlugin()
                Text("mounted")
            }
        }

        waitForIdle()
        plugin.launch {
            started.complete(Unit)
            try {
                awaitCancellation()
            } finally {
                cancelled.complete(Unit)
            }
        }
        started.await()

        try {
            runOnIdle { mounted.value = false }
            waitForIdle()
            assertTrue(
                withTimeoutOrNull(1_000) { cancelled.await() } != null,
                "Unmount must cancel child coroutines launched in the empty plugin scope",
            )
        } finally {
            plugin.cancel()
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun use_and_remember_entries_keep_plugin_identity_stable() = runComposeUiTest {
        val render = mutableStateOf(0)
        var initialUsePlugin: Plugin<Unit, String>? = null
        var initialRememberPlugin: Plugin<Unit, String>? = null
        lateinit var latestUsePlugin: Plugin<Unit, String>
        lateinit var latestRememberPlugin: Plugin<Unit, String>
        val initialized = AtomicBoolean(false)

        setContent {
            latestUsePlugin = useEmptyPlugin()
            latestRememberPlugin = rememberEmptyPlugin()
            if (initialized.compareAndSet(false, true)) {
                initialUsePlugin = latestUsePlugin
                initialRememberPlugin = latestRememberPlugin
            }
            Text("render=${render.value}")
        }

        waitForIdle()
        runOnIdle { render.value += 1 }
        waitForIdle()

        assertSame(initialUsePlugin, latestUsePlugin)
        assertSame(initialRememberPlugin, latestRememberPlugin)
    }
}
