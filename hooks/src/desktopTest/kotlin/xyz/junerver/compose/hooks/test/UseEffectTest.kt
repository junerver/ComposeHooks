package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.awaitCancellation
import xyz.junerver.compose.hooks.MutableRef
import xyz.junerver.compose.hooks.rememberEffect
import xyz.junerver.compose.hooks.useEffect

/*
  Description: useEffect contract tests
  Author: AI Assistant
  Date: 2026/7/15
  Email: junerver@gmail.com
  Version: v1.0
*/

class UseEffectTest {
    private data class Dependency(val value: Int)

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun no_dependency_effect_runs_once_across_recomposition() = runComposeUiTest {
        val render = mutableStateOf(0)
        var runs = 0

        setContent {
            useEffect {
                runs += 1
            }
            Text("render=${render.value}")
        }

        waitForIdle()
        assertEquals(1, runs)

        runOnIdle { render.value += 1 }
        waitForIdle()
        assertEquals(1, runs)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun equal_but_distinct_dependency_does_not_restart_effect() = runComposeUiTest {
        val render = mutableStateOf(0)
        var dependency = Dependency(1)
        var runs = 0

        setContent {
            render.value
            useEffect(dependency) {
                runs += 1
            }
        }

        waitForIdle()
        assertEquals(1, runs)

        runOnIdle {
            dependency = Dependency(1)
            render.value += 1
        }
        waitForIdle()
        assertEquals(1, runs)

        runOnIdle {
            dependency = Dependency(2)
            render.value += 1
        }
        waitForIdle()
        assertEquals(2, runs)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun key_change_cancels_previous_effect_before_starting_latest_block() = runComposeUiTest {
        val key = mutableStateOf(0)
        val callback = mutableStateOf("first")
        val events = CopyOnWriteArrayList<String>()

        setContent {
            val currentKey = key.value
            val currentCallback = callback.value
            useEffect(currentKey) {
                events += "start:$currentCallback"
                try {
                    awaitCancellation()
                } finally {
                    events += "cancel:$currentCallback"
                }
            }
            Text("key=$currentKey")
        }

        waitForIdle()
        assertEquals(listOf("start:first"), events.toList())

        runOnIdle {
            callback.value = "latest"
            key.value = 1
        }
        waitForIdle()

        assertEquals(
            listOf("start:first", "cancel:first", "start:latest"),
            events.toList(),
        )
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun remember_alias_tracks_the_same_dependency_contract() = runComposeUiTest {
        val key = mutableStateOf(0)
        var useRuns = 0
        var rememberRuns = 0

        setContent {
            useEffect(key.value) {
                useRuns += 1
            }
            rememberEffect(key.value) {
                rememberRuns += 1
            }
        }

        waitForIdle()
        assertEquals(1, useRuns)
        assertEquals(1, rememberRuns)

        runOnIdle { key.value += 1 }
        waitForIdle()
        assertEquals(2, useRuns)
        assertEquals(2, rememberRuns)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun ref_dependency_replacement_observes_only_the_latest_instance() = runComposeUiTest {
        val first = MutableRef("first")
        val second = MutableRef("second")
        val selected = mutableStateOf(first)
        val observed = mutableListOf<String>()

        setContent {
            val dependency = selected.value
            useEffect(dependency) {
                observed += dependency.current
            }
            Text("selected=${dependency.current}")
        }

        waitForIdle()
        assertEquals(listOf("first"), observed)

        runOnIdle { selected.value = second }
        waitForIdle()
        assertEquals(listOf("first", "second"), observed)

        runOnIdle { first.current = "stale" }
        waitForIdle()
        assertEquals(listOf("first", "second"), observed)

        runOnIdle { second.current = "latest" }
        waitForIdle()
        assertEquals(listOf("first", "second", "latest"), observed)
    }
}
