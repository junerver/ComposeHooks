package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import kotlin.reflect.KProperty
import xyz.junerver.compose.hooks.utils.CacheManager
import xyz.junerver.compose.hooks.utils.HooksEventManager

/*
  Description: A persistence hook that is easy to extend. It uses memory for
  persistence by default. You can pass in the corresponding function yourself
  to customize the persistence solution.

  Author: Junerver
  Date: 2024/4/9-16:59
  Email: junerver@gmail.com
  Version: v1.0
*/


/**
 * The final return value of the persistence hook is a tuple like
 * [state,setState]
 */
@Stable
data class PersistentHolder<T>(
    val state: State<T>,
    val save: SaveToPersistent<T>,
    val clear: HookClear,
) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = state.value

    operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: T) {
        save(newValue)
    }
}

/**
 * By default, [CacheManager.cache] is used for memory persistence.
 * [usePersistent] is a lightweight encapsulation, you need to provide your
 * own persistence solution globally through `PersistentContext.Provider`;
 */
val PersistentContext by lazy {
    createContext<PersistentContextValue>(
        Triple(
            ::memoryGetPersistent,
            ::memorySavePersistent,
            ::memoryClearPersistent
        )
    )
}

internal val InternalMemoryPersistentContext by lazy {
    createContext<PersistentContextValue>(
        Triple(
            ::memoryGetPersistent,
            ::memorySavePersistent,
            ::memoryClearPersistent
        )
    )
}

/**
 * Use persistent
 *
 * @param key persistence key
 * @param defaultValue persistent default values
 * @param forceUseMemory whether to force the use of memory for persistence
 * @param T
 * @return
 */
@Suppress("UNCHECKED_CAST")
@Composable
fun <T> usePersistent(key: String, defaultValue: T, forceUseMemory: Boolean = false): PersistentHolder<T> {
    val (get, set, clear) = useContext(context = if (forceUseMemory) InternalMemoryPersistentContext else PersistentContext)
    val getValue = { get(key, defaultValue as Any) as T }
    val state = _useState(getValue())

    /**
     * Register an observer callback for each component that uses this state,
     * and notify the update component when this storage changes;
     */
    var unObserver by useRef(default = {})
    useMount {
        unObserver = memoryAddObserver(key) {
            state.value = getValue()
        }
    }
    useUnmount {
        unObserver()
    }
    return remember {
        PersistentHolder(
            state = state,
            save = { value -> set(key, value) },
            clear = { clear(key) }
        )
    }
}

/** Callback function when performing persistence operation */
private typealias SavePersistentCallback = (Unit) -> Unit

/**
 * you should call this function in your [PersistentSave] fun to notify
 * state update
 */
fun notifyDefaultPersistentObserver(key: String) {
    HooksEventManager.post(key.persistentKey, Unit)
}

private fun memorySavePersistent(key: String, value: Any?) {
    CacheManager.saveCache(key.persistentKey, value)
    notifyDefaultPersistentObserver(key)
}

private fun memoryGetPersistent(key: String, defaultValue: Any): Any = CacheManager.getCache(key.persistentKey, defaultValue)

private fun memoryClearPersistent(key: String) {
    CacheManager.clearCache(key.persistentKey)
    notifyDefaultPersistentObserver(key)
}

private fun memoryAddObserver(key: String, observer: SavePersistentCallback): () -> Unit =
    HooksEventManager.register(key.persistentKey, observer)
