package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import xyz.junerver.compose.hooks.rememberMount
import xyz.junerver.compose.hooks.rememberUnmount
import xyz.junerver.compose.hooks.useMount
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.useUnmount
import xyz.junerver.compose.hooks.useUnmountedRef
import xyz.junerver.compose.hooks.useref.Ref

/*
  Description: useMount/useUnmount comprehensive TDD tests
  Author: AI Assistant
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

@Suppress("DEPRECATION")
class UseMountUnmountTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useMount_executes_once_on_mount() = runComposeUiTest {
        setContent {
            var mountCount by useState(0)

            useMount {
                mountCount++
            }

            Text("mountCount=$mountCount")
        }
        waitForIdle()
        onNodeWithText("mountCount=1").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useMount_does_not_execute_on_recomposition() = runComposeUiTest {
        setContent {
            var mountCount by useState(0)
            var recomposeCount by useState(0)

            useMount {
                mountCount++
            }

            SideEffect {
                if (recomposeCount < 3) {
                    recomposeCount++
                }
            }

            Text("mountCount=$mountCount recomposeCount=$recomposeCount")
        }
        waitForIdle()
        // Mount should only happen once despite multiple recompositions
        onNodeWithText("mountCount=1 recomposeCount=3").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useUnmount_executes_on_component_removal() = runComposeUiTest {
        var unmountCalled = false

        setContent {
            var showChild by useState(true)

            if (showChild) {
                key("child") {
                    ChildWithUnmount { unmountCalled = true }
                }
            }

            SideEffect {
                if (showChild) {
                    showChild = false
                }
            }

            Text("showChild=$showChild")
        }

        waitForIdle()
        Thread.sleep(100)
        waitForIdle()
        assertTrue(unmountCalled, "useUnmount should be called when component is removed")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useUnmount_uses_latest_block_and_runs_exactly_once() = runComposeUiTest {
        val mounted = mutableStateOf(true)
        val callbackVersion = mutableStateOf(0)
        val calls = mutableListOf<String>()

        setContent {
            if (mounted.value) {
                val block: () -> Unit = if (callbackVersion.value == 0) {
                    { calls += "stale" }
                } else {
                    { calls += "latest" }
                }
                useUnmount(block)
                Text("version=${callbackVersion.value}")
            }
        }

        waitForIdle()
        runOnIdle { callbackVersion.value = 1 }
        waitForIdle()
        runOnIdle { mounted.value = false }
        waitForIdle()
        runOnIdle { mounted.value = false }
        waitForIdle()

        assertEquals(listOf("latest"), calls)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useMount_is_cancelled_when_component_unmounts() = runComposeUiTest {
        val mounted = mutableStateOf(true)
        val started = CompletableDeferred<Unit>()
        val cancelled = AtomicBoolean(false)

        setContent {
            if (mounted.value) {
                useMount {
                    started.complete(Unit)
                    try {
                        awaitCancellation()
                    } finally {
                        cancelled.set(true)
                    }
                }
                Text("mounted=true")
            }
        }

        waitForIdle()
        started.await()
        runOnIdle { mounted.value = false }
        waitForIdle()

        assertTrue(cancelled.get())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun unmounted_ref_is_stable_and_old_ref_observes_removal() = runComposeUiTest {
        val mounted = mutableStateOf(true)
        val recomposeTick = mutableStateOf(0)
        lateinit var currentRef: Ref<Boolean>
        var firstRef: Ref<Boolean>? = null

        setContent {
            if (mounted.value) {
                currentRef = useUnmountedRef()
                if (firstRef == null) firstRef = currentRef
                Text("unmounted=${currentRef.current} tick=${recomposeTick.value}")
            }
        }

        waitForIdle()
        val originalRef = requireNotNull(firstRef)
        assertFalse(originalRef.current)
        runOnIdle { recomposeTick.value = 1 }
        waitForIdle()
        assertSame(originalRef, currentRef)

        runOnIdle { mounted.value = false }
        waitForIdle()
        assertTrue(originalRef.current)

        runOnIdle { mounted.value = true }
        waitForIdle()
        assertNotSame(originalRef, currentRef)
        assertFalse(currentRef.current)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun remember_aliases_preserve_mount_and_unmount_semantics() = runComposeUiTest {
        val mounted = mutableStateOf(true)
        val mountCalls = AtomicInteger(0)
        val unmountCalls = AtomicInteger(0)

        setContent {
            if (mounted.value) {
                rememberMount { mountCalls.incrementAndGet() }
                rememberUnmount { unmountCalls.incrementAndGet() }
                Text("mounted=true")
            }
        }

        waitForIdle()
        assertEquals(1, mountCalls.get())
        assertEquals(0, unmountCalls.get())

        runOnIdle { mounted.value = false }
        waitForIdle()
        assertEquals(1, mountCalls.get())
        assertEquals(1, unmountCalls.get())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useMount_with_suspend_function() = runComposeUiTest {
        setContent {
            var result by useState("")

            useMount {
                // Simulate async operation
                result = "loaded"
            }

            Text("result=$result")
        }
        waitForIdle()
        onNodeWithText("result=loaded").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun multiple_useMount_all_execute() = runComposeUiTest {
        setContent {
            var count1 by useState(0)
            var count2 by useState(0)

            useMount {
                count1 = 1
            }

            useMount {
                count2 = 2
            }

            Text("count1=$count1 count2=$count2")
        }
        waitForIdle()
        onNodeWithText("count1=1 count2=2").assertExists()
    }
}

@androidx.compose.runtime.Composable
private fun ChildWithUnmount(onUnmount: () -> Unit) {
    useUnmount {
        onUnmount()
    }
    Text("Child")
}
