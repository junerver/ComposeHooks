@file:OptIn(ExperimentalCoroutinesApi::class)

package xyz.junerver.compose.hooks.test

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import xyz.junerver.compose.hooks.userequest.utils.acquireCacheDeferred
import xyz.junerver.compose.hooks.userequest.utils.awaitPlus
import xyz.junerver.compose.hooks.userequest.utils.getCacheDeferred
import xyz.junerver.compose.hooks.userequest.utils.setCacheDeferred

/*
  Description: CachePromise 并发注册与等待生命周期测试
  Author: Junerver
  Date: 2026/7/15
  Email: junerver@gmail.com
  Version: v1.0
*/
class CachePromiseTest {
    private fun uniqueKey(prefix: String): String = "$prefix-${kotlin.random.Random.nextInt()}"

    @Test
    fun cancelingOneWaiterKeepsActivePromiseRegistered() = runTest {
        val key = uniqueKey("cancel-waiter")
        val requestGate = CompletableDeferred<Unit>()
        val source = backgroundScope.async {
            requestGate.await()
            42
        }
        setCacheDeferred(key, source)
        val canceledWaiter = launch { source.awaitPlus() }
        val activeWaiter = async { source.awaitPlus() }

        try {
            runCurrent()
            canceledWaiter.cancelAndJoin()

            assertSame(source, getCacheDeferred<Int>(key))
            assertFalse(source.isCompleted)

            requestGate.complete(Unit)
            assertEquals(42, activeWaiter.await())
            runCurrent()

            assertNull(getCacheDeferred<Int>(key))
        } finally {
            requestGate.complete(Unit)
            canceledWaiter.cancelAndJoin()
            activeWaiter.cancelAndJoin()
            source.cancelAndJoin()
        }
    }

    @Test
    fun concurrentAcquireStartsExactlyOneCandidate() = runTest {
        val key = uniqueKey("atomic-acquire")
        val raceStart = CompletableDeferred<Unit>()
        val requestGate = CompletableDeferred<Unit>()
        var startCount = 0
        val attempts = List(64) {
            async(Dispatchers.Default) {
                raceStart.await()
                val candidate = backgroundScope.async(start = CoroutineStart.LAZY) {
                    startCount += 1
                    requestGate.await()
                    42
                }
                val selected = acquireCacheDeferred(key, currentPromise = null, candidate)
                if (selected === candidate) {
                    candidate.start()
                } else {
                    candidate.cancel()
                }
                candidate to selected
            }
        }
        var acquired = emptyList<Pair<Deferred<Int>, Deferred<Int>>>()

        try {
            raceStart.complete(Unit)
            acquired = attempts.awaitAll()
            runCurrent()

            val winner = acquired.first().second
            assertTrue(acquired.all { (_, selected) -> selected === winner })
            assertEquals(1, startCount)
            assertSame(winner, getCacheDeferred<Int>(key))

            requestGate.complete(Unit)
            assertEquals(42, winner.await())
            runCurrent()

            assertNull(getCacheDeferred<Int>(key))
        } finally {
            raceStart.complete(Unit)
            requestGate.complete(Unit)
            attempts.forEach { it.cancel() }
            acquired.forEach { (candidate, _) -> candidate.cancel() }
            runCurrent()
        }
    }

    @Test
    fun olderCompletionCannotRemoveReplacement() = runTest {
        val key = uniqueKey("replacement")
        val first = CompletableDeferred<Int>()
        val second = CompletableDeferred<Int>()

        try {
            assertSame(first, acquireCacheDeferred(key, currentPromise = null, first))
            assertSame(second, acquireCacheDeferred(key, currentPromise = first, second))

            first.complete(1)
            assertSame(second, getCacheDeferred<Int>(key))

            second.complete(2)
            assertNull(getCacheDeferred<Int>(key))
        } finally {
            first.cancelAndJoin()
            second.cancelAndJoin()
        }
    }
}
