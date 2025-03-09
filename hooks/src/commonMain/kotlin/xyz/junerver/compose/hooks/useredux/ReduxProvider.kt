package xyz.junerver.compose.hooks.useredux

import androidx.compose.runtime.Composable
import kotlin.reflect.KClass
import xyz.junerver.compose.hooks.ComposeComponent
import xyz.junerver.compose.hooks.Dispatch
import xyz.junerver.compose.hooks.createContext
import xyz.junerver.compose.hooks.useMap
import xyz.junerver.compose.hooks.useReducer

/**
 * Internal data class that holds the Redux context values.
 *
 * This class maintains three maps for managing state and dispatch functions:
 * - State map: Maps state types to their current values
 * - Dispatch map: Maps action types to their dispatch functions
 * - Alias map: Maps string aliases to state-dispatch pairs
 *
 * @property stateMap Maps state types to their current values
 * @property dispatchMap Maps action types to their dispatch functions
 * @property aliasMap Maps aliases to state-dispatch pairs
 */
@PublishedApi
internal data class ReduxContextValue(
    val stateMap: Map<KClass<*>, Any>,
    val dispatchMap: Map<KClass<*>, Dispatch<Any>>,
    val aliasMap: Map<String, Pair<Any, Dispatch<Any>>>,
)

/**
 * Internal context for Redux state management.
 * Initialized with empty maps for states, dispatches, and aliases.
 */
@PublishedApi
internal val ReduxContext by lazy {
    createContext(ReduxContextValue(mapOf(), mapOf(), mapOf()))
}

/**
 * A provider component for Redux state management.
 *
 * This component sets up the Redux context for state management in a Compose application.
 * It takes a [Store] created by [createStore] and makes its state and dispatch functions
 * available to all child components through the context system.
 *
 * @param store The Redux store containing reducers, state, and middleware
 * @param content The child components that will have access to the Redux store
 *
 * @example
 * ```kotlin
 * // Create a store
 * val store = createStore {
 *     // Counter reducer
 *     counterReducer with 0
 *
 *     // Named todo reducer
 *     named("todos") {
 *         todoReducer with emptyList<Todo>()
 *     }
 * }
 *
 * // Provide the store to the app
 * ReduxProvider(store = store) {
 *     // Components can now access the store
 *     Column {
 *         Counter()
 *         TodoList()
 *
 *         // Nested components also have access
 *         UserProfile {
 *             Settings()
 *         }
 *     }
 * }
 *
 * // Access store in a component
 * @Composable
 * fun Counter() {
 *     // Get state and dispatch by type
 *     val count by useSelector<Int>()
 *     val dispatch = useDispatch<CounterAction>()
 *
 *     // Or by alias
 *     val (todoState, todoDispatch) = useStore("todos")
 *
 *     Button(onClick = { dispatch(CounterAction.Increment) }) {
 *         Text("Count: $count")
 *     }
 * }
 * ```
 */
@Composable
fun ReduxProvider(store: Store, content: ComposeComponent) {
    val stateMap: MutableMap<KClass<*>, Any> = useMap()
    val dispatchMap: MutableMap<KClass<*>, Dispatch<Any>> = useMap()
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
