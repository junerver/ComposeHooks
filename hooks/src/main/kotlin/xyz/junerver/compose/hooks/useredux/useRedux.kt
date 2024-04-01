package xyz.junerver.compose.hooks.useredux

import androidx.compose.runtime.Composable
import kotlin.reflect.KClass
import xyz.junerver.compose.hooks.Dispatch
import xyz.junerver.compose.hooks.Reducer
import xyz.junerver.compose.hooks._useState
import xyz.junerver.compose.hooks.createContext
import xyz.junerver.compose.hooks.useContext
import xyz.junerver.compose.hooks.useMap
import xyz.junerver.kotlin.Tuple3

/**
 * key: state type
 * value: reducer\initState\ action type
 */
typealias Store = Map<KClass<*>, Tuple3<Reducer<Any, Any>, Any, KClass<*>>>

val ReduxContext =
    createContext<Pair<Map<KClass<*>, Any>, Map<KClass<*>, Dispatch<Any>>>>(Pair(mapOf(), mapOf()))

@Composable
fun ReduxProvider(store: Store, content: @Composable () -> Unit) {
    val stateMap: MutableMap<KClass<*>, Any> = useMap()
    val dispatchMap: MutableMap<KClass<*>, Dispatch<Any>> = useMap()
    if (store.entries.isNotEmpty()) {
        for (entry in store.entries) {
            val (state, setState) = _useState(entry.value.second)
            val reducer = entry.value.first
            stateMap[entry.key] = state
            dispatchMap[entry.value.third] = { action: Any -> setState(reducer(state, action)) }
        }
    }
    ReduxContext.Provider(value = stateMap to dispatchMap) {
        content()
    }
}

@Composable
inline fun <reified T> useSelector(): T {
    val map = useContext(context = ReduxContext)
    return map.first[T::class] as T
}

@Composable
inline fun <reified A> useDispatch(): Dispatch<A> {
    val map = useContext(context = ReduxContext)
    return map.second[A::class] as Dispatch<A>
}

class StoreScope private constructor(val map: MutableMap<KClass<*>, Tuple3<Reducer<Any, Any>, Any, KClass<*>>>) {
    inline fun <reified T : Any, reified A : Any> add(pair: Pair<Reducer<T, A>, T>) {
        map[T::class] = Tuple3(pair.first as Reducer<Any, Any>, pair.second, A::class)
    }

    inline infix fun <reified T : Any, reified A : Any> Reducer<T, A>.with(state: T) {
        add(Pair(this@with, state))
    }

    companion object {
        fun getInstance(map: MutableMap<KClass<*>, Tuple3<Reducer<Any, Any>, Any, KClass<*>>>) =
            StoreScope(map)
    }
}

fun createStore(fn: StoreScope.() -> Unit): Store {
    val map = mutableMapOf<KClass<*>, Tuple3<Reducer<Any, Any>, Any, KClass<*>>>()
    StoreScope.getInstance(map).fn()
    return map
}
