/*
  Description: Back-to-front and front-to-back lifecycle hook tests
  Author: Junerver
  Date: 2026/07/14
  Email: junerver@gmail.com
  Version: v1.0
*/

package xyz.junerver.compose.hooks.test

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlin.test.Test
import kotlin.test.assertEquals
import xyz.junerver.compose.hooks.useBackToFrontEffect
import xyz.junerver.compose.hooks.useFrontToBackEffect

private class ControllableLifecycleOwner : LifecycleOwner {
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
class UseBackFrontTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun backToFront_runs_only_after_real_pause_and_uses_latest_effect() = runComposeUiTest {
        val owner = ControllableLifecycleOwner().apply { resumeInitially() }
        val dependency = mutableStateOf(0)
        var calls = 0
        var observedDependency = -1

        setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                useBackToFrontEffect(dependency.value) {
                    calls++
                    observedDependency = dependency.value
                }
            }
        }

        waitForIdle()
        assertEquals(0, calls)

        runOnIdle { dependency.value = 1 }
        waitForIdle()
        assertEquals(0, calls)

        runOnIdle { owner.pause() }
        waitForIdle()
        assertEquals(0, calls)

        runOnIdle { owner.resume() }
        waitForIdle()
        assertEquals(1, calls)
        assertEquals(1, observedDependency)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun frontToBack_ignores_dependency_disposal_and_unmount() = runComposeUiTest {
        val owner = ControllableLifecycleOwner().apply { resumeInitially() }
        val dependency = mutableStateOf(0)
        val mounted = mutableStateOf(true)
        var calls = 0
        var observedDependency = -1

        setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                if (mounted.value) {
                    useFrontToBackEffect(dependency.value) {
                        calls++
                        observedDependency = dependency.value
                    }
                }
            }
        }

        waitForIdle()
        assertEquals(0, calls)

        runOnIdle { dependency.value = 1 }
        waitForIdle()
        assertEquals(0, calls)

        runOnIdle { owner.pause() }
        waitForIdle()
        assertEquals(1, calls)
        assertEquals(1, observedDependency)

        runOnIdle { owner.resume() }
        waitForIdle()
        runOnIdle { mounted.value = false }
        waitForIdle()
        assertEquals(1, calls)
    }
}
