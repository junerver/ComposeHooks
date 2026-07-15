package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import kotlin.DeepRecursiveFunction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlinx.coroutines.runBlocking
import xyz.junerver.compose.hooks.rememberMemoizedFn
import xyz.junerver.compose.hooks.useMemoizedFn
import xyz.junerver.compose.hooks.usememoizedfn.useMemoizedFnImpl

@Suppress("DEPRECATION")
class UseMemoizedFnTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun memoized_function_caches_nullable_recursive_results() = runComposeUiTest {
        lateinit var memoized: DeepRecursiveFunction<Int, String?>
        var calls = 0

        setContent {
            memoized = useMemoizedFn { n ->
                calls += 1
                if (n == 0) {
                    null
                } else {
                    callRecursive(n - 1)
                }
            }
        }

        waitForIdle()

        assertNull(runBlocking { memoized(3) })
        assertNull(runBlocking { memoized(3) })
        assertEquals(4, calls, "Nullable results must still be cached for every recursive argument")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun failed_computation_is_retried_and_only_success_is_cached() = runComposeUiTest {
        lateinit var memoized: DeepRecursiveFunction<Int, Int>
        var attempts = 0

        setContent {
            memoized = useMemoizedFn { value ->
                attempts += 1
                check(attempts > 1) { "first attempt fails" }
                value
            }
        }

        waitForIdle()

        assertFailsWith<IllegalStateException> { memoized(7) }
        assertEquals(7, memoized(7))
        assertEquals(7, memoized(7))
        assertEquals(2, attempts)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun use_and_remember_entries_keep_function_identity_and_cache() = runComposeUiTest {
        val render = mutableStateOf(0)
        var useCalls = 0
        var rememberCalls = 0
        var initialUse: DeepRecursiveFunction<Int, Int>? = null
        var initialRemember: DeepRecursiveFunction<Int, Int>? = null
        lateinit var latestUse: DeepRecursiveFunction<Int, Int>
        lateinit var latestRemember: DeepRecursiveFunction<Int, Int>

        setContent {
            latestUse = useMemoizedFn { value ->
                useCalls += 1
                value * 2
            }
            latestRemember = rememberMemoizedFn { value ->
                rememberCalls += 1
                value * 3
            }
            if (initialUse == null) {
                initialUse = latestUse
                initialRemember = latestRemember
            }
            Text("render=${render.value}")
        }

        waitForIdle()
        assertEquals(8, latestUse(4))
        assertEquals(12, latestRemember(4))

        runOnIdle { render.value += 1 }
        waitForIdle()

        assertSame(initialUse, latestUse)
        assertSame(initialRemember, latestRemember)
        assertEquals(8, latestUse(4))
        assertEquals(12, latestRemember(4))
        assertEquals(1, useCalls)
        assertEquals(1, rememberCalls)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun memoized_function_reuses_cached_result_for_same_argument() = runComposeUiTest {
        lateinit var memoized: DeepRecursiveFunction<Int, Int>
        var calls = 0

        setContent {
            memoized = useMemoizedFnImpl<Int, Int> { n ->
                calls++
                if (n <= 1) {
                    n
                } else {
                    callRecursive(n - 1) + callRecursive(n - 2)
                }
            }
        }

        waitForIdle()

        val first = runBlocking { memoized(10) }
        val second = runBlocking { memoized(10) }

        assertEquals(55, first)
        assertEquals(55, second)
        assertEquals(11, calls, "Each distinct argument in fibonacci(10) should be computed once")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun memoized_function_caches_results_per_argument() = runComposeUiTest {
        lateinit var memoized: DeepRecursiveFunction<Int, Int>
        val callsPerArg = mutableMapOf<Int, Int>()

        setContent {
            memoized = useMemoizedFnImpl<Int, Int> { n ->
                callsPerArg[n] = (callsPerArg[n] ?: 0) + 1
                if (n <= 1) {
                    n
                } else {
                    callRecursive(n - 1) + callRecursive(n - 2)
                }
            }
        }

        waitForIdle()

        assertEquals(5, runBlocking { memoized(5) })
        assertEquals(8, runBlocking { memoized(6) })
        assertEquals(5, runBlocking { memoized(5) })

        assertEquals(1, callsPerArg[6], "Argument 6 should only be computed once")
        assertEquals(1, callsPerArg[5], "Argument 5 should only be computed once")
        assertEquals(1, callsPerArg[4], "Sub-results should also be cached")
        assertEquals(7, callsPerArg.size, "fibonacci(6) should touch each argument from 0 to 6 once")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun memoized_function_supports_deep_recursive_computation() = runComposeUiTest {
        lateinit var memoized: DeepRecursiveFunction<Int, Long>

        setContent {
            memoized = useMemoizedFnImpl<Int, Long> { n ->
                if (n <= 1) {
                    1L
                } else {
                    n.toLong() * callRecursive(n - 1)
                }
            }
        }

        waitForIdle()

        assertEquals(2432902008176640000L, runBlocking { memoized(20) })
    }
}
