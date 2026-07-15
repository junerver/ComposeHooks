package xyz.junerver.compose.hooks.test

import androidx.compose.runtime.mutableStateOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import xyz.junerver.compose.hooks.useref.MutableRef
import xyz.junerver.compose.hooks.usestate.AsyncCalcState
import xyz.junerver.compose.hooks.usestate.AsyncStateController

/*
  Description: useStateAsync controller concurrency tests
  Author: Junerver
  Date: 2026/7/15
  Email: junerver@gmail.com
  Version: v1.0
*/

class AsyncStateControllerTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun old_result_is_ignored_after_composition_publishes_new_key() = runTest {
        val oldKey = Any()
        val newKey = Any()
        val dependencyKeyRef = MutableRef(oldKey)
        val lazyRef = MutableRef(false)
        val state = mutableStateOf<String?>("initial")
        val started = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val factoryRef = MutableRef<suspend () -> String> {
            started.complete(Unit)
            release.await()
            "stale"
        }
        val controller = AsyncStateController(
            scope = this,
            state = state,
            factoryRef = factoryRef,
            onErrorRef = MutableRef({}),
            latestDependencyKeyRef = dependencyKeyRef,
        )
        val asyncState = AsyncCalcState(controller, dependencyKeyRef, lazyRef, state)

        controller.updateKey(oldKey)
        controller.request(oldKey)
        runCurrent()
        assertTrue(started.isCompleted)

        dependencyKeyRef.current = newKey
        release.complete(Unit)
        advanceUntilIdle()

        assertEquals("initial", asyncState.value)
    }
}
