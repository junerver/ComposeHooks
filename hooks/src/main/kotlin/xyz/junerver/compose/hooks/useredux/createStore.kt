package xyz.junerver.compose.hooks.useredux

import kotlin.reflect.KClass
import xyz.junerver.compose.hooks.Middleware
import xyz.junerver.compose.hooks.Reducer
import xyz.junerver.kotlin.Tuple2
import xyz.junerver.kotlin.Tuple5

internal typealias StoreRecord = Tuple5<
    Reducer<Any, Any>, // reducer
    Any, // initialState
    KClass<*>, // state type
    KClass<*>, // action type
    String // alias
    >

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
    inline fun <reified T : Any, reified A : Any> add(
        pair: Tuple2<Reducer<T, A>, T>,
        alias: String? = null,
    ) {
        list.add(
            Tuple5(
                pair.first as Reducer<Any, Any>,
                pair.second,
                T::class,
                A::class,
                alias ?: T::class.qualifiedName ?: "unknown" // default alias
            )
        )
    }

    inline infix fun <reified T : Any, reified A : Any> Reducer<T, A>.with(state: T) {
        add(Tuple2(this@with, state))
    }

    object NamedScope {
        inline infix fun <reified T : Any, reified A : Any> Reducer<T, A>.with(state: T): Tuple2<Reducer<T, A>, T> {
            return Tuple2(this@with, state)
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

fun registerErr(): Nothing {
    error("Please confirm that you have correctly registered in `createStore`!")
}

operator fun Store.plus(other: Store): Store {
    return Store(this.middlewares + other.middlewares, this.records + other.records)
}
