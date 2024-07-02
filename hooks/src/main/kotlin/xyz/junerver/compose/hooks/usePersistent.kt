package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import xyz.junerver.kotlin.Tuple3
import xyz.junerver.kotlin.plus

/*
  Description: A persistence hook that is easy to extend. It uses memory for
  persistence by default. You can pass in the corresponding function yourself
  to customize the persistence solution.

  Author: Junerver
  Date: 2024/4/9-16:59
  Email: junerver@gmail.com
  Version: v1.0
*/

/** pass in the key to get the persistent object */
private typealias PersistentGet = (String, Any) -> Any

/** Pass in the key, persist the object, and perform persistence */
private typealias PersistentSave = (String, Any?) -> Unit

/** Perform clear persistent by pass key */
private typealias PersistentClear = (String) -> Unit

/** Perform persistent save */
private typealias SaveToPersistent<T> = (T?) -> Unit

/**
 * The final return value of the persistence hook is a tuple like
 * [state,setState]
 */
private typealias PersistentHookReturn<T> = Tuple3<T, SaveToPersistent<T>, PersistentClear>

/**
 * By default, [memorySaveMap] is used for memory persistence.
 * [usePersistent] is a lightweight encapsulation, you need to provide your
 * own persistence solution globally through `PersistentContext.Provider`;
 */
val PersistentContext =
    createContext<Tuple3<PersistentGet, PersistentSave, PersistentClear>>((::memoryGetPersistent to ::memorySavePersistent) + ::memoryClearPersistent)

internal val InternalMemoryPersistentContext =
    createContext<Tuple3<PersistentGet, PersistentSave, PersistentClear>>((::memoryGetPersistent to ::memorySavePersistent) + ::memoryClearPersistent)

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
fun <T> usePersistent(
    key: String,
    defaultValue: T,
    forceUseMemory: Boolean = false,
): PersistentHookReturn<T> {
    val (get, set, clear) = useContext(context = if (forceUseMemory) InternalMemoryPersistentContext else PersistentContext)

    /**
     * Register an observer callback for each component that uses this state,
     * and notify the update component when this storage changes;
     */
    val unObserver = useRef(default = {})
    val update = useUpdate()
    useMount {
        unObserver.current = memoryAddObserver(key) { update() }
    }
    useUnmount {
        unObserver.current()
    }
    return Tuple3(
        first = get(key, defaultValue as Any) as T,
        second = { value ->
            set(key, value)
        },
        third = clear
    )
}

private val memorySaveMap = mutableMapOf<String, Any?>()

/** Callback function when performing persistence operation */
private typealias SavePersistentCallback = () -> Unit

private val listener = mutableMapOf<String, MutableList<SavePersistentCallback>>()

/**
 * you should call this function in your [PersistentSave] fun to notify
 * state update
 */
fun notifyDefaultPersistentObserver(key: String) {
    listener[key]?.takeIf { it.isNotEmpty() }?.forEach { it.invoke() }
}

private fun memorySavePersistent(key: String, value: Any?) {
    memorySaveMap[key] = value
    notifyDefaultPersistentObserver(key)
}

private fun memoryGetPersistent(key: String, defaultValue: Any): Any {
    return memorySaveMap[key] ?: defaultValue
}

private fun memoryClearPersistent(key: String) {
    memorySaveMap.remove(key)
}

private fun memoryAddObserver(key: String, observer: SavePersistentCallback): () -> Unit {
    listener[key] ?: run { listener[key] = mutableListOf() }
    listener[key]!!.add(observer)
    return fun() {
        listener[key]?.remove(observer)
    }
}
