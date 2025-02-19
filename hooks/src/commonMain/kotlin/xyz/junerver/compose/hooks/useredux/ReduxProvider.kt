package xyz.junerver.compose.hooks.useredux

import androidx.compose.runtime.Composable
import kotlin.reflect.KClass
import xyz.junerver.compose.hooks.ComposeComponent
import xyz.junerver.compose.hooks.Dispatch
import xyz.junerver.compose.hooks.createContext
import xyz.junerver.compose.hooks.useMap
import xyz.junerver.compose.hooks.useReducer

@PublishedApi
internal data class ReduxContextValue(
    val stateMap: Map<KClass<*>, Any>,
    val dispatchMap: Map<KClass<*>, Dispatch<Any>>,
    val aliasMap: Map<String, Pair<Any, Dispatch<Any>>>,
)

/** Redux context */
@PublishedApi
internal val ReduxContext by lazy {
    createContext(ReduxContextValue(mapOf(), mapOf(), mapOf()))
}

/**
 * Redux provider, you should provide a state store to this Provider by use [createStore]
 *
 * @param store
 * @param content
 * @receiver
 */
@Composable
fun ReduxProvider(store: Store, content: ComposeComponent) {
    val stateMap: MutableMap<KClass<*>, Any> = useMap()
    val dispatchMap: MutableMap<KClass<*>, Dispatch<Any>> = useMap()
    // alias - state\dispatch
    val aliasMap: MutableMap<String, Pair<Any, Dispatch<Any>>> = useMap()
    store.records.forEach { entry ->
        val (state, dispatch) = useReducer(
            reducer = entry.reducer,
            initialState = entry.initialState,
            store.middlewares
        )
        stateMap[entry.stateType] = state
        dispatchMap[entry.actionType] = dispatch
        aliasMap[entry.alias] = state to dispatch
    }
    ReduxContext.Provider(value = ReduxContextValue(stateMap, dispatchMap, aliasMap)) {
        content()
    }
}
