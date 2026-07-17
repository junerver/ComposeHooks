package xyz.junerver.compose.hooks

import android.view.Window
import java.lang.reflect.Proxy
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import xyz.junerver.compose.hooks.useidle.IdleCallbackHost
import xyz.junerver.compose.hooks.useidle.IdleTimeMark
import xyz.junerver.compose.hooks.useidle.IdleTimeSource
import xyz.junerver.compose.hooks.useidle.IdleWindowCallbackRegistry
import xyz.junerver.compose.hooks.useidle.remainingIdleDuration

/*
  Description: Android biometric and idle hook behavior tests.
  Author: Junerver
  Date: 2026/7/17
  Email: junerver@gmail.com
  Version: v1.0
*/

class BiometricIdleHooksTest {
    @AfterTest
    fun cleanup() {
        BiometricRequestStore.clear()
    }

    @Test
    fun biometricOptionsFactorySupportsAndroidOnlyOptionsType() {
        val options = createBiometricOptions {
            title = "configured"
            negativeButtonText = "stop"
        }

        assertEquals("configured", options.title)
        assertEquals("stop", options.negativeButtonText)
    }

    @Test
    fun biometricRequestStoreSnapshotsMutableOptions() {
        val original = BiometricOptions().apply { title = "first" }
        val requestId = BiometricRequestStore.put(original)

        original.title = "mutated"

        val stored = BiometricRequestStore.get(requestId)
        assertNotSame(original, stored)
        assertEquals("first", stored?.title)
    }

    @Test
    fun biometricRequestStoreIsolatesConcurrentRequests() = runTest {
        val requestIds = (0 until 64).map { index ->
            async(Dispatchers.Default) {
                BiometricRequestStore.put(BiometricOptions().apply { title = "request-$index" })
            }
        }.awaitAll()

        assertEquals(requestIds.size, requestIds.toSet().size)
        assertEquals(64, requestIds.mapNotNull(BiometricRequestStore::get).size)
    }

    @Test
    fun biometricLaunchFailureRemovesRequestAndRethrows() {
        val requestId = BiometricRequestStore.put(BiometricOptions())

        val failure = assertFailsWith<IllegalStateException> {
            launchBiometricRequest(requestId) {
                error("launcher is unavailable")
            }
        }

        assertEquals("launcher is unavailable", failure.message)
        assertNull(BiometricRequestStore.get(requestId))
    }

    @Test
    fun biometricRequestSurvivesConfigurationChangeOnly() {
        val retainedId = BiometricRequestStore.put(BiometricOptions())
        releaseBiometricRequest(retainedId, isChangingConfigurations = true)
        assertTrue(BiometricRequestStore.get(retainedId) != null)

        releaseBiometricRequest(retainedId, isChangingConfigurations = false)
        assertNull(BiometricRequestStore.get(retainedId))
    }

    @Suppress("DEPRECATION")
    @Test
    fun legacyBiometricOptionsPropertyRemainsSourceCompatible() {
        val options = BiometricOptions().apply { title = "legacy" }

        BiometricActivity.options = options

        assertSame(options, BiometricActivity.options)
    }

    @Test
    fun idleRegistrySharesOneCallbackAndRestoresAfterLastSubscriber() {
        val original = recordingWindowCallback().callback
        val slot = FakeCallbackSlot(original)
        val identity = Any()
        val firstHost = FakeIdleCallbackHost(slot, identity)
        val secondHost = FakeIdleCallbackHost(slot, identity)
        val timeSource = FakeIdleTimeSource()
        val registry = IdleWindowCallbackRegistry()

        val first = registry.acquire(firstHost, timeSource)
        val installed = slot.callback
        val second = registry.acquire(secondHost, timeSource)

        assertSame(installed, slot.callback)
        assertNotSame(original, installed)

        first.close()
        first.close()
        assertSame(installed, slot.callback)

        second.close()
        assertSame(original, slot.callback)
    }

