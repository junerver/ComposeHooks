package xyz.junerver.compose.hooks.test

import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import xyz.junerver.compose.hooks.usedebounce.Debounce
import xyz.junerver.compose.hooks.usedebounce.UseDebounceOptions
import xyz.junerver.compose.hooks.usethrottle.Throttle
import xyz.junerver.compose.hooks.usethrottle.UseThrottleOptions

/*
  Description: Debounce 与 Throttle 真实并发线性化测试
  Author: Junerver
  Date: 2026/7/15
  Email: junerver@gmail.com
  Version: v1.0
*/
class TimingPrimitiveConcurrencyTest {
    private val fixedNow = Instant.fromEpochMilliseconds(1_000L)

    @Test
    fun concurrentDebounceLeadingInvokesExactlyOncePerWindow() = runBlocking {
        val workerCount = 8
        val rounds = 50
        val dispatcher = Executors.newFixedThreadPool(workerCount).asCoroutineDispatcher()
        val scope = CoroutineScope(SupervisorJob() + dispatcher)
        val calls = AtomicInteger(0)
        val debounce = Debounce(
            fn = { _: Int -> calls.incrementAndGet() },
            scope = scope,
            options = UseDebounceOptions.optionOf {
                wait = 10.seconds
                leading = true
                trailing = false
            },
            now = { fixedNow },
        )

        try {
            repeat(rounds) {
                debounce.cancel()
                val barrier = CyclicBarrier(workerCount)
                List(workerCount) { value ->
                    async(dispatcher) {
                        barrier.await()
                        debounce.invoke(value)
                    }
                }.awaitAll()
            }

            assertEquals(rounds, calls.get())
        } finally {
            debounce.cancel()
            scope.cancel()
            dispatcher.close()
        }
    }

    @Test
    fun concurrentThrottleLeadingInvokesExactlyOncePerWindow() = runBlocking {
        val workerCount = 8
        val rounds = 50
        val dispatcher = Executors.newFixedThreadPool(workerCount).asCoroutineDispatcher()
        val scope = CoroutineScope(SupervisorJob() + dispatcher)
        val calls = AtomicInteger(0)
        val throttle = Throttle(
            fn = { _: Int -> calls.incrementAndGet() },
            scope = scope,
            options = UseThrottleOptions.optionOf {
                wait = 10.seconds
                leading = true
                trailing = false
            },
            now = { fixedNow },
        )

        try {
            repeat(rounds) {
                throttle.cancel()
                val barrier = CyclicBarrier(workerCount)
                List(workerCount) { value ->
                    async(dispatcher) {
                        barrier.await()
                        throttle.invoke(value)
                    }
                }.awaitAll()
            }

            assertEquals(rounds, calls.get())
        } finally {
            throttle.cancel()
            scope.cancel()
            dispatcher.close()
        }
    }
}
