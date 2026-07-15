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
                val owner = Any()
                val candidate = backgroundScope.async(start = CoroutineStart.LAZY) {
                    startCount += 1
                    requestGate.await()
                    42
                }
                val selected = acquireCacheDeferred(key, owner, requestVersion = 1, candidate)
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
        val owner = Any()
        val first = CompletableDeferred<Int>()
        val second = CompletableDeferred<Int>()

        try {
            assertSame(first, acquireCacheDeferred(key, owner, requestVersion = 1, first))
            assertSame(second, acquireCacheDeferred(key, owner, requestVersion = 2, second))

            first.complete(1)
            assertSame(second, getCacheDeferred<Int>(key))

            second.complete(2)
            assertNull(getCacheDeferred<Int>(key))
        } finally {
            first.cancelAndJoin()
            second.cancelAndJoin()
        }
    }

    @Test
    fun sameOwnerHigherVersionWinsDespiteStaleCallerState() = runTest {
        val key = uniqueKey("owner-version")
        val owner = Any()
        val first = CompletableDeferred<Int>()
        val second = CompletableDeferred<Int>()
        val third = CompletableDeferred<Int>()
        val lateSecond = CompletableDeferred<Int>()

        try {
            assertSame(first, acquireCacheDeferred(key, owner, requestVersion = 1, first))
            val staleCallerSnapshot = getCacheDeferred<Int>(key)
            assertSame(second, acquireCacheDeferred(key, owner, requestVersion = 2, second))

            // v2 已注册，但调用方仍持有 v1 的旧快照；v3 必须仅凭 owner/version 完成替换。
            assertSame(first, staleCallerSnapshot)
            assertSame(second, getCacheDeferred<Int>(key))
            assertSame(third, acquireCacheDeferred(key, owner, requestVersion = 3, third))

            // 更低版本迟到时不得覆盖 v3，必须复用当前更高版本。
            assertSame(third, acquireCacheDeferred(key, owner, requestVersion = 2, lateSecond))
            assertSame(third, getCacheDeferred<Int>(key))

            first.complete(1)
            second.complete(2)
            assertSame(third, getCacheDeferred<Int>(key))

            third.complete(3)
            assertNull(getCacheDeferred<Int>(key))
        } finally {
            first.cancelAndJoin()
            second.cancelAndJoin()
            third.cancelAndJoin()
            lateSecond.cancelAndJoin()
        }
    }
}
