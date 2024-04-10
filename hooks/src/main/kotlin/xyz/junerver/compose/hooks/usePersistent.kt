package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import xyz.junerver.kotlin.Tuple2
import xyz.junerver.kotlin.Tuple3
import xyz.junerver.kotlin.plus

/**
 * Description:
 * @author Junerver
 * date: 2024/4/9-16:59
 * Email: junerver@gmail.com
 * Version: v1.0
 */
/**
 * pass in the key to get the persistent object
 */
typealias PersistentGet = (String) -> Any?

/**
 * Pass in the key, persist the object, and perform persistence
 */
typealias PersistentSave = (String, Any?) -> Unit

/**
 * Callback function when performing persistence operation
 */
typealias SavePersistentCallback = () -> Unit

/**
 * observers that listen to persistent variables
 */
typealias PersistentObserver = (String, SavePersistentCallback) -> (() -> Unit)

/**
 * Perform persistent save
 */
typealias SaveToPersistent<T> = (T?) -> Unit
/**
 * The final return value of the persistence hook is a tuple like [state,setState]
 */
typealias PersistentHookReturn<T> = Tuple2<T, SaveToPersistent<T>>

/**
 * By default, [memorySaveMap] is used for memory persistence.
 * [usePersistent] is a lightweight encapsulation, you need to provide your own persistence solution
 * globally through [PersistentContext.Provider];
 */
val PersistentContext =
    createContext<Tuple3<PersistentGet, PersistentSave, PersistentObserver>>((::memoryGetPersistent to ::memorySavePersistent) + ::memoryAddObserver)

@Composable
fun <T> usePersistent(key: String, defaultValue: T): PersistentHookReturn<T> {
    val (get, set, observer) = useContext(context = PersistentContext)
    /**
     * By using forced recompose of components, cross-component persistence updates and refresh UI are achieved.
     */
    val unObserver = useRef(default = {})
    val update = useUpdate()
    useMount {
       unObserver.current =  observer(key) { update() }
    }
    useUnmount {
        unObserver.current()
    }
    return Tuple2(
        first = get(key) as? T ?: defaultValue,
        second = { value ->
            set(key, value)
        }
    )
}

private val memorySaveMap = mutableMapOf<String, Any?>()

private val listener = mutableMapOf<String, MutableList<SavePersistentCallback>>()

/**
 * 默认的监听
 */
fun notifyDefaultPersistentObserver(key: String) {
    listener[key]?.takeIf { it.isNotEmpty() }?.forEach { it.invoke() }
}

private fun memorySavePersistent(key: String, value: Any?) {
    memorySaveMap[key] = value
    notifyDefaultPersistentObserver(key)
}

private fun memoryGetPersistent(key: String): Any? {
    return memorySaveMap[key]
}

private fun memoryAddObserver(key: String, observer: SavePersistentCallback): () -> Unit {
    listener[key] ?: run { listener[key] = mutableListOf() }
    listener[key]!!.add(observer)
    return fun() {
        listener[key]?.remove(observer)
    }
}
