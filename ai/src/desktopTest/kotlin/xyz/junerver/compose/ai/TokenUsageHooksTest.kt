/*
  Description: Token usage tracker and hooks behavior tests
  Author: Junerver
  Date: 2026/07/14
  Email: junerver@gmail.com
  Version: v1.0
*/

package xyz.junerver.compose.ai

import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.collections.immutable.PersistentList
import xyz.junerver.compose.ai.usechat.ChatUsage

@Suppress("DEPRECATION")
class TokenUsageHooksTest {
    @Test
    fun tracker_aggregates_records_and_reset_restores_empty_stats() {
        val tracker = TokenUsageTracker()

        tracker.recordUsage("request-1", "provider-a", "model-a", ChatUsage(10, 5, 15))
        tracker.recordUsage("request-2", "provider-b", "model-b", ChatUsage(20, 7, 27))

        assertEquals(30, tracker.stats.totalPromptTokens)
        assertEquals(12, tracker.stats.totalCompletionTokens)
        assertEquals(42, tracker.stats.totalTokens)
        assertEquals(2, tracker.stats.requestCount)
        assertEquals(listOf("request-1", "request-2"), tracker.stats.records.map { it.requestId })
        assertEquals("provider-b", tracker.stats.records.last().provider)
        assertEquals("model-b", tracker.stats.records.last().model)
        assertTrue(tracker.stats.records is PersistentList<*>)

        tracker.reset()

        assertSame(TokenUsageStats.EMPTY, tracker.stats)
    }

    @Test
    fun tracker_saturates_totals_instead_of_overflowing_to_negative_values() {
        val tracker = TokenUsageTracker()

        tracker.recordUsage("request-1", "provider", "model", ChatUsage(Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE))
        tracker.recordUsage("request-2", "provider", "model", ChatUsage(1, 1, 1))

        assertEquals(Int.MAX_VALUE, tracker.stats.totalPromptTokens)
        assertEquals(Int.MAX_VALUE, tracker.stats.totalCompletionTokens)
        assertEquals(Int.MAX_VALUE, tracker.stats.totalTokens)
        assertEquals(2, tracker.stats.requestCount)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useWindowTokens_returns_initial_estimate_without_effect_delay_and_updates_with_inputs() = runComposeUiTest {
        val messages = mutableStateOf<List<Any>>(listOf("abcdef"))
        val maxTokens = mutableStateOf<Int?>(3)
        val observations = mutableListOf<WindowTokenUsage>()

        setContent {
            val usage = useWindowTokens(messages.value, maxTokens.value)
            SideEffect { observations += usage }
        }

        waitForIdle()
        assertEquals(WindowTokenUsage(1, 2, 3, 1), observations.first())
        assertEquals(WindowTokenUsage(1, 2, 3, 1), observations.last())

        runOnIdle {
            messages.value = listOf("abc", "abcdef")
            maxTokens.value = 2
        }
        waitForIdle()

        assertEquals(WindowTokenUsage(2, 3, 2, 0), observations.last())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun token_provider_isolates_tracker_and_recomposes_stats_consumers() = runComposeUiTest {
        lateinit var providedTracker: TokenUsageTracker
        lateinit var contextTracker: TokenUsageTracker
        var optionalTracker: TokenUsageTracker? = null
        var latestStats = TokenUsageStats.EMPTY
        val observations = mutableListOf<TokenUsageStats>()

        setContent {
            TokenUsageProvider {
                providedTracker = useTokenUsage()
                contextTracker = useTokenUsage()
                optionalTracker = rememberTokenTracker()
                latestStats = useTokenStats()
                SideEffect { observations += latestStats }
            }
        }

        waitForIdle()
        assertSame(providedTracker, contextTracker)
        assertSame(providedTracker, optionalTracker)
        assertEquals(TokenUsageStats.EMPTY, latestStats)

        runOnIdle {
            providedTracker.recordUsage("request", "provider", "model", ChatUsage(4, 6, 10))
        }
        waitForIdle()

        assertEquals(10, latestStats.totalTokens)
        assertEquals(1, latestStats.requestCount)
        assertEquals(10, observations.last().totalTokens)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun provider_instances_are_isolated_and_optional_tracker_is_null_outside_provider() = runComposeUiTest {
        lateinit var first: TokenUsageTracker
        lateinit var second: TokenUsageTracker
        var outside: TokenUsageTracker? = TokenUsageTracker()

        setContent {
            outside = rememberTokenTracker()
            TokenUsageProvider { first = useTokenUsage() }
            TokenUsageProvider { second = useTokenUsage() }
        }

        waitForIdle()

        assertNull(outside)
        assertNotSame(first, second)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun token_hook_aliases_preserve_values_and_context_identity() = runComposeUiTest {
        lateinit var usageTracker: TokenUsageTracker
        lateinit var rememberedTracker: TokenUsageTracker
        var optionalTracker: TokenUsageTracker? = null
        var rememberedOptionalTracker: TokenUsageTracker? = null
        var stats = TokenUsageStats.EMPTY
        var rememberedStats = TokenUsageStats.EMPTY
        var window = WindowTokenUsage()
        var rememberedWindow = WindowTokenUsage()

        setContent {
            TokenUsageProvider {
                usageTracker = useTokenUsage()
                rememberedTracker = rememberTokenUsage()
                optionalTracker = useTokenTracker()
                rememberedOptionalTracker = rememberTokenTracker()
                stats = useTokenStats()
                rememberedStats = rememberTokenStats()
                window = useWindowTokens(listOf("abcdef"), 3)
                rememberedWindow = rememberWindowTokens(listOf("abcdef"), 3)
            }
        }

        waitForIdle()

        assertSame(usageTracker, rememberedTracker)
        assertSame(optionalTracker, rememberedOptionalTracker)
        assertEquals(stats, rememberedStats)
        assertEquals(window, rememberedWindow)
        assertEquals(WindowTokenUsage(1, 2, 3, 1), window)
    }
}
