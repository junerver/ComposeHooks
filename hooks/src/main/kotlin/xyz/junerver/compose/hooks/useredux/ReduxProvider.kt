package xyz.junerver.compose.hooks.useredux

import androidx.compose.runtime.Composable
import kotlin.reflect.KClass
import xyz.junerver.compose.hooks.Dispatch
import xyz.junerver.compose.hooks._useState
import xyz.junerver.compose.hooks.createContext
import xyz.junerver.compose.hooks.useMap

/** Redux context */
val ReduxContext =
    createContext<Pair<Map<KClass<*>, Any>, Map<KClass<*>, Dispatch<Any>>>>(Pair(mapOf(), mapOf()))

/**
 * Redux provider, you should provide a state store to this Provider by use [createStore]
 *
 * @param store
 * @param content
 * @receiver
 */
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