    @Test
    fun idleCallbackTracksAllInputKindsAndDelegatesModernMethods() {
        val delegate = recordingWindowCallback()
        val host = FakeIdleCallbackHost(FakeCallbackSlot(delegate.callback))
        val timeSource = FakeIdleTimeSource()
        val registry = IdleWindowCallbackRegistry()
        val subscription = registry.acquire(host, timeSource)
        val initialSequence = subscription.activities.value.sequence

        repeat(5) { index ->
            timeSource.advanceBy(100.milliseconds)
            when (index) {
                0 -> host.callback.dispatchTouchEvent(null)
                1 -> host.callback.dispatchKeyEvent(null)
                2 -> host.callback.dispatchKeyShortcutEvent(null)
                3 -> host.callback.dispatchTrackballEvent(null)
                else -> host.callback.dispatchGenericMotionEvent(null)
            }
        }
        host.callback.onPointerCaptureChanged(true)
        host.callback.onProvideKeyboardShortcuts(emptyList(), null, 0)

        assertEquals(initialSequence + 5, subscription.activities.value.sequence)
        assertEquals(timeSource.current.instant, subscription.activities.value.lastActive)
        assertTrue("onPointerCaptureChanged" in delegate.calls)
        assertTrue("onProvideKeyboardShortcuts" in delegate.calls)
        subscription.close()
    }

    @Test
    fun idleRegistryDoesNotOverwriteExternalCallbackOnRelease() {
        val original = recordingWindowCallback().callback
        val external = recordingWindowCallback().callback
        val host = FakeIdleCallbackHost(FakeCallbackSlot(original))
        val registry = IdleWindowCallbackRegistry()
        val subscription = registry.acquire(host, FakeIdleTimeSource())

        host.callback = external
        subscription.close()

        assertSame(external, host.callback)
    }

    @Test
    fun idleRemainingDurationUsesMonotonicTimeAndSupportsInfinity() {
        assertEquals(
            750.milliseconds,
            remainingIdleDuration(
                nowNanos = 1_250.milliseconds.inWholeNanoseconds,
                activeAtNanos = 1.seconds.inWholeNanoseconds,
                timeout = 1.seconds,
            ),
        )
        assertEquals(
            Duration.ZERO,
            remainingIdleDuration(
                nowNanos = 2_001.milliseconds.inWholeNanoseconds,
                activeAtNanos = 1.seconds.inWholeNanoseconds,
                timeout = 1.seconds,
            ),
        )
        assertTrue(
            remainingIdleDuration(
                nowNanos = 2.seconds.inWholeNanoseconds,
                activeAtNanos = 1.seconds.inWholeNanoseconds,
                timeout = Duration.INFINITE,
            ).isInfinite(),
        )
        assertFalse(1.nanoseconds.isInfinite())
    }

    private class FakeCallbackSlot(var callback: Window.Callback)

    private class FakeIdleCallbackHost(
        private val slot: FakeCallbackSlot,
        override val identity: Any = Any(),
    ) : IdleCallbackHost {
        override var callback: Window.Callback
            get() = slot.callback
            set(value) {
                slot.callback = value
            }
    }

    private class FakeIdleTimeSource : IdleTimeSource {
        var current = IdleTimeMark(
            instant = Instant.fromEpochMilliseconds(1_000),
            elapsedRealtimeNanos = 1.seconds.inWholeNanoseconds,
        )
            private set

        override fun now(): IdleTimeMark = current

        fun advanceBy(duration: Duration) {
            current = IdleTimeMark(
                instant = current.instant + duration,
                elapsedRealtimeNanos = current.elapsedRealtimeNanos + duration.inWholeNanoseconds,
            )
        }
    }

    private data class RecordingCallback(
        val callback: Window.Callback,
        val calls: MutableList<String>,
    )

    private fun recordingWindowCallback(): RecordingCallback {
        val calls = mutableListOf<String>()
        val callback = Proxy.newProxyInstance(
            Window.Callback::class.java.classLoader,
            arrayOf(Window.Callback::class.java),
        ) { _, method, _ ->
            calls += method.name
            primitiveDefault(method.returnType)
        } as Window.Callback
        return RecordingCallback(callback, calls)
    }

    private fun primitiveDefault(type: Class<*>): Any? = when (type) {
        java.lang.Boolean.TYPE -> false
        java.lang.Byte.TYPE -> 0.toByte()
        java.lang.Character.TYPE -> '\u0000'
        java.lang.Short.TYPE -> 0.toShort()
        java.lang.Integer.TYPE -> 0
        java.lang.Long.TYPE -> 0L
        java.lang.Float.TYPE -> 0f
        java.lang.Double.TYPE -> 0.0
        else -> null
    }
}
