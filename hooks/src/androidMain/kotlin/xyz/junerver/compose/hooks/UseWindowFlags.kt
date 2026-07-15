package xyz.junerver.compose.hooks

import android.view.Window
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

/*
  Description:
  Author: Junerver
  Date: 2024/7/3-11:36
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun useWindowFlags(key: String, flags: Int): Triple<AddFlagsFn, ClearFlagsFn, IsFlagsAdded> =
    androidx.compose.runtime.key(key) {
        useWindowFlagsForKey(key, flags)
    }

@Composable
private fun useWindowFlagsForKey(key: String, flags: Int): Triple<AddFlagsFn, ClearFlagsFn, IsFlagsAdded> {
    val window = LocalActivity.current?.window
    val target = useCreation(window) { window?.let(::AndroidWindowFlagTarget) }.current
    val defaultValue = target?.readFlagsOrNull()?.let { hasAllWindowFlags(it, flags) } == true
    val (requestedState, setRequested) = usePersistent(
        key = key,
        defaultValue = defaultValue,
        forceUseMemory = true,
    )
    val actualState = useState(false)
    val requested = requestedState.value

    DisposableEffect(target, flags, requested) {
        val acquired = requested && target != null && windowFlagCoordinator.acquire(target, flags)
        actualState.value = acquired
        onDispose {
            if (acquired) {
                windowFlagCoordinator.release(target, flags)
            }
            actualState.value = false
        }
    }

    val actions = useCreation(setRequested) {
        Pair<AddFlagsFn, ClearFlagsFn>(
            first = { setRequested(true) },
            second = { setRequested(false) },
        )
    }.current
    return useCreation(actions, actualState) {
        Triple(actions.first, actions.second, actualState)
    }.current
}

internal fun hasAllWindowFlags(currentFlags: Int, requestedFlags: Int): Boolean =
    requestedFlags != 0 && currentFlags and requestedFlags == requestedFlags

internal interface WindowFlagTarget {
    fun currentFlags(): Int

    fun addFlags(flags: Int)

    fun clearFlags(flags: Int)
}

private class AndroidWindowFlagTarget(private val window: Window) : WindowFlagTarget {
    override fun currentFlags(): Int = window.attributes.flags

    override fun addFlags(flags: Int) {
        window.addFlags(flags)
    }

    override fun clearFlags(flags: Int) {
        window.clearFlags(flags)
    }

    override fun equals(other: Any?): Boolean = other is AndroidWindowFlagTarget && other.window === window

    override fun hashCode(): Int = System.identityHashCode(window)
}

internal class WindowFlagCoordinator {
    private data class Ownership(
        var originalFlags: Int = 0,
        val ownerCounts: MutableMap<Int, Int> = mutableMapOf(),
    )

    private val lock = Any()
    private val ownership = mutableMapOf<WindowFlagTarget, Ownership>()

    fun acquire(target: WindowFlagTarget, flags: Int): Boolean {
        if (flags == 0) return false
        return synchronized(lock) {
            val currentFlags = target.readFlagsOrNull() ?: return@synchronized false
            val existing = ownership[target]
            var originalFlags = existing?.originalFlags ?: 0
            var flagsToAdd = 0
            flags.forEachFlagBit { bit ->
                if ((existing?.ownerCounts?.get(bit) ?: 0) == 0 && currentFlags and bit != 0) {
                    originalFlags = originalFlags or bit
                }
                if (currentFlags and bit == 0) {
                    flagsToAdd = flagsToAdd or bit
                }
            }
            if (!target.addFlagsSafely(flagsToAdd)) return@synchronized false

            val state = existing ?: Ownership()
            state.originalFlags = originalFlags
            flags.forEachFlagBit { bit ->
                state.ownerCounts[bit] = (state.ownerCounts[bit] ?: 0) + 1
            }
            ownership[target] = state
            true
        }
    }

    fun release(target: WindowFlagTarget, flags: Int): Boolean {
        if (flags == 0) return false
        return synchronized(lock) {
            val state = ownership[target] ?: return@synchronized false
            var hasOwner = false
            var flagsToClear = 0
            flags.forEachFlagBit { bit ->
                val ownerCount = state.ownerCounts[bit] ?: return@forEachFlagBit
                hasOwner = true
                if (ownerCount == 1 && state.originalFlags and bit == 0) {
                    flagsToClear = flagsToClear or bit
                }
            }
            if (!hasOwner) return@synchronized false

            val cleared = target.clearFlagsSafely(flagsToClear)
            flags.forEachFlagBit { bit ->
                when (val ownerCount = state.ownerCounts[bit] ?: return@forEachFlagBit) {
                    1 -> {
                        state.ownerCounts.remove(bit)
                        state.originalFlags = state.originalFlags and bit.inv()
                    }

                    else -> state.ownerCounts[bit] = ownerCount - 1
                }
            }
            if (state.ownerCounts.isEmpty()) {
                ownership.remove(target)
            }
            cleared
        }
    }
}

private val windowFlagCoordinator = WindowFlagCoordinator()

private fun WindowFlagTarget.readFlagsOrNull(): Int? = try {
    currentFlags()
} catch (_: Exception) {
    null
}

private fun WindowFlagTarget.addFlagsSafely(flags: Int): Boolean = try {
    if (flags != 0) addFlags(flags)
    true
} catch (_: Exception) {
    false
}

private fun WindowFlagTarget.clearFlagsSafely(flags: Int): Boolean = try {
    if (flags != 0) clearFlags(flags)
    true
} catch (_: Exception) {
    false
}

private inline fun Int.forEachFlagBit(block: (Int) -> Unit) {
    var remaining = this
    while (remaining != 0) {
        val bit = remaining and -remaining
        block(bit)
        remaining = remaining and bit.inv()
    }
}
