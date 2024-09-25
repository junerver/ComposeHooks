package xyz.junerver.compose.hooks.useredux

import kotlin.reflect.KClass
import xyz.junerver.compose.hooks.Middleware
import xyz.junerver.compose.hooks.Reducer

/**
 * A state-stored record that is used to hold a single record
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
 * all states are finally stored to this class
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
 * A function used to construct a state `Store` instance,
 * you can easily use [StoreScope.with] in the tail closure function to create a [StoreRecord].
 *
 * You can also use the [StoreScope.named] function to add a store with a specified name
 *
 * ```
 * val simpleStore = createStore(arrayOf(logMiddleware())) {
 *     simpleReducer with SimpleData("default", 18)
 *     named("todolist"){ todoReducer with persistentListOf() }
 * }
 * ```
 *
 *
 * @param fn
 * @return
 * @receiver
 */
fun createStore(middlewares: Array<Middleware<Any, Any>> = emptyArray(), fn: StoreScope.() -> Unit): Store {
    val list = mutableListOf<StoreRecord>()
    StoreScope.getInstance(list).fn()
    return Store(middlewares, list)
}

/**
 * friendly error message
 */
@PublishedApi
internal fun registerErr(target: String): Nothing {
    error("Please confirm $target that you have correctly registered in `createStore`!")
}

/**
 * [Store] 实例的创建并不要求一次性创建，你可以创建多个[Store]实例，然后使用这个扩展操作符进行组合。
 *
 * [Store] instances don't need to be created all at once, you can create multiple
 * [Store] instances and then use this extension operator [Store.plus] to combine
 *
 * ```kotlin
 * // provide store for all components
 * ReduxProvider(store = simpleStore + fetchStore) {
 *      // sub components
 * }
 * ```
 *
 */
operator fun Store.plus(other: Store): Store = Store(this.middlewares + other.middlewares, this.records + other.records)

/**
 * 等同于使用扩展操作符
 */
fun combineStores(vararg stores: Store): Store = stores.reduce { acc, store -> acc + store }
