package xyz.junerver.compose.hooks.useredux

import kotlin.reflect.KClass
import xyz.junerver.compose.hooks.Middleware
import xyz.junerver.compose.hooks.Reducer

/**
 * A record class that holds a single state store configuration.
 *
 * This class encapsulates all the necessary information for a single state store,
 * including its reducer, initial state, type information, and alias.
 *
 * @property reducer The reducer function for state updates
 * @property initialState The initial state value
 * @property stateType The Kotlin class of the state type
 * @property actionType The Kotlin class of the action type
 * @property alias Optional name for identifying the store
 */
data class StoreRecord
    @PublishedApi
    internal constructor(
        val reducer: Reducer<Any, Any>,
        val initialState: Any,
        val stateType: KClass<*>,
        val actionType: KClass<*>,
        val alias: String,
    )

/**
 * The main store class that holds all state records and middlewares.
 *
 * This class is the central state container that manages all registered reducers
 * and their associated states, along with any middleware for state processing.
 *
 * @property middlewares Array of middleware functions for processing actions
 * @property records List of store records containing state configurations
 */
data class Store internal constructor(
    val middlewares: Array<Middleware<Any, Any>>,
    val records: List<StoreRecord>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Store

        if (!middlewares.contentEquals(other.middlewares)) return false
        if (records != other.records) return false

        return true
    }

    override fun hashCode(): Int {
        var result = middlewares.contentHashCode()
        result = 31 * result + records.hashCode()
        return result
    }
}

/**
 * A scope class for building store configurations.
 *
 * This class provides a DSL for configuring stores with reducers, initial states,
 * and aliases in a type-safe manner.
 */
class StoreScope private constructor(val list: MutableList<StoreRecord>) {
    @Suppress("UNCHECKED_CAST")
    inline fun <reified S : Any, reified A : Any> add(pair: Pair<Reducer<S, A>, S>, alias: String? = null) {
        list.add(
            StoreRecord(
                reducer = pair.first as Reducer<Any, Any>,
                initialState = pair.second,
                stateType = S::class,
                actionType = A::class,
                alias = alias ?: (S::class.qualifiedName ?: "unknown") // default alias
            )
        )
    }

    inline infix fun <reified S : Any, reified A : Any> Reducer<S, A>.with(initialState: S) {
        add(Pair(this@with, initialState))
    }

    object NamedScope {
        inline infix fun <reified S : Any, reified A : Any> Reducer<S, A>.with(initialState: S): Pair<Reducer<S, A>, S> =
            Pair(this@with, initialState)
    }

    inline fun <reified T : Any, reified A : Any> named(alias: String, fn: NamedScope.() -> Pair<Reducer<T, A>, T>) {
        add(NamedScope.fn(), alias)
    }

    companion object {
        internal fun getInstance(records: MutableList<StoreRecord>) = StoreScope(records)
    }
}

/**
 * Creates a new Redux store with the specified middlewares and state configurations.
 *
 * This function provides a DSL for creating a Redux store with multiple reducers and
 * their associated states. It supports both unnamed and named store configurations.
 *
 * @param middlewares Array of middleware functions for processing actions
 * @param fn Configuration block for defining store records
 * @return A configured [Store] instance
 *
 * @example
 * ```kotlin
 * // Create a store with middleware and multiple reducers
 * val store = createStore(arrayOf(logMiddleware())) {
 *     // Simple reducer with default state
 *     counterReducer with 0
 *     
 *     // Named reducer for better identification
 *     named("todos") { 
 *         todoReducer with emptyList<Todo>() 
 *     }
 *     
 *     // Complex state with custom initial value
 *     userReducer with UserState(
 *         name = "",
 *         age = 0,
 *         isLoggedIn = false
 *     )
 * }
 * 
 * // Use in a component
 * ReduxProvider(store = store) {
 *     // Child components can access the store
 *     TodoList()
 *     UserProfile()
 * }
 * ```
 */
fun createStore(middlewares: Array<Middleware<Any, Any>> = emptyArray(), fn: StoreScope.() -> Unit): Store {
    val list = mutableListOf<StoreRecord>()
    StoreScope.getInstance(list).fn()
    return Store(middlewares, list)
}

/**
 * Helper function for generating friendly error messages when store registration fails.
 *
 * @param target The name of the missing or incorrectly registered component
 * @throws IllegalStateException with a descriptive error message
 */
@PublishedApi
internal fun registerErr(target: String): Nothing {
    error("Please confirm $target that you have correctly registered in `createStore`!")
}

/**
 * Combines two stores into a single store.
 *
 * This operator allows you to combine multiple stores, which is useful when you want
 * to split your store configurations into logical groups or modules.
 *
 * @param other The store to combine with this store
 * @return A new store containing all middlewares and records from both stores
 *
 * @example
 * ```kotlin
 * // Create separate stores for different features
 * val userStore = createStore { userReducer with UserState() }
 * val todoStore = createStore { todoReducer with TodoState() }
 * 
 * // Combine stores when providing to the app
 * ReduxProvider(store = userStore + todoStore) {
 *     App()
 * }
 * ```
 */
operator fun Store.plus(other: Store): Store = Store(this.middlewares + other.middlewares, this.records + other.records)

/**
 * Combines multiple stores into a single store.
 *
 * This function provides an alternative to the plus operator when you need to
 * combine more than two stores at once.
 *
 * @param stores Variable number of stores to combine
 * @return A new store containing all middlewares and records from all input stores
 *
 * @example
 * ```kotlin
 * val combinedStore = combineStores(
 *     userStore,
 *     todoStore,
 *     settingsStore
 * )
 * ```
 */
fun combineStores(vararg stores: Store): Store = stores.reduce { acc, store -> acc + store }
