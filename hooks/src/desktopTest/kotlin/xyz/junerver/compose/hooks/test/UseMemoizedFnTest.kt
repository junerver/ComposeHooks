package xyz.junerver.compose.hooks.test

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import kotlin.DeepRecursiveFunction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking
import xyz.junerver.compose.hooks.usememoizedfn.useMemoizedFnImpl

@Suppress("DEPRECATION")
class UseMemoizedFnTest {
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
