package xyz.junerver.compose.hooks.useredux

import androidx.compose.runtime.Composable
import kotlin.reflect.KClass
import xyz.junerver.compose.hooks.Dispatch
import xyz.junerver.compose.hooks._useState
import xyz.junerver.compose.hooks.createContext
import xyz.junerver.compose.hooks.useMap
import xyz.junerver.kotlin.Tuple3
import xyz.junerver.kotlin.plus

/** Redux context */
val ReduxContext =
    createContext<
        Tuple3<
            Map<KClass<*>, Any>,
            Map<KClass<*>, Dispatch<Any>>,
            Map<String, Pair<Any, Dispatch<Any>>>
            >
        >(Tuple3(mapOf(), mapOf(), mapOf()))

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
    // alias - state\dispatch
    val aliasMap: MutableMap<String, Pair<Any, Dispatch<Any>>> = useMap()
    if (store.isNotEmpty()) {
        for (entry in store) {
            val (state, setState) = _useState(entry.second)
            val reducer = entry.first
            val dispatch = { action: Any -> setState(reducer(state, action)) }
            stateMap[entry.third] = state
            dispatchMap[entry.fourth] = dispatch
            aliasMap[entry.fifth] = state to dispatch
        }
    }
    ReduxContext.Provider(value = (stateMap to dispatchMap) + aliasMap) {
        content()
    }
}
