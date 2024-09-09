package xyz.junerver.compose.hooks.useredux

import kotlin.reflect.KClass
import xyz.junerver.compose.hooks.Middleware
import xyz.junerver.compose.hooks.Reducer
import xyz.junerver.kotlin.Tuple2

data class StoreRecord(
    val reducer: Reducer<Any, Any>,
    val initialState: Any,
    val stateType: KClass<*>,
    val actionType: KClass<*>,
    val alias: String,
)

data class Store(
    val middlewares: Array<Middleware<Any, Any>>,
    val records: List<StoreRecord>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

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
    inline fun <reified S : Any, reified A : Any> add(
        pair: Tuple2<Reducer<S, A>, S>,
        alias: String? = null,
    ) {
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
        add(Tuple2(this@with, initialState))
    }

    object NamedScope {
        inline infix fun <reified S : Any, reified A : Any> Reducer<S, A>.with(initialState: S): Tuple2<Reducer<S, A>, S> {
            return Tuple2(this@with, initialState)
        }
    }

    inline fun <reified T : Any, reified A : Any> named(
        alias: String,
        fn: NamedScope.() -> Tuple2<Reducer<T, A>, T>,
    ) {
        add(NamedScope.fn(), alias)
    }

    companion object {
        internal fun getInstance(records: MutableList<StoreRecord>) =
            StoreScope(records)
    }
}

/**
 * Create store
 *
 * @param fn
 * @return
 * @receiver
 */
fun createStore(
    middlewares: Array<Middleware<Any, Any>> = emptyArray(),
    fn: StoreScope.() -> Unit,
): Store {
    val list = mutableListOf<StoreRecord>()
    StoreScope.getInstance(list).fn()
    return Store(middlewares, list)
}

fun registerErr(target: String): Nothing {
    error("Please confirm $target that you have correctly registered in `createStore`!")
}

operator fun Store.plus(other: Store): Store {
    return Store(this.middlewares + other.middlewares, this.records + other.records)
}

fun combineStores(vararg stores: Store): Store = stores.reduce { acc, store -> acc + store }
