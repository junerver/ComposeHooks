package xyz.junerver.compose.hooks.useidle

import android.os.SystemClock
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.Window
import androidx.appcompat.view.WindowCallbackWrapper
import java.util.WeakHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import xyz.junerver.compose.hooks.utils.currentInstant

/*
  Description: Shared Window.Callback coordination for useIdle.
  Author: Junerver
  Date: 2024/7/9-9:10
  Email: junerver@gmail.com
  Version: v1.0
*/

internal data class IdleTimeMark(
    val instant: Instant,
    val elapsedRealtimeNanos: Long,
)

internal fun interface IdleTimeSource {
    fun now(): IdleTimeMark
}

private object AndroidIdleTimeSource : IdleTimeSource {
    override fun now(): IdleTimeMark = IdleTimeMark(
        instant = currentInstant,
        elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos(),
    )
}

internal data class IdleActivitySnapshot(
    val lastActive: Instant,
    val activeAtNanos: Long,
    val sequence: Long,
)

internal class IdleActivityTracker(
    private val timeSource: IdleTimeSource,
) {
    private val mutableActivities = MutableStateFlow(timeSource.now().toSnapshot(sequence = 0))

    val activities: StateFlow<IdleActivitySnapshot> = mutableActivities.asStateFlow()

    fun markActive() {
        val now = timeSource.now()
        mutableActivities.update { previous ->
            now.toSnapshot(sequence = previous.sequence + 1)
        }
    }
}

private fun IdleTimeMark.toSnapshot(sequence: Long): IdleActivitySnapshot = IdleActivitySnapshot(
    lastActive = instant,
    activeAtNanos = elapsedRealtimeNanos,
    sequence = sequence,
)

internal fun remainingIdleDuration(
    nowNanos: Long,
    activeAtNanos: Long,
    timeout: Duration,
): Duration {
    if (timeout == Duration.INFINITE) return Duration.INFINITE
    if (timeout <= Duration.ZERO) return Duration.ZERO
    val elapsed = (nowNanos - activeAtNanos).coerceAtLeast(0).nanoseconds
    return (timeout - elapsed).coerceAtLeast(Duration.ZERO)
}

internal class InactivityWindowCallback(
    originalCallback: Window.Callback,
    private val markActive: () -> Unit,
) : WindowCallbackWrapper(originalCallback) {
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        markActive()
        return super.dispatchTouchEvent(event)
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        markActive()
        return super.dispatchKeyEvent(event)
    }

    override fun dispatchKeyShortcutEvent(event: KeyEvent?): Boolean {
        markActive()
        return super.dispatchKeyShortcutEvent(event)
    }

    override fun dispatchTrackballEvent(event: MotionEvent?): Boolean {
        markActive()
        return super.dispatchTrackballEvent(event)
    }

    override fun dispatchGenericMotionEvent(event: MotionEvent?): Boolean {
        markActive()
        return super.dispatchGenericMotionEvent(event)
    }
}

internal interface IdleCallbackHost {
    val identity: Any
    var callback: Window.Callback
}

internal class AndroidIdleCallbackHost(
    private val window: Window,
) : IdleCallbackHost {
    override val identity: Any = window

    override var callback: Window.Callback
        get() = window.callback
        set(value) {
            window.callback = value
        }
}

internal class IdleWindowCallbackSubscription(
    val activities: StateFlow<IdleActivitySnapshot>,
    private val nowProvider: () -> IdleTimeMark,
    private val release: () -> Unit,
) : AutoCloseable {
    private val closed = AtomicBoolean(false)

    override fun close() {
        if (closed.compareAndSet(false, true)) {
            release()
        }
    }

    fun now(): IdleTimeMark = nowProvider()
}

internal class IdleWindowCallbackRegistry {
    private val coordinators = WeakHashMap<Any, Coordinator>()

    fun acquire(
        host: IdleCallbackHost,
        timeSource: IdleTimeSource = AndroidIdleTimeSource,
    ): IdleWindowCallbackSubscription = synchronized(coordinators) {
        val coordinator = coordinators.getOrPut(host.identity) {
            Coordinator(host, timeSource)
        }
        coordinator.retain()
        IdleWindowCallbackSubscription(
            activities = coordinator.tracker.activities,
            nowProvider = coordinator.timeSource::now,
            release = { release(host.identity, coordinator) },
        )
    }

    private fun release(identity: Any, coordinator: Coordinator) {
        synchronized(coordinators) {
            if (coordinators[identity] !== coordinator) return
            if (coordinator.release()) {
                coordinators.remove(identity)
            }
        }
    }

    private class Coordinator(
        private val host: IdleCallbackHost,
        val timeSource: IdleTimeSource,
    ) {
        val tracker = IdleActivityTracker(timeSource)
        private var subscriberCount = 0
        private var installedCallback: Window.Callback? = null
        private var restoreCallback: Window.Callback? = null

        fun retain() {
            subscriberCount += 1
            ensureInstalled()
        }

        fun release(): Boolean {
            check(subscriberCount > 0) { "Idle callback subscription count underflow" }
            subscriberCount -= 1
            if (subscriberCount != 0) return false
            uninstallIfOwned()
            return true
        }

        private fun ensureInstalled() {
            val current = host.callback
            if (current === installedCallback) return
            restoreCallback = current
            installedCallback = InactivityWindowCallback(current, tracker::markActive)
            host.callback = checkNotNull(installedCallback)
        }

        private fun uninstallIfOwned() {
            if (host.callback === installedCallback) {
                host.callback = checkNotNull(restoreCallback)
            }
            installedCallback = null
            restoreCallback = null
        }
    }
}
